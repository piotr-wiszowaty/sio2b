package pw.atari.sio;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SIO2B implements ActionListener, IO, GUIManager, Logger {
    private static final int PCLK_HC0X = 48000000;
    private static final int PCLK_USB = 40000000;
    private static final int BT_CHUNK_SIZE = 64;
    private static final int F_OSC = 1773447;

    private static final String EMPTY_DISK_TEXT = "                                                                      ";

    private JFrame frame;
    private JPanel mainPanel;
    private JProgressBar progressBar;
    private JButton[] diskButtons = new JButton[SIO.MAX_DISKS];
    private JButton[] reloadButtons = new JButton[SIO.MAX_DISKS];
    private JButton[] ejectButtons = new JButton[SIO.MAX_DISKS];
    private JButton serialDeviceButton;
    private JTextField[] diskLabels = new JTextField[SIO.MAX_DISKS];
    private JTextField serialDeviceLabel;
    private JLabel selectSpeedLabel;
    private JComboBox<String> sioSpeedSelector;
    private JTextField currentBPSDisplay;
    private JTextField statusBar;
    private String directory;
    private File serialDevice;
    private SerialWrapper serial;
    private SIO sio;

    private String[] sioSpeeds;
    private Pattern hsiExtractPattern = Pattern.compile("\\d+");

    private String initialSerialDevicePath;
    private String initialDisk1Path;
    private String initialDisk2Path;
    private String initialDisk3Path;
    private String initialDisk4Path;

    private boolean debugEnabled = false;
    private boolean useJssc = false;
    private boolean useUSB = false;
    private int highSpeedIndex = 40;

    public static void main(String[] args) {
        new SIO2B(args);
    }

    public SIO2B(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--serial-port=")) {
                initialSerialDevicePath = getArgValue(arg);
            } else if (arg.startsWith("--d1=")) {
                initialDisk1Path = getArgValue(arg);
            } else if (arg.startsWith("--d2=")) {
                initialDisk2Path = getArgValue(arg);
            } else if (arg.startsWith("--d3=")) {
                initialDisk3Path = getArgValue(arg);
            } else if (arg.startsWith("--d4=")) {
                initialDisk4Path = getArgValue(arg);
            } else if (arg.equals("--verbose")) {
                debugEnabled = true;
            } else if (arg.equals("--with-jssc")) {
                useJssc = true;
            } else if (arg.startsWith("--hs-index=")) {
                highSpeedIndex = Integer.parseInt(getArgValue(arg));
            } else if (arg.equals("--usb")) {
                useUSB = true;
            } else {
                System.err.println("unknown argument: " + arg);
                System.exit(0);
            }
        }

        TreeSet<Integer> set = new TreeSet<Integer>();
        set.add(40); 
        set.add(16);
        set.add(10);
        set.add(9);
        set.add(8);
        set.add(6);
        set.add(4);
        set.add(2);
        set.add(1);
        set.add(highSpeedIndex);
        Iterator<Integer> iterator = set.descendingIterator();
        sioSpeeds = new String[set.size()];
        int i = 0;
        while (iterator.hasNext()) {
            Integer k = iterator.next();
            int baud = calculateBaud(k);
            sioSpeeds[i++] = String.format("HS Index: %2d (%1.1f kbps)", k, (float) baud / 1000.0);
        }

        sio = new SIO(this, this, this);
        sio.setSIOSpeed((byte) (highSpeedIndex & 0xff), calculateUBRR(highSpeedIndex));
        sio.start();

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private String getArgValue(String arg) {
        return arg.split("=", 2)[1];
    }

    private void createDiskControls(Container pane, int index, int y) {
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = y;
        c.insets = new Insets(4, 0, 4, 4);
        c.weightx = 0.0;
        JButton b = new JButton("D" + (index + 1) + ":");
        b.setActionCommand("disk-" + index);
        b.addActionListener(this);
        pane.add(b, c);
        diskButtons[index] = b;

        c.gridx = 1;
        c.insets = new Insets(0, 0, 0, 0);
        c.weightx = 1.0;
        JTextField tf = new JTextField(EMPTY_DISK_TEXT);
        tf.setEditable(false);
        pane.add(tf, c);
        diskLabels[index] = tf;

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.insets = new Insets(4, 0, 4, 4);
        c.weightx = 0.0;
        b = new JButton("Reload");
        b.setActionCommand("reload-" + index);
        b.addActionListener(this);
        pane.add(b, c);
        reloadButtons[index] = b;

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.insets = new Insets(4, 0, 4, 4);
        c.weightx = 0.0;
        b = new JButton("Eject");
        b.setActionCommand("eject-" + index);
        b.addActionListener(this);
        pane.add(b, c);
        ejectButtons[index] = b;
    }

    private void createAndShowGUI() {
        frame = new JFrame("SIO2B");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent winEvt) {
                sio.stop();
                try {
                    if (serial != null) {
                        serial.close();
                    }
                } catch (Exception e) {
                    e(e.getMessage(), e);
                }
                System.exit(0);
            }
        });

        mainPanel = new JPanel(new BorderLayout());
        JPanel gridPanel = new JPanel(new GridBagLayout());

        progressBar = new JProgressBar(0, 100);
        mainPanel.add(progressBar, BorderLayout.NORTH);

        int y = 0;

        for (int i = 0; i < SIO.MAX_DISKS; i++) {
            createDiskControls(gridPanel, i, y++);
        }

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 1;
        c.insets = new Insets(4, 0, 4, 4);
        serialDeviceButton = new JButton("Serial port");
        serialDeviceButton.setActionCommand("serial-port");
        serialDeviceButton.addActionListener(this);
        gridPanel.add(serialDeviceButton, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.insets = new Insets(0, 0, 0, 0);
        c.weightx = 1.0;
        serialDeviceLabel = new JTextField(serialDevice != null ? serialDevice.getAbsolutePath() : EMPTY_DISK_TEXT);
        serialDeviceLabel.setEditable(false);
        gridPanel.add(serialDeviceLabel, c);

        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 1;
        c.weightx = 1.0;
        selectSpeedLabel = new JLabel("Select SIO speed:");
        gridPanel.add(selectSpeedLabel, c);

        c.gridx = 1;
        c.gridy = y++;
        c.gridwidth = 3;
        c.weightx = 1.0;
        sioSpeedSelector = new JComboBox<String>(sioSpeeds);
        sioSpeedSelector.setActionCommand("sio-speed");
        sioSpeedSelector.addActionListener(this);
        for (int i = 0; i < sioSpeeds.length; i++) {
            Matcher matcher = hsiExtractPattern.matcher(sioSpeeds[i]);
            matcher.find();
            if (Integer.parseInt(matcher.group()) == highSpeedIndex) {
                sioSpeedSelector.setSelectedIndex(i);
                break;
            }
        }
        gridPanel.add(sioSpeedSelector, c);

        mainPanel.add(gridPanel, BorderLayout.CENTER);

        JPanel textPanel = new JPanel(new BorderLayout());

        currentBPSDisplay = new JTextField();
        currentBPSDisplay.setEditable(false);
        textPanel.add(currentBPSDisplay, BorderLayout.NORTH);

        statusBar = new JTextField();
        statusBar.setEditable(false);
        textPanel.add(statusBar, BorderLayout.SOUTH);

        mainPanel.add(textPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);

        frame.pack();
        frame.setVisible(true);

        if (initialSerialDevicePath != null) {
            changeSerialDevicePath(initialSerialDevicePath);
        }
        if (initialDisk1Path != null) {
            sio.loadDiskImage(initialDisk1Path, 1);
            presetDirectory(initialDisk1Path);
        }
        if (initialDisk2Path != null) {
            sio.loadDiskImage(initialDisk2Path, 2);
            presetDirectory(initialDisk2Path);
        }
        if (initialDisk3Path != null) {
            sio.loadDiskImage(initialDisk3Path, 3);
            presetDirectory(initialDisk3Path);
        }
        if (initialDisk4Path != null) {
            sio.loadDiskImage(initialDisk4Path, 4);
            presetDirectory(initialDisk4Path);
        }
    }

    private void presetDirectory(String path) {
        if (directory == null) {
            File file = new File(path);
            if (file.exists()) {
                directory = file.getParent();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        Matcher matcher1 = Pattern.compile("disk-([0-3])").matcher(e.getActionCommand());
        Matcher matcher2 = Pattern.compile("reload-([0-3])").matcher(e.getActionCommand());
        Matcher matcher3 = Pattern.compile("eject-([0-3])").matcher(e.getActionCommand());
        if (matcher1.matches()) {
            int index = Integer.parseInt(matcher1.group(1));
            final JFileChooser fc = (directory == null) ? new JFileChooser() : new JFileChooser(directory);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Atari files (.atr, .com, .exe, .xex)", "atr", "com", "exe", "xex");
            fc.setFileFilter(filter);
            int result = fc.showOpenDialog(mainPanel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                if (f.isFile()) {
                    directory = f.getParent();
                    sio.loadDiskImage(f.getAbsolutePath(), index + 1);
                } else if (f.isDirectory()) {
                    diskLabels[index].setText(EMPTY_DISK_TEXT);
                    directory = f.getAbsolutePath();
                }
            }
        } else if (matcher2.matches()) {
            int index = Integer.parseInt(matcher2.group(1));
            String path = diskLabels[index].getText();
            sio.loadDiskImage(path, index + 1);
        } else if (matcher3.matches()) {
            sio.eject(Integer.parseInt(matcher3.group(1)) + 1);
        } else if ("serial-port".equals(e.getActionCommand())) {
            String s = (String) JOptionPane.showInputDialog(frame, "Specify serial port:");
            if (s != null) {
                changeSerialDevicePath(s);
            }
        } else if ("sio-speed".equals(e.getActionCommand())) {
            JComboBox<String> cb = (JComboBox<String>) e.getSource();
            String item = (String) cb.getSelectedItem();
            Matcher matcher = hsiExtractPattern.matcher(item);
            matcher.find();
            int highSpeedIndex = Integer.parseInt(matcher.group());
            sio.setSIOSpeed(highSpeedIndex, calculateUBRR(highSpeedIndex));
        }
    }

    private void changeSerialDevicePath(String path) {
        if (serialDevice != null && serialDevice.getPath().equals(path)) {
            return;
        }

        serialDeviceLabel.setText(path);

        if (serialDevice != null) {
            sio.ioStop();
            try {
                serial.close();
            } catch (Exception e) {
                e("close", e);
            }
        }

        serialDevice = new File(path);
        try {
            if (useJssc) {
                serial = new JsscSerialWrapper(serialDevice);
            } else {
                serial = new BasicSerialWrapper(serialDevice);
            }
            sio.ioStart();
        } catch (Exception e) {
            e(e.getMessage(), e);
        }
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        try {
            return serial.read(buffer, offset, length);
        } catch (Exception e) {
            e(e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        try {
            if (useUSB) {
                for (int i = 0; i < length; i += BT_CHUNK_SIZE) {
                    serial.write(buffer, offset+i, Math.min(BT_CHUNK_SIZE, length - i));
                }
            } else {
                serial.write(buffer, offset, length);
            }
        } catch (Exception e) {
            e(e.getMessage(), e);
        }
    }

    @Override
    public void showMessage(String message) {
        statusBar.setText(message);
    }

    @Override
    public void setDiskLabel(int index, String label, String path) {
        diskLabels[index].setText(path);
    }

    private void log(String level, String message) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(fmt.format(new Date()) + "|" + level + "|" + message);
    }

    @Override
    public void e(String message, Exception exception) {
        log("ERROR", message);
        exception.printStackTrace();
    }

    @Override
    public void e(String message) {
        log("ERROR", message);
    }

    @Override
    public void d(String message) {
        if (debugEnabled) {
            log("DEBUG", message);
        }
    }

    @Override
    public void d(String message, Exception exception) {
        if (debugEnabled) {
            log("DEBUG", message);
            exception.printStackTrace();
        }
    }

    @Override
    public Locale getLocale() {
        return new Locale("en");
    }

    private int calculateBaud(int highSpeedIndex) {
        return (F_OSC / 2) / (highSpeedIndex + 7);
    }

    private int calculateUBRR(int highSpeedIndex) {
        int baud = calculateBaud(highSpeedIndex);
        if (useUSB) {
            return PCLK_USB / 16 / baud - 1;
        } else {
            return PCLK_HC0X / baud;
        }
    }

    @Override
    public void updateSIOSpeed(int highSpeedIndex) {
        int baud = calculateBaud(highSpeedIndex);
        String message = String.format("Current HS Index: %2d (%1.1f kbps)", highSpeedIndex, (float) baud / 1000.0);
        currentBPSDisplay.setText(message);
    }

    @Override
    public void updateProgress(int progress) {
        progressBar.setValue(progress);
    }
}
