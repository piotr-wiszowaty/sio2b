[text-section] init

[code]
start equ $2000
 opt h+f-
 org start
 cli
[end-code]

[text-section] text

$022F constant sdmctl
$0230 constant dladr
$0300 constant ddevic
$0301 constant dunit
$0302 constant dcmnd
$0303 constant dstats
$0304 constant dbufa
$0306 constant dtimlo
$0307 constant dunuse
$0308 constant dbyt
$030A constant daux1
$030B constant daux2
$7000 constant screen

$00 constant mode-sio-none
$40 constant mode-sio-read
$80 constant mode-sio-write

$68 constant net-chan-1
$69 constant net-chan-2

$f3 constant cmd-net-chan-open
$f4 constant cmd-net-chan-close
$f5 constant cmd-net-chan-status
$f6 constant cmd-net-chan-read
$f7 constant cmd-net-chan-write

$80 constant mode-tcp
$00 constant mode-udp

4000 constant tcp-port
create hostname " localhost" 0 c,
create buffer 128 allot

create msg-ok ,' OK '
create msg-error ,' ERR '
create msg-sio ,' SIO '

create msg-hello " Hello, World!" $0a c, 0 c,

variable cursor
variable negative

create dlist
 $70 c, $70 c, $70 c,
 $42 c, screen ,
 $02 c, $02 c, $02 c, $02 c, $02 c, $02 c,
 $02 c, $02 c, $02 c, $02 c, $02 c, $02 c,
 $02 c, $02 c, $02 c, $02 c, $02 c, $02 c,
 $02 c, $02 c, $02 c, $02 c, $02 c, $41 c, dlist ,

: ascii-to-internal ( c -- c )
[code]
 lda pstack,x
 tay
 lda a2i_lut,y
 sta pstack,x
 jmp next

a2i_lut
 dta $40,$41,$42,$43,$44,$45,$46,$47,$48,$49,$4A,$4B,$4C,$4D,$4E,$4F,$50,$51,$52,$53,$54,$55,$56,$57,$58,$59,$5A,$5B,$5C,$5D,$5E,$5F
 dta $00,$01,$02,$03,$04,$05,$06,$07,$08,$09,$0A,$0B,$0C,$0D,$0E,$0F,$10,$11,$12,$13,$14,$15,$16,$17,$18,$19,$1A,$1B,$1C,$1D,$1E,$1F,$20,$21,$22,$23,$24,$25,$26,$27,$28,$29,$2A,$2B,$2C,$2D,$2E,$2F,$30,$31,$32,$33,$34,$35,$36,$37,$38,$39,$3A,$3B,$3C,$3D,$3E,$3F
 dta $60,$61,$62,$63,$64,$65,$66,$67,$68,$69,$6A,$6B,$6C,$6D,$6E,$6F,$70,$71,$72,$73,$74,$75,$76,$77,$78,$79,$7A,$7B,$7C,$7D,$7E,$7F
 dta $C0,$C1,$C2,$C3,$C4,$C5,$C6,$C7,$C8,$C9,$CA,$CB,$CC,$CD,$CE,$CF,$D0,$D1,$D2,$D3,$D4,$D5,$D6,$D7,$D8,$D9,$DA,$DB,$DC,$DD,$DE,$DF
 dta $80,$81,$82,$83,$84,$85,$86,$87,$88,$89,$8A,$8B,$8C,$8D,$8E,$8F,$90,$91,$92,$93,$94,$95,$96,$97,$98,$99,$9A,$9B,$9C,$9D,$9E,$9F,$A0,$A1,$A2,$A3,$A4,$A5,$A6,$A7,$A8,$A9,$AA,$AB,$AC,$AD,$AE,$AF,$B0,$B1,$B2,$B3,$B4,$B5,$B6,$B7,$B8,$B9,$BA,$BB,$BC,$BD,$BE,$BF
 dta $E0,$E1,$E2,$E3,$E4,$E5,$E6,$E7,$E8,$E9,$EA,$EB,$EC,$ED,$EE,$EF,$F0,$F1,$F2,$F3,$F4,$F5,$F6,$F7,$F8,$F9,$FA,$FB,$FC,$FD,$FE,$FF
[end-code] ;

: cursor-next   ( -- u )
  cursor @ dup 1+ cursor ! ;

: set-cursor    ( u -- )
  screen + cursor ! ;

: put-char      ( c -- )
  ascii-to-internal
  cursor-next c! ;

: put-digit     ( c-addr c -- )
  $0F and
  dup 9 > if 23 else 16 then +
  negative c@ or
  swap c! ;

: print-hex-byte ( c -- )
  cursor-next over 4 rshift put-digit
  cursor-next swap put-digit ;

: print-hex-word ( u -- )
  cursor-next over 12 rshift put-digit
  cursor-next over  8 rshift put-digit
  cursor-next over  4 rshift put-digit
  cursor-next swap           put-digit ;

: space
  $20 put-char ;

: show-str     ( c-addr -- )
  dup count cursor @ swap cmove
  c@ cursor @ + cursor ! ;

: @show-str    ( u c-addr -- )
  [label] at_print_str
  swap set-cursor show-str ;

: print-str ( n addr -- )
  0 set-cursor
  swap 0 do
    dup c@ put-char
    1+
  loop
  drop ;

\ Print zero-terminated string
: print-str0 ( addr -- )
  begin
    dup c@ while
    dup c@ put-char
    1+
  repeat
  drop ;

: str-len ( addr -- n )
  dup
  begin
    dup c@ while
    1+
  repeat
  swap - ;

: jsioint
[code]
 jsr $E459
 lda #0
 dex
 sta pstack,x
 tya
 dex
 sta pstack,x
 jmp next
[end-code] ;

\ send data through SIO interface
\ u1 - $40:read, $80:write
\ u2 - data address
\ u3 - data length
\ u4 - device
\ u5 - command
\ n  - =1 - ok, >=128 - error
: sio-command   ( u1 u2 u3 u4 u5 -- n )
  dcmnd c!
  ddevic c!
  $01 dunit c!
  dup daux1 ! dbyt !
  dbufa !
  dstats c!
  $08 dtimlo c!
  jsioint ;

: check-sio-error ( c -- c|0 )
  dup 1 = if drop 0 then ;

: print-sio-error ( c -- )
  [ 22 40 * ] literal msg-sio @show-str
  print-hex-byte ;

: print-netchan-status ( n -- )
  >r mode-sio-read buffer 128 r> cmd-net-chan-status sio-command if
    [ 23 40 * ] literal msg-ok @show-str
    buffer print-str0
  else
    [ 23 40 * ] literal msg-error @show-str
  then ;

: main
  0 negative !

  $00 sdmctl c!
  dlist dladr !
  $22 sdmctl c!

  mode-tcp buffer 0 + c!
  tcp-port buffer 1+ !
  hostname buffer 3 + hostname str-len 1+ cmove

  mode-sio-write buffer 128 net-chan-1 cmd-net-chan-open sio-command
  check-sio-error if
    print-sio-error
    net-chan-1 print-netchan-status
    begin again
  then

  buffer 128 0 fill
  msg-hello str-len buffer !
  msg-hello buffer 2 + msg-hello str-len cmove
  mode-sio-write buffer 128 net-chan-1 cmd-net-chan-write sio-command
  check-sio-error if
    print-sio-error
    net-chan-1 print-netchan-status
    begin again
  then

  begin
    buffer 128 0 fill
    40 buffer !
    mode-sio-read buffer 128 net-chan-1 cmd-net-chan-read sio-command
    1 = while
    [ 40 22 * ] literal set-cursor buffer @ print-hex-word
    buffer @ if
      buffer @ buffer 2 + print-str
    then
  repeat

  mode-sio-none 0 0 net-chan-1 cmd-net-chan-close sio-command
  check-sio-error if
    print-sio-error
    net-chan-1 print-netchan-status
    begin again
  then

  net-chan-1 print-netchan-status

  begin again ;

[code]
 run start
[end-code]
