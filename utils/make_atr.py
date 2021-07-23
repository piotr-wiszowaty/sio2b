#!/usr/bin/env python3

import argparse

parser = argparse.ArgumentParser()
parser.add_argument("-t", "--tracks", type=int, default=40, help="number of tracks")
parser.add_argument("-s", "--sectors-per-track", type=int, default=18, help="number of sectors per track")
parser.add_argument("-S", "--sector-size", type=int, default=128, help="sector size")
parser.add_argument("file", metavar="FILE", help="output file")
args = parser.parse_args()

with open(args.file, "wb") as f:
    total_sectors = args.tracks * args.sectors_per_track
    size = 3*128 + (total_sectors - 3) * args.sector_size
    total_paragraphs = size >> 4
    f.write(bytes([0x96, 0x02,
        total_paragraphs & 0xff, (total_paragraphs >> 8) & 0xff,
        args.sector_size & 0xff, (args.sector_size >> 8) & 0xff,
        (total_paragraphs >> 16) & 0xff,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,
        0x00]))
    f.write(b"\x00" * size)
    print(f"size: {total_paragraphs >> 6} kB")
