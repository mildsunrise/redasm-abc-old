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

    public static List<File> export(File swf, File pref) throws IOException, RABCDasmException {
        runCommand(EXPORT_COMMAND, "Error when exporting ABC blocks.", swf.getPath(), pref.getPath());
        List<File> ret = new ArrayList<>();
        for (int i=0; true; i++) {
            File ch = new File(pref.getPath()+i+".abc");
            if (!ch.exists()) break;
            ret.add(ch);
        }
        return ret;
    }

    public static void replace(File swf, int idx, File abc) throws IOException, RABCDasmException {
        runCommand(REPLACE_COMMAND, "Error when replacing the block "+idx+".",
                swf.getPath(), String.valueOf(idx), abc.getPath());
    }

    public static void disassemble(File abc, File dir) {
        //TODO
    }

    public static void assemble(File dirmain, File abc) {
        //TODO
    }

    public static Process executeCommand(String command, String... args) throws IOException {
        List<String> cm = new ArrayList<>(Arrays.asList(args));
        cm.add(0, command);
        ProcessBuilder pb = new ProcessBuilder(cm);
        return pb.start();
    }

    public static final String STACKTRACE_SEPARATOR = "----------------";
    public static void runCommand(String command, String msg, String... args) throws IOException, RABCDasmException {
        try {
            final StringWriter out = new StringWriter();
            final Process pr = executeCommand(command, args);
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
            List<String> lines = new ArrayList<>(Arrays.asList(outs.split("\r?\n")));//FIXME
            String msgS = StringUtils.join(lines.subList(0, lines.indexOf(STACKTRACE_SEPARATOR)), "\n");
            msgS = msgS.substring(msgS.indexOf(":")+2);
            throw new RABCDasmException(status, outs, msg+ansi().newline().bold().a(msgS).boldOff());
        } catch (IOException ex) {
            throw new IOException("When executing "+command+": "+ex.getMessage(), ex);
        }
    }

}
