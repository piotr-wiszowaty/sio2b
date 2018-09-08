package pw.atari.sio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

class BasicSerialWrapper implements SerialWrapper {
    private FileInputStream serialInput;
    private FileOutputStream serialOutput;

    BasicSerialWrapper(File serialDevice) throws Exception {
        serialInput = new FileInputStream(serialDevice);
        serialOutput = new FileOutputStream(serialDevice);
    }

    public void close() throws Exception {
        if (serialInput != null) {
            serialInput.close();
        }
        if (serialOutput != null) {
            serialOutput.close();
        }
    }

    public int read(byte[] buffer, int offset, int length) throws Exception {
        return serialInput.read(buffer, offset, length);
    }

    public void write(byte[] buffer, int offset, int length) throws Exception {
        serialOutput.write(buffer, offset, length);
    }
}
