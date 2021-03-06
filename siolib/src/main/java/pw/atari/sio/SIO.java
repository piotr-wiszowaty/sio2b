//
// Copyright (C) 2014-2021 Piotr Wiszowaty
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
//
package pw.atari.sio;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

public class SIO implements Runnable {
    public static final int MAX_DISKS = 4;

    private static final int DEVICE_TIME = 0x64;

    private static final int CMD_FORMAT_DISK = 0x21;
    private static final int CMD_FORMAT_MEDIUM = 0x22;
    private static final int CMD_SEND_HIGH_SPEED_INDEX = 0x3f;
    private static final int CMD_HAPPY_CONFIG = 0x48;
    private static final int CMD_READ_PERCOM = 0x4e;
    private static final int CMD_PUT_SECTOR = 0x50;
    private static final int CMD_READ_SECTOR = 0x52;
    private static final int CMD_READ_STATUS = 0x53;
    private static final int CMD_WRITE_SECTOR = 0x57;
    private static final int CMD_GET_CHUNK = 0xf8;
    private static final int CMD_GET_NEXT_CHUNK = 0xf9;
    private static final int CMD_TICK = 0xfc;

    private static final int END = 0xc0;
    private static final int ESC = 0xdb;
    private static final int ESC_END = 0xdc;
    private static final int ESC_ESC = 0xdd;

    private static final long SIO_READ_TIMEOUT = 2000l;

    private boolean ioRun;
    private Thread ioThread;
    private IO io;
    private Logger logger;
    private GUIManager gui;
    private DiskImage[] diskImages = new DiskImage[MAX_DISKS];
    private int highSpeedIndex = 40;
    private int ubrr = 2544;

    private boolean slipEscape = false;

    public SIO(IO io, Logger logger, GUIManager gui) {
        this.io = io;
        this.logger = logger;
        this.gui = gui;
    }

    public void start() {
    }

    public void stop() {
    }

    public void ioStart() {
        ioRun = true;
        ioThread = new Thread(this, "I/O thread");
        ioThread.start();
    }

    public void ioStop() {
        try {
            ioRun = false;
            if (ioThread != null) {
                ioThread.join();
            }
        } catch (InterruptedException e) {
        }
    }

    public void loadDiskImage(String path, int diskNumber) {
        int i = diskNumber - 1;
        if (diskImages[i] != null) {
            try {
                diskImages[i].close();
            } catch (IOException e) {
                logger.e(e.getMessage(), e);
            }
        }
        try {
            String lpath = path.toLowerCase(gui.getLocale());
            if (lpath.endsWith(".atr")) {
                diskImages[i] = new ATR(path);
            } else if (i == 0 && (lpath.endsWith(".xex") || lpath.endsWith(".exe") || lpath.endsWith(".com"))) {
                XEX x = new XEX(path);
                x.setHighSpeed(highSpeedIndex < 40);
                diskImages[i] = x;
            } else if (lpath.endsWith(".debug")) {
                diskImages[i] = new DebugDiskImage(path);
            } else {
                diskImages[i] = null;
            }
        } catch (Exception e) {
            diskImages[i] = null;
            logger.e(e.getMessage(), e);
        }
        if (diskImages[i] != null) {
            File file = new File(path);
            String fileName = file.getName();
            gui.setDiskLabel(i, "D" + diskNumber + ":" + fileName, file.getAbsolutePath());
        } else {
            gui.setDiskLabel(i, "D" + diskNumber + ":", "");
        }
    }

    public String eject(int diskNumber) {
        int i = diskNumber - 1;
        String path = null;
        if (diskImages[i] != null) {
            path = diskImages[i].getPath();
            try {
                diskImages[i].close();
                gui.setDiskLabel(i, "D" + (i + 1) + ":", "");
            } catch (IOException e) {
                logger.e(e.getMessage(), e);
            }
            diskImages[i] = null;
        }
        return path;
    }

    public String getPath(int diskNumber) {
        int i = diskNumber - 1;
        if (diskImages[i] != null) {
            return diskImages[i].getPath();
        } else {
            return null;
        }
    }

    public void setSIOSpeed(int highSpeedIndex, int ubrr) {
        this.highSpeedIndex = highSpeedIndex;
        this.ubrr = ubrr;
        for (DiskImage di : diskImages) {
            if (di != null && di instanceof XEX) {
                ((XEX) di).setHighSpeed(highSpeedIndex < 40);
            }
        }
    }

    public int getHighSpeedIndex() {
        return highSpeedIndex;
    }

    private byte checksum(byte[] data, int offset, int length) {
        int checksum = 0;
        int temp;
        for (int i = 0; i < length; i++) {
            temp = checksum + (data[offset + i] & 0xff);
            checksum = (temp & 0xff) + ((temp >> 8) & 0xff);
        }
        return (byte) (checksum & 0xff);
    }

    private void sendErrorResponse() throws IOException {
        byte[] buffer = new byte[1];
        buffer[0] = 'E';
        write(buffer, 0, 1);
    }

    private void write(byte[] buffer, int offset, int length) throws IOException {
        byte[] buffer2 = new byte[length + 2];
        buffer2[0] = (byte) (length & 0xff);
        buffer2[1] = (byte) ((length >> 8) & 0xff);
        for (int i = 0; i < length; i++) {
            buffer2[2+i] = buffer[offset+i];
        }
        io.write(buffer2, 0, length+2);
    }

    private void sendData(byte[] data) throws IOException {
        logger.d("sending (" + data.length + ")");
        byte[] buffer = new byte[2 + data.length];
        buffer[0] = 'C';
        for (int i = 0; i < data.length; i++) {
            buffer[1 + i] = data[i];
        }
        buffer[1 + data.length] = checksum(data, 0, data.length);
        write(buffer, 0, buffer.length);
    }

    private int slipRead(byte[] buffer, int offset, int length) throws IOException {
        byte[] buffer2 = new byte[length];
        int length2 = io.read(buffer2, 0, length);
        int i;
        int offset2 = offset;
        for (i = 0; i < length2; i++) {
            byte b = buffer2[i];
            if (slipEscape) {
                slipEscape = false;
                if ((b & 0xff) == ESC_END) {
                    buffer[offset2++] = (byte) END;
                } else if ((b & 0xff) == ESC_ESC) {
                    buffer[offset2++] = (byte) ESC;
                } else {
                    // error: invalid escape sequence
                }
            } else {
                if ((b & 0xff) == END) {
                    // flush
                    offset2 = offset;
                } else if ((b & 0xff) == ESC) {
                    slipEscape = true;
                } else {
                    buffer[offset2++] = b;
                }
            }
        }

        return offset2 - offset;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        int length;
        int i = 0;
        DiskImage di;
        int ddevic;
        int dcmnd;
        int daux1;
        int daux2;
        long t0;

        while (ioRun) {
            try {
                // Wait for SLIP END symbol
                i = 0;
                while (i < 1) {
                    length = io.read(buffer, 0, 1);
                    if (length == 1 && (buffer[0] & 0xff) == END) {
                        i++;
                    }
                }

                // Receive command (4 bytes)
                i = 0;
                t0 = System.currentTimeMillis();
                while (i < 4) {
                    length = slipRead(buffer, i, 4 - i);
                    i += length;
                    if (System.currentTimeMillis() - t0 > SIO_READ_TIMEOUT) {
                        continue;
                    }
                }
                ddevic = buffer[0] & 0xff;
                dcmnd = buffer[1] & 0xff;
                daux1 = buffer[2] & 0xff;
                daux2 = buffer[3] & 0xff;

                // Show command
                String message = String.format("$%02X $%02X $%02X $%02X", ddevic, dcmnd, daux1, daux2);
                if (dcmnd != CMD_TICK) {
                    gui.showMessage(message);
                }
                logger.d("command: " + message);

                // Execute command
                switch (dcmnd) {
                    case CMD_READ_PERCOM:
                        if (ddevic >= 0x31 && ddevic < 0x31+diskImages.length && (di = diskImages[ddevic - 0x31]) != null) {
                            buffer[i = 0] = 'C';
                            buffer[++i] = (byte) di.getTotalTracks();                     // [0] # of tracks
                            buffer[++i] = (byte) 3;                                       // [1] head movement speed (6 ms)
                            buffer[++i] = (byte) ((di.getSectorsPerTrack() >> 8) & 0xff); // [2] # sectors/track high byte
                            buffer[++i] = (byte) (di.getSectorsPerTrack() & 0xff);        // [3] # sectors/track low byte
                            buffer[++i] = (byte) 0;                                       // [4] # active sides - 1
                            buffer[++i] = (byte) 0x00;                                    // [5] bit1=1: 8" (=0: 5.25"), bit2=1: MFM (=0: FM), bit3=1: sideless disk
                            buffer[++i] = (byte) ((di.getSectorSize() >> 8) & 0xff);      // [6] sector size high byte
                            buffer[++i] = (byte) (di.getSectorSize() & 0xff);             // [7] sector size low byte
                            buffer[++i] = (byte) 0xff;                                    // [8] unused, $FF
                            buffer[++i] = (byte) 0x00;                                    // [9] unused, $00
                            buffer[++i] = (byte) 0x00;                                    // [10] unused, $00
                            buffer[++i] = (byte) 0x00;                                    // [11] unused, $00
                            buffer[++i] = checksum(buffer, 1, 12);
                            write(buffer, 0, i+1);
                        } else {
                            sendErrorResponse();
                        }
                        break;

                    case CMD_READ_STATUS:
                        if (ddevic >= 0x31 && ddevic < 0x31+diskImages.length && (di = diskImages[ddevic - 0x31]) != null) {
                            buffer[i = 0] = 'C';
                            buffer[++i] = (byte) ((di.getSectorSize() == 0x100 ? 0x20 : 0x00) | (di.isWriteProtected() ? 0x08 : 0x00));
                            buffer[++i] = (byte) 0xff;
                            buffer[++i] = (byte) 0xf0;   // disk operation timeout
                            buffer[++i] = 0x00;
                            buffer[++i] = checksum(buffer, 1, 4);
                            write(buffer, 0, i+1);
                        } else {
                            sendErrorResponse();
                        }
                        break;

                    case CMD_FORMAT_DISK:
                        if (ddevic >= 0x31 && ddevic < 0x31+diskImages.length && (di = diskImages[ddevic - 0x31]) != null) {
                            try {
                                di.format();
                                byte[] sector = new byte[di.getSectorSize()];
                                sector[0] = -1;
                                sector[1] = -1;
                                sendData(sector);
                            } catch (Exception e) {
                                logger.e(e.getMessage(), e);
                                sendErrorResponse();
                            }
                        }
                        break;

                    case CMD_FORMAT_MEDIUM:
                        if (ddevic >= 0x31 && ddevic < 0x31+diskImages.length && (di = diskImages[ddevic - 0x31]) != null) {
                            try {
                                di.format();
                                byte[] sector = new byte[128];
                                sector[0] = -1;
                                sector[1] = -1;
                                sendData(sector);
                            } catch (Exception e) {
                                logger.e(e.getMessage(), e);
                                sendErrorResponse();
                            }
                        }
                        break;

                    case CMD_READ_SECTOR:
                        if (ddevic >= 0x31 && ddevic < 0x31+diskImages.length && (di = diskImages[ddevic - 0x31]) != null) {
                            try {
                                byte[] sector = di.getSector((daux2 << 8) + daux1);
                                sendData(sector);
                                gui.updateProgress(di.getReadProgress());
                            } catch (Exception e) {
                                logger.e(e.getMessage(), e);
                                sendErrorResponse();
                            }
                        } else if (ddevic == DEVICE_TIME) {
                            try {
                                byte[] sector = new byte[7];
                                Calendar cal = Calendar.getInstance();
                                sector[0] = (byte) (cal.get(Calendar.YEAR) & 0xff);
                                sector[1] = (byte) (cal.get(Calendar.YEAR) >> 8);
                                sector[2] = (byte) cal.get(Calendar.MONTH);
                                sector[3] = (byte) cal.get(Calendar.DAY_OF_MONTH);
                                sector[4] = (byte) cal.get(Calendar.HOUR_OF_DAY);
                                sector[5] = (byte) cal.get(Calendar.MINUTE);
                                sector[6] = (byte) cal.get(Calendar.SECOND);
                                sendData(sector);
                            } catch (Exception e) {
                                logger.e(e.getMessage(), e);
                                sendErrorResponse();
                            }
                        } else {
                            sendErrorResponse();
                        }
                        break;

                    case CMD_WRITE_SECTOR:
                    case CMD_PUT_SECTOR:
                        if (ddevic >= 0x31 && ddevic < 0x31+diskImages.length && (di = diskImages[ddevic - 0x31]) != null) {
                            int sectorNumber = (daux2 << 8) + daux1;
                            int size = di.getSectorSize(sectorNumber);
                            buffer[0] = 'c';
                            buffer[1] = (byte) (size & 0xff);
                            buffer[2] = (byte) ((size >> 8) & 0xff);
                            write(buffer, 0, 3);

                            int count = 0;
                            boolean error = false;
                            t0 = System.currentTimeMillis();
                            while (count < size + 1) {
                                count += slipRead(buffer, count, size + 1 - count);
                                if (buffer[0] != 'c') {
                                    error = true;
                                    break;
                                }
                                if (System.currentTimeMillis() - t0 > SIO_READ_TIMEOUT) {
                                    logger.w("read timeout (1)");
                                    error = true;
                                    break;
                                }
                            }
                            if (error) {
                                break;
                            }
                            try {
                                di.putSector(sectorNumber, buffer, 1);
                                buffer[0] = 'C';
                            } catch (Exception e) {
                                logger.e(e.getMessage(), e);
                                buffer[0] = 'E';
                            }
                            write(buffer, 0, 1);
                        } else {
                            buffer[0] = 'e';
                            write(buffer, 0, 1);
                        }
                        break;

                    case CMD_GET_CHUNK:
                    case CMD_GET_NEXT_CHUNK:
                        if ((di = diskImages[ddevic - 0x31]) != null) {
                            if (di instanceof XEX) {
                                byte[] data = ((XEX) di).getChunk(daux1 | (daux2 << 8), dcmnd == CMD_GET_NEXT_CHUNK);
                                sendData(data);
                                gui.updateProgress(di.getReadProgress());
                            } else {
                                sendErrorResponse();
                            }
                        } else {
                            sendErrorResponse();
                        }
                        break;

                    case CMD_SEND_HIGH_SPEED_INDEX:
                        gui.updateSIOSpeed(highSpeedIndex);
                        sendData(new byte[] {(byte) (highSpeedIndex & 0xff)});
                        break;

                    case CMD_HAPPY_CONFIG:
                        sendData(new byte[0]);
                        break;

                    case CMD_TICK:
                        gui.updateSIOSpeed(daux1);
                        buffer[0] = 0;
                        for (i = 0; i < diskImages.length; i++) {
                            if (diskImages[i] != null) {
                                buffer[0] |= (1 << i);
                            }
                        }
                        buffer[1] = (byte) (highSpeedIndex & 0xff);
                        buffer[2] = (byte) (ubrr & 0xff);
                        buffer[3] = (byte) ((ubrr >> 8) & 0xff);
                        write(buffer, 0, 4);
                        break;

                    default:
                        break;
                }
            } catch (IOException e) {
                if (ioRun) {
                    logger.e("run(): " + e.getMessage(), e);
                    break;
                }
            }
        }

        logger.d("I/O thread finish");
    }

    private String hexDump(byte[] a, int size) {
        StringBuilder b = new StringBuilder();
        int i;
        int c;
        int limit = Math.min(a.length, size);
        for (int offset = 0; offset < limit; offset += 16) {
            b.append(String.format("%04x  ", offset));
            for (i = 0; i < 16; i++) {
                if (offset + i < limit) {
                    b.append(String.format("%02x ", 0xff & a[offset + i]));
                } else {
                    b.append("   ");
                }
                if (i == 3 || i == 11) {
                    b.append(" ");
                } else if (i == 7) {
                    if (offset + i < limit) {
                        b.append("- ");
                    } else {
                        b.append("  ");
                    }
                }
            }
            b.append("  ");
            for (i = 0; i < 16; i++) {
                if (offset + i < limit) {
                    c = a[offset + i];
                    if (c >= 0x20 && c <= 0x7f) {
                        b.append((char) c);
                    } else {
                        b.append('.');
                    }
                } else {
                    b.append(' ');
                }
            }
            b.append('\n');
        }
        return b.toString();
    }
}
