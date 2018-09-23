package pw.atari.sio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class TCPNetChannel extends NetChannel {
    private static final long CONNECT_TIMEOUT = 2000l;
    private static final long WRITE_TIMEOUT = 2000l;

    private String host;
    private int port;
    private SocketChannel socket;

    @Override
    boolean open(String host, int port) {
        this.host = host;
        this.port = port;
        try {
            socket = SocketChannel.open();
            socket.configureBlocking(false);
            socket.connect(new InetSocketAddress(host, port));
            long t0 = System.currentTimeMillis();
            while (!socket.finishConnect()) {
                if (System.currentTimeMillis() - t0 > CONNECT_TIMEOUT) {
                    socket.close();
                    status = "closed";
                    socket = null;
                    return false;
                }
            }
            status = "open";
            return true;
        } catch (IOException e) {
            status = e.getMessage();
            return false;
        }
    }

    @Override
    void close() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
        }
        status = "closed";
    }

    @Override
    int read(byte[] buffer, int offset) {
        if (socket != null) {
            try {
                ByteBuffer inBuffer = ByteBuffer.allocate(buffer.length - offset);
                int r = socket.read(inBuffer);
                if (r > 0) {
                    inBuffer.flip();
                    inBuffer.get(buffer, offset, r);
                }
                return r;
            } catch (IOException e) {
                status = e.getMessage();
                return -1;
            }
        } else {
            return -1;
        }
    }

    @Override
    int write(byte[] buffer, int offset, int length) {
        if (socket != null) {
            try {
                ByteBuffer outBuffer = ByteBuffer.allocate(length);
                outBuffer.put(buffer, offset, length);
                outBuffer.flip();
                int written = 0;
                long t0 = System.currentTimeMillis();
                while (outBuffer.hasRemaining()) {
                    written += socket.write(outBuffer);
                    if (System.currentTimeMillis() - t0 > WRITE_TIMEOUT) {
                        socket.close();
                        status = "closed";
                        socket = null;
                        break;
                    }
                }
                return written;
            } catch (IOException e) {
                status = e.getMessage();
                return 0;
            }
        } else {
            return 0;
        }
    }
}
