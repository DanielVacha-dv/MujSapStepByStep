package com.sap.stepbystep.kmf.helpers;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class KMFHelperRuntime {

    /**
     * Exec {@code logcat -v threadtime -d  -f} into specified file {@code fileLogcat}.
     *
     * @param fileLogcat logcat file
     * @return {@link java.io.File}
     */
    public static File execLogcat(File fileLogcat) {
        if (fileLogcat == null)
            return null;

        try {
            Process process = Runtime.getRuntime().exec("/system/bin/sh -");

            DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream());

            dataOutputStream.writeBytes(
                    "logcat -v threadtime -d  -f "
                            .concat(fileLogcat.getAbsolutePath())
            );

            dataOutputStream.flush();
            dataOutputStream.close();
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        return fileLogcat;
    }
}

