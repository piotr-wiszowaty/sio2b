package pw.atari.sio;

public interface Logger {
    void e(String message);
    void e(String message, Exception exception);
    void d(String message);
    void d(String message, Exception exception);
}
