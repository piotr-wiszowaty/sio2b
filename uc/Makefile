TARGET = main
OBJS = main.o startup_stm32f0.o
STACK_SIZE = 512
FAMILY = STM32F030

COMMONFLAGS = -g -mcpu=cortex-m0 -mthumb -Iinclude
CFLAGS += $(COMMONFLAGS) -D$(FAMILY) -fpack-struct -Wall -Werror
ASFLAGS = $(CFLAGS)
LDFLAGS = $(COMMONFLAGS) -fno-exceptions -ffunction-sections -fdata-sections -nostartfiles -Wl,--gc-sections,-Tstm32f030c8.ld,--defsym=STACK_SIZE=$(STACK_SIZE)

CC = arm-none-eabi-gcc
OBJCOPY = arm-none-eabi-objcopy
OBJDUMP = arm-none-eabi-objdump
SIZE = arm-none-eabi-size
GDB = arm-none-eabi-gdb

all: $(TARGET).elf $(TARGET).bin $(TARGET).lst size

$(TARGET).elf: $(OBJS)
	$(CC) -o $(TARGET).elf $(LDFLAGS) -Wl,-Map,$(TARGET).map $^

%.bin: %.elf
	$(OBJCOPY) -O binary $^ $@

%.lst: %.elf
	#$(OBJDUMP) -h -S $^ >$@
	$(OBJDUMP) -h -D $^ >$@

.PHONY: size burn clean gdb

size:
	$(SIZE) --format=berkeley $(TARGET).elf

burn:
	stm32_burn

gdb:
	$(GDB) -ex "tar rem :3333" -ex "bre main" -ex "mon reset halt" -ex "mon gdb_breakpoint_override hard" $(TARGET).elf

clean:
	rm -f $(TARGET).{elf,bin,lst,map} $(OBJS)
