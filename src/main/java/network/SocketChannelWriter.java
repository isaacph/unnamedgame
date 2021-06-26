package network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.*;

public class SocketChannelWriter<OutgoingType> {

    private static class SocketPayload<OutgoingType> {
        public SocketChannel socket;
        public OutgoingType payload;
        public Runnable onSent;

        public SocketPayload(SocketChannel socket, OutgoingType payload, Runnable onSent) {
            this.socket = socket;
            this.payload = payload;
            this.onSent = onSent;
        }
    }

    private final Queue<SocketPayload<OutgoingType>> socketQueue;
    private ByteBuffer sending;
    private SocketChannel sendingTarget;
    private Runnable sendingFinishCallback;

    public SocketChannelWriter() {
        socketQueue = new ArrayDeque<>();
        sending = ByteBuffer.wrap(new byte[] {});
        sendingTarget = null;
    }

    public void queueSend(SocketChannel socket, OutgoingType payload) {
        socketQueue.add(new SocketPayload<>(socket, payload, null));
    }

    public void queueSend(SocketChannel socket, OutgoingType payload, Runnable onSent) {
        socketQueue.add(new SocketPayload<>(socket, payload, onSent));
    }

    public void update() throws SocketChannelWriter.Exception, ClosedChannelException {
        do {
            if(sending.remaining() == 0) {
                if(socketQueue.isEmpty()) break;
                try {
                    SocketPayload<OutgoingType> p = socketQueue.remove();
                    if(p.socket != null && p.payload != null) {
                        sendingTarget = p.socket;
                        sending = serialize(p.payload);
                        sendingFinishCallback = p.onSent;
                    }
                } catch(IOException e) {
                    throw new SocketChannelWriter.Exception(e, sendingTarget, false);
                }
            }
            if(sending.remaining() > 0) {
                try {
                    int bytes = sendingTarget.write(sending);
                    System.out.println("W: " + bytes);
                    if(sending.remaining() <= bytes && sendingFinishCallback != null) {
                        sendingFinishCallback.run();
                    }
                } catch(IOException e) {
                    sending = ByteBuffer.wrap(new byte[] {});
                    throw new SocketChannelWriter.Exception(e, sendingTarget, true);
                }
            }
        } while(sending.remaining() == 0);
    }

    private ByteBuffer serialize(OutgoingType payload) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(new byte[] {0, 0, 0, 0});
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(payload);
        oos.close();
        byte[] ba = bos.toByteArray();
        bos.close();
        int size = ba.length - 4;
        bos = new ByteArrayOutputStream(4);
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(size);
        dos.close();
        byte[] basize = bos.toByteArray();
        bos.close();
        System.arraycopy(basize, 0, ba, 0, 4);
        return ByteBuffer.wrap(ba);
    }

    public void clearQueue() {
        socketQueue.clear();
    }

    public static class Exception extends IOException {

        public SocketChannel channel;
        public boolean serialized;

        public Exception(IOException e, SocketChannel channel, boolean serialized) {
            super(e);
            this.channel = channel;
            this.serialized = serialized;
        }
    }
}
