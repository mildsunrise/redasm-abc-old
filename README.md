k# Robust, Easy [Dis-]Assembler for ActionScript Bytecode

## Introduction

Did you know **[RABCDasm](https://github.com/CyberShadow/RABCDAsm)**, the robust ActionScript (dis)assembler?  
Well, **redasm-abc** aims to provide an easy-to-use assistant to it.

Forget about the **export**-**disassemble**-**edit**-**assemble**-**replace** process.  
**Note the difference:**

### RABCDasm (multiple ABC tags)

```bash
$ abcexport file.swf
$ rabcdasm file-0.abc
$ rabcdasm file-1.abc
$ rabcdasm file-2.abc
$ # edit what you want...
$ rabcasm file-0/file-0.main.asasm
$ rabcasm file-1/file-1.main.asasm
$ rabcasm file-2/file-2.main.asasm
$ abcreplace file.swf 0 file-0/file-0.main.abc
$ abcreplace file.swf 1 file-0/file-1.main.abc
$ abcreplace file.swf 2 file-0/file-2.main.abc
```

### redasm-abc (multiple ABC tags)

```bash
$ redasm
$ # edit what you want...
$ redasm
```

## Installation

You need a Java 7 JDK to run redasm-abc,  
and a [D compiler](http://dlang.org) to build RABCDasm.

TODO

## Usage

Put the SWF you want to hack in an **empty directory**.  
Then run `redasm` to extract all his ABC blocks and disassemble them.  
Edit what you want, then run `redasm` again to apply the changes to the SWF.

redasm-abc will create a directory for each disassembled ABC block.  
By default, the directories are named `block-0`, `block-1`, `block-2`, ...  
but you are free to **rename** them to your needs.

**Important:** redasm-abc will also create a directory named `build`, where the
assembled blocks will be put. _Never_ rename, move or modify this directory.
