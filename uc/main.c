#include <stm32f030x8.h>

#define SPI_DEBUG		0
#define STOP_BIT_DEBUG		0

#define XTAL_FREQ		8000000
#define SYS_FREQ		48000000
#define HCLK			SYS_FREQ
#define PCLK			SYS_FREQ

#define BAUD_BT_HC05		38400
#define BAUD_SIO		19200
#define BAUD_BT			115200

#define END			0xc0
#define ESC			0xdb
#define ESC_END			0xdc
#define ESC_ESC			0xdd

// PORTA
#define KEY_PIN			0
#define BT_TX_PIN		2
#define BT_RX_PIN		3
#define RESET_PIN		8

// PORTB
#define SIO_CMD			(1 << 5)
#define SIO_TX_PIN		6
#define SIO_RX_PIN		7
#define NSS			12
#define SCK			13
#define MISO			14
#define MOSI			15

#define BT_CHUNK_SIZE		64

#define TICK_INTERVAL		7000

#define SPECIAL_DEVICE_LOW	0x62
#define SPECIAL_DEVICE_HIGH	0x6F

#define CMD_SEND_HIGH_SPEED_INDEX	0x3f
#define CMD_PUT_SECTOR			0x50
#define CMD_READ_SECTOR			0x52
#define CMD_READ_STATUS			0x53
#define CMD_WRITE_SECTOR		0x57
#define CMD_GET_CHUNK			0xf8
#define CMD_GET_NEXT_CHUNK		0xf9
#define CMD_TICK			0xfc
#define CMD_SETUP			0xfd
#define CMD_SETUP_STATUS		0xfe

#define INPUT_HEADER_LENGTH	 2

#define min(a, b)			 ((a) < (b) ? (a) : (b))

#define BT_UART_TX_STR(str)		bt_uart_tx((uint8_t *) str, sizeof(str) - 1)
#define STRNCMP(buf, str)		strncmp(buf, (uint8_t *) str, sizeof(str) - 1)
#define HC05_EXIT_SETUP	\
	GPIOA->BSRR = 0x10000 << KEY_PIN; \
	USART2->BRR = PCLK / BAUD_BT

typedef enum {
	IDLE = 0x00,
	RECEIVE_COMMAND = 0x01,
	WAIT_COMMAND_INACTIVE = 0x02,
	WAIT_AFTER_COMMAND_INACTIVE = 0x03,
	FORWARD_COMMAND = 0x04,
	RECEIVE_COMMAND_RESPONSE_LENGTH = 0x05,
	RECEIVE_TICK_RESPONSE = 0x06,
	RECEIVE_WRITE_RESPONSE = 0x07,
	WAIT_COMMAND_ACK = 0x08,
	SEND_COMMAND_RESPONSE = 0x09,
	RECEIVE_DATA = 0x0a,
	SEND_ERROR = 0x0b,
	WAIT_DATA_RESPONSE = 0x0c,
	GET_SETUP_DATA = 0x0d,
	ACK_SETUP_DATA = 0x0e,
	SEND_SETUP_STATUS = 0x0f,
} State;

typedef enum {
	SETUP_IDLE = 0x20,
	SETUP_HC06_SETPIN = 0x21,
	SETUP_HC06_SETPIN_RESPONSE = 0x22,
	SETUP_HC06_SETNAME_RESPONSE = 0x23,
	SETUP_HC06_SETNAME = 0x24,
	SETUP_HC05_RESET = 0x25,
	SETUP_HC05_SETPIN = 0x26,
	SETUP_HC05_SETPIN_RESPONSE = 0x27,
	SETUP_HC05_SETNAME_RESPONSE = 0x28,
} BTSetupState;

uint8_t sio_rx_buffer[1024];
volatile int sio_rx_wi;
volatile int sio_rx_ri;
uint8_t bt_rx_buffer[1024];
volatile int bt_rx_wi;
volatile int bt_rx_ri;
volatile uint8_t *bt_tx_char;
volatile int bt_tx_count;
uint8_t buffer[1536];
uint8_t buffer2[16];
volatile int counter1;
volatile int counter2;
volatile int counter3;
volatile int counter4;
#if SPI_DEBUG
uint8_t spi_tx_buffer[32];
volatile int spi_tx_wi;
volatile int spi_tx_ri;
#endif
uint8_t high_speed_index = 40;
uint8_t next_high_speed_index = 40;
int next_ubrr = 2544;

void TIM3_IRQHandler()
{
	TIM3->SR = 0;
	if (counter1) {
		counter1--;
	}
	if (counter2) {
		counter2--;
	}
	if (counter3) {
		counter3--;
	}
	if (counter4) {
		counter4--;
	}
}

void USART2_IRQHandler()
{
	if (USART2->ISR & USART_ISR_RXNE) {
		bt_rx_buffer[bt_rx_wi++ & (sizeof(bt_rx_buffer) - 1)] = USART2->RDR;
	}
	if (USART2->ISR & USART_ISR_TXE) {
		if (bt_tx_count) {
			USART2->TDR = *bt_tx_char++;
			bt_tx_count--;
		} else {
			USART2->CR1 &= ~USART_CR1_TXEIE;
		}
	}
}

void USART1_IRQHandler()
{
	if (USART1->ISR & USART_ISR_RXNE) {
		sio_rx_buffer[sio_rx_wi++ & (sizeof(sio_rx_buffer) - 1)] = USART1->RDR;
	}
}

void handle_sio_framing_error()
{
	if (USART1->ISR & USART_ISR_FE) {
		sio_rx_ri = sio_rx_wi;
		USART1->ICR = USART_ICR_FECF;
		USART1->BRR = PCLK / BAUD_SIO;
		high_speed_index = 40;
	}
}

void bt_uart_tx(uint8_t *c, int count)
{
	while (bt_tx_count);
	bt_tx_char = c;
	bt_tx_count = count;
	USART2->CR1 |= USART_CR1_TXEIE;
}

int bt_uart_rx()
{
	if (bt_rx_wi != bt_rx_ri) {
		return 0xff & bt_rx_buffer[bt_rx_ri++ & (sizeof(bt_rx_buffer) - 1)];
	} else {
		return -1;
	}
}

void bt_uart_get_ok()
{
	while (bt_uart_rx() != 'O');
	while (bt_uart_rx() != 'K');
	while (bt_uart_rx() != '\r');
	while (bt_uart_rx() != '\n');
}

void bt_uart_rx_str(char *str)
{
	while (*str) {
		while (bt_uart_rx() != *str);
		str++;
	}
}

void sio_uart_tx(uint8_t c)
{
	while ((USART1->ISR & USART_ISR_TXE) == 0);
	USART1->TDR = c;
	if (USART1->BRR < PCLK / BAUD_SIO) {
		while ((USART1->ISR & USART_ISR_TC) == 0);
#if STOP_BIT_DEBUG
		GPIOB->BSRR = 1 << NSS;
#endif
		// Delay
		TIM6->PSC = PCLK / 1000000 - 1;
		TIM6->ARR = 5;
		TIM6->CR1 = TIM_CR1_OPM | TIM_CR1_CEN;
		while (TIM6->CR1 & TIM_CR1_CEN);
#if STOP_BIT_DEBUG
		GPIOB->BSRR = 0x10000 << NSS;
#endif
	}
}

int sio_uart_rx()
{
	if (sio_rx_wi != sio_rx_ri) {
		return 0xff & sio_rx_buffer[sio_rx_ri++ & (sizeof(sio_rx_buffer) - 1)];
	} else {
		return -1;
	}
}

void setup_clock()
{
	// Turn on HSE
	RCC->CR |= RCC_CR_HSEON;
	while ((RCC->CR & RCC_CR_HSERDY) == 0);

	// Setup PLL : input x 6
	RCC->CFGR |= RCC_CFGR_PLLMUL6 | RCC_CFGR_PLLSRC_HSE_PREDIV;

	// Turn on PLL
	RCC->CR |= RCC_CR_PLLON;
	while ((RCC->CR & RCC_CR_PLLRDY) == 0);

	// SYS_FREQ > 24 MHz : 1 wait state
	FLASH->ACR |= FLASH_ACR_LATENCY;
	while ((FLASH->ACR & FLASH_ACR_LATENCY) != FLASH_ACR_LATENCY);

	// Switch system clock to PLL
	RCC->CFGR |= RCC_CFGR_SW_PLL;
	while ((RCC->CFGR & RCC_CFGR_SWS) != RCC_CFGR_SWS_PLL);
}

int checksum_valid(uint8_t *data, int length)
{
	uint8_t checksum = 0;
	unsigned short temp;
	int i;

	for (i = 0; i < length; i++) {
		temp = checksum + data[i];
		checksum = (temp & 0xff) + ((temp >> 8) & 0xff);
	}
	return data[length] == checksum;
}

#if SPI_DEBUG
void SPI2_IRQHandler()
{
	if (SPI2->SR & SPI_SR_TXE) {
		if (spi_tx_ri != spi_tx_wi) {
			*((char *) &SPI2->DR) = spi_tx_buffer[spi_tx_ri++ & (sizeof(spi_tx_buffer) - 1)];
		} else {
			NVIC_DisableIRQ(SPI2_IRQn);
		}
	}
}

void spi_tx(char c)
{
	spi_tx_buffer[spi_tx_wi++ & (sizeof(spi_tx_buffer) - 1)] = c;
	NVIC_EnableIRQ(SPI2_IRQn);
}

void spi_tx3(char c0, char c1, char c2)
{
	spi_tx_buffer[spi_tx_wi++ & (sizeof(spi_tx_buffer) - 1)] = c0;
	spi_tx_buffer[spi_tx_wi++ & (sizeof(spi_tx_buffer) - 1)] = c1;
	spi_tx_buffer[spi_tx_wi++ & (sizeof(spi_tx_buffer) - 1)] = c2;
	NVIC_EnableIRQ(SPI2_IRQn);
}
#endif

void set_state(State *state, State new_state, int code)
{
#if SPI_DEBUG
	spi_tx3(*state, new_state, code);
#endif

	*state = new_state;
	if (new_state == IDLE) {
		counter3 = TICK_INTERVAL;
	}
}

void bt_set_state(BTSetupState *state, BTSetupState new_state, int code)
{
#if SPI_DEBUG
	spi_tx3(*state, new_state, code);
#endif

	*state = new_state;
}

int strncmp(uint8_t *str1, uint8_t *str2, int length)
{
	while (length--) {
		if (*str1 < *str2) {
			return -1;
		}
		if (*str1++ > *str2++) {
			return 1;
		}
	}
	return 0;
}

int encode_slip(uint8_t *buffer, int n_bytes, int buffer_size)
{
	uint8_t *src = buffer + n_bytes - 1;
	uint8_t *dst = buffer + buffer_size - 1;
	int length = 0;
	uint8_t c;

	while (n_bytes--) {
		switch (c = *src--) {
			case END:
				*dst-- = ESC_END;
				*dst-- = ESC;
				length += 2;
				break;
			case ESC:
				*dst-- = ESC_ESC;
				*dst-- = ESC;
				length += 2;
				break;
			default:
				*dst-- = c;
				length++;
				break;
		}
	}

	return length;
}

int main()
{
	State state = IDLE;
	BTSetupState bt_setup_state = SETUP_IDLE;
	uint8_t ddevic;
	uint8_t dcmnd;
	//uint8_t daux1;
	//uint8_t daux2;
	uint8_t disks_statuses = 0;
	unsigned short tick_counter = 0;
	int response_length;
	int sector_size;
	int rx_i;
	unsigned char setup_result;
	uint8_t pin[4];
	uint8_t name[20];
	int name_length;
	int rx_i_2;
	int length;
	int c;
	int i;

	// Enable clocks
	RCC->AHBENR |= RCC_AHBENR_GPIOAEN | RCC_AHBENR_GPIOBEN;
	RCC->APB2ENR = RCC_APB2ENR_USART1EN;
	RCC->APB1ENR = RCC_APB1ENR_USART2EN | RCC_APB1ENR_TIM3EN | RCC_APB1ENR_TIM6EN;

	// Setup pins
	GPIOA->MODER |= (1 << (2*KEY_PIN)) | (1 << (2*RESET_PIN));
	GPIOA->BSRR = 0x10000 << KEY_PIN;
	GPIOA->BSRR = 1 << RESET_PIN;

	setup_clock();

	/*
	GPIOB->MODER = (1 << (2*SCK));
	while (1) {
		TIM6->PSC = PCLK / 1000000 - 1;
		TIM6->ARR = 1000000 / BAUD_SIO;
		TIM6->CR1 = TIM_CR1_OPM | TIM_CR1_CEN;
		while (TIM6->CR1 & TIM_CR1_CEN);

		GPIOB->ODR ^= 1 << SCK;
	}
	*/

#if SPI_DEBUG
	// Setup SPI
	RCC->APB1ENR |= RCC_APB1ENR_SPI2EN;
	GPIOB->MODER |= (2 << (2*NSS)) | (2 << (2*SCK)) | (2 << (2*MISO)) | (2 << (2*MOSI));
	SPI2->CR1 = SPI_CR1_BR_2 | SPI_CR1_BR_1 | SPI_CR1_BR_0 | SPI_CR1_MSTR | SPI_CR1_SSM | SPI_CR1_SSI | SPI_CR1_SPE;
	SPI2->CR2 = SPI_CR2_DS_2 | SPI_CR2_DS_1 | SPI_CR2_DS_0 | SPI_CR2_TXEIE | SPI_CR2_SSOE | SPI_CR2_NSSP;
	spi_tx_wi = 0;
	spi_tx_ri = 0;
#endif
#if STOP_BIT_DEBUG
	GPIOB->MODER &= ~(3 << (2*NSS));
	GPIOB->MODER |= 1 << (2*NSS);
#endif

	// Setup Timer3: 10 kHz
	counter1 = counter2 = counter3 = counter4 = 0;
	TIM3->ARR = PCLK/10000 - 1;
	TIM3->CR1 = TIM_CR1_CEN;
	TIM3->DIER = TIM_DIER_UIE;
	NVIC_EnableIRQ(TIM3_IRQn);

	// Setup UART1 - Bluetooth
	bt_tx_count = 0;
	bt_rx_wi = bt_rx_ri = 0;
	GPIOA->AFR[0] = (1 << (4*BT_TX_PIN)) | (1 << (4*BT_RX_PIN));
	GPIOA->MODER |= (2 << (2*BT_TX_PIN)) | (2 << (2*BT_RX_PIN));
	GPIOA->PUPDR |= 1 << (2*BT_TX_PIN);
	USART2->BRR = PCLK / BAUD_BT;
	USART2->CR1 = USART_CR1_RXNEIE | USART_CR1_TE | USART_CR1_RE | USART_CR1_UE;
	NVIC_EnableIRQ(USART2_IRQn);

	// Setup UART2 - SIO
	sio_rx_wi = sio_rx_ri = 0;
	GPIOB->AFR[0] = (0 << (4*SIO_TX_PIN)) | (0 << (4*SIO_RX_PIN));
	GPIOB->MODER |= (2 << (2*SIO_TX_PIN)) | (2 << (2*SIO_RX_PIN));
	GPIOB->OTYPER |= 1 << SIO_TX_PIN;
	USART1->BRR = PCLK / BAUD_SIO;
	USART1->CR1 = USART_CR1_RXNEIE | USART_CR1_TE | USART_CR1_RE | USART_CR1_UE;
	NVIC_EnableIRQ(USART1_IRQn);

	while (1) {
		switch (state) {
			case IDLE:
				if (!(GPIOB->IDR & SIO_CMD)) {
					for (i = 0; i < 5; i++) {
						buffer[i] = 0xff;
					}
					rx_i = 0;
					set_state(&state, RECEIVE_COMMAND, 1);
				} else {
					while (sio_uart_rx() > -1);
					if (bt_setup_state != SETUP_IDLE) {
						counter3 = TICK_INTERVAL;
					}
					if (counter3 == 0) {
						ddevic = buffer[0] = 0x31;
						dcmnd = buffer[1] = CMD_TICK;
						buffer[2] = high_speed_index;			// daux1
						buffer[3] = tick_counter++ & 0xff;		// daux2
						set_state(&state, FORWARD_COMMAND, 2);
					}
				}
				break;

			case RECEIVE_COMMAND:
				if ((c = sio_uart_rx()) > -1) {
					buffer[rx_i++] = c;
					if (rx_i == 5) {
						set_state(&state, WAIT_COMMAND_INACTIVE, 1);
					}
				} else if (GPIOB->IDR & SIO_CMD) {
					handle_sio_framing_error();
					set_state(&state, IDLE, 2);
				}
				break;

			case WAIT_COMMAND_INACTIVE:
				if (GPIOB->IDR & SIO_CMD) {
					if (checksum_valid(buffer, 4)) {
						ddevic = buffer[0];
						dcmnd = buffer[1];
						//daux1 = buffer[2];
						//daux2 = buffer[3];
						counter1 = 15;
						set_state(&state, WAIT_AFTER_COMMAND_INACTIVE, 1);
					} else {
						// invalid checksum - ignore command
						handle_sio_framing_error();
						set_state(&state, IDLE, 2);
					}
				}
				break;

			case WAIT_AFTER_COMMAND_INACTIVE:
				if (counter1 == 0) {
					set_state(&state, FORWARD_COMMAND, 1);
				}
				break;

			case FORWARD_COMMAND:
				if (dcmnd == CMD_READ_STATUS || dcmnd == CMD_READ_SECTOR
						|| dcmnd == CMD_WRITE_SECTOR || dcmnd == CMD_PUT_SECTOR
						|| dcmnd == CMD_GET_CHUNK || dcmnd == CMD_GET_NEXT_CHUNK
						|| dcmnd == CMD_SEND_HIGH_SPEED_INDEX) {
					if ((disks_statuses & (1 << (ddevic - 0x31))) || (ddevic >= SPECIAL_DEVICE_LOW && ddevic <= SPECIAL_DEVICE_HIGH)) {
						length = encode_slip(buffer, 4, sizeof(buffer));
						buffer[sizeof(buffer) - 1 - length] = END;
						bt_uart_tx(buffer + sizeof(buffer) - length - 1, length + 1);
						sio_uart_tx('A');
						counter1 = (250 + 1000000*10/19200)/100 + 1;
						rx_i = 0;
						counter2 = 10000;
						set_state(&state, RECEIVE_COMMAND_RESPONSE_LENGTH, 2);
					} else {
						set_state(&state, IDLE, 1);
					}
				} else if (dcmnd == CMD_TICK) {
					length = encode_slip(buffer, 4, sizeof(buffer));
					buffer[sizeof(buffer) - length - 1] = END;
					bt_uart_tx(buffer + sizeof(buffer) - length - 1, length + 1);
					rx_i = 0;
					counter2 = 10000;
					while ((c = bt_uart_rx()) != -1) {
#if SPI_DEBUG
						spi_tx(c);
#endif
					}
					set_state(&state, RECEIVE_COMMAND_RESPONSE_LENGTH, 4);
				} else if (dcmnd == CMD_SETUP) {
					sio_uart_tx('A');
					rx_i = 0;
					counter2 = 10000;
					setup_result = 'p';
					set_state(&state, GET_SETUP_DATA, 5);
				} else if (dcmnd == CMD_SETUP_STATUS) {
					sio_uart_tx('A');
					counter2 = 15;
					set_state(&state, SEND_SETUP_STATUS, 6);
				} else {
					// Unknown command
					sio_uart_tx('N');
					set_state(&state, IDLE, 7);
				}
				break;

			case RECEIVE_COMMAND_RESPONSE_LENGTH:
				if ((c = bt_uart_rx()) != -1) {
					buffer[rx_i++] = c;
				}
				if (rx_i > 1) {
					response_length = buffer[0] | (buffer[1] << 8);
					counter2 = 10000;
					if (dcmnd == CMD_WRITE_SECTOR || dcmnd == CMD_PUT_SECTOR) {
						set_state(&state, RECEIVE_WRITE_RESPONSE, 1);
					} else if (dcmnd == CMD_TICK) {
						set_state(&state, RECEIVE_TICK_RESPONSE, 2);
					} else {
						set_state(&state, WAIT_COMMAND_ACK, 3);
					}
				} else if (counter2 == 0) {
					if (dcmnd == CMD_TICK) {
						set_state(&state, IDLE, 4);
					} else {
						counter1 = 2700;
						set_state(&state, SEND_ERROR, 5);
					}
				}
				break;

			case RECEIVE_TICK_RESPONSE:
				if ((c = bt_uart_rx()) != -1) {
					buffer[rx_i++] = c;
				}
				if (rx_i > 5) {
					disks_statuses = buffer[2];
					next_high_speed_index = buffer[3];
					next_ubrr = buffer[4] | (buffer[5] << 8);
					set_state(&state, IDLE, 1);
				}
				if (counter2 == 0) {
					set_state(&state, IDLE, 2);
				}
				break;

			case RECEIVE_WRITE_RESPONSE:
				if ((c = bt_uart_rx()) != -1) {
					buffer[rx_i++] = c;
				}
				if (rx_i > 4) {
					if (buffer[2] == 'c') {
						sector_size = buffer[3] | (buffer[4] << 8);
						counter2 = 10000;
						rx_i = 1;
						set_state(&state, RECEIVE_DATA, 2);
					} else {
						counter1 = 2700;
						set_state(&state, SEND_ERROR, 3);
					}
				}
				if (counter2 == 0) {
					set_state(&state, IDLE, 4);
				}
				break;

			case WAIT_COMMAND_ACK:
				if (counter1 == 0) {
					if (!(GPIOB->IDR & SIO_CMD)) {
						// Do not send data when next command is being sent
						set_state(&state, IDLE, 1);
					} else {
						set_state(&state, SEND_COMMAND_RESPONSE, 2);
					}
				}
				break;

			case SEND_COMMAND_RESPONSE:
				if ((c = bt_uart_rx()) != -1) {
					sio_uart_tx(c);
					if (--response_length == 0) {
						if (dcmnd == CMD_SEND_HIGH_SPEED_INDEX) {
							while (!(USART1->ISR & USART_ISR_TC));
							USART1->BRR = next_ubrr;
							high_speed_index = next_high_speed_index;
						}
						set_state(&state, IDLE, 1);
					}
				} else if (counter2 == 0) {
					set_state(&state, IDLE, 2);
				}
				break;

			case RECEIVE_DATA:
				if ((c = sio_uart_rx()) > -1) {
					buffer[rx_i++] = c;
				} else if (counter2 == 0) {
					buffer[0] = 'e';
					bt_uart_tx(buffer, 1);
					set_state(&state, IDLE, 1);
				} else if (rx_i == sector_size + 2) {
					if (checksum_valid(buffer + 1, sector_size)) {
						sio_uart_tx('A');
						buffer[0] = 'c';
						rx_i = 0;
						length = encode_slip(buffer, 1 + sector_size, sizeof(buffer));
						bt_uart_tx(buffer + sizeof(buffer) - length, length);
						set_state(&state, WAIT_DATA_RESPONSE, 2);
					} else {
						sio_uart_tx('N');
						buffer[0] = 'e';
						bt_uart_tx(buffer, 1);
						set_state(&state, IDLE, 3);
					}
				}
				break;

			case WAIT_DATA_RESPONSE:
				if ((c = bt_uart_rx()) != -1) {
					buffer[rx_i++] = c;
				}
				if (rx_i > INPUT_HEADER_LENGTH) {
					sio_uart_tx(buffer[INPUT_HEADER_LENGTH]);
					set_state(&state, IDLE, 1);
				} else if (counter2 == 0) {
					set_state(&state, SEND_ERROR, 2);
				}
				break;

			case SEND_ERROR:
				if (counter1 == 0) {
					sio_uart_tx('E');
					set_state(&state, IDLE, 1);
				}
				break;

			case GET_SETUP_DATA:
				if ((c = sio_uart_rx()) > -1) {
					buffer[rx_i++] = c;
					if (rx_i == 4 + 20 + 1) {
						if (checksum_valid(buffer, 4 + 20)) {
							sio_uart_tx('A');
							for (i = 0; i < 4; i++) {
								pin[i] = buffer[i];
							}
							for (i = 0; i < 20; i++) {
								name[i] = buffer[4 + i];
							}
							bt_set_state(&bt_setup_state, SETUP_HC06_SETPIN, 1);
							counter2 = 50;
							set_state(&state, ACK_SETUP_DATA, 2);
						} else {
							sio_uart_tx('N');
							set_state(&state, IDLE, 3);
						}
					}
				} else if (counter2 == 0) {
					set_state(&state, IDLE, 4);
				}
				break;

			case ACK_SETUP_DATA:
				if (counter2 == 0) {
					sio_uart_tx('C');
					set_state(&state, IDLE, 1);
				}
				break;

			case SEND_SETUP_STATUS:
				if (counter2 == 0) {
					sio_uart_tx('C');
					sio_uart_tx(setup_result);
					sio_uart_tx(setup_result);
					set_state(&state, IDLE, 1);
				}
				break;
		}

		switch (bt_setup_state) {
			case SETUP_IDLE:
				break;

			case SETUP_HC06_SETPIN:
				for (name_length = 20; name[name_length-1] == 0 || name[name_length-1] == ' '; name_length--);
				counter4 = 30000;
				rx_i_2 = 0;
				BT_UART_TX_STR("AT+PIN");
				bt_uart_tx(pin, 4);
				bt_set_state(&bt_setup_state, SETUP_HC06_SETPIN_RESPONSE, 1);
				break;

			case SETUP_HC06_SETPIN_RESPONSE:
				if ((c = bt_uart_rx()) > -1) {
					buffer2[rx_i_2++] = c;
					if (rx_i_2 == 8) {
						if (!STRNCMP(buffer2, "OKsetPIN") || !STRNCMP(buffer2, "OKsetpin")) {
							counter4 = 12000;
							bt_set_state(&bt_setup_state, SETUP_HC06_SETNAME, 1);
						} else {
							setup_result = 'e';
							bt_set_state(&bt_setup_state, SETUP_IDLE, 2);
						}
					}
				} else if (counter4 == 0) {
					GPIOA->BSRR = (1 << KEY_PIN) | (0x10000 << RESET_PIN);
					USART2->BRR = PCLK / BAUD_BT_HC05;
					counter4 = 200;
					bt_set_state(&bt_setup_state, SETUP_HC05_RESET, 3);
				}
				break;

			case SETUP_HC06_SETNAME:
				if (counter4 == 0) {
					counter4 = 50000;
					rx_i_2 = 0;
					BT_UART_TX_STR("AT+NAME");
					bt_uart_tx(name, name_length);
					bt_set_state(&bt_setup_state, SETUP_HC06_SETNAME_RESPONSE, 1);
				}
				break;

			case SETUP_HC06_SETNAME_RESPONSE:
				if ((c = bt_uart_rx()) > -1) {
					buffer2[rx_i_2++] = c;
					if (rx_i_2 == 9) {
						if (!STRNCMP(buffer2, "OKsetname") || !STRNCMP(buffer2, "OKname")) {
							setup_result = 'c';
							bt_set_state(&bt_setup_state, SETUP_IDLE, 1);
						} else {
							setup_result = 'e';
							bt_set_state(&bt_setup_state, SETUP_IDLE, 2);
						}
					}
				} else if (counter4 == 0) {
					setup_result = 'e';
					bt_set_state(&bt_setup_state, SETUP_IDLE, 3);
				}
				break;

			case SETUP_HC05_RESET:
				if (counter4 == 0) {
					GPIOA->BSRR = 1 << RESET_PIN;
					counter4 = 6000;
					bt_set_state(&bt_setup_state, SETUP_HC05_SETPIN, 1);
				}
				break;

			case SETUP_HC05_SETPIN:
				if (counter4 == 0) {
					while (bt_uart_rx() > -1);			// BT_TX==0 during reset
					BT_UART_TX_STR("AT+PSWD=");
					bt_uart_tx(pin, 4);
					BT_UART_TX_STR("\r\n");
					rx_i_2 = 0;
					counter4 = 50000;
					bt_set_state(&bt_setup_state, SETUP_HC05_SETPIN_RESPONSE, 1);
				}
				break;

			case SETUP_HC05_SETPIN_RESPONSE:
				if ((c = bt_uart_rx()) > -1) {
					buffer2[rx_i_2++] = c;
					if (c == '\n') {
						if (!STRNCMP(buffer2, "OK")) {
							BT_UART_TX_STR("AT+NAME=");
							bt_uart_tx(name, name_length);
							BT_UART_TX_STR("\r\n");
							rx_i_2 = 0;
							counter4 = 10000;
							bt_set_state(&bt_setup_state, SETUP_HC05_SETNAME_RESPONSE, 1);
						} else {
							HC05_EXIT_SETUP;
							setup_result = 'e';
							bt_set_state(&bt_setup_state, SETUP_IDLE, 2);
						}
					}
				} else if (counter4 == 0) {
					HC05_EXIT_SETUP;
					setup_result = 'e';
					bt_set_state(&bt_setup_state, SETUP_IDLE, 3);
				}
				break;

			case SETUP_HC05_SETNAME_RESPONSE:
				if ((c = bt_uart_rx()) > -1) {
					buffer2[rx_i_2++] = c;
					if (c == '\n') {
						HC05_EXIT_SETUP;
						if (!STRNCMP(buffer2, "OK")) {
							setup_result = 'c';
							bt_set_state(&bt_setup_state, SETUP_IDLE, 1);
						} else {
							setup_result = 'e';
							bt_set_state(&bt_setup_state, SETUP_IDLE, 2);
						}
					}
				} else if (counter4 == 0) {
					HC05_EXIT_SETUP;
					setup_result = 'e';
					bt_set_state(&bt_setup_state, SETUP_IDLE, 3);
				}
				break;
		}
	}
}
