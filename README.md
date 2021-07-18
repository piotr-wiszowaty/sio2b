sio2b
=====

An [Atari XL/XE](http://en.wikipedia.org/wiki/Atari_8-bit_family) disk
drive emulator. Uses a PC or an Android phone as a storage device.
Connects to the storage device over Bluetooth.

Features
--------

* handle ATR files (90k and 180k)
* directly load executable files
* emulate up to 4 disk drives (`D1`..`D4`)
* handle high speeds (up to HS\_index=1)

Hardware
--------

Microcontroller: STM32F030C8T6
Bluetooth module: HC-05/HC-06
Enclosure: Z24A (half-transparent) by [Kradex](https://www.kradex.com.pl).

References
----------

The bootloader used for loading executable files is based on one
found in [Atari800](https://atari800.github.io/).
