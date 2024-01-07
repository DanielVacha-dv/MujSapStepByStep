package com.sap.stepbystep.kmf.helpers;

import java.util.Calendar;
import java.util.Date;

public class KMFHelperUtilDate {
    protected static String TAG = KMFHelperUtilDate.class.getName();

    /**
     * Get time. If time in millis is not set return current time.
     *
     * @param timeInMillis time in millis
     * @return {@link java.util.Calendar#getTime()}
     */
    public static Date getTime(Long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        if (timeInMillis != null)
            calendar.setTimeInMillis(timeInMillis);
        return calendar.getTime();
    }
}
