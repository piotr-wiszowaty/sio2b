package pw.atari.sio;

abstract class NetChannel {
    protected String status;

    abstract boolean open(String host, int port);
    abstract void close();
    abstract int read(byte[] buffer, int offset);
    abstract int write(byte[] buffer, int offset, int length);

    String status() {
        return status;
    }
}
