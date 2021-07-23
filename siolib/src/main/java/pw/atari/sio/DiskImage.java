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

import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class DiskImage {
    protected String path;
    protected RandomAccessFile file;
    protected byte[] image;

    public abstract boolean isWriteProtected();
    public abstract int getSectorSize();
    public abstract int getSectorSize(int sectorNumber);
    public abstract int getTotalTracks();
    public abstract int getSectorsPerTrack();
    public abstract byte[] getSector(int sectorNumber) throws SectorNotFoundException;
    public abstract void putSector(int sectorNumber, byte[] data, int offset) throws SectorNotFoundException, IOException;
    public abstract int getReadProgress();
    public abstract void format() throws IOException;

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
