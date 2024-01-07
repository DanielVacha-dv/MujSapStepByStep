package com.sap.stepbystep.kmf.helpers;

import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.sap.stepbystep.kmf.app.KMFAppConstants;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class KMFHelperString {
    public static final String MIME_TYPE_UNKNOWN = "application/octet-stream";
    public static final String DATE_FORMAT_YMD_SAP = "yyyyMMdd";
    public static final String DATE_FORMAT_HMS_SAP = "HHmmss";
    public static final String DATE_FORMAT_DMY1 = "dd.MM.yyyy";
    public static final String DATE_FORMAT_DMYHM1 = "dd.MM.yyyy HH:mm";
    public static final String DATE_FORMAT_DMYHM2 = "dd.MM.yy HH:mm";
    public static final String DATE_FORMAT_DMYHMS1 = "dd.MM.yyyy HH:mm:ss";
    public static final String DATE_FORMAT_HMS1 = "'PT'HH'H'mm'M'ss'S'";
    public static final String DATE_FORMAT_HMS2 = "HH:mm:ss";
    public static final String DATE_FORMAT_HMS3 = "HHmmss";
    public static final String DATE_FORMAT_HMSS1 = "HH:mm:ss:SSS";
    public static final String DATE_FORMAT_DMHM1 = "dd.MM HH:mm";
    public static final String DATE_FORMAT_YMDHMS1 = "yyyyMMdd HHmmss";
    public static final String DATE_FORMAT_YMDHMS2 = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_FORMAT_YMDHMS3 = "yyyyMMddHHmmss";
    public static final String DATE_FORMAT_YMDHMSZ1 = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String DATE_FORMAT_YMDHMSM1 = "yyyyMMddHHmmssSSS";
    public static final String DATE_FORMAT_YMDHMSMZ1 = "yyyyMMddHHmmssSSSZ";
    public static final String DATE_FORMAT_DM1 = "dd.MM";
    public static final String DATE_FORMAT_HM1 = "HH:mm";
    public static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
    public static final String NUMBER_PATTERN_0DEC = "0";
    public static final String NUMBER_PATTERN_1DEC = "0.0";
    public static final String NUMBER_PATTERN_1DEC_3GRP = "#,##0.0";
    public static final String NUMBER_PATTERN_1DEC_MAX = "0.#";
    public static final String NUMBER_PATTERN_1DEC_MAX_3GRP = "#,##0.#";
    public static final String NUMBER_PATTERN_2DEC = "0.00";
    public static final String NUMBER_PATTERN_2DEC_3GRP = "#,##0.00";
    public static final String NUMBER_PATTERN_2DEC_MAX = "0.##";
    public static final String NUMBER_PATTERN_2DEC_MAX_3GRP = "#,##0.##";
    public static final String NUMBER_PATTERN_3DEC = "0.000";
    public static final String NUMBER_PATTERN_3DEC_3GRP = "#,##0.000";
    public static final String NUMBER_PATTERN_3DEC_MAX = "0.###";
    public static final String NUMBER_PATTERN_3DEC_MAX_3GRP = "#,##0.###";
    public static final String HASH_SHA1 = "SHA-1";
    public static final String CHARSET_ASCII = "ASCII";
    protected static String TAG = KMFHelperString.class.getName();

    /**
     * Bracket {@code value}.
     *
     * @param value value to bracket
     * @return String cocatenate {@link KMFAppConstants#BRACE_LEFT}{@code value} {@link KMFAppConstants#BRACE_RIGHT}.
     */
    public static String bracket(String value) {
        if (value == null)
            return null;

        return new String("").concat(KMFAppConstants.BRACKET_LEFT + value + KMFAppConstants.BRACKET_RIGHT);
    }

    /**
     * Concatenates strings with the specified separator.
     *
     * @param string1
     * @param separator
     * @param string2
     * @return
     */
    public static String concatStingsWithSeparator(String string1, String separator, String string2) {
        if (string1 == null)
            string1 = new String("");

        if (separator == null)
            separator = new String("");

        if (string2 == null)
            string2 = new String("");

        return string1.concat(separator).concat(string2);
    }

    /**
     * Concatenates strings with the specified separator and spaces.
     *
     * @param string1
     * @param separator
     * @param string2
     * @return string1 + space + separator + space + string2
     */
    public static String concatStingsWithSeparatorAndSpaces(String string1, String separator, String string2) {
        if (string1 == null)
            string1 = new String("");

        if (separator == null)
            separator = new String("");

        if (string2 == null)
            string2 = new String("");

        String space = KMFAppConstants.SPACE;

        return string1.concat(space).concat(separator).concat(space).concat(string2);
    }

    /**
     * Format {@code sqlDate} into string using format {@code simpleDateFormat}.
     *
     * @param sqlDate          date to format
     * @param simpleDateFormat date format
     * @return When {@code sqlDate} is {@code null} or {@code simpleDateFormat} is {@code null} return {@code null},
     * otherwise formated string.
     * @see java.text.SimpleDateFormat#format(java.util.Date)
     */
    public static String formatSqlDateToStringUsingFormat(java.sql.Date sqlDate, String simpleDateFormat) {
        if (sqlDate == null)
            return null;

        SimpleDateFormat formatOut = new SimpleDateFormat(simpleDateFormat);
        return formatOut.format(sqlDate);
    }

    /**
     * Format {@link String} date into string using formats.
     *
     * @param date                date to format
     * @param simpleDateFormatIn  format of {@code date}
     * @param simpleDateFormatOut format of output date
     * @return
     */
    public static String formatStringDateToStringUsingFormats(String date, String simpleDateFormatIn, String simpleDateFormatOut) {
        SimpleDateFormat formatIn = new SimpleDateFormat(simpleDateFormatIn);
        SimpleDateFormat formatOut = new SimpleDateFormat(simpleDateFormatOut);
        try {
            return formatOut.format(formatIn.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Format {@code utilDate} into string using format {@code simpleDateFormat}.
     *
     * @param utilDate         date to format
     * @param simpleDateFormat date format
     * @return When {@code utilDate} is {@code null} or {@code simpleDateFormat} is {@code null} return {@code null},
     * otherwise formated string.
     * @see java.text.SimpleDateFormat#format(java.util.Date)
     */
    public static String formatUtilDateToStringUsingFormat(java.util.Date utilDate, String simpleDateFormat) {
        if (utilDate == null || simpleDateFormat == null)
            return null;

        SimpleDateFormat formatOut = new SimpleDateFormat(simpleDateFormat);
        return formatOut.format(utilDate);
    }

    /**
     * Get file extensions from file filePath.
     *
     * @param filePath file path
     * @return
     */
    public static String getFileExtensionFromUrl(String filePath) {
        return MimeTypeMap.getFileExtensionFromUrl(filePath);
    }

    /**
     * Get mime type of extension.
     *
     * @param extension extension
     * @return
     */
    public static String getMimeType(String extension) {
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getMimeTypeFromExtension(extension);
    }

    /**
     * Remove leading zeros from number in string.
     *
     * @param number
     * @return
     */
    public static String removeLeadingZeros(String number) {
        return number.replaceFirst("^0+(?!$)", "");
    }

    /**
     * Convert hex to string.
     * <p/>
     * Note: UTF-8 equals SAP code 4110
     *
     * @param hexValue hex value
     * @return String from hex in UTF-8
     */
    public static String hexToString(String hexValue) {
        if (hexValue == null)
            return null;

        byte[] txtInByte = new byte[hexValue.length() / 2];
        int j = 0;
        for (int i = 0; i < hexValue.length(); i += 2) {
//            txtInByte[j++] = Byte.parseByte(hexValue.substring(i, i + 2), 16);
            txtInByte[j++] = (byte) Integer.parseInt(hexValue.substring(i, i + 2), 16);
        }

        return new String(txtInByte, Charset.forName("UTF-8"));
    }

    /**
     * Convert string to hex.
     *
     * @param stringValue string value
     * @return hex from string
     */
    public static String stringToHex(String stringValue) {
        if (stringValue == null)
            return null;

        byte[] bytes = stringValue.getBytes();
        char[] chars = new char[2 * bytes.length];
        for (int i = 0; i < bytes.length; ++i) {
            chars[2 * i] = HEX_CHARS[(bytes[i] & 0xF0) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[bytes[i] & 0x0F];
        }
        return new String(chars);
    }

    /**
     * Format number with pattern.
     *
     * @param number     number in string
     * @param patternOut pattern in string
     * @return formatted number
     */
    public static String formatStringNumberToStringUsingPattern(String number, String patternOut) {
        return formatStringNumberToStringUsingPattern(number, patternOut, null, null, null);
    }

    /**
     * Format number with pattern and unit.
     *
     * @param number     number in string
     * @param patternOut pattern in string
     * @param unit       unit in string
     * @return formatted number
     */
    public static String formatStringNumberToStringUsingPattern(String number, String patternOut, String unit) {
        return formatStringNumberToStringUsingPattern(number, patternOut, unit, null, null);
    }

    /**
     * Format number with pattern, unit, decimal separator and grouping separator
     *
     * @param number            number in string
     * @param patternOut        pattern in string
     * @param decimalSeparator  decimal separator in char
     * @param groupingSeparator grouping separator in char
     * @return formatted number in string
     */
    public static String formatStringNumberToStringUsingPattern(String number, String patternOut, String unit, Character decimalSeparator, Character groupingSeparator) {
        if (number == null)
            return null;

        Double doublePrice;
        String numberInFormat;
        try {
            doublePrice = Double.parseDouble(number);
        } catch (NumberFormatException nfe) {
            Log.e(
                    TAG
                    , "Number can not be parsed as double."
                    , nfe
            );
            return null;
        }

        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance();
        DecimalFormatSymbols decimalFormatSymbols = decimalFormat.getDecimalFormatSymbols();

        if (patternOut == null)
            patternOut = decimalFormat.toPattern();

        decimalFormat.applyPattern(patternOut);

        if (decimalSeparator != null)
            decimalFormatSymbols.setDecimalSeparator(decimalSeparator);

        if (groupingSeparator != null)
            decimalFormatSymbols.setGroupingSeparator(groupingSeparator);

        decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);

        numberInFormat = (decimalFormat.format(doublePrice)).toString();

        return (unit != null) ? concatStingsWithSeparator(numberInFormat, KMFAppConstants.SPACE, unit) : numberInFormat;
    }

    /**
     * Get readable file size.
     *
     * @param size size of file
     * @return readable file size with unit
     */
    public static String getReadableFileSize(int size) {
        return getReadableFileSize(Long.valueOf(size));
    }

    /**
     * Get readable file size.
     *
     * @param size size of file
     * @return readable file size with unit
     */
    public static String getReadableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Get roman numeral.
     *
     * @param number number
     * @return {@link String}
     */
    public static String getRomanNumeral(int number) {
        return getRomanNumeral(Long.valueOf(number));
    }

    /**
     * Get roman numeral.
     *
     * @param number number
     * @return {@link String}
     */
    public static String getRomanNumeral(long number) {
        if (number <= 0)
            return null;

        StringBuilder romanNumeral = new StringBuilder();

        final RomanNumeral[] values = RomanNumeral.values();
        for (int i = values.length - 1; i >= 0; i--) {
            while (number >= values[i].weight) {
                romanNumeral.append(values[i]);
                number -= values[i].weight;
            }
        }
        return romanNumeral.toString();
    }

    /**
     * Get SHA-1 hash.
     *
     * @param value value to hash
     * @return {@link String}
     */
    public static String getHashSHA1(String value) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance(HASH_SHA1);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "KMFHelperString.getHashSHA1()", e);
            return null;
        }
        try {
            messageDigest.update(value.getBytes(CHARSET_ASCII));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "KMFHelperString.getHashSHA1()", e);
            return null;
        }

        byte[] data = messageDigest.digest();
        return convertToHex(data);
    }

    /**
     * Convert to hex.
     *
     * @param data data
     * @return {@link String}
     */
    public static String convertToHex(byte[] data) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(
                Base64.encodeToString(data, Base64.NO_WRAP)
        );
        return stringBuffer.toString();
    }

    enum RomanNumeral {
        I(1), IV(4), V(5), IX(9), X(10), XL(40), L(50), XC(90), C(100), CD(400), D(500), CM(900), M(1000);
        int weight;

        RomanNumeral(int weight) {
            this.weight = weight;
        }
    }
}
