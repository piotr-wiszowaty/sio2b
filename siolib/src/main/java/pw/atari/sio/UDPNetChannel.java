package pw.atari.sio;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

class UDPNetChannel extends NetChannel {
    private String host;
    private int port;
    private InetAddress addr;
    private DatagramSocket socket;

    @Override
    boolean open(String host, int port) {
        this.host = host;
        this.port = port;
        try {
            addr = InetAddress.getByName(host);
            status = "open";
            return true;
        } catch (UnknownHostException e) {
            status = e.getMessage();
            return false;
        }
    }

    @Override
    void close() {
        status = "closed";
    }

    @Override
    int read(byte[] buffer, int offset) {
        // TODO
        return 0;
    }

    @Override
    int write(byte[] buffer, int offset, int length) {
        // TODO
        return 0;
    }
}
