package pw.atari.sio;

import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class DiskImage {
    protected String path;
    protected RandomAccessFile file;
    protected byte[] image;

    public abstract boolean isWriteProtected();
    public abstract int getSectorSize();
    public abstract int getSectorSize(int sectorNumber);
    public abstract byte[] getSector(int sectorNumber) throws SectorNotFoundException;
    public abstract void putSector(int sectorNumber, byte[] data, int offset) throws SectorNotFoundException, IOException;
    public abstract int getReadProgress();

    public DiskImage(String path) throws Exception {
        this.path = path;
        this.file = new RandomAccessFile(path, "rw");
        
        this.image = new byte[(int) this.file.length()];
        file.read(this.image, 0, this.image.length);
    }

    public void close() throws IOException {
        file.close();
    }

    public void update(int offset, int size) throws IOException {
        file.seek(offset);
        file.write(image, offset, size);
    }
    
    public String getPath() {
        return path;
    }
}
