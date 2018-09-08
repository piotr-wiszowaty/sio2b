package pw.atari.sio;

import java.io.File;

import jssc.SerialPort;

class JsscSerialWrapper implements SerialWrapper {
    private SerialPort serialPort;

    JsscSerialWrapper(File serialDevice) throws Exception {
        serialPort = new SerialPort(serialDevice.getPath());
        serialPort.openPort();
    }

    public void close() throws Exception {
        if (serialPort != null) {
            serialPort.closePort();
        }
    }

    public int read(byte[] buffer, int offset, int length) throws Exception {
        byte[] buffer2 = serialPort.readBytes(length);
        for (int i = 0; i < buffer2.length; i++) {
            buffer[offset + i] = buffer2[i];
        }
        return buffer2.length;
    }

    public void write(byte[] buffer, int offset, int length) throws Exception {
        byte[] buffer2 = new byte[length];
        for (int i = 0; i < length; i++) {
            buffer2[i] = buffer[offset + i];
        }
        serialPort.writeBytes(buffer2);
    }
}
