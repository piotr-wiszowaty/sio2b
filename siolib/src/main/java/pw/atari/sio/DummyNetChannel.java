package pw.atari.sio;

class DummyNetChannel extends NetChannel {
    DummyNetChannel() {
        this.status = "closed";
    }

    @Override
    boolean open(String host, int port) {
        return false;
    }

    @Override
    void close() {
    }

    @Override
    int read(byte[] buffer, int offset) {
        return 0;
    }

    @Override
    int write(byte[] buffer, int offset, int length) {
        return 0;
    }
}
