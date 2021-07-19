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
import java.io.FileInputStream;
import java.io.FileOutputStream;

class BasicSerialWrapper implements SerialWrapper {
    private FileInputStream serialInput;
    private FileOutputStream serialOutput;

    BasicSerialWrapper(File serialDevice) throws Exception {
        serialInput = new FileInputStream(serialDevice);
        serialOutput = new FileOutputStream(serialDevice);
    }

    public void close() throws Exception {
        if (serialInput != null) {
            serialInput.close();
        }
        if (serialOutput != null) {
            serialOutput.close();
        }
    }

    public int read(byte[] buffer, int offset, int length) throws Exception {
        return serialInput.read(buffer, offset, length);
    }

    public void write(byte[] buffer, int offset, int length) throws Exception {
        serialOutput.write(buffer, offset, length);
    }
}
