package org.jmendeth.redasm;

import java.io.File;

/**
 * Represents info about an ABC block and its disassembly.
 */
public class BlockInfo {
    private File dir;
    private File block;
    private int idx;
    private Long recomp_time;
    private Long actual_time;

    public BlockInfo(File dir, File block, int idx) {
        this(dir, block, idx, null, null);
    }

    public BlockInfo(File dir, File block, int idx, Long recomptime, Long actualtime) {
        this.dir = dir;
        this.block = block;
        this.idx = idx;
        this.recomp_time = recomptime;
        this.actual_time = actualtime;
    }

    public File getBlock() {
        return block;
    }

    public File getDir() {
        return dir;
    }

    public int getIdx() {
        return idx;
    }

    public Long getRecompTime() {
        return recomp_time;
    }
    
    public boolean hasChanged() {
        if (recomp_time == null || actual_time == null) return true;
        return recomp_time < actual_time;
    }

    public Long getActual_time() {
        return actual_time;
    }

    public void setActual_time(Long actual_time) {
        this.actual_time = actual_time;
    }

    public void setBlock(File block) {
        this.block = block;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public void setRecompTime(Long recomp_time) {
        this.recomp_time = recomp_time;
    }
}
