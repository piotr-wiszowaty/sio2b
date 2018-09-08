package pw.atari.sio;

interface SerialWrapper {
    void close() throws Exception;
    int read(byte[] buffer, int offset, int length) throws Exception;
    void write(byte[] buffer, int offset, int length) throws Exception;
}
