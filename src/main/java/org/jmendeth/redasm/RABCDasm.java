package org.jmendeth.redasm;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.fusesource.jansi.Ansi.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Utilities for interacting with RABCDasm.
 */
public class RABCDasm {

    public static final String EXPORT_COMMAND  = "abcexport";
    public static final String REPLACE_COMMAND = "abcreplace";
    public static final String DISASSM_COMMAND = "rabcdasm";
    public static final String ASSM_COMMAND    = "rabcasm";

    public static List<File> export(File swf, File pref, File root) throws IOException, RABCDasmException {
        runCommand(EXPORT_COMMAND, "Error when exporting ABC blocks.", root,
                getCPath(swf,root),getCPath(pref,root));
        List<File> ret = new ArrayList<>();
        for (int i=0; true; i++) {
            File ch = new File(pref.getPath()+i+".abc");
            if (!ch.exists()) break;
            ret.add(ch);
        }
        return ret;
    }

    public static void replace(File swf, int idx, File abc, File root) throws IOException, RABCDasmException {
        runCommand(REPLACE_COMMAND, "Error when replacing the block "+idx+".", root,
                getCPath(swf,root), String.valueOf(idx), getCPath(abc,root));
    }

    public static File disassemble(File abc, File dir, File root) throws IOException, RABCDasmException {
        runCommand(DISASSM_COMMAND, "Error when disassembling ABC block.", root,
                getCPath(abc,root), getCPath(dir,root));
        final String main = dir.getName()+".main.asasm";
        return new File(dir, main);
    }

    public static void assemble(File dirmain, File abc, File root) throws IOException, RABCDasmException {
        runCommand(ASSM_COMMAND, "Error when assembling ABC block.", root,
                getCPath(dirmain,root), getCPath(abc,root));
    }

    public static Process executeCommand(String command, String[] args, File wdir) throws IOException {
        List<String> cm = new ArrayList<>(Arrays.asList(args));
        cm.add(0, command);
        return new ProcessBuilder(cm).directory(wdir).start();
    }

    public static final String STACKTRACE_SEPARATOR = "----------------";
    public static void runCommand(String command, String msg, File wdir, String... args) throws IOException, RABCDasmException {
        try {
            final StringWriter out = new StringWriter();
            final Process pr = executeCommand(command, args, wdir);
            Thread errcp = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        IOUtils.copy(pr.getErrorStream(), out);
                    } catch (IOException ex) {}
                }
            });
            Thread outcp = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        IOUtils.copy(pr.getInputStream(), out);
                    } catch (IOException ex) {}
                }
            });
            errcp.start();
            outcp.start();
            
            int status;
            while (true)
                try {
                    status = pr.waitFor();
                    break;
                } catch (InterruptedException ex) {}
            while (true)
                try {
                    errcp.join();
                    break;
                } catch (InterruptedException ex) {}
            while (true)
                try {
                    outcp.join();
                    break;
                } catch (InterruptedException ex) {}
            if (status == 0) return;

            //Try to find the stacktrace
            final String outs = out.toString();
            List<String> lines = new ArrayList<>(Arrays.asList(outs.split("\n")));
            String msgS = StringUtils.join(lines.subList(0, lines.indexOf(STACKTRACE_SEPARATOR)), "\n");
            msgS = msgS.substring(msgS.indexOf(":")+2);
            throw new RABCDasmException(status, outs, msg+ansi().newline().bold().a(msgS.replace("\t", "  ")).boldOff());
        } catch (IOException ex) {
            throw new IOException("When executing "+command+": "+ex.getMessage(), ex);
        }
    }

    public static String getCPath(File f, File root) {//FIXME: we should definitively migrate to Path
        if (root != null) f = root.toPath().relativize(f.toPath()).toFile();
        return f.getPath();
    }

}
