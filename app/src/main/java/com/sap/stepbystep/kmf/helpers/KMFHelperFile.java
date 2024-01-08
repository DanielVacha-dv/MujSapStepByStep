package com.sap.stepbystep.kmf.helpers;


import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Static methods returning {@link java.io.File}:
 * <ul>
 * <li>{@link KMFHelperFile#copy(java.io.File, java.io.File)}</li>
 * </ul>
 *
 * @version 0.1
 */
public class KMFHelperFile {

    public static final String TAG = KMFHelperFile.class.getName();

    /**
     * Copy file from {@code source} to {@code destination}.
     *
     * @param source      source
     * @param destination destination
     * @return {@link java.io.File}
     */
    public static File copy(File source, File destination) {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(destination);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception e) {
            Log.e(TAG, "copyFile(File source, File destination)", e);

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            return null;
        }

        return destination;
    }

    /**
     * Get mime type.
     *
     * @param file file
     * @return {@link String}
     */
    public static String getMimeType(File file) {
        String mimeType = KMFHelperString
                .getMimeType(
                        file.getName().substring(file.getName().lastIndexOf("."))
                );
        if (mimeType == null || mimeType.isEmpty()) {
            return KMFHelperString.MIME_TYPE_UNKNOWN;
        } else {
            return mimeType;
        }
    }
}

