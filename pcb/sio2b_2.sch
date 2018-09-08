v 20140308 2
C 67700 72900 1 0 1 crystal-1.sym
{
T 67500 73400 5 10 0 0 180 2 1
device=CRYSTAL
T 67400 73400 5 10 1 1 0 6 1
refdes=U2
T 67500 73600 5 10 0 0 180 2 1
symversion=0.1
T 67700 73200 5 10 1 1 0 6 1
value=8 MHz
T 67700 72900 5 10 0 1 90 6 1
footprint=HC49-SMT
}
C 67600 71500 1 270 1 capacitor-1.sym
{
T 68300 71700 5 10 0 0 270 6 1
device=CAPACITOR
T 67900 72200 5 10 1 1 180 6 1
refdes=C1
T 68500 71700 5 10 0 0 270 6 1
symversion=0.1
T 67900 71800 5 10 1 1 180 6 1
value=22 pF
T 67600 71500 5 10 0 1 270 6 1
footprint=0805
}
C 66700 71500 1 270 1 capacitor-1.sym
{
T 67400 71700 5 10 0 0 270 6 1
device=CAPACITOR
T 66800 72100 5 10 1 1 0 6 1
refdes=C2
T 67600 71700 5 10 0 0 270 6 1
symversion=0.1
T 66800 71600 5 10 1 1 0 6 1
value=22 pF
T 66700 71500 5 10 0 1 270 6 1
footprint=0805
}
C 67900 71000 1 0 1 gnd-1.sym
C 67000 71000 1 0 1 gnd-1.sym
N 67800 71300 67800 71500 4
N 66900 71300 66900 71500 4
C 57100 77200 1 0 0 3.3V-plus-1.sym
C 57100 78900 1 0 0 gnd-1.sym
C 58600 80300 1 270 0 capacitor-4.sym
{
T 59700 80100 5 10 0 0 270 0 1
device=POLARIZED_CAPACITOR
T 59300 80100 5 10 0 0 270 0 1
symversion=0.1
T 58600 80300 5 10 0 0 0 0 1
footprint=EIA B
T 59100 79900 5 10 1 1 0 0 1
refdes=C8
T 59100 79700 5 10 1 1 0 0 1
value=10 uF
}
C 55200 80300 1 270 0 capacitor-4.sym
{
T 56300 80100 5 10 0 0 270 0 1
device=POLARIZED_CAPACITOR
T 55900 80100 5 10 0 0 270 0 1
symversion=0.1
T 55200 80300 5 10 0 0 0 0 1
footprint=EIA B
T 55700 79900 5 10 1 1 0 0 1
refdes=C9
T 55700 79700 5 10 1 1 0 0 1
value=1 uF
}
N 57200 79200 57200 79900 4
C 55300 78900 1 0 0 gnd-1.sym
C 58700 78900 1 0 0 gnd-1.sym
N 58800 79200 58800 79400 4
N 55400 80300 55400 80700 4
N 55400 80700 56200 80700 4
N 58200 80500 58800 80500 4
N 58800 80300 58800 80700 4
C 58600 80700 1 0 0 3.3V-plus-1.sym
C 55200 80700 1 0 0 5V-plus-1.sym
C 60100 79200 1 270 0 led-2.sym
{
T 60700 79100 5 10 0 0 270 0 1
device=LED
T 60100 79200 5 10 0 0 0 0 1
footprint=0805
T 60100 79000 5 10 1 1 180 0 1
refdes=D3
}
C 60300 79400 1 90 0 resistor-2.sym
{
T 59950 79800 5 10 0 0 90 0 1
device=RESISTOR
T 60300 79400 5 10 0 1 90 0 1
footprint=0805
T 60400 79900 5 10 1 1 0 0 1
refdes=R4
T 60400 79700 5 10 1 1 0 0 1
value=470
}
C 60100 77800 1 0 0 gnd-1.sym
N 58800 80500 60200 80500 4
C 56200 79900 1 0 0 MCP170xT-1.sym
{
T 56500 81200 5 10 0 0 0 0 1
footprint=SOT89
T 57700 80950 5 10 1 1 0 0 1
refdes=U3
T 56500 80950 5 10 1 1 0 0 1
device=MCP170xT
}
N 56200 80500 55400 80500 4
C 73800 77300 1 90 1 led-2.sym
{
T 73200 77200 5 10 0 0 90 6 1
device=LED
T 73800 77300 5 10 0 0 180 2 1
footprint=0805
T 74000 77000 5 10 1 1 0 6 1
refdes=D2
}
C 73400 77400 1 0 1 resistor-2.sym
{
T 73000 77750 5 10 0 0 0 6 1
device=RESISTOR
T 73400 77400 5 10 0 0 0 6 1
footprint=0805
T 73100 77900 5 10 1 1 0 6 1
refdes=R11
T 73100 77700 5 10 1 1 0 6 1
value=1.5 k
}
C 73700 78700 1 0 0 connector4-2.sym
{
T 74400 80800 5 10 1 1 0 6 1
refdes=CONN4
T 74000 80750 5 10 0 0 0 0 1
device=CONNECTOR_4
T 74000 80950 5 10 0 0 0 0 1
footprint=JUMPER4
}
C 73200 79800 1 270 1 gnd-1.sym
C 73700 80500 1 0 1 3.3V-plus-1.sym
N 73500 80500 73500 80300 4
N 73500 80300 73700 80300 4
N 73500 79900 73700 79900 4
T 74000 76500 9 10 1 0 0 6 1
red
T 60400 78900 9 10 1 0 0 0 1
green
N 55400 79200 55400 79400 4
C 71700 70900 1 0 1 connector6-1.sym
{
T 69900 72700 5 10 0 0 0 6 1
device=CONNECTOR_6
T 71700 72900 5 10 1 1 0 6 1
refdes=CONN3
T 71700 70900 5 10 0 0 0 0 1
footprint=JUMPER6
}
C 69600 72800 1 0 0 5V-plus-1.sym
N 69800 72800 69800 72600 4
N 69800 72600 70000 72600 4
C 69500 72400 1 270 0 gnd-1.sym
N 69800 72300 70000 72300 4
N 69900 72300 69900 72000 4
N 69900 72000 70000 72000 4
T 55300 74500 9 12 1 0 0 0 1
CP-Z-24/TR
C 62900 71300 1 0 0 STM32F030x4_x6_x8-LQFP48-1.sym
{
T 63200 80800 5 10 1 1 0 0 1
device=STM32F030x4_x6_x8
T 65600 80800 5 10 1 1 0 0 1
refdes=U1
T 63200 81000 5 10 0 0 0 0 1
footprint=LQFP48_7
}
C 68400 75200 1 0 0 HC-06.sym
{
T 68700 80800 5 10 1 1 0 0 1
device=HC-06
T 70900 80800 5 10 1 1 0 0 1
refdes=U4
T 68700 81000 5 10 0 0 0 0 1
footprint=HC-06
}
C 61400 75300 1 0 0 resistor-2.sym
{
T 61800 75650 5 10 0 0 0 0 1
device=RESISTOR
T 61400 75300 5 10 0 0 0 0 1
footprint=0805
T 61300 75500 5 10 1 1 0 0 1
refdes=R3
T 62200 75500 5 10 1 1 0 0 1
value=10 k
}
C 60800 75500 1 270 0 gnd-1.sym
N 61100 75400 61400 75400 4
N 62300 75400 62900 75400 4
C 62500 74000 1 90 0 3.3V-plus-1.sym
N 62500 74200 62900 74200 4
C 62500 72800 1 90 0 3.3V-plus-1.sym
N 62500 73000 62900 73000 4
N 62700 72400 62700 73000 4
N 62700 72700 62900 72700 4
N 62700 72400 62900 72400 4
C 62400 71000 1 0 0 gnd-1.sym
N 62500 71300 62500 71800 4
N 62500 71500 62900 71500 4
N 62500 71800 62900 71800 4
C 62200 73700 1 270 0 gnd-1.sym
N 62500 73600 62900 73600 4
N 62300 74800 62900 74800 4
{
T 62600 74855 5 10 1 1 0 3 1
netname=NRST
}
C 58000 76000 1 90 0 capacitor-1.sym
{
T 57300 76200 5 10 0 0 90 0 1
device=CAPACITOR
T 57700 76800 5 10 1 1 180 0 1
refdes=C4
T 57100 76200 5 10 0 0 90 0 1
symversion=0.1
T 57700 76300 5 10 1 1 180 0 1
value=100 nF
T 58000 76000 5 10 0 1 90 0 1
footprint=0805
}
C 59000 76000 1 90 0 capacitor-1.sym
{
T 58300 76200 5 10 0 0 90 0 1
device=CAPACITOR
T 58700 76800 5 10 1 1 180 0 1
refdes=C5
T 58100 76200 5 10 0 0 90 0 1
symversion=0.1
T 58700 76300 5 10 1 1 180 0 1
value=100 nF
T 59000 76000 5 10 0 1 90 0 1
footprint=0805
}
C 57000 76000 1 90 0 capacitor-1.sym
{
T 56300 76200 5 10 0 0 90 0 1
device=CAPACITOR
T 56700 76800 5 10 1 1 180 0 1
refdes=C6
T 56100 76200 5 10 0 0 90 0 1
symversion=0.1
T 56700 76300 5 10 1 1 180 0 1
value=100 nF
T 57000 76000 5 10 0 1 90 0 1
footprint=0805
}
C 56000 76000 1 90 0 capacitor-1.sym
{
T 55300 76200 5 10 0 0 90 0 1
device=CAPACITOR
T 55700 76800 5 10 1 1 180 0 1
refdes=C7
T 55100 76200 5 10 0 0 90 0 1
symversion=0.1
T 55700 76300 5 10 1 1 180 0 1
value=100 nF
T 56000 76000 5 10 0 1 90 0 1
footprint=0805
}
C 57200 75400 1 0 0 gnd-1.sym
N 55800 76000 55800 75800 4
N 55800 75800 58800 75800 4
N 58800 75800 58800 76000 4
N 57800 76000 57800 75800 4
N 56800 76000 56800 75800 4
N 57300 75700 57300 75800 4
N 55800 76900 55800 77100 4
N 55800 77100 58800 77100 4
N 58800 77100 58800 76900 4
N 57800 76900 57800 77100 4
N 56800 76900 56800 77100 4
N 57300 77100 57300 77200 4
C 62300 74600 1 0 1 capacitor-1.sym
{
T 62100 75300 5 10 0 0 0 6 1
device=CAPACITOR
T 62200 74900 5 10 1 1 0 6 1
refdes=C3
T 62100 75500 5 10 0 0 0 6 1
symversion=0.1
T 61700 74900 5 10 1 1 0 6 1
value=100 nF
T 62300 74600 5 10 0 1 0 6 1
footprint=0805
}
C 60800 74900 1 270 0 gnd-1.sym
N 61100 74800 61400 74800 4
C 55400 71600 1 0 0 connector6-1.sym
{
T 57200 73400 5 10 0 0 0 0 1
device=CONNECTOR_6
T 55400 71600 5 10 0 0 0 0 1
footprint=JUMPER6
T 55500 73600 5 10 1 1 0 0 1
refdes=CONN1
}
N 57300 73500 57300 73300 4
N 57300 73300 57100 73300 4
C 57600 72600 1 90 0 gnd-1.sym
N 57300 72700 57100 72700 4
N 57100 72400 58300 72400 4
{
T 57400 72450 5 10 1 1 0 0 1
netname=SWDIO
}
N 57100 73000 58300 73000 4
{
T 57400 73050 5 10 1 1 0 0 1
netname=SWCLK
}
N 57100 72100 58300 72100 4
{
T 57500 72150 5 10 1 1 0 0 1
netname=NRST
}
N 67000 73000 66100 73000 4
N 66900 72400 66900 73000 4
N 67800 72400 67800 73600 4
N 67800 73000 67700 73000 4
N 66100 73600 67800 73600 4
C 57100 73500 1 0 0 3.3V-plus-1.sym
C 68100 77000 1 90 0 3.3V-plus-1.sym
C 67800 77000 1 270 0 gnd-1.sym
N 68100 76900 68400 76900 4
N 68100 77200 68400 77200 4
C 72000 76200 1 90 0 gnd-1.sym
N 71700 76300 71400 76300 4
C 72000 76800 1 90 0 gnd-1.sym
N 71700 76900 71400 76900 4
C 73600 75900 1 0 0 gnd-1.sym
N 72100 78100 71400 78100 4
{
T 71900 78155 5 10 1 1 0 3 1
netname=KEY
}
N 72500 77500 71400 77500 4
{
T 71900 77555 5 10 1 1 0 3 1
netname=LED
}
N 73700 76200 73700 76400 4
N 68400 80500 67400 80500 4
{
T 67900 80555 5 10 1 1 0 3 1
netname=BT_TX
}
N 68400 80200 67400 80200 4
{
T 67900 80255 5 10 1 1 0 3 1
netname=BT_RX
}
N 70000 71700 68800 71700 4
{
T 69400 71755 5 10 1 1 0 3 1
netname=SIO_CMD
}
N 70000 71400 68800 71400 4
{
T 69400 71455 5 10 1 1 0 3 1
netname=SIO_DOUT
}
N 70000 71100 68800 71100 4
{
T 69400 71155 5 10 1 1 0 3 1
netname=SIO_DIN
}
N 61700 76600 62900 76600 4
{
T 62000 76650 5 10 1 1 0 0 1
netname=SWDIO
}
N 61700 76300 62900 76300 4
{
T 62000 76350 5 10 1 1 0 0 1
netname=SWCLK
}
N 62900 79900 61900 79900 4
{
T 62400 79955 5 10 1 1 0 3 1
netname=BT_RX
}
N 62900 79600 61900 79600 4
{
T 62400 79655 5 10 1 1 0 3 1
netname=BT_TX
}
N 62900 80500 61900 80500 4
{
T 62300 80555 5 10 1 1 0 3 1
netname=KEY
}
N 62900 80200 61900 80200 4
{
T 62300 80255 5 10 1 1 0 3 1
netname=LED
}
N 67300 78700 66100 78700 4
{
T 66700 78755 5 10 1 1 0 3 1
netname=SIO_DIN
}
N 67300 78400 66100 78400 4
{
T 66700 78455 5 10 1 1 0 3 1
netname=SIO_DOUT
}
N 67300 79000 66100 79000 4
{
T 66700 79055 5 10 1 1 0 3 1
netname=SIO_CMD
}
N 73400 77500 73700 77500 4
N 73700 77500 73700 77300 4
T 65200 78700 9 12 1 0 0 0 1
tx
T 65200 78400 9 12 1 0 0 0 1
rx
N 73700 79100 72700 79100 4
{
T 73200 79155 5 10 1 1 0 3 1
netname=BT_TX
}
N 73700 79500 72700 79500 4
{
T 73200 79555 5 10 1 1 0 3 1
netname=BT_RX
}
N 60200 78100 60200 78300 4
N 60200 79200 60200 79400 4
N 60200 80300 60200 80500 4
T 63700 77500 9 12 1 0 0 0 1
rx
T 63700 77800 9 12 1 0 0 0 1
tx
C 73700 73100 1 0 0 connector4-2.sym
{
T 74400 75200 5 10 1 1 0 6 1
refdes=CONN2
T 74000 75150 5 10 0 0 0 0 1
device=CONNECTOR_4
T 74000 75350 5 10 0 0 0 0 1
footprint=HEADER4_1
}
T 64400 76900 9 12 1 0 0 0 1
spi1_nss
T 64400 76600 9 12 1 0 0 0 1
spi1_sck
T 64300 76300 9 12 1 0 0 0 1
spi1_miso
T 64300 76000 9 12 1 0 0 0 1
spi1_mosi
N 67300 76900 66100 76900 4
{
T 66700 76955 5 10 1 1 0 3 1
netname=NSS
}
N 67300 76600 66100 76600 4
{
T 66700 76655 5 10 1 1 0 3 1
netname=SCK
}
N 67300 76300 66100 76300 4
{
T 66700 76355 5 10 1 1 0 3 1
netname=MISO
}
N 67300 76000 66100 76000 4
{
T 66700 76055 5 10 1 1 0 3 1
netname=MOSI
}
N 73700 73500 72500 73500 4
{
T 73100 73555 5 10 1 1 0 3 1
netname=MOSI
}
N 73700 74700 72500 74700 4
{
T 73100 74755 5 10 1 1 0 3 1
netname=MISO
}
N 73700 74300 72500 74300 4
{
T 73100 74355 5 10 1 1 0 3 1
netname=SCK
}
N 73700 73900 72500 73900 4
{
T 73100 73955 5 10 1 1 0 3 1
netname=NSS
}
C 73700 71300 1 0 0 connector1-2.sym
{
T 74400 72200 5 10 1 1 0 6 1
refdes=CONN5
T 74000 72150 5 10 0 0 0 0 1
device=CONNECTOR_1
T 74000 72350 5 10 0 0 0 0 1
footprint=JUMPER1
}
C 73300 71100 1 0 0 gnd-1.sym
N 73400 71400 73400 71700 4
N 73400 71700 73700 71700 4
T 63700 79900 9 12 1 0 0 0 1
tx
T 63700 79600 9 12 1 0 0 0 1
rx
N 72100 80500 71400 80500 4
{
T 71900 80555 5 10 1 1 0 3 1
netname=KEY
}
N 72100 79600 71400 79600 4
{
T 71900 79655 5 10 1 1 0 3 1
netname=LED
}
N 62900 78100 61900 78100 4
{
T 62300 78155 5 10 1 1 0 3 1
netname=RESET
}
N 68400 77500 67400 77500 4
{
T 67800 77555 5 10 1 1 0 3 1
netname=RESET
}