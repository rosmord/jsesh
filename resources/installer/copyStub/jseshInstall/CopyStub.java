/*
 * Created on 8 dec. 2004
 * S. Rosmorduc
 * */
package jseshInstall;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility for mac installation. copy
 * /System/Library/Frameworks/JavaVM.framework/Resources/MacOS/JavaApplicationStub
 * to the right place.
 */
public class CopyStub {

    /**
     * Position of the JavaApplicationStub on the mac disk.
     * <p>This file should be copied in any java mac application.
     * It can't be distributed, as it can change with jvm versions.
     * 
     * <p> So, basically, we put a dummy file instead in our mac distribution,
     * and at install time, we copy the real file at the proper place.
     */
    static final String STUB = "/System/Library/Frameworks/JavaVM.framework/Resources/MacOS/JavaApplicationStub";

    public static void main(String[] args) throws IOException {
        File base = new File(args[0]);
        File stub = new File(STUB);
        // The mac application base file
        File mac = new File(new File(base, "JSesh.app"), "Contents");
        File maclibs = new File(new File(mac, "Resources"), "Java");
        File subTarget = new File(mac, "MacOS");
        // move the jar files :

        // Copy the stub
        if (stub.exists()) {
            System.out.println("Stub exists, moving data to mac application");
            File libs = new File(base, "libs");
            File[] jars = libs.listFiles();

            boolean success = true;
            for (int i = 0; success && i < jars.length; i++) {
                success = success
                        && jars[i]
                                .renameTo(new File(maclibs, jars[i].getName()));
            }

            if (success) {
                libs.delete();
            }

            // As far as I know, Java hasn't got
            // any command to copy a file "as is"
            // Hence, we copy the file bytewise
            File target = new File(subTarget, "JavaApplicationStub");
            OutputStream out = new FileOutputStream(target);
            InputStream in = new FileInputStream(stub);
            int c;
            while ((c = in.read()) != -1) {
                out.write(c);
            }
            in.close();
            out.close();
            // Now, Java hasn't got any real chmod command either
            // I know this is platform specific, but it would have been nice
            // anyway
            String chmod[] = new String[3];
            chmod[0] = "/bin/chmod";
            chmod[1] = "a+rx";
            chmod[2] = target.getCanonicalPath();
            Runtime.getRuntime().exec(chmod);
        } else {
            System.out.println("Stub does not exist");
        }
    }
}