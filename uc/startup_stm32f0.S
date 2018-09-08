	.equ	STACK_FILL, 0xdeadbeef

	.text
	.balign 2
	.syntax unified
	.thumb
	.thumb_func
	.global Reset_Handler

Reset_Handler:
	movs	r4, #4

	ldr	r1, =__data_init_start
    	ldr	r2, =__data_start
    	ldr	r3, =__data_end
1:	cmp	r2, r3
	beq	2f
	ldr	r0, [r1]
	str	r0, [r2]
	add	r1, r4
	add	r2, r4
	b	1b

	movs	r0, #0
	ldr	r1, =__bss_start
	ldr	r2, =__bss_end
1:	cmp	r1, r2
	beq	2f
	str	r0, [r1]
	add	r1, r4
	b	1b
2:

	ldr	r0, =STACK_FILL
	ldr	r1, =__stack_start
	ldr	r2, =__stack_end
1:	cmp	r1, r2
	beq	2f
	str	r0, [r1]
	add	r1, r4
	b	1b
2:

	ldr	r0, =main
	blx	r0
	b	.
