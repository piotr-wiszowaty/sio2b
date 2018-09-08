/* STM32F030 interrupt vector table */

static void __Default_Handler() __attribute__ ((interrupt));
static void __Default_Handler()
{
	while (1);
}

void NMI_Handler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void HardFault_Handler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void SVCall_Handler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void PendSV_Handler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void SysTick_Handler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void WWDG_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void RTC_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void FLASH_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void RCC_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void EXTI0_1_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void EXTI2_3_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void EXTI4_15_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void DMA_CH1_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void DMA_CH2_3_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void DMA_CH4_5_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void ADC_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void TIM1_BRK_UP_TRG_COM_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void TIM1_CC_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void TIM3_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void TIM14_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void TIM15_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void TIM16_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void TIM17_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void I2C1_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void I2C2_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void SPI1_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void SPI2_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void USART1_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));
void USART2_IRQHandler() __attribute__ ((interrupt, weak, alias("__Default_Handler")));

extern const char __stack_end;				// imported main stack end (from linker script)

void Reset_Handler(void);					// import the address of Reset_Handler()

void (* const vectors[])(void) __attribute__ ((section(".vectors"))) = {
	(void (*)(void))&__stack_end,		// 0x0000 Main stack end address
	Reset_Handler,						// 0x0004 Reset
	NMI_Handler,						// 0x0008 Non maskable interrupt. The RCC Clock Security System (CSS) is linked to the NMI vector. 
	HardFault_Handler,					// 0x000C All class of fault
	0,									// 0x0010
	0,									// 0x0014
	0,									// 0x0018
	0,									// 0x001C
	0,									// 0x0020
	0,									// 0x0024
	0,									// 0x0028
	SVCall_Handler,						// 0x002C System service call via SWI instruction
	0,									// 0x0030
	0,									// 0x0034
	PendSV_Handler,						// 0x0038 Pendable request for system service
	SysTick_Handler,					// 0x003C System tick timer
	WWDG_IRQHandler,					// 0x0040 Window Watchdog interrupt
	0,									// 0x0044
	RTC_IRQHandler,						// 0x0048 RTC global interrupt
	FLASH_IRQHandler,					// 0x004C Flash global interrupt
	RCC_IRQHandler,						// 0x0050 RCC global interrupt
	EXTI0_1_IRQHandler,					// 0x0054 EXTI Line[1:0] interrupts
	EXTI2_3_IRQHandler,					// 0x0058 EXTI Line[3:2] interrupts
	EXTI4_15_IRQHandler,				// 0x005C EXTI Line[15:4] interrupts
	0,									// 0x0060
	DMA_CH1_IRQHandler,					// 0x0064 DMA channel 1 interrupt
	DMA_CH2_3_IRQHandler,				// 0x0068 DMA channel 2 and 3 interrupts
	DMA_CH4_5_IRQHandler,				// 0x006C DMA channel 4 and 5 interrupts
	ADC_IRQHandler,						// 0x0070
	TIM1_BRK_UP_TRG_COM_IRQHandler,		// 0x0074 TIM1 Break, update, trigger and commutation interrupt
	TIM1_CC_IRQHandler,					// 0x0078 TIM1 Capture Compare interrupt
	0,									// 0x007C
	TIM3_IRQHandler,					// 0x0080 TIM3 global interrupt
	0,									// 0x0084
	0,									// 0x0088
	TIM14_IRQHandler,					// 0x008C TIM14 global interrupt
	TIM15_IRQHandler,					// 0x0090 TIM15 global interrupt
	TIM16_IRQHandler,					// 0x0094 TIM16 global interrupt
	TIM17_IRQHandler,					// 0x0098 TIM17 global interrupt
	I2C1_IRQHandler,					// 0x009C I2C1 global interrupt
	I2C2_IRQHandler,					// 0x00A0 I2C2 global interrupt
	SPI1_IRQHandler,					// 0x00A4 SPI1 global interrupt
	SPI2_IRQHandler,					// 0x00A8 SPI2 global interrupt
	USART1_IRQHandler,					// 0x00AC USART1 global interrupt
	USART2_IRQHandler,					// 0x00B0 USART2 global interrupt
	0,									// 0x00B4
	0,									// 0x00B8
	0,									// 0x00BC
};
