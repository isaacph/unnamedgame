package server;

import game.ClientConnection;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

class ConnectionTest
{
	private static class Payload implements Serializable
	{
		public String string;
		public Payload(String str) {
			this.string = str;
		}
	}

	private static String makeBigStr(char c, int size) {
		StringBuilder buffer = new StringBuilder(size);
		for(int i = 0; i < size; ++i) {
			buffer.append(c);
		}
		return buffer.toString();
	}

	public static void main(String... args) throws IOException {
		int port = 1234;
		System.out.println("Starting server on port " + port);
		ServerConnection<Payload, Payload> server = new ServerConnection<>(port);
		ClientConnection<Payload, Payload> client = new ClientConnection<>();
		server.init();
		client.connect(new InetSocketAddress("localhost", port));

		while(!client.isConnected()) {
			client.update();
		}
		client.queueSend(new Payload(makeBigStr('A', 16384)));
		while(true) {
			if(client.isConnected()) {
				Collection<Payload> cp = client.update();
				for(Payload p : cp) {
					System.out.println("From server: " + p.string);
				}
				client.close();
			}

			Map<Integer, Collection<Payload>> sp = server.pollAndSend();
			for(int key : sp.keySet()) {
				for(Payload p : sp.get(key)) {
					System.out.println("From client " + key + ": " + p.string);
					server.send(key, Collections.singletonList(new Payload(p.string)));
				}
			}
		}
	}
}
