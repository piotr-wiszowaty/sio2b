//
// Copyright (C) 2018-2021 Piotr Wiszowaty
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

public class XEX extends DiskImage {
    private static final int SECTOR_SIZE = 128;
    private static final int BOOT_PATH_OFFSET = 0x14;
    private static final int BOOT_PATH_LENGTH = 30;

    private static byte[] bootloaderNormalSpeed = new byte[] {
        /* #bootloader_start# */
        (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x4c, (byte) 0x82, 
        (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x4c, (byte) 0x6f, (byte) 0x61, (byte) 0x64, 
        (byte) 0x69, (byte) 0x6e, (byte) 0x67, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x9b, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x60, 
        (byte) 0xa2, (byte) 0x00, (byte) 0x9d, (byte) 0x44, (byte) 0x03, (byte) 0x98, (byte) 0x9d, (byte) 0x45, 
        (byte) 0x03, (byte) 0xa9, (byte) 0xff, (byte) 0x9d, (byte) 0x48, (byte) 0x03, (byte) 0xa9, (byte) 0x09, 
        (byte) 0x9d, (byte) 0x42, (byte) 0x03, (byte) 0x4c, (byte) 0x56, (byte) 0xe4, (byte) 0xad, (byte) 0x08, 
        (byte) 0x03, (byte) 0x8d, (byte) 0x0a, (byte) 0x03, (byte) 0xad, (byte) 0x09, (byte) 0x03, (byte) 0x8d, 
        (byte) 0x0b, (byte) 0x03, (byte) 0xa9, (byte) 0x31, (byte) 0x8d, (byte) 0x00, (byte) 0x03, (byte) 0xa9, 
        (byte) 0x01, (byte) 0x8d, (byte) 0x01, (byte) 0x03, (byte) 0xa9, (byte) 0x40, (byte) 0x8d, (byte) 0x03, 
        (byte) 0x03, (byte) 0xa9, (byte) 0x08, (byte) 0x8d, (byte) 0x06, (byte) 0x03, (byte) 0x20, (byte) 0x59, 
        (byte) 0xe4, (byte) 0x60, (byte) 0xa9, (byte) 0x80, (byte) 0x8d, (byte) 0x04, (byte) 0x03, (byte) 0xa9, 
        (byte) 0x08, (byte) 0x8d, (byte) 0x05, (byte) 0x03, (byte) 0xa9, (byte) 0x80, (byte) 0xad, (byte) 0x08, 
        (byte) 0x03, (byte) 0xa9, (byte) 0x00, (byte) 0x8d, (byte) 0x09, (byte) 0x03, (byte) 0xa9, (byte) 0x04, 
        (byte) 0x8d, (byte) 0x0a, (byte) 0x03, (byte) 0xa9, (byte) 0x00, (byte) 0x8d, (byte) 0x0b, (byte) 0x03, 
        (byte) 0xa9, (byte) 0x31, (byte) 0x8d, (byte) 0x00, (byte) 0x03, (byte) 0xa9, (byte) 0x01, (byte) 0x8d, 
        (byte) 0x01, (byte) 0x03, (byte) 0xa9, (byte) 0x40, (byte) 0x8d, (byte) 0x03, (byte) 0x03, (byte) 0xa9, 
        (byte) 0x08, (byte) 0x8d, (byte) 0x06, (byte) 0x03, (byte) 0xa9, (byte) 0x52, (byte) 0x8d, (byte) 0x02, 
        (byte) 0x03, (byte) 0x20, (byte) 0x59, (byte) 0xe4, (byte) 0xa9, (byte) 0x14, (byte) 0xa0, (byte) 0x07, 
        (byte) 0x20, (byte) 0x48, (byte) 0x07, (byte) 0xa9, (byte) 0x00, (byte) 0x8d, (byte) 0xe0, (byte) 0x02, 
        (byte) 0x8d, (byte) 0xe1, (byte) 0x02, (byte) 0xa9, (byte) 0xf9, (byte) 0x8d, (byte) 0x02, (byte) 0x03, 
        (byte) 0xa9, (byte) 0x3f, (byte) 0x8d, (byte) 0x04, (byte) 0x03, (byte) 0xa9, (byte) 0x07, (byte) 0x8d, 
        (byte) 0x05, (byte) 0x03, (byte) 0xa9, (byte) 0x04, (byte) 0x8d, (byte) 0x08, (byte) 0x03, (byte) 0xa9, 
        (byte) 0x00, (byte) 0x8d, (byte) 0x09, (byte) 0x03, (byte) 0x20, (byte) 0x5e, (byte) 0x07, (byte) 0x10, 
        (byte) 0x05, (byte) 0xa9, (byte) 0xf8, (byte) 0x4c, (byte) 0xcd, (byte) 0x07, (byte) 0xa9, (byte) 0xff, 
        (byte) 0xcd, (byte) 0x3f, (byte) 0x07, (byte) 0xd0, (byte) 0x1d, (byte) 0xcd, (byte) 0x40, (byte) 0x07, 
        (byte) 0xd0, (byte) 0x18, (byte) 0xcd, (byte) 0x41, (byte) 0x07, (byte) 0xd0, (byte) 0x13, (byte) 0xcd, 
        (byte) 0x42, (byte) 0x07, (byte) 0xd0, (byte) 0x0e, (byte) 0xad, (byte) 0xe0, (byte) 0x02, (byte) 0x0d, 
        (byte) 0xe1, (byte) 0x02, (byte) 0xd0, (byte) 0x03, (byte) 0x6c, (byte) 0x45, (byte) 0x07, (byte) 0x6c, 
        (byte) 0xe0, (byte) 0x02, (byte) 0xa9, (byte) 0xff, (byte) 0xcd, (byte) 0x3f, (byte) 0x07, (byte) 0xd0, 
        (byte) 0x34, (byte) 0xcd, (byte) 0x40, (byte) 0x07, (byte) 0xd0, (byte) 0x2f, (byte) 0xad, (byte) 0x41, 
        (byte) 0x07, (byte) 0x8d, (byte) 0x3f, (byte) 0x07, (byte) 0xad, (byte) 0x42, (byte) 0x07, (byte) 0x8d, 
        (byte) 0x40, (byte) 0x07, (byte) 0xa9, (byte) 0xf9, (byte) 0x8d, (byte) 0x02, (byte) 0x03, (byte) 0xa9, 
        (byte) 0x41, (byte) 0x8d, (byte) 0x04, (byte) 0x03, (byte) 0xa9, (byte) 0x07, (byte) 0x8d, (byte) 0x05, 
        (byte) 0x03, (byte) 0xa9, (byte) 0x02, (byte) 0x8d, (byte) 0x08, (byte) 0x03, (byte) 0xa9, (byte) 0x00, 
        (byte) 0x8d, (byte) 0x09, (byte) 0x03, (byte) 0x20, (byte) 0x5e, (byte) 0x07, (byte) 0x10, (byte) 0x05, 
        (byte) 0xa9, (byte) 0xf8, (byte) 0x4c, (byte) 0x2c, (byte) 0x08, (byte) 0xad, (byte) 0x45, (byte) 0x07, 
        (byte) 0x0d, (byte) 0x46, (byte) 0x07, (byte) 0xd0, (byte) 0x0c, (byte) 0xad, (byte) 0x3f, (byte) 0x07, 
        (byte) 0x8d, (byte) 0x45, (byte) 0x07, (byte) 0xad, (byte) 0x40, (byte) 0x07, (byte) 0x8d, (byte) 0x46, 
        (byte) 0x07, (byte) 0xa9, (byte) 0x47, (byte) 0x8d, (byte) 0xe2, (byte) 0x02, (byte) 0xa9, (byte) 0x07, 
        (byte) 0x8d, (byte) 0xe3, (byte) 0x02, (byte) 0x38, (byte) 0xad, (byte) 0x41, (byte) 0x07, (byte) 0xed, 
        (byte) 0x3f, (byte) 0x07, (byte) 0xad, (byte) 0x42, (byte) 0x07, (byte) 0xed, (byte) 0x40, (byte) 0x07, 
        (byte) 0xb0, (byte) 0x09, (byte) 0x20, (byte) 0x80, (byte) 0x08, (byte) 0x4c, (byte) 0xcb, (byte) 0x07, 
        (byte) 0x6c, (byte) 0xe2, (byte) 0x02, (byte) 0x38, (byte) 0xad, (byte) 0x41, (byte) 0x07, (byte) 0xed, 
        (byte) 0x3f, (byte) 0x07, (byte) 0x8d, (byte) 0x43, (byte) 0x07, (byte) 0xad, (byte) 0x42, (byte) 0x07, 
        (byte) 0xed, (byte) 0x40, (byte) 0x07, (byte) 0x8d, (byte) 0x44, (byte) 0x07, (byte) 0x18, (byte) 0xa9, 
        (byte) 0x01, (byte) 0x6d, (byte) 0x43, (byte) 0x07, (byte) 0x8d, (byte) 0x43, (byte) 0x07, (byte) 0xa9, 
        (byte) 0x00, (byte) 0x6d, (byte) 0x44, (byte) 0x07, (byte) 0x8d, (byte) 0x44, (byte) 0x07, (byte) 0x38, 
        (byte) 0xa9, (byte) 0x00, (byte) 0xed, (byte) 0x43, (byte) 0x07, (byte) 0xa9, (byte) 0x02, (byte) 0xed, 
        (byte) 0x44, (byte) 0x07, (byte) 0xb0, (byte) 0x0a, (byte) 0xa9, (byte) 0x00, (byte) 0x8d, (byte) 0x43, 
        (byte) 0x07, (byte) 0xa9, (byte) 0x02, (byte) 0x8d, (byte) 0x44, (byte) 0x07, (byte) 0xa9, (byte) 0xf9, 
        (byte) 0x8d, (byte) 0x02, (byte) 0x03, (byte) 0xad, (byte) 0x3f, (byte) 0x07, (byte) 0x8d, (byte) 0x04, 
        (byte) 0x03, (byte) 0xad, (byte) 0x40, (byte) 0x07, (byte) 0x8d, (byte) 0x05, (byte) 0x03, (byte) 0xad, 
        (byte) 0x43, (byte) 0x07, (byte) 0x8d, (byte) 0x08, (byte) 0x03, (byte) 0xad, (byte) 0x44, (byte) 0x07, 
        (byte) 0x8d, (byte) 0x09, (byte) 0x03, (byte) 0x20, (byte) 0x5e, (byte) 0x07, (byte) 0x10, (byte) 0x05, 
        (byte) 0xa9, (byte) 0xf8, (byte) 0x4c, (byte) 0xc0, (byte) 0x08, (byte) 0x18, (byte) 0xad, (byte) 0x43, 
        (byte) 0x07, (byte) 0x6d, (byte) 0x3f, (byte) 0x07, (byte) 0x8d, (byte) 0x3f, (byte) 0x07, (byte) 0xad, 
        (byte) 0x44, (byte) 0x07, (byte) 0x6d, (byte) 0x40, (byte) 0x07, (byte) 0x8d, (byte) 0x40, (byte) 0x07, 
        (byte) 0x4c, (byte) 0x6b, (byte) 0x08, 
        /* #bootloader_end# */
    };

    private static byte[] bootloaderHighSpeed = new byte[] {
        /* #bootloader_hs_start# */
        (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x4c, (byte) 0x5d, 
        (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x4c, (byte) 0x6f, (byte) 0x61, (byte) 0x64, 
        (byte) 0x69, (byte) 0x6e, (byte) 0x67, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x9b, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0a, (byte) 0x00, (byte) 0x00, (byte) 0x60, (byte) 0xa2, 
        (byte) 0x00, (byte) 0x9d, (byte) 0x44, (byte) 0x03, (byte) 0x98, (byte) 0x9d, (byte) 0x45, (byte) 0x03, 
        (byte) 0xa9, (byte) 0xff, (byte) 0x9d, (byte) 0x48, (byte) 0x03, (byte) 0xa9, (byte) 0x09, (byte) 0x9d, 
        (byte) 0x42, (byte) 0x03, (byte) 0x4c, (byte) 0x56, (byte) 0xe4, (byte) 0xa9, (byte) 0x31, (byte) 0x8d, 
        (byte) 0x00, (byte) 0x03, (byte) 0xa9, (byte) 0x01, (byte) 0x8d, (byte) 0x01, (byte) 0x03, (byte) 0xa9, 
        (byte) 0x08, (byte) 0x8d, (byte) 0x06, (byte) 0x03, (byte) 0xa9, (byte) 0x80, (byte) 0x8d, (byte) 0x04, 
        (byte) 0x03, (byte) 0xa9, (byte) 0x08, (byte) 0x8d, (byte) 0x05, (byte) 0x03, (byte) 0xa9, (byte) 0x80, 
        (byte) 0xad, (byte) 0x08, (byte) 0x03, (byte) 0xa9, (byte) 0x00, (byte) 0x8d, (byte) 0x09, (byte) 0x03, 
        (byte) 0xa9, (byte) 0x04, (byte) 0x8d, (byte) 0x0a, (byte) 0x03, (byte) 0xa9, (byte) 0x00, (byte) 0x8d, 
        (byte) 0x0b, (byte) 0x03, (byte) 0xa9, (byte) 0x52, (byte) 0x8d, (byte) 0x02, (byte) 0x03, (byte) 0xa9, 
        (byte) 0x40, (byte) 0x8d, (byte) 0x03, (byte) 0x03, (byte) 0x20, (byte) 0x59, (byte) 0xe4, (byte) 0x30, 
        (byte) 0xf6, (byte) 0x18, (byte) 0xad, (byte) 0x04, (byte) 0x03, (byte) 0x69, (byte) 0x80, (byte) 0x8d, 
        (byte) 0x04, (byte) 0x03, (byte) 0xad, (byte) 0x05, (byte) 0x03, (byte) 0x69, (byte) 0x00, (byte) 0x8d, 
        (byte) 0x05, (byte) 0x03, (byte) 0xee, (byte) 0x0a, (byte) 0x03, (byte) 0xad, (byte) 0x0a, (byte) 0x03, 
        (byte) 0xc9, (byte) 0x09, (byte) 0xd0, (byte) 0xdb, (byte) 0xa9, (byte) 0x14, (byte) 0xa0, (byte) 0x07, 
        (byte) 0x20, (byte) 0x47, (byte) 0x07, (byte) 0xa9, (byte) 0x40, (byte) 0x8d, (byte) 0x03, (byte) 0x03, 
        (byte) 0xa9, (byte) 0x43, (byte) 0x8d, (byte) 0x04, (byte) 0x03, (byte) 0xa9, (byte) 0x07, (byte) 0x8d, 
        (byte) 0x05, (byte) 0x03, (byte) 0xa9, (byte) 0x01, (byte) 0x8d, (byte) 0x08, (byte) 0x03, (byte) 0xa9, 
        (byte) 0x00, (byte) 0x8d, (byte) 0x09, (byte) 0x03, (byte) 0xa9, (byte) 0x3f, (byte) 0x8d, (byte) 0x02, 
        (byte) 0x03, (byte) 0x20, (byte) 0x59, (byte) 0xe4, (byte) 0x20, (byte) 0x2f, (byte) 0x09, (byte) 0x20, 
        (byte) 0x45, (byte) 0x09, (byte) 0xa9, (byte) 0x00, (byte) 0x8d, (byte) 0xe0, (byte) 0x02, (byte) 0x8d, 
        (byte) 0xe1, (byte) 0x02, (byte) 0xa9, (byte) 0xf9, (byte) 0x8d, (byte) 0x02, (byte) 0x03, (byte) 0xa9, 
        (byte) 0x3d, (byte) 0x8d, (byte) 0x04, (byte) 0x03, (byte) 0xa9, (byte) 0x07, (byte) 0x8d, (byte) 0x05, 
        (byte) 0x03, (byte) 0xa9, (byte) 0x04, (byte) 0x8d, (byte) 0x08, (byte) 0x03, (byte) 0xa9, (byte) 0x00, 
        (byte) 0x8d, (byte) 0x09, (byte) 0x03, (byte) 0x20, (byte) 0x6b, (byte) 0x09, (byte) 0x10, (byte) 0x05, 
        (byte) 0xa9, (byte) 0xf8, (byte) 0x4c, (byte) 0xec, (byte) 0x07, (byte) 0xa9, (byte) 0xff, (byte) 0xcd, 
        (byte) 0x3d, (byte) 0x07, (byte) 0xd0, (byte) 0x29, (byte) 0xcd, (byte) 0x3e, (byte) 0x07, (byte) 0xd0, 
        (byte) 0x24, (byte) 0xcd, (byte) 0x3f, (byte) 0x07, (byte) 0xd0, (byte) 0x1f, (byte) 0xcd, (byte) 0x40, 
        (byte) 0x07, (byte) 0xd0, (byte) 0x1a, (byte) 0xad, (byte) 0xe0, (byte) 0x02, (byte) 0x0d, (byte) 0xe1, 
        (byte) 0x02, (byte) 0xd0, (byte) 0x0c, (byte) 0xad, (byte) 0x44, (byte) 0x07, (byte) 0x8d, (byte) 0xe0, 
        (byte) 0x02, (byte) 0xad, (byte) 0x45, (byte) 0x07, (byte) 0x8d, (byte) 0xe1, (byte) 0x02, (byte) 0x20, 
        (byte) 0x36, (byte) 0x09, (byte) 0x6c, (byte) 0xe0, (byte) 0x02, (byte) 0xa9, (byte) 0xff, (byte) 0xcd, 
        (byte) 0x3d, (byte) 0x07, (byte) 0xd0, (byte) 0x34, (byte) 0xcd, (byte) 0x3e, (byte) 0x07, (byte) 0xd0, 
        (byte) 0x2f, (byte) 0xad, (byte) 0x3f, (byte) 0x07, (byte) 0x8d, (byte) 0x3d, (byte) 0x07, (byte) 0xad, 
        (byte) 0x40, (byte) 0x07, (byte) 0x8d, (byte) 0x3e, (byte) 0x07, (byte) 0xa9, (byte) 0xf9, (byte) 0x8d, 
        (byte) 0x02, (byte) 0x03, (byte) 0xa9, (byte) 0x3f, (byte) 0x8d, (byte) 0x04, (byte) 0x03, (byte) 0xa9, 
        (byte) 0x07, (byte) 0x8d, (byte) 0x05, (byte) 0x03, (byte) 0xa9, (byte) 0x02, (byte) 0x8d, (byte) 0x08, 
        (byte) 0x03, (byte) 0xa9, (byte) 0x00, (byte) 0x8d, (byte) 0x09, (byte) 0x03, (byte) 0x20, (byte) 0x6b, 
        (byte) 0x09, (byte) 0x10, (byte) 0x05, (byte) 0xa9, (byte) 0xf8, (byte) 0x4c, (byte) 0x57, (byte) 0x08, 
        (byte) 0xad, (byte) 0x44, (byte) 0x07, (byte) 0x0d, (byte) 0x45, (byte) 0x07, (byte) 0xd0, (byte) 0x0c, 
        (byte) 0xad, (byte) 0x3d, (byte) 0x07, (byte) 0x8d, (byte) 0x44, (byte) 0x07, (byte) 0xad, (byte) 0x3e, 
        (byte) 0x07, (byte) 0x8d, (byte) 0x45, (byte) 0x07, (byte) 0xa9, (byte) 0x46, (byte) 0x8d, (byte) 0xe2, 
        (byte) 0x02, (byte) 0xa9, (byte) 0x07, (byte) 0x8d, (byte) 0xe3, (byte) 0x02, (byte) 0x38, (byte) 0xad, 
        (byte) 0x3f, (byte) 0x07, (byte) 0xed, (byte) 0x3d, (byte) 0x07, (byte) 0xad, (byte) 0x40, (byte) 0x07, 
        (byte) 0xed, (byte) 0x3e, (byte) 0x07, (byte) 0xb0, (byte) 0x12, (byte) 0x20, (byte) 0x36, (byte) 0x09, 
        (byte) 0x20, (byte) 0xb4, (byte) 0x08, (byte) 0x20, (byte) 0x2f, (byte) 0x09, (byte) 0x20, (byte) 0x45, 
        (byte) 0x09, (byte) 0x4c, (byte) 0xea, (byte) 0x07, (byte) 0x6c, (byte) 0xe2, (byte) 0x02, (byte) 0x38, 
        (byte) 0xad, (byte) 0x3f, (byte) 0x07, (byte) 0xed, (byte) 0x3d, (byte) 0x07, (byte) 0x8d, (byte) 0x41, 
        (byte) 0x07, (byte) 0xad, (byte) 0x40, (byte) 0x07, (byte) 0xed, (byte) 0x3e, (byte) 0x07, (byte) 0x8d, 
        (byte) 0x42, (byte) 0x07, (byte) 0x18, (byte) 0xa9, (byte) 0x01, (byte) 0x6d, (byte) 0x41, (byte) 0x07, 
        (byte) 0x8d, (byte) 0x41, (byte) 0x07, (byte) 0xa9, (byte) 0x00, (byte) 0x6d, (byte) 0x42, (byte) 0x07, 
        (byte) 0x8d, (byte) 0x42, (byte) 0x07, (byte) 0x38, (byte) 0xa9, (byte) 0x00, (byte) 0xed, (byte) 0x41, 
        (byte) 0x07, (byte) 0xa9, (byte) 0x02, (byte) 0xed, (byte) 0x42, (byte) 0x07, (byte) 0xb0, (byte) 0x0a, 
        (byte) 0xa9, (byte) 0x00, (byte) 0x8d, (byte) 0x41, (byte) 0x07, (byte) 0xa9, (byte) 0x02, (byte) 0x8d, 
        (byte) 0x42, (byte) 0x07, (byte) 0xa9, (byte) 0xf9, (byte) 0x8d, (byte) 0x02, (byte) 0x03, (byte) 0xad, 
        (byte) 0x3d, (byte) 0x07, (byte) 0x8d, (byte) 0x04, (byte) 0x03, (byte) 0xad, (byte) 0x3e, (byte) 0x07, 
        (byte) 0x8d, (byte) 0x05, (byte) 0x03, (byte) 0xad, (byte) 0x41, (byte) 0x07, (byte) 0x8d, (byte) 0x08, 
        (byte) 0x03, (byte) 0xad, (byte) 0x42, (byte) 0x07, (byte) 0x8d, (byte) 0x09, (byte) 0x03, (byte) 0x20, 
        (byte) 0x6b, (byte) 0x09, (byte) 0x10, (byte) 0x05, (byte) 0xa9, (byte) 0xf8, (byte) 0x4c, (byte) 0xf4, 
        (byte) 0x08, (byte) 0x18, (byte) 0xad, (byte) 0x41, (byte) 0x07, (byte) 0x6d, (byte) 0x3d, (byte) 0x07, 
        (byte) 0x8d, (byte) 0x3d, (byte) 0x07, (byte) 0xad, (byte) 0x42, (byte) 0x07, (byte) 0x6d, (byte) 0x3e, 
        (byte) 0x07, (byte) 0x8d, (byte) 0x3e, (byte) 0x07, (byte) 0x4c, (byte) 0x96, (byte) 0x08, (byte) 0x78, 
        (byte) 0xa9, (byte) 0x00, (byte) 0x8d, (byte) 0x0e, (byte) 0xd4, (byte) 0x60, (byte) 0xa5, (byte) 0x10, 
        (byte) 0x8d, (byte) 0x0e, (byte) 0xd2, (byte) 0xa9, (byte) 0x40, (byte) 0x8d, (byte) 0x0e, (byte) 0xd4, 
        (byte) 0x58, (byte) 0x8d, (byte) 0x0a, (byte) 0xd4, (byte) 0x60, (byte) 0xa9, (byte) 0x00, (byte) 0x8d, 
        (byte) 0x0e, (byte) 0xd2, (byte) 0xad, (byte) 0x43, (byte) 0x07, (byte) 0x8d, (byte) 0x04, (byte) 0xd2, 
        (byte) 0xa9, (byte) 0x00, (byte) 0x8d, (byte) 0x06, (byte) 0xd2, (byte) 0xa9, (byte) 0x28, (byte) 0x8d, 
        (byte) 0x08, (byte) 0xd2, (byte) 0xa9, (byte) 0xa0, (byte) 0x8d, (byte) 0x01, (byte) 0xd2, (byte) 0x8d, 
        (byte) 0x03, (byte) 0xd2, (byte) 0x8d, (byte) 0x05, (byte) 0xd2, (byte) 0xa9, (byte) 0xa8, (byte) 0x8d, 
        (byte) 0x07, (byte) 0xd2, (byte) 0x60, (byte) 0xad, (byte) 0x08, (byte) 0x03, (byte) 0x8d, (byte) 0x0a, 
        (byte) 0x03, (byte) 0xad, (byte) 0x09, (byte) 0x03, (byte) 0x8d, (byte) 0x0b, (byte) 0x03, (byte) 0xa9, 
        (byte) 0x40, (byte) 0x8d, (byte) 0x03, (byte) 0x03, (byte) 0xa9, (byte) 0x00, (byte) 0x8d, (byte) 0x0e, 
        (byte) 0xd2, (byte) 0xa9, (byte) 0x34, (byte) 0x8d, (byte) 0x03, (byte) 0xd3, (byte) 0xa2, (byte) 0x50, 
        (byte) 0xca, (byte) 0xd0, (byte) 0xfd, (byte) 0xa9, (byte) 0x00, (byte) 0x85, (byte) 0x31, (byte) 0xa9, 
        (byte) 0x23, (byte) 0x8d, (byte) 0x0f, (byte) 0xd2, (byte) 0xad, (byte) 0x00, (byte) 0x03, (byte) 0x20, 
        (byte) 0x40, (byte) 0x0a, (byte) 0xad, (byte) 0x02, (byte) 0x03, (byte) 0x20, (byte) 0x40, (byte) 0x0a, 
        (byte) 0xad, (byte) 0x0a, (byte) 0x03, (byte) 0x20, (byte) 0x40, (byte) 0x0a, (byte) 0xad, (byte) 0x0b, 
        (byte) 0x03, (byte) 0x20, (byte) 0x40, (byte) 0x0a, (byte) 0xa5, (byte) 0x31, (byte) 0x20, (byte) 0x40, 
        (byte) 0x0a, (byte) 0xa9, (byte) 0x3c, (byte) 0x8d, (byte) 0x03, (byte) 0xd3, (byte) 0xa9, (byte) 0x13, 
        (byte) 0x8d, (byte) 0x0f, (byte) 0xd2, (byte) 0xa9, (byte) 0x20, (byte) 0x8d, (byte) 0x0e, (byte) 0xd2, 
        (byte) 0xa9, (byte) 0x00, (byte) 0x85, (byte) 0x30, (byte) 0x20, (byte) 0x67, (byte) 0x0a, (byte) 0x30, 
        (byte) 0x6f, (byte) 0xc9, (byte) 0x41, (byte) 0xf0, (byte) 0x07, (byte) 0xa9, (byte) 0x80, (byte) 0x85, 
        (byte) 0x30, (byte) 0x4c, (byte) 0x38, (byte) 0x0a, (byte) 0x20, (byte) 0x67, (byte) 0x0a, (byte) 0x30, 
        (byte) 0x5f, (byte) 0xc9, (byte) 0x43, (byte) 0xf0, (byte) 0x07, (byte) 0xa9, (byte) 0x80, (byte) 0x85, 
        (byte) 0x30, (byte) 0x4c, (byte) 0x38, (byte) 0x0a, (byte) 0x2c, (byte) 0x03, (byte) 0x03, (byte) 0x50, 
        (byte) 0x4f, (byte) 0xad, (byte) 0x04, (byte) 0x03, (byte) 0x8d, (byte) 0x12, (byte) 0x0a, (byte) 0xad, 
        (byte) 0x05, (byte) 0x03, (byte) 0x8d, (byte) 0x13, (byte) 0x0a, (byte) 0x18, (byte) 0xad, (byte) 0x08, 
        (byte) 0x03, (byte) 0x49, (byte) 0xff, (byte) 0x69, (byte) 0x01, (byte) 0x85, (byte) 0x32, (byte) 0xad, 
        (byte) 0x09, (byte) 0x03, (byte) 0x49, (byte) 0xff, (byte) 0x69, (byte) 0x00, (byte) 0x85, (byte) 0x33, 
        (byte) 0xa9, (byte) 0x00, (byte) 0x85, (byte) 0x31, (byte) 0x20, (byte) 0x67, (byte) 0x0a, (byte) 0x30, 
        (byte) 0x27, (byte) 0x8d, (byte) 0x00, (byte) 0x10, (byte) 0x18, (byte) 0x65, (byte) 0x31, (byte) 0x69, 
        (byte) 0x00, (byte) 0x85, (byte) 0x31, (byte) 0xee, (byte) 0x12, (byte) 0x0a, (byte) 0xd0, (byte) 0x03, 
        (byte) 0xee, (byte) 0x13, (byte) 0x0a, (byte) 0xe6, (byte) 0x32, (byte) 0xd0, (byte) 0xe5, (byte) 0xe6, 
        (byte) 0x33, (byte) 0xd0, (byte) 0xe1, (byte) 0x20, (byte) 0x67, (byte) 0x0a, (byte) 0x30, (byte) 0x08, 
        (byte) 0xc5, (byte) 0x31, (byte) 0xf0, (byte) 0x04, (byte) 0xa9, (byte) 0x80, (byte) 0x85, (byte) 0x30, 
        (byte) 0xa9, (byte) 0x03, (byte) 0x8d, (byte) 0x0f, (byte) 0xd2, (byte) 0x24, (byte) 0x30, (byte) 0x60, 
        (byte) 0x8d, (byte) 0x0a, (byte) 0xd2, (byte) 0x8d, (byte) 0x0d, (byte) 0xd2, (byte) 0x18, (byte) 0x65, 
        (byte) 0x31, (byte) 0x69, (byte) 0x00, (byte) 0x85, (byte) 0x31, (byte) 0xa2, (byte) 0x14, (byte) 0xca, 
        (byte) 0xd0, (byte) 0xfd, (byte) 0xa9, (byte) 0x08, (byte) 0x8d, (byte) 0x0e, (byte) 0xd2, (byte) 0x2c, 
        (byte) 0x0e, (byte) 0xd2, (byte) 0xd0, (byte) 0xfb, (byte) 0xa9, (byte) 0x00, (byte) 0x8d, (byte) 0x0e, 
        (byte) 0xd2, (byte) 0xa2, (byte) 0x0a, (byte) 0xca, (byte) 0xd0, (byte) 0xfd, (byte) 0x60, (byte) 0xa2, 
        (byte) 0x00, (byte) 0xa0, (byte) 0x00, (byte) 0xa9, (byte) 0x20, (byte) 0xe8, (byte) 0xd0, (byte) 0x06, 
        (byte) 0xc8, (byte) 0xd0, (byte) 0x03, (byte) 0x4c, (byte) 0x8b, (byte) 0x0a, (byte) 0x2c, (byte) 0x0e, 
        (byte) 0xd2, (byte) 0xd0, (byte) 0xf2, (byte) 0xa9, (byte) 0x00, (byte) 0x8d, (byte) 0x0e, (byte) 0xd2, 
        (byte) 0xa9, (byte) 0x20, (byte) 0x8d, (byte) 0x0e, (byte) 0xd2, (byte) 0xad, (byte) 0x0d, (byte) 0xd2, 
        (byte) 0x24, (byte) 0x30, (byte) 0x60, (byte) 0xa9, (byte) 0x80, (byte) 0x85, (byte) 0x30, (byte) 0x60, 

        /* #bootloader_hs_end# */
    };

    private int offset = 2;
    private int prevChunkLength = 0;
    private boolean highSpeed = false;

    public XEX(String path) throws Exception {
        super(path);

        // Check XEX header
        if ((image[0] & 0xff) != 0xff || (image[1] & 0xff) != 0xff) {
            throw new Exception("Invalid header");
        }

        // Update displayed file name in bootloader
        byte[] bs = (new File(path)).getName().getBytes();
        int i;
        for (i = 0; i < min(bs.length, BOOT_PATH_LENGTH); i++) {
            bootloaderNormalSpeed[BOOT_PATH_OFFSET + i] = bootloaderHighSpeed[BOOT_PATH_OFFSET + i] = bs[i];
        }
        bootloaderNormalSpeed[BOOT_PATH_OFFSET + i] = bootloaderHighSpeed[BOOT_PATH_OFFSET + i] = (byte) 0x9b;
    }

    public boolean isWriteProtected() {
        return true;
    }

    public int getSectorSize() {
        return SECTOR_SIZE;
    }

    public int getSectorSize(int sectorNumber) {
        return SECTOR_SIZE;
    }

    private byte[] selectBootloader() {
        return highSpeed && image.length > 8192 ? bootloaderHighSpeed : bootloaderNormalSpeed;
    }

    public byte[] getSector(int sectorNumber) throws SectorNotFoundException {
        if (sectorNumber <= (selectBootloader().length + SECTOR_SIZE - 1) / SECTOR_SIZE) {
            this.offset = 2;
            this.prevChunkLength = 0;
            int address = SECTOR_SIZE * (sectorNumber - 1);
            byte[] result = new byte[SECTOR_SIZE];
            byte[] data = selectBootloader();
            for (int i = 0; i < SECTOR_SIZE; i++) {
            	result[i] = (address + i < data.length) ? data[address + i] : (byte) 0xff;
            }
            return result;
        } else {
            throw new SectorNotFoundException("sector not found: " + sectorNumber);
        }
    }

    @Override
    public void putSector(int sectorNumber, byte[] data, int offset) throws SectorNotFoundException, IOException {
        throw new IOException("write not supported");
    }

    @Override
    public void format() throws IOException {
        throw new IOException("formatting not supported");
    }

    private int min(int a, int b) {
        return a <= b ? a : b;
    }

    public byte[] getChunk(int length, boolean advance) {
        if (advance) {
            offset += prevChunkLength;
            prevChunkLength = length;
        }

        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (offset+i < image.length) ? image[offset+i] : (byte) 0xff;
        }
        return data;
    }

    @Override
    public String toString() {
        return "len=" + image.length;
    }

    public void setHighSpeed(boolean flag) {
        this.highSpeed = flag;
    }

    public int getReadProgress() {
        return 100 * (offset + prevChunkLength) / image.length;
    }
}
