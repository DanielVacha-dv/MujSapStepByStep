package com.sap.stepbystep.kmf.odata;

import com.sap.stepbystep.kmf.app.KMFAppConstants;
import com.sap.stepbystep.kmf.helpers.KMFHelperString;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class KMFODataRequestFilter {
    //koncovy filtr
    private String mPropertyName;
    private String mPropertyValue;
    private String mOperator;

    //filtr pro rekurzi
    private KMFODataRequestFilter mSubFilterA;
    private KMFODataRequestFilter mSubFilterB;
    private boolean mGroup = false;
    private boolean mUseSingleQuotation = true;
    private String mSubfilterOperator;

    public KMFODataRequestFilter(String propertyName, String operator, String propertyValue) {
        this(propertyName, operator, propertyValue, true);
    }

    public KMFODataRequestFilter(String propertyName, String operator, boolean propertyValue) {
        this(propertyName, operator, String.valueOf(propertyValue), false);
    }

    public KMFODataRequestFilter(String propertyName, String operator, String propertyValue, boolean useSingleQuotation) {
        this.mPropertyName = propertyName;
        this.mPropertyValue = propertyValue;
        this.mOperator = operator;
        this.mUseSingleQuotation = useSingleQuotation;
    }

    public KMFODataRequestFilter(KMFODataRequestFilter filterA, String subfilterOperator, KMFODataRequestFilter filterB, boolean group) {
        this.mSubFilterA = filterA;
        this.mSubFilterB = filterB;
        this.mSubfilterOperator = subfilterOperator;
        this.mGroup = group;
    }

    public KMFODataRequestFilter addFilter(String subfilterOperator, String propertyName, String operator, String propertyValue, boolean group) {
        //sebe dam do A a do B dam vstup
        return addFilter(subfilterOperator, propertyName, operator, propertyValue, group, true);
    }

    public KMFODataRequestFilter addFilter(String subfilterOperator, String propertyName, String operator, boolean propertyValue, boolean group) {
        //sebe dam do A a do B dam vstup
        return addFilter(subfilterOperator, propertyName, operator, String.valueOf(propertyValue), group, false);
    }

    public KMFODataRequestFilter addFilter(String subfilterOperator, String propertyName, String operator, String propertyValue, boolean group, boolean useSingleQuotation) {
        //sebe dam do A a do B dam vstup
        return new KMFODataRequestFilter(this, subfilterOperator, new KMFODataRequestFilter(propertyName, operator, propertyValue, useSingleQuotation), group);
    }

    public KMFODataRequestFilter addFilter(String subfilterOperator, KMFODataRequestFilter subFilter, boolean group) {
        //sebe dam do A a do B dam vstup
        return new KMFODataRequestFilter(this, subfilterOperator, subFilter, group);
    }

    public String generateFilterString() {
        String filter = "";
        StringBuilder sb = new StringBuilder();
        if (mPropertyName != null && mPropertyValue != null && mOperator != null) {
            sb.
                    append(mPropertyName)
                    .append(KMFAppConstants.HTML_SPACE)
                    .append(mOperator)
                    .append(KMFAppConstants.HTML_SPACE)
                    .append(mUseSingleQuotation ? KMFAppConstants.HTML_SINGLE_QUOTATION_MARK : "");
            try {
                sb.append(URLEncoder.encode(mPropertyValue, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                sb.append(mPropertyValue);
            }

            filter = sb.append(mUseSingleQuotation ? KMFAppConstants.HTML_SINGLE_QUOTATION_MARK : "")
                    .toString();

        } else if (mSubFilterA != null && mSubFilterB != null && mSubfilterOperator != null) {
            String subfiltr = new StringBuilder()
                    .append(mSubFilterA.generateFilterString())
                    .append(KMFAppConstants.HTML_SPACE)
                    .append(mSubfilterOperator)
                    .append(KMFAppConstants.HTML_SPACE)
                    .append(mSubFilterB.generateFilterString())
                    .toString();

            //group pridam zavorky
            if (mGroup) {
                filter = KMFHelperString.bracket(subfiltr);
            } else {
                filter = subfiltr;
            }
        }
        return filter;
    }

    public String generateFilterString4Request() {
        return KMFODataConstants.FILTER + KMFHelperString.bracket(generateFilterString());
    }
}