package server;

import game.ClientPayload;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ServerConnection<IncomingType, OutgoingType>
{
	public final int port;
	private ServerSocketChannel serverSocketChannel;
	private final Map<Integer, SocketChannel> connections = new HashMap<>();
	private final Map<Integer, Collection<OutgoingType>> toSend = new HashMap<>();
	private int connectionCounter = 0;
	private SocketChannelReader<IncomingType> reader = new SocketChannelReader<>();
	private SocketChannelWriter<OutgoingType> writer = new SocketChannelWriter<>();
	private Predicate<OutgoingPairError<OutgoingType>> sendErrorHandler;
	private Consumer<IOException> acceptErrorHandler;
	private Predicate<IncomingPairError> readErrorHandler;
	private Consumer<Integer> connectionRemoveHandler;

	public static class OutgoingPairError<OutgoingType> {
		public int clientID;
		public Collection<OutgoingType> payload;
		public IOException e;
	}
	public static class IncomingPairError {
		public int clientID;
		public IOException e;
	}

	public ServerConnection(int port) {
		this.port = port;
		this.sendErrorHandler = error -> {
			System.err.println("Error sending packet to client " + error.clientID);
			error.e.printStackTrace();
			return true;
		};
		this.acceptErrorHandler = error -> {
			System.err.println("Error accepting client connection");
			error.printStackTrace();
		};
		this.readErrorHandler = error -> {
			System.err.println("Error receiving packet from client " + error.clientID);
			error.e.printStackTrace();
			return false;
		};
		this.connectionRemoveHandler = integer -> {};
	}

	private int makeUniqueConnectionID() {
		return connectionCounter++;
	}

	public void init() throws IOException {
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.bind(new InetSocketAddress(port));
	}

	public void send(int conID, Collection<OutgoingType> payloads) {
		toSend.putIfAbsent(conID, new ArrayList<>());
		toSend.get(conID).addAll(payloads);
	}

	public void send(int conId, OutgoingType payload) {
		send(conId, Collections.singletonList(payload));
	}

	public Map<Integer, Collection<IncomingType>> pollAndSend() {

		Collection<Integer> conToRemove = new ArrayList<>();
		// send to connections
		for(int key : toSend.keySet()) {
			for(OutgoingType payload : toSend.get(key)) {
				Collection<OutgoingType> payloads = Collections.singletonList(payload);
				try {
					writer.writeTo(connections.get(key), payloads);
				} catch(IOException e) {
					OutgoingPairError<OutgoingType> error = new OutgoingPairError<>();
					error.e = e;
					error.clientID = key;
					error.payload = payloads;
					if(!sendErrorHandler.test(error)) {
						conToRemove.add(key);
					}
				}
			}
		}
		toSend.clear();
		for(int conId : conToRemove) {
			connections.remove(conId);
			this.connectionRemoveHandler.accept(conId);
		}
		conToRemove.clear();

		// accept new connections
		try {
			SocketChannel socketChannel = serverSocketChannel.accept();
			while(socketChannel != null) {
				socketChannel.configureBlocking(false);
				connections.put(makeUniqueConnectionID(), socketChannel);
				socketChannel = serverSocketChannel.accept();
			}
		} catch(IOException e) {
			this.acceptErrorHandler.accept(e);
		}

		// read from connections
		// read from existing connections
		Map<Integer, Collection<IncomingType>> msgToProcess = new HashMap<>();
		for(int conId : connections.keySet()) {
			try {
				Collection<IncomingType> payloads = reader.readFrom(connections.get(conId));
				if(payloads != null) {
					msgToProcess.put(conId, payloads);
				} else {
					conToRemove.add(conId);
				}
			} catch(IOException e) {
				IncomingPairError error = new IncomingPairError();
				error.clientID = conId;
				error.e = e;
				if(!readErrorHandler.test(error)) {
					conToRemove.add(conId);
				}
			}
		}
		for(int conId : conToRemove) {
			connections.remove(conId);
		}

		return msgToProcess;
	}

	/** The return type for the handler is whether to keep the connection after the error **/
	public void setSendErrorHandler(Predicate<OutgoingPairError<OutgoingType>> sendErrorHandler) {
		this.sendErrorHandler = sendErrorHandler;
	}

	public void setAcceptErrorHandler(Consumer<IOException> acceptErrorHandler) {
		this.acceptErrorHandler = acceptErrorHandler;
	}

	/** The return type for the handler is whether to keep the connection after the error **/
	public void setReadErrorHandler(Predicate<IncomingPairError> readErrorHandler) {
		this.readErrorHandler = readErrorHandler;
	}

	public void setConnectionRemoveHandler(Consumer<Integer> removeHandler) {
		this.connectionRemoveHandler = removeHandler;
	}
}
