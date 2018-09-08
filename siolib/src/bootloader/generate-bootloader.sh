#!/bin/sh

cd `dirname $0`
xasm /o:bootloader.obx /l:/tmp/bootloader.lst bootloader.asx || exit 1
xasm /o:bootloader_hs.obx /l:/tmp/bootloader_hs.lst bootloader_hs.asx || exit 1
python dump.py ../main/java/pw/atari/sio/XEX.java
