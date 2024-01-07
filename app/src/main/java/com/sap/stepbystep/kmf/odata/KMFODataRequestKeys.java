package com.sap.stepbystep.kmf.odata;

import com.sap.stepbystep.kmf.app.KMFAppConstants;
import com.sap.stepbystep.kmf.helpers.KMFHelperString;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class KMFODataRequestKeys {

    private HashMap<String, String> mRequestKeys = new HashMap<String, String>();

    public KMFODataRequestKeys() {
    }

    public KMFODataRequestKeys(String key, String value) {
        mRequestKeys.put(key, value);
    }

    public KMFODataRequestKeys(KMFODataEntity entity) {
        for (String key : entity.getKeyPropertyNames()) {
            mRequestKeys.put(key, entity.getEntryPropertyValue(key));
        }
    }

    /**
     * Add key into keys.
     *
     * @param key
     * @param value
     * @return
     */
    public KMFODataRequestKeys addKey(String key, String value) {
        mRequestKeys.put(key, value);
        return this;
    }

    /**
     * Generate keys string depending whether keys are used collection or import function:
     * <ul>
     * <li>collection -> "DOC_ID='1',DOC_TYPE='KMF'"</li>
     * <li>import function -> "DOC_ID='1'&DOC_TYPE='KMF'"</li>
     * </ul>
     *
     * @param isImportFunction indicate whether generate keys for import function or collection
     * @return
     */
    public String generateKeysString(boolean isImportFunction) {
        if (mRequestKeys.isEmpty()) {
            return null;
        }

        String separator;
        if (isImportFunction) {
            separator = KMFAppConstants.AMPERSAND;
        } else {
            separator = KMFAppConstants.COMMA;
        }

        StringBuilder keysString = new StringBuilder();
        for (Map.Entry<String, String> key : mRequestKeys.entrySet()) {
            if (keysString.length() == 0) {
                keysString = new StringBuilder();
            } else {
                keysString.append(separator);
            }

            keysString
                    .append(key.getKey())
                    .append(KMFAppConstants.EQUATE)
                    .append(KMFAppConstants.SINGLE_QUOTATION_MARK);
            try {
                keysString.append(URLEncoder.encode(key.getValue(), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                keysString.append(key.getValue());
            }
            keysString.append(KMFAppConstants.SINGLE_QUOTATION_MARK);
        }
        return keysString.toString();
    }

    /**
     * Generate keys for request URL depending whether keys are used collection or import function:
     * <ul>
     * <li>collection -> "DOC_ID='1',DOC_TYPE='KMF'"</li>
     * <li>import function -> "DOC_ID='1'&DOC_TYPE='KMF'"</li>
     * </ul>
     *
     * @param isImportFunction indicate whether generate keys for import function or collection
     * @return
     */
    public String generateKeysString4Request(boolean isImportFunction) {
        if (isImportFunction) {
            return KMFAppConstants.QUESTION_MARK + generateKeysString(isImportFunction);
        } else {
            return KMFHelperString.bracket(generateKeysString(isImportFunction));
        }
    }
}