package com.sap.stepbystep.kmf.app;

import com.sap.stepbystep.kmf.data.KMFConfigOData;
import com.sap.stepbystep.kmf.data.KMFConfigPreferences;
import com.sap.stepbystep.kmf.data.KMFConfigSettings;
//import com.sap.stepbystep.kmf.services.KMFGcmReceiver;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class KMFXMLParser4Config {

    public static final String PREFERENCES = "PREFERENCES";
    public static final String ODATA = "ODATA";
    public static final String SETTINGS = "SETTINGS";

    private static final String USE_SMP = "USE_SMP";
    private static final String USE_TECHNICAL_USER = "USE_TECHNICAL_USER";
    private static final String USE_ONLINE_STORE = "USE_ONLINE_STORE";
    private static final String USE_JSON_FORMAT = "USE_JSON_FORMAT";
    private static final String USE_PAGING = "USE_PAGING";
    private static final String PAGING_SIZE = "PAGING_SIZE";
    private static final String PAGING_LIST_SIZE = "PAGING_LIST_SIZE";
    private static final String DEEP_INSERT_ENTITIES_COUNT = "DEEP_INSERT_ENTITIES_COUNT";
    private static final String MAX_THREADS = "MAX_THREADS";
    private static final String GCM_SENDER_ID = "GCM_SENDER_ID";
    private static final String GCM_LISTENER = "GCM_LISTENER";
    private static final String CHECK_NETWORK_BEFORE_MAKE_REQUEST = "CHECK_NETWORK_BEFORE_MAKE_REQUEST";
    private static final String PERSISTENCE_SECUREMODE = "PERSISTENCE_SECUREMODE";
    private static final String CONNECTIVITY_HTTP_PORT = "CONNECTIVITY_HTTP_PORT";
    private static final String CONNECTIVITY_CONNTIMEOUT = "CONNECTIVITY_CONNECTION_TIMEOUT";
    private static final String CONNECTIVITY_SCONNTIMEOUT = "CONNECTIVITY_SOCKET_CONNECTION_TIMEOUT";
    private static final String LOG_LEVEL = "LOG_LEVEL";

    public HashMap<String, Object> mKMFDataConfig;

    public KMFXMLParser4Config(InputStream inputStream) {
        parseInputStream(inputStream);
    }

    /**
     * Parse input stream and save data into mKMFDataConfig.
     *
     * @param inputStream
     */
    private void parseInputStream(InputStream inputStream) {
        XmlPullParserFactory factory = null;
        XmlPullParser parser = null;

        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            parser = factory.newPullParser();
            parser.setInput(inputStream, null);

            String text = null;
            mKMFDataConfig = new HashMap<String, Object>();
            KMFConfigPreferences preferences = null;
            KMFConfigOData odata = null;
            KMFConfigSettings settings = null;

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagname = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagname.equalsIgnoreCase(PREFERENCES)) {
                            preferences = new KMFConfigPreferences();
                        } else if (tagname.equalsIgnoreCase(ODATA)) {
                            odata = new KMFConfigOData();
                        } else if (tagname.equalsIgnoreCase(SETTINGS)) {
                            settings = new KMFConfigSettings();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        //PREFERENCES
                        if (tagname.equalsIgnoreCase(PERSISTENCE_SECUREMODE)) {
                            preferences.setPersistenceSecureMode(Boolean.parseBoolean(text));
                        } else if (tagname.equalsIgnoreCase(CONNECTIVITY_HTTP_PORT)) {
                            preferences.setConnectivityHttpPort(Integer.parseInt(text));
                        } else if (tagname.equalsIgnoreCase(CONNECTIVITY_CONNTIMEOUT)) {
                            preferences.setConnectivityConnTimeOut(Integer.parseInt(text));
                        } else if (tagname.equalsIgnoreCase(CONNECTIVITY_SCONNTIMEOUT)) {
                            preferences.setConnectivitySconnTimeOut(Integer.parseInt(text));
                        } else if (tagname.equalsIgnoreCase(LOG_LEVEL)) {
                            preferences.setLogLevel(Integer.parseInt(text));
//                        } else if (tagname.equalsIgnoreCase(GCM_SENDER_ID)) {
//                            KMFGcmReceiver.setSenderId(text);
//                        } else if (tagname.equalsIgnoreCase(GCM_LISTENER)) {
//                            KMFGcmReceiver.addListener(text);
                        } else if (tagname.equalsIgnoreCase(PREFERENCES)) {
                            mKMFDataConfig.put(PREFERENCES, preferences);
                        }

                        //ODATA
                        else if (tagname.equalsIgnoreCase(USE_SMP)) {
                            odata.setUseSMP(Boolean.parseBoolean(text));
                        } else if (tagname.equalsIgnoreCase(USE_ONLINE_STORE)) {
                            odata.setUseOnlineStore(Boolean.parseBoolean(text));
                        } else if (tagname.equalsIgnoreCase(USE_JSON_FORMAT)) {
                            odata.setUseJsonFormat(Boolean.parseBoolean(text));
                        } else if (tagname.equalsIgnoreCase(USE_PAGING)) {
                            odata.setUsePaging(Boolean.parseBoolean(text));
                        } else if (tagname.equalsIgnoreCase(PAGING_SIZE)) {
                            odata.setPagingSize(Integer.parseInt(text));
                        } else if (tagname.equalsIgnoreCase(PAGING_LIST_SIZE)) {
                            odata.setPagingListSize(Integer.parseInt(text));
                        } else if (tagname.equalsIgnoreCase(DEEP_INSERT_ENTITIES_COUNT)) {
                            odata.setDeepInsertEntitiesCount(Integer.parseInt(text));
                        } else if (tagname.equalsIgnoreCase(MAX_THREADS)) {
                            odata.setMaxThreads(Integer.parseInt(text));
                        } else if (tagname.equalsIgnoreCase(ODATA)) {
                            mKMFDataConfig.put(ODATA, odata);
                        }

                        //SETTINGS
                        else if (tagname.equalsIgnoreCase(CHECK_NETWORK_BEFORE_MAKE_REQUEST)) {
                            settings.setCheckNetworkBeforeMakeRequest(Boolean.parseBoolean(text));
                        } else if (tagname.equalsIgnoreCase(USE_TECHNICAL_USER)) {
                            settings.setUseTechnicalUser(Boolean.parseBoolean(text));
                        } else if (tagname.equalsIgnoreCase(SETTINGS)) {
                            mKMFDataConfig.put(SETTINGS, settings);
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return configuration for oData.
     *
     * @return {@link KMFConfigPreferences}
     */
    public KMFConfigPreferences getConfigPreferences() {
        return (KMFConfigPreferences) mKMFDataConfig.get(PREFERENCES);
    }

    /**
     * Return configuration for OData.
     *
     * @return {@link KMFConfigOData}
     */
    public KMFConfigOData getConfigOData() {
        return (KMFConfigOData) mKMFDataConfig.get(ODATA);
    }

    /**
     * Return configuration for Settings.
     *
     * @return {@link KMFConfigSettings}
     */
    public KMFConfigSettings getConfigSettings() {
        return (KMFConfigSettings) mKMFDataConfig.get(SETTINGS);
    }
}

