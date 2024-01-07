package com.sap.stepbystep.kmf.helpers;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class KMFHelperRootDetection {

    public static final String TAG = KMFHelperRootDetection.class.getName();
    public static final String RELEASE_KEYS = "release-keys";
    public static final String DEV_KEYS = "dev-keys";

    /**
     * Check whether device is rooted.
     *
     * @param context application context
     * @return {@code true} when device is rooted otherwise {@code false}
     */
    public static boolean isDeviceRooted(Context context) {
        return !containsBuildPropReleaseKeys()
                || !existsOTACerts()
                || existsSuperuserApk()
                || containsRootedAPKs(context)
                || existsSUBinaries()
                || executeCommandSU();
    }

    /**
     * Check for test-keys.
     * Check to see if build.prop includes the line ro.build.tags="release-keys" indicating a
     * developer build or unofficial ROM.
     *
     * @return {@code false} when device is rooted otherwise {@code true}
     */
    private static boolean containsBuildPropReleaseKeys() {
        String buildTags = Build.TAGS;
        if ((buildTags != null) && (buildTags.contains(RELEASE_KEYS) || buildTags.contains(DEV_KEYS))) {
            Log.i(
                    TAG
                    , "\"Buil.prop\" contains \"" + RELEASE_KEYS + "\" or \"" + DEV_KEYS + "\". The device is not rooted."
            );
            return true;
        } else {
            Log.e(
                    TAG
                    , "\"Buil.prop\" not contains \"" + RELEASE_KEYS + "\" or \"" + DEV_KEYS + "\". The device is rooted!"
            );
            return false;
        }
    }

    /**
     * Check for OTA certificates.
     * Check to see if the file "/etc/security/otacerts.zip" exists.
     *
     * @return {@code false} when device is rooted otherwise {@code true}
     */
    private static boolean existsOTACerts() {
        String path = "/etc/security/otacerts.zip";
        File fileOTACerts = new File(path);
        if (fileOTACerts.exists()) {
            Log.i(TAG, "File \"" + path + "\" exists. The device is not rooted.");
            return true;
        } else {
            Log.e(TAG, "File \"" + path + "\" not exists. The device is rooted!");
            return false;
        }
    }

    /**
     * Check for "Superuser.apk" app.
     * Check to see if the file "/system/app/Superuser.apk" exists.
     *
     * @return {@code true} when device is rooted otherwise {@code false}
     */
    private static boolean existsSuperuserApk() {
        String path = "/system/app/Superuser.apk";
        File fileSuperuser = new File(path);
        if (fileSuperuser.exists()) {
            Log.e(TAG, "File \"" + path + "\" exists. The device is rooted!");
            return true;
        } else {
            Log.i(TAG, "File \" + path + \" not exists. The device is not rooted.");
            return false;
        }
    }

    /**
     * Check for several known rooted apk's:
     * <ul>
     * <li>"com.noshufou.android.su"</li>
     * <li>"com.thirdparty.superuser"</li>
     * <li>"eu.chainfire.supersu"</li>
     * <li>"com.koushikdutta.superuser"</li>
     * <li>"com.zachspong.temprootremovejb"</li>
     * <li>"com.ramdroid.appquarantine"</li>
     * </ul>
     *
     * @param context application context
     * @return @return {@code true} when device is rooted otherwise {@code false}
     */
    private static boolean containsRootedAPKs(Context context) {
        Set<String> rootedAPKs = new HashSet<String>();
        rootedAPKs.add("com.noshufou.android.su");
        rootedAPKs.add("com.thirdparty.superuser");
        rootedAPKs.add("eu.chainfire.supersu");
        rootedAPKs.add("com.koushikdutta.superuser");
        rootedAPKs.add("com.zachspong.temprootremovejb");
        rootedAPKs.add("com.ramdroid.appquarantine");

        for (ApplicationInfo applicationInfo : context.getPackageManager().getInstalledApplications(0)) {
            if (rootedAPKs.contains(applicationInfo.packageName)) {
                Log.e(TAG, "The device contain rooted apk \"" + applicationInfo.packageName + "\". The device is rooted!");
                return true;
            }
        }

        Log.i(TAG, "The device do not contains any of rooted apk's (" + rootedAPKs.toString() + "). The device is not rooted.");
        return false;
    }

    /**
     * Check for SU binaries:
     * <ul>
     * <li>"/system/bin/su"</li>
     * <li>"/system/xbin/su"</li>
     * <li>"/sbin/su"</li>
     * <li>"/system/su"</li>
     * <li>"/system/bin/.ext/.su"</li>
     * <li>"/system/usr/we-need-root/su-backup"</li>
     * </ul>
     *
     * @return @return {@code true} when device is rooted otherwise {@code false}
     */
    private static boolean existsSUBinaries() {
        ArrayList<String> suBinaries = new ArrayList<String>();
        suBinaries.add("/system/bin/su");
        suBinaries.add("/system/xbin/su");
        suBinaries.add("/sbin/su");
        suBinaries.add("/system/su");
        suBinaries.add("/system/bin/.ext/.su");
        suBinaries.add("/system/usr/we-need-root/su-backup");
        suBinaries.add("/system/xbin/mu");

        for (String suBinary : suBinaries) {
            if ((new File(suBinary).exists())) {
                Log.e(TAG, "Su binary \"" + suBinary + "\" exists. The device is rooted!");
                return true;
            }
        }

        Log.i(TAG, "The device do not contains any of su binaries (" + suBinaries.toString() + "). The device is not rooted.");
        return false;
    }

    /**
     * Attempt SU command directly.
     * Attempt the to run the command su and check the id of the current user, if it returns 0 then the su command has been successful.
     *
     * @return @return {@code true} when device is rooted otherwise {@code false}
     */
    private static boolean executeCommandSU() {
        try {
            Process process = Runtime.getRuntime().exec("su -c id");
            process.waitFor();

            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String result = bufferedReader.readLine();
            if (result != null && result.contains("uid=0(root)")) {
                Log.e(TAG, "Executing command \"su -c id\" with result: \"" + result + "\". The device is rooted!");
                return true;
            } else {
                Log.i(TAG, "Executing command \"su -c id\" with result: \"null\". The device is not rooted.");
                return false;
            }
        } catch (IOException e) {
            Log.i(TAG, "Executing command \"su -c id\" fail. The device is not rooted.", e);
            return false;
        } catch (InterruptedException e) {
            Log.i(TAG, "Executing command \"su -c id\" fail. The device is not rooted.", e);
            return false;
        }
    }
}

