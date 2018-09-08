/* NOTE: script requires defined STACK_SIZE symbol */

SEARCH_DIR(.);

OUTPUT_FORMAT("elf32-littlearm", "elf32-bigarm", "elf32-littlearm");
OUTPUT_ARCH(arm);

MEMORY
{
	rom (rx)	: org = 0x08000000, len = 64k
	ram (rwx)	: org = 0x20000000, len = 8k
	memory_b1 (rx)  : org = 0x60000000, len = 0k
}

ENTRY(Reset_Handler);

SECTIONS
{
	.text :
	{
		. = ALIGN(4);
		KEEP(*(.vectors));
		. = ALIGN(4);
		*(.text .text.* .gnu.linkonce.t.*);
		. = ALIGN(4);
		*(.rodata .rodata.* .gnu.linkonce.r.*);
	} > rom AT > rom

	.data :
	{
		. = ALIGN(4);
		__data_init_start = LOADADDR (.data);
		__data_start = .;
		. = ALIGN(4);
		*(.data .data.* .gnu.linkonce.d.*)
		. = ALIGN(4);
		__data_end = .;
	} > ram AT > rom

	.bss :
	{
		. = ALIGN(4);
		__bss_start = .;
		. = ALIGN(4);
		*(.bss .bss.* .gnu.linkonce.b.*)
		. = ALIGN(4);
		*(COMMON);
		. = ALIGN(4);
		__bss_end = .;
	} > ram AT > ram

	.stack :
	{
		. = ALIGN(4);
		__stack_start = .;
		. += STACK_SIZE;
		. = ALIGN(4);
		__stack_end = .;
		end = .;
	} > ram

	/* MEMORY_bank1 section, code must be located here explicitly            */
	/* Example: extern int foo(void) __attribute__ ((section (".mb1text"))); */
	.memory_b1_text :
	{
		*(.mb1text)        /* .mb1text sections (code) */
		*(.mb1text*)       /* .mb1text* sections (code)  */
		*(.mb1rodata)      /* read-only data (constants) */
		*(.mb1rodata*)
	} >memory_b1

	.stab 			0 (NOLOAD) : { *(.stab) }
	.stabstr 		0 (NOLOAD) : { *(.stabstr) }
	/* DWARF debug sections.
	* Symbols in the DWARF debugging sections are relative to the beginning
	* of the section so we begin them at 0. */
	/* DWARF 1 */
	.debug			0 : { *(.debug) }
	.line			0 : { *(.line) }
	/* GNU DWARF 1 extensions */
	.debug_srcinfo		0 : { *(.debug_srcinfo) }
	.debug_sfnames		0 : { *(.debug_sfnames) }
	/* DWARF 1.1 and DWARF 2 */
	.debug_aranges		0 : { *(.debug_aranges) }
	.debug_pubnames		0 : { *(.debug_pubnames) }
	/* DWARF 2 */
	.debug_info		0 : { *(.debug_info .gnu.linkonce.wi.*) }
	.debug_abbrev		0 : { *(.debug_abbrev) }
	.debug_line		0 : { *(.debug_line) }
	.debug_frame		0 : { *(.debug_frame) }
	.debug_str		0 : { *(.debug_str) }
	.debug_loc		0 : { *(.debug_loc) }
	.debug_macinfo		0 : { *(.debug_macinfo) }
	/* SGI/MIPS DWARF 2 extensions */
	.debug_weaknames	0 : { *(.debug_weaknames) }
	.debug_funcnames	0 : { *(.debug_funcnames) }
	.debug_typenames	0 : { *(.debug_typenames) }
	.debug_varnames		0 : { *(.debug_varnames) }

	.note.gnu.arm.ident	0 : { KEEP(*(.note.gnu.arm.ident)) }
	.ARM.attributes		0 : { KEEP(*(.ARM.attributes)) }
	/DISCARD/		: { *(.note.GNU-stack) }
}

