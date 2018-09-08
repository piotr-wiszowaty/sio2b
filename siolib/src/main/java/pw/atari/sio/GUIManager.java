package pw.atari.sio;

import java.util.Locale;

public interface GUIManager {
    void setDiskLabel(int index, String label, String path);
    void showMessage(String message);
    void updateSIOSpeed(int highSpeedIndex);
    Locale getLocale();
    void updateProgress(int percent);
}
