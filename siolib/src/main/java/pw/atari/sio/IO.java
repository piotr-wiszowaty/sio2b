package pw.atari.sio;

import java.io.IOException;

public interface IO {
    int read(byte[] buffer, int offset, int length) throws IOException;
    void write(byte[] buffer, int offset, int length) throws IOException;
}
