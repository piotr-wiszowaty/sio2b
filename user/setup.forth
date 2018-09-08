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
$4000 constant screen

$40 constant sio-read
$80 constant sio-write

80 constant name-origin
120 constant pin-origin
200 constant result-origin

$FD constant cmd-setup
$FE constant cmd-get-setup-status

$63 constant setup-complete
$65 constant setup-error
$70 constant setup-in-progress

variable cursor
variable setup-result
create title ,' *** SIO2B setup ***'
create msg1 ,' Name: [                    ]'
create msg2 ,' PIN:  [    ]'
create msg3 ,' Done         '
create msg4 ,' Error        '
create msg5 ,' Setting up...'
create legend 40 c, $DC c, $DD c, $DE c, $DF c, ' :move cursor,' ' Return'* ' :accept          '
create pin " 6502"
create name " SIO2Bv2             "
create name-index 0 ,
create pin-index 0 ,
create field-select 0 ,    \ 0=name, 1=pin
create dlist
 $70 c, $70 c, $70 c,
 $42 c, screen ,
 $02 c, $02 c, $02 c, $02 c, $02 c, $02 c,
 $02 c, $02 c, $02 c, $02 c, $02 c, $02 c,
 $02 c, $02 c, $02 c, $02 c, $02 c, $02 c,
 $02 c, $02 c, $02 c, $02 c, $02 c, $41 c, dlist ,

: get-char    ( -- c )
[code]
 lda #0
 dex
 sta pstack,x
 stx w
 jsr do_gc
 ldx w
 dex
 sta pstack,x
 jmp next
do_gc
 lda $E425
 pha
 lda $E424
 pha
 rts
[end-code] ;

: debug  ( n -- )
[code]
 lda pstack,x
 inx
 inx
 jmp next
[end-code] ;

: cursor-next   ( -- u )
  cursor @ dup 1+ cursor ! ;

: set-cursor    ( u -- )
  screen + cursor ! ;

: put-char      ( c -- )
  cursor-next c! ;

: space 0 put-char ;

: print-str     ( c-addr -- )
  dup count cursor @ swap cmove
  c@ cursor @ + cursor ! ;

: @print-str    ( u c-addr -- )
  [label] at_print_str
  swap set-cursor print-str ;

: highlight     ( u -- )
  screen + dup c@ $80 or swap c! ;

: unhighlight   ( u -- )
  screen + dup c@ $7F and swap c! ;

: highlight-name
  name-origin 7 + name-index @ + highlight ;

: unhighlight-name
  name-origin 7 + name-index @ + unhighlight ;

: highlight-pin
  pin-origin 7 + pin-index @ + highlight ;

: unhighlight-pin
  pin-origin 7 + pin-index @ + unhighlight ;

: go-up 
  field-select @ 1 = if
    \ pin
    unhighlight-pin
    highlight-name
    0 field-select !
  then ;

: go-down
  field-select @ 0= if
    \ name
    1 field-select !
    unhighlight-name
    highlight-pin
  then ;

: go-left
  field-select @ if
    \ pin
    pin-index @ if
      unhighlight-pin
      pin-index @ 1- pin-index !
      highlight-pin
    then
  else
    \ name
    name-index @ if
      unhighlight-name
      name-index @ 1- name-index !
      highlight-name
    then
  then ;

: go-right
  field-select @ if
    \ pin
    pin-index @ 3 < if
      unhighlight-pin
      pin-index @ 1+ pin-index !
      highlight-pin
    then
  else
    \ name
    name-index @ 19 < if
      unhighlight-name
      name-index @ 1+ name-index !
      highlight-name
    then
  then ;

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
\ u4 - command
: sio-command   ( u1 u2 u3 u4 -- n )
  dcmnd c!
  dup daux1 ! dbyt !
  dbufa !
  $31 ddevic c!
  $01 dunit c!
  dstats c!
  $08 dtimlo c!
  jsioint ;

: no-op
[code]
 nop
 jmp next
[end-code] ;

: main
  $00 sdmctl c!
  dlist dladr !
  $22 sdmctl c!

  20 0 do name i + c@ ascii-to-internal msg1 8 + i + c! loop
  4 0 do pin i + c@ ascii-to-internal msg2 8 + i + c! loop

  0 title @print-str
  name-origin msg1 @print-str
  pin-origin msg2 @print-str
  [ 23 40 * ] literal legend @print-str

  highlight-name

  begin
    get-char
    \ dup 39 set-cursor put-char    \ display pressed key
    dup $9b = not while
    dup $1C = if go-up then       \ [Control] + [Up]
    dup $1D = if go-down then     \ [Control] + [Down]
    dup $1E = if go-left then     \ [Control] + [Left]
    dup $1F = if go-right then    \ [Control] + [Right]
    field-select @ if
      \ pin
      dup $30 >= over $39 <= and if
        dup pin pin-index @ + c!
        dup ascii-to-internal pin-origin 7 + pin-index @ + screen + c!
        pin-index @ 3 < if
          unhighlight-pin
          pin-index @ 1+ pin-index !
        then
        highlight-pin
      then
    else
      \ name
      dup $20 >= over $7E <= and if
        dup name name-index @ + c!
        dup ascii-to-internal name-origin 7 + name-index @ + screen + c!
        name-index @ 19 < if
          unhighlight-name
          name-index @ 1+ name-index !
        then
        highlight-name
      then
    then
    drop
  repeat drop

  unhighlight-pin
  unhighlight-name

  result-origin msg5 @print-str

  sio-write pin 24 cmd-setup sio-command drop

  setup-in-progress setup-result !
  begin
    setup-result @ setup-in-progress = while
    2000 0 do no-op loop
    sio-read setup-result 1 cmd-get-setup-status sio-command drop
  repeat
  setup-result @ setup-complete = if result-origin msg3 @print-str then
  setup-result @ setup-error = if result-origin msg4 @print-str then

  begin again ;

[code]
 run start
[end-code]
