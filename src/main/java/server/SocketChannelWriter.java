package server;

import game.ClientPayload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collection;

public class SocketChannelWriter<OutgoingType> {

    public SocketChannelWriter() {

    }

    public void writeTo(SocketChannel socket, Collection<OutgoingType> payloads) throws IOException {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(payloads);
            System.out.println("W: " + bos.toByteArray().length);
            socket.write(ByteBuffer.wrap(bos.toByteArray()));
        }
    }
}
