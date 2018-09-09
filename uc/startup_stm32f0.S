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

__Default_Handler:
	ldr	r3, NVIC_INT_CTRL_CONST
	ldr	r2, [r3, #0]
	uxtb	r2, r2
	b	.

NVIC_INT_CTRL_CONST: .word 0xe000ed04


	.section .vectors, "a", %progbits
vectors:

	.word	__stack_end			// 0x0000 Main stack end address
	.word	Reset_Handler			// 0x0004 Reset
	.word	NMI_Handler			// 0x0008 Non maskable interrupt. The RCC Clock Security System (CSS) is linked to the NMI vector.
	.word	HardFault_Handler		// 0x000C All class of fault
	.word	0				// 0x0010
	.word	0				// 0x0014
	.word	0				// 0x0018
	.word	0				// 0x001C
	.word	0				// 0x0020
	.word	0				// 0x0024
	.word	0				// 0x0028
	.word	SVCall_Handler			// 0x002C System service call via SWI instruction
	.word	0				// 0x0030
	.word	0				// 0x0034
	.word	PendSV_Handler			// 0x0038 Pendable request for system service
	.word	SysTick_Handler			// 0x003C System tick timer
	.word	WWDG_IRQHandler			// 0x0040 Window Watchdog interrupt
	.word	0				// 0x0044
	.word	RTC_IRQHandler			// 0x0048 RTC global interrupt
	.word	FLASH_IRQHandler		// 0x004C Flash global interrupt
	.word	RCC_IRQHandler			// 0x0050 RCC global interrupt
	.word	EXTI0_1_IRQHandler		// 0x0054 EXTI Line[1:0] interrupts
	.word	EXTI2_3_IRQHandler		// 0x0058 EXTI Line[3:2] interrupts
	.word	EXTI4_15_IRQHandler		// 0x005C EXTI Line[15:4] interrupts
	.word	0				// 0x0060
	.word	DMA_CH1_IRQHandler		// 0x0064 DMA channel 1 interrupt
	.word	DMA_CH2_3_IRQHandler		// 0x0068 DMA channel 2 and 3 interrupts
	.word	DMA_CH4_5_IRQHandler		// 0x006C DMA channel 4 and 5 interrupts
	.word	ADC_IRQHandler			// 0x0070
	.word	TIM1_BRK_UP_TRG_COM_IRQHandler	// 0x0074 TIM1 Break, update, trigger and commutation interrupt
	.word	TIM1_CC_IRQHandler		// 0x0078 TIM1 Capture Compare interrupt
	.word	0				// 0x007C
	.word	TIM3_IRQHandler			// 0x0080 TIM3 global interrupt
	.word	0				// 0x0084
	.word	0				// 0x0088
	.word	TIM14_IRQHandler		// 0x008C TIM14 global interrupt
	.word	TIM15_IRQHandler		// 0x0090 TIM15 global interrupt
	.word	TIM16_IRQHandler		// 0x0094 TIM16 global interrupt
	.word	TIM17_IRQHandler		// 0x0098 TIM17 global interrupt
	.word	I2C1_IRQHandler			// 0x009C I2C1 global interrupt
	.word	I2C2_IRQHandler			// 0x00A0 I2C2 global interrupt
	.word	SPI1_IRQHandler			// 0x00A4 SPI1 global interrupt
	.word	SPI2_IRQHandler			// 0x00A8 SPI2 global interrupt
	.word	USART1_IRQHandler		// 0x00AC USART1 global interrupt
	.word	USART2_IRQHandler		// 0x00B0 USART2 global interrupt
	.word	0				// 0x00B4
	.word	0				// 0x00B8
	.word	0				// 0x00BC

	.weak	NMI_Handler
	.thumb_set NMI_Handler, __Default_Handler

	.weak	HardFault_Handler
	.thumb_set HardFault_Handler, __Default_Handler

	.weak	SVCall_Handler
	.thumb_set SVCall_Handler, __Default_Handler

	.weak	PendSV_Handler
	.thumb_set PendSV_Handler, __Default_Handler

	.weak	SysTick_Handler
	.thumb_set SysTick_Handler, __Default_Handler

	.weak	WWDG_IRQHandler
	.thumb_set WWDG_IRQHandler, __Default_Handler

	.weak	RTC_IRQHandler
	.thumb_set RTC_IRQHandler, __Default_Handler

	.weak	FLASH_IRQHandler
	.thumb_set FLASH_IRQHandler, __Default_Handler

	.weak	RCC_IRQHandler
	.thumb_set RCC_IRQHandler, __Default_Handler

	.weak	EXTI0_1_IRQHandler
	.thumb_set EXTI0_1_IRQHandler, __Default_Handler

	.weak	EXTI2_3_IRQHandler
	.thumb_set EXTI2_3_IRQHandler, __Default_Handler

	.weak	EXTI4_15_IRQHandler
	.thumb_set EXTI4_15_IRQHandler, __Default_Handler

	.weak	DMA_CH1_IRQHandler
	.thumb_set DMA_CH1_IRQHandler, __Default_Handler

	.weak	DMA_CH2_3_IRQHandler
	.thumb_set DMA_CH2_3_IRQHandler, __Default_Handler

	.weak	DMA_CH4_5_IRQHandler
	.thumb_set DMA_CH4_5_IRQHandler, __Default_Handler

	.weak	ADC_IRQHandler
	.thumb_set ADC_IRQHandler, __Default_Handler

	.weak	TIM1_BRK_UP_TRG_COM_IRQHandler
	.thumb_set TIM1_BRK_UP_TRG_COM_IRQHandler, __Default_Handler

	.weak	TIM1_CC_IRQHandler
	.thumb_set TIM1_CC_IRQHandler, __Default_Handler

	.weak	TIM3_IRQHandler
	.thumb_set TIM3_IRQHandler, __Default_Handler

	.weak	TIM14_IRQHandler
	.thumb_set TIM14_IRQHandler, __Default_Handler

	.weak	TIM15_IRQHandler
	.thumb_set TIM15_IRQHandler, __Default_Handler

	.weak	TIM16_IRQHandler
	.thumb_set TIM16_IRQHandler, __Default_Handler

	.weak	TIM17_IRQHandler
	.thumb_set TIM17_IRQHandler, __Default_Handler

	.weak	I2C1_IRQHandler
	.thumb_set I2C1_IRQHandler, __Default_Handler

	.weak	I2C2_IRQHandler
	.thumb_set I2C2_IRQHandler, __Default_Handler

	.weak	SPI1_IRQHandler
	.thumb_set SPI1_IRQHandler, __Default_Handler

	.weak	SPI2_IRQHandler
	.thumb_set SPI2_IRQHandler, __Default_Handler

	.weak	USART1_IRQHandler
	.thumb_set USART1_IRQHandler, __Default_Handler

	.weak	USART2_IRQHandler
	.thumb_set USART2_IRQHandler, __Default_Handler
