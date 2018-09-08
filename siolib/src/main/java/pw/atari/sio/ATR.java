package pw.atari.sio;

import java.io.IOException;
import java.util.Arrays;

/*
 * Conventions
 * -----------
 *
 * DWORD - 32bit unsigned long (little endian)
 * WORD  - 16bit unsigned short (little endian)
 * BYTE  - 8bit unsigned char
 *
 * Header
 * ------
 *
 * 16 bytes long.
 *
 * Type     Name         Description
 * WORD     wMagic         $0296 (sum of 'NICKATARI')
 * WORD     wPars         size of this disk image, in paragraphs (size/$10)
 * WORD     wSecSize     sector size. ($80 or $100) bytes/sector
 * BYTE     btParsHigh     high part of size, in paragraphs (added by REV 3.00)
 * DWORD     dwCRC         32bit CRC of file (added by APE?)
 * DWORD     dwUnused     unused
 * BYTE     btFlags     bit 0 (ReadOnly) (added by APE?)
 *
 * Body
 * ----
 * Then there are continuous sectors. Some ATR files are incorrect - if sector size is > $80 first three sectors should be $80 long.
 * But, few files have these sectors $100 long.
 */

public class ATR extends DiskImage {
    private static final int HEADER_SIZE = 16;

    private static final class SectorCoordinates {
        int offset;
        int size;
    }

    private int totalParagraphs;
    private int sectorSize;
    private boolean writeProtected;
    private int totalSectors;
    private int lastReadSector;

    public ATR(String path) throws Exception {
        super(path);

        // Check magic
        byte[] header = Arrays.copyOfRange(image, 0, HEADER_SIZE);

        //file.read(header, 0, 16);
        if (header[0] != (byte) 0x96 || header[1] != (byte) 0x02) {
            throw new Exception("Invalid ATR magic");
        }
        totalParagraphs = (header[2] & 0xff) | ((header[3] & 0xff) << 8) | ((header[6] & 0xff) << 16);
        sectorSize = (header[4] & 0xff) | ((header[5] & 0xff) << 8);
        writeProtected = (header[15] & 1) == 1;
        if (sectorSize == 128 || sectorSize == 512) {
            totalSectors = 16*totalParagraphs / sectorSize;
        } else if (sectorSize == 256) {
            if ((totalParagraphs & 0x0f) == 0x00) {
                totalSectors = 16*totalParagraphs / sectorSize;
            } else if ((totalParagraphs & 0x0f) == 0x08) {
                totalSectors = 16*(totalParagraphs + 24) / sectorSize;
            }
        }
    }

    private SectorCoordinates sectorCoordinates(int sectorNumber) {
        SectorCoordinates sc = new SectorCoordinates();
        if (sectorSize == 128 || sectorSize == 512) {
            sc.offset = HEADER_SIZE + (sectorNumber - 1)*sectorSize;
            sc.size = sectorSize;
        } else if (sectorSize == 256) {
            if ((totalParagraphs & 0x0f) == 0x00) {
                if (sectorNumber < 4) {
                    sc.offset = HEADER_SIZE + (sectorNumber - 1) * 128;
                    sc.size = 128;
                } else {
                    sc.offset = HEADER_SIZE + 768 + (sectorNumber - 4) * 256;
                    sc.size = 256;
                }
            } else if ((totalParagraphs & 0x0f) == 0x08) {
                if (sectorNumber < 4) {
                    sc.offset = HEADER_SIZE + (sectorNumber - 1) * 128;
                    sc.size = 128;
                } else {
                    sc.offset = HEADER_SIZE + 384 + (sectorNumber - 4) * 256;
                    sc.size = 256;
                }
            }
        }
        return sc;
    }

    public boolean isWriteProtected() {
        return writeProtected;
    }

    public int getSectorSize() {
        return sectorSize;
    }

    public int getSectorSize(int sectorNumber) {
        return sectorCoordinates(sectorNumber).size;
    }

    public byte[] getSector(int sectorNumber) throws SectorNotFoundException {
        if (sectorNumber <= totalSectors) {
            lastReadSector = sectorNumber;
            SectorCoordinates sc = sectorCoordinates(sectorNumber);
            return Arrays.copyOfRange(image, sc.offset, sc.offset + sc.size);
        } else {
            throw new SectorNotFoundException("sector not found: " + sectorNumber);
        }
    }

    public void putSector(int sectorNumber, byte[] data, int offset) throws SectorNotFoundException, IOException {
        if (sectorNumber <= totalSectors) {
            SectorCoordinates sc = sectorCoordinates(sectorNumber);
            for (int i = 0; i < sc.size; i++) {
                image[sc.offset + i] = data[offset + i];
            }
            update(sc.offset, sc.size);
        } else {
            throw new SectorNotFoundException("sector not found: " + sectorNumber);
        }
    }

    public int getReadProgress() {
        return 100 * lastReadSector / totalSectors;
    }

    @Override
    public String toString() {
        return "#pars=$" + String.format("%x", totalParagraphs) + ";sec_size=" + sectorSize + ";wprot=" + writeProtected + ";#sectors=" + totalSectors;
    }
}
