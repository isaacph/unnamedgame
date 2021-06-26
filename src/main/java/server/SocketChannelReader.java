package server;

import org.lwjgl.BufferUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;

public class SocketChannelReader<IncomingType> {

    private ByteBuffer buffer = BufferUtils.createByteBuffer(1<<20);
    private int receivingRemaining = 0;
    private ByteArrayOutputStream receiving = new ByteArrayOutputStream();

    public SocketChannelReader() {

    }

    @SuppressWarnings("unchecked")
    public Collection<IncomingType> readFrom(SocketChannel socket) throws IOException {
        if(socket == null || !socket.isConnected())
            return null;
        ArrayList<IncomingType> payloads = new ArrayList<>();

        buffer.clear();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
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
            byte[] received = bos.toByteArray();
            int receivedOffset = 0;
            bos.close();
            try {
                while(received.length - receivedOffset > 0) {
                    if(receivingRemaining == 0) {
                        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(received, receivedOffset, 4));
                        receivingRemaining = dis.readInt();
                        receivedOffset += 4;
                    }
                    if(receivingRemaining > 0) {
                        int count = Math.min(receivingRemaining, received.length - receivedOffset);
                        receiving.write(received, receivedOffset, count);
                        receivedOffset += count;
                        receivingRemaining -= count;

                        if(receivingRemaining == 0) {
                            try(ByteArrayInputStream bis2 = new ByteArrayInputStream(receiving.toByteArray())) {
                                ObjectInputStream ois2 = new ObjectInputStream(bis2);
                                payloads.add((IncomingType) ois2.readObject());
                            }
                            catch(InvalidClassException e) {
                                System.err.println("ServerPayload: invalid type");
                                e.printStackTrace();
                            } finally {
                                receiving.reset();
                            }
                        }
                    }
                }
            } catch(Exception e) {
                System.err.println("ServerPayload: error, now we pray");
                e.printStackTrace();
            }
        }
        return payloads;
    }
}
