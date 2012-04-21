# Robust, Easy [Dis-]Assembler for ActionScript Bytecode

**NOTE:** Work still in progress! Will be published soon. Stay tuned!

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
First-run, extracting file.swf... done.
3 ABC tags were found in the SWF.

$ # edit what you want...
$ redasm
Checking changed files... done.
Tag 0: assembling... done.
       replacing...  done.
Tag 1: up-to-date.
Tag 2: assembling... done.
       replacing...  done.

Done!
```

## Installation

TODO

## Usage

Put the SWF you want to hack in an **empty directory**.  
Then run `redasm` to extract all his ABC blocks and disassemble them.  
Edit what you want, then run `redasm` again to apply the changes to the SWF.

redasm-abc will create a directory for each disassembled ABC block.  
By default, the directories are named `file-0`, `file-1`, `file-2`, ...  
but you are free to **rename** them to your needs.

**Important:** redasm-abc will also create a directory named `build`, where the
assembled blocks will be put. _Never_ rename, move or modify this directory.
