package server;

import org.lwjgl.BufferUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;

public class SocketChannelReader<IncomingType> {

    private ByteBuffer buffer = BufferUtils.createByteBuffer(8192);

    public SocketChannelReader() {

    }

    @SuppressWarnings("unchecked")
    public Collection<IncomingType> readFrom(SocketChannel socket) throws IOException {
        if(socket == null || !socket.isConnected())
            return null;
        ArrayList<IncomingType> payloads = new ArrayList<>();

        buffer.clear();
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            int length, total = 0;
            do {
                length = socket.read(buffer);
                if(length > 0) {
                    total += length;
                    byte[] bytes = new byte[length];
                    buffer.flip();
                    buffer.get(bytes);
                    bos.write(bytes);
                }
                buffer.clear();
            } while (length > 0);
            if(total > 0) {
                System.out.println(total);
                try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray())) {
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    payloads.addAll((Collection<IncomingType>) ois.readObject());
                    return payloads;
                }
                catch (ClassNotFoundException e) {
                    System.err.println("ServerPayload: invalid type");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return payloads;
    }
}
