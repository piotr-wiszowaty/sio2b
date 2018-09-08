#!/usr/bin/env python

import sys

BOOT_PATH = "bootloader.obx"
BOOT_HS_PATH = "bootloader_hs.obx"
XEX_PATH = sys.argv[1]

data = open(BOOT_PATH, "rb").read()
if len(data) > 512:
    sys.stderr.write("ERROR: bootloader.obx too long (%d)\n" % len(data))
    sys.exit(1)

data_hs = open(BOOT_HS_PATH, "rb").read()
if len(data_hs) > 8*128:
    sys.stderr.write("ERROR: bootloader_hs.obx too long (%d)\n" % len(data_hs))
    sys.exit(1)

lines = open(XEX_PATH, "rt").readlines()

with open(XEX_PATH, "wt") as f:
    copy = True
    for line in lines:
        if "#bootloader_end#" in line or "#bootloader_hs_end#" in line:
            copy = True
        if copy:
            f.write(line)
        if "#bootloader_start#" in line or "#bootloader_hs_start#" in line:
            if "#bootloader_start#" in line:
                src = data
            else:
                src = data_hs
            copy = False
            i = 0
            for x in src:
                if i == 0:
                    f.write(" " * 8)
                f.write("(byte) 0x%02x, " % ord(x))
                i = i + 1
                if i == 8:
                    f.write("\n")
                    i = 0
            f.write("\n")
