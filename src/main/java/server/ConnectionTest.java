package server;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

class ConnectionTest
{
	public static class Payload implements Serializable
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

	public static void main(String... args) throws IOException, InterruptedException {
		int port = 1234;
		System.out.println("Starting server on port " + port);
		ServerConnection<Payload, Payload> server = new ServerConnection<>(port);
		server.init();
		while(true) {
			Map<Integer, Collection<Payload>> sp = server.pollAndSend();
			for(int key : sp.keySet()) {
				for(Payload p : sp.get(key)) {
					System.out.println("From client " + key + ": ");
					server.send(key, Collections.singletonList(new Payload(p.string)));
				}
			}
		}
	}
}
