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

    public void format() throws IOException {
        throw new IOException("formatting not supported");
    }
}
