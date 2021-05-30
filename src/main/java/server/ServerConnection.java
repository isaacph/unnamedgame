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
	private int connectionCounter = 0;
	private SocketChannelReader<IncomingType> reader = new SocketChannelReader<>();
	private SocketChannelWriter<OutgoingType> writer = new SocketChannelWriter<>();
	private Predicate<OutgoingPairError> sendErrorHandler;
	private Consumer<IOException> acceptErrorHandler;
	private Predicate<IncomingPairError> readErrorHandler;
	private Consumer<Integer> connectionRemoveHandler;

	public static class OutgoingPairError {
		public int clientID;
		public IOException e;
		public boolean serialized;
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
		for(OutgoingType p : payloads) {
			send(conID, p);
		}
	}

	public void send(int conId, OutgoingType payload) {
		writer.queueSend(connections.get(conId), payload);
	}

	public Map<Integer, Collection<IncomingType>> pollAndSend() {

		Collection<Integer> conToRemove = new ArrayList<>();
		// send to connections
		try {
			writer.update();
		} catch(SocketChannelWriter.Exception e) {
			OutgoingPairError error = new OutgoingPairError();
			error.clientID = -1;
			for(int conID : connections.keySet()) {
				if(connections.get(conID).equals(e.channel)) {
					error.clientID = conID;
				}
			}
			error.serialized = e.serialized;
			error.e = e;
			if(!sendErrorHandler.test(error) && error.clientID != -1) {
				conToRemove.add(error.clientID);
			}
		}
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
					if(!payloads.isEmpty()) msgToProcess.put(conId, payloads);
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
	public void setSendErrorHandler(Predicate<OutgoingPairError> sendErrorHandler) {
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

	public void removeConnection(int conId) {
		SocketChannel channel = connections.remove(conId);
		if(channel != null && channel.isOpen() && (channel.isConnected() || channel.isConnectionPending())) {
			try {
				channel.close();
			} catch(IOException e) {
				System.err.println("ServerConnection: remove channel error");
				e.printStackTrace();
			}
		}
	}

	public void cleanDisconnect(int conId, OutgoingType dcMessage) {
		SocketChannel channel = connections.get(conId);
		if(channel != null && channel.isOpen() && (channel.isConnected() || channel.isConnectionPending())) {
			connections.remove(conId);
			writer.queueSend(channel, dcMessage, () -> {
				try {
					if(channel.isOpen() && channel.isConnected() || channel.isConnectionPending()) {
						channel.close();
					}
				} catch(IOException e) {
					System.err.println("ServerConnection: trying to cleanly close channel");
					e.printStackTrace();
				}
			});
		} else {
			removeConnection(conId);
		}
	}
}
