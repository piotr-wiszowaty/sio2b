package pw.atari.sio;

public class SectorNotFoundException extends Exception {
    private static final long serialVersionUID = -1;

    private String message;

    public SectorNotFoundException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
