package org.jmendeth.redasm;

import java.util.*;

import java.io.File;
import java.io.IOException;
import java.nio.channels.*;
import java.nio.file.StandardOpenOption;

import org.apache.commons.lang3.StringUtils;

import static org.fusesource.jansi.Ansi.*;
import org.fusesource.jansi.AnsiConsole;

/**
 * The main redasm-abc app.
 */
public class App {

    static final boolean debug = false;
    static boolean showStacks = debug;

    //CONSTANTS --don't modify or it'll break compatibility
    public static final String ROOT_MARKER   = ".redasm";
    public static final String TAG_MARKER    = ".redasm-tag";
    public static final String BUILD_DIRNAME = "build";

    public static final int NO_ROOT_FOUND_STATUS = 5;
    public static final int FILE_ERROR_STATUS = 3;
    public static final int LOCK_FAILED_STATUS = 4;
    public static final int RABCDASM_ERROR_STATUS = 1;
    public static final int NO_TAGS_FOUND_STATUS = 2;
    public static final int UNKNOWN_ERROR_STATUS = 10;

    //Run-time vars
    public static final String curDir = System.getProperty("user.dir");
    static File root;
    static File rootmark;
    static File swf;
    static Boolean initial;
    static File build;
    static Map<Integer, String> tags;
    static List<File> blocks;

    public static void main(String[] args) {
        //FIXME: add clean, quiet and verbose options

        //INITIALIZE ANSI
        AnsiConsole.systemInstall();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                AnsiConsole.err.flush();
                System.err.flush();
                AnsiConsole.out.flush();
                System.out.print(ansi().reset());
                System.out.flush();
                AnsiConsole.systemUninstall();
            }
        }));

        //DETERMINE ROOT
        determineRoot();

        if (initial) {
            //FIXME: add shutdown-hook
            System.out.println(ansi().fg(Color.YELLOW).a("First run, creating files...").reset().a("\n"));
        }

        //LOCK MARKER FILE
        try (FileChannel marker = FileChannel.open(rootmark.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            final FileLock lock = marker.tryLock();
            if (lock == null) {
                printError("Fatal", "Could not acquire lock.\n"
                        + "Make sure no other instances of redasm-abc are running.");
                System.exit(LOCK_FAILED_STATUS);
            }
            //We ackired the lock
            try {
                if (initial) {
                    //INITIALIZE ROOT

                    //Create build directory
                    runProcess(new RunTarget() {
                        @Override
                        public void run() throws Throwable {
                            assertFS(build.mkdirs(), "creating the build directory");
                        }
                    }, "Creating build directory...");

                    //Extract ABC blocks
                    final File pref = new File(build, "block-");

                    runProcess(new RunTarget() {
                        @Override
                        public void run() throws Throwable {
                           blocks = RABCDasm.export(swf, pref);
                        }
                    }, "Extracting ABC blocks...");

                    if (blocks.isEmpty()) {
                        printError("Error", "It seems the SWF contains no ActionScript blocks.\nNothing to disassemble. Exiting.");
                        System.exit(NO_TAGS_FOUND_STATUS);
                    }

                    //If there's only one block (most cases) rename it
                    if (blocks.size() == 1) {
                        File bl = blocks.get(0);
                        File nbl = new File(bl.getParent(), "block.abc");
                        assertFS(bl.renameTo(nbl), "renaming block file");
                        blocks.set(0, nbl);
                    }

                    //Disassemble ABC blocks
                    //TODO

                    //Finally, mark directory as initialized
                    initial = false;
                } else {
                    //MAIN BUILD PROCESS
                }
                System.out.println(ansi().fg(Color.GREEN).bold().a("Finished correctly.").reset().newline());
            } finally {
                lock.release();
            }
        } catch (IOException ex) {
            printError("I/O error", ex.getLocalizedMessage());
            System.exit(FILE_ERROR_STATUS);
        } catch (FsException ex) {
            printError("FS error", ex.getLocalizedMessage());
            System.exit(FILE_ERROR_STATUS);
        } catch (RABCDasmException ex) {
            printError("RABCDasm error", ex.getMessage() + " (Status "+ex.getCode()+")");
            if (showStacks) {
                AnsiConsole.err.println(ex.getOut());
            }
            System.exit(RABCDASM_ERROR_STATUS);
        } catch (Throwable t) {
            printError("Unknown exception", t.getLocalizedMessage());
            t.printStackTrace();
            System.exit(UNKNOWN_ERROR_STATUS);
        }
    }

    public static class FsException extends Exception {
        public FsException(Throwable cause) {
            super(cause);
        }
        public FsException(String message, Throwable cause) {
            super(message, cause);
        }
        public FsException(String message) {
            super(message);
        }
        public FsException() {}
    }

    public static void assertFS(boolean i, String fnc) throws FsException {
        if (i) return;
        throw new FsException("Error when "+fnc+".");
    }

    public static void determineRoot() {
        File rt;

        //1. Check if current directory is root
        if ((rootmark=hasRoot(rt=new File(curDir))) != null) {
            setPaths(rt, false);
            return;
        }

        //2. Check if current directory *can be* a root
        if (canBeRoot(rt)) {
            setPaths(rt, true);
            return;
        }

        //3. Check if any parent directory is a root
        List<File> roots = new ArrayList<>(Arrays.asList(File.listRoots()));
        rt = rt.getAbsoluteFile();
        while (!roots.contains(rt=rt.getParentFile()))
            if ((rootmark=hasRoot(rt))!=null) {
                setPaths(rt, false);
                return;
            }

        printError("Fatal",
                  "No previously-disassembled SWF found.\n"
                + "Please, put the SWF you want to disassemble in\n"
                + "an empty folder, then call redasm from there.");
        System.exit(NO_ROOT_FOUND_STATUS);
    }

    public static boolean canBeRoot(File rt) {
        int i = 0;
        for (File ch : rt.listFiles()) {
            if (ch.isDirectory()) return false;
            if (isSWF(ch)) i++;
        }
        return i==1;
    }

    public static boolean isSWF(File ch) {
        return ch.isFile() && hasExtension(ch.getName(), "swf");
    }

    public static void setPaths(File rt, boolean initiale) {
        root = rt;
        initial = initiale;
        rootmark = new File(rt, ROOT_MARKER);
        build = new File(rt, BUILD_DIRNAME);

        for (File ch : rt.listFiles()) {
            if (isSWF(ch)) {
                swf = ch;
                break;
            }
        }
    }

    /**
     * Checks if the current directory is a root (has a marker).
     * If it is, return the marker.
     * Else, return null.
     */
    public static File hasRoot(File dir) {
        File rt = new File(dir, ROOT_MARKER);
        if (rt.exists()) return rt;
        return null;
    }

    public static boolean hasExtension(String name, String ext) {
        if (!ext.startsWith(".")) ext = "."+ext;
        return name.toLowerCase(Locale.ENGLISH).endsWith(ext.toLowerCase(Locale.ENGLISH));
    }

    public static final int CATEGORY_SPACE = 1;
    public static void printError(String category, String msg) {
        final String catstr = category+":";
        final int totsp = catstr.length()+CATEGORY_SPACE;
        final String sp = StringUtils.repeat(' ', totsp);
        final String[] lines = msg.split("\n");
        AnsiConsole.err.println(ansi().fg(Color.RED).bold().a(catstr).boldOff().a(StringUtils.repeat(' ', CATEGORY_SPACE)+lines[0]));

        for (int i = 1; i < lines.length; i++)
            AnsiConsole.err.println(ansi().a(sp+lines[i]));
        AnsiConsole.err.print(ansi().reset());
    }

    static final String done_msg = ansi().fg(Color.GREEN).a("done.").toString();
    static final String failed_msg = ansi().fg(Color.RED).a("failed!").toString();
    public static void runProcess(RunTarget target, String msg) throws Throwable {
        boolean done = false;
        System.out.print(ansi().fg(Color.WHITE).a(msg));
        try {
            System.out.flush();
            target.run();
            done = true;
        } finally {
            System.out.println(ansi().a(" ").a(done? done_msg : failed_msg).reset());
        }
    }

    public static interface RunTarget {
        public void run() throws Throwable;
    }
    
}
