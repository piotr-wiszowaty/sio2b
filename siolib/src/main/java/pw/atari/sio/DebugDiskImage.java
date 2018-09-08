package pw.atari.sio;

import java.io.IOException;

public class DebugDiskImage extends DiskImage {
    private static final int SECTOR_SIZE = 8;

    public DebugDiskImage(String path) throws Exception {
        super(path);
    }

    public boolean isWriteProtected() {
        return false;
    }

    public int getSectorSize() {
        return SECTOR_SIZE;
    }

    public int getSectorSize(int sectorNumber) {
        return SECTOR_SIZE;
    }

    public byte[] getSector(int sectorNumber) throws SectorNotFoundException {
        throw new SectorNotFoundException("sector not found: " + sectorNumber);
    }

    public int getReadProgress() {
        return 0;
    }

    @Override
    public void putSector(int sectorNumber, byte[] data, int offset) throws SectorNotFoundException, IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SECTOR_SIZE; i++) {
            sb.append(String.format("%02X ", data[offset + i]));
        }
        System.out.println(String.format("%02X : %s", sectorNumber, sb.toString()));
    }
}
