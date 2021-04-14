package server;

public class ServerMain {

    public static void main(String... args) {
        Server server = new Server();
        server.start();

        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }
}
