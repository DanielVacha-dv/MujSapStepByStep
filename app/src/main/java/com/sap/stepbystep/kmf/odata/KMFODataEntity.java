package com.sap.stepbystep.kmf.odata;

import android.util.Log;

import com.sap.mobile.lib.parser.IODataCollection;
import com.sap.mobile.lib.parser.IODataComplexType;
import com.sap.mobile.lib.parser.IODataEntityType;
import com.sap.mobile.lib.parser.IODataEntry;
import com.sap.mobile.lib.parser.IODataFunctionImport;
import com.sap.mobile.lib.parser.IODataProperty;
import com.sap.mobile.lib.parser.IParserDocument;
import com.sap.mobile.lib.parser.ODataEntry;
import com.sap.mobile.lib.parser.ODataLink;
import com.sap.stepbystep.kmf.helpers.KMFHelperString;
import com.sap.stepbystep.kmf.helpers.KMFHelperUtilDate;

import java.util.List;

public class KMFODataEntity
        implements Cloneable {

    protected static String TAG = KMFODataEntity.class.getName();

    protected String mName;

    protected IODataEntityType mEntityType;
    protected String mCollectionId;
    protected List<String> mKeyPropertyNames;

    protected IODataFunctionImport mFunctionImport;
    protected String mReturnType;
    protected String mEntitySet;

    protected IODataComplexType mComplexType;

    protected IODataEntry mEntry = new ODataEntry();

    public KMFODataEntity(String name) {
        constructor(name);
    }

    protected KMFODataEntity() {

    }

    protected void constructor(String name) {
        mName = name;

        if (KMFApplication.getMetaDocument() == null) {
            Log.d(TAG, "KMFApplication.getMetaDocument() is null");
            return;
        }

        if ((mEntityType = KMFApplication.getMetaDocument().getEntityType(mName)) != null) {
            mKeyPropertyNames = KMFApplication.getMetaDocument().getEntityType(mName).getKeyPropertyNames();
            setCollectionId(mName); // Collection ID is set if meta data countains entity with coresponding name.
        } else
            setFunctionImport(mName); // Function import is set if meta data do not countains entity with coresponding name.
    }

    @Override
    public KMFODataEntity clone() throws CloneNotSupportedException {
        return (KMFODataEntity) super.clone();
    }

    /**
     * Get entity name({@link KMFODataEntity#mName}).
     *
     * @return Name of entity.
     */
    public String getName() {
        return mName;
    }

    /**
     * Get collection ID({@link KMFODataEntity#mCollectionId})
     *
     * @return Return entity collection ID.
     */
    public String getCollectionId() {
        return mCollectionId;
    }

    /**
     * Set collection ID from service document using name (member title).
     * Collection ID is used for:
     * - creating request, f.e. URL ".../service/DOCSet", where "DOCSet" is collection ID,
     * using method GET returns all data for documents
     * - parsing returned data from server ({@link com.sap.mobile.lib.parser.Parser} and parseODataFeed)
     *
     * @param name
     */
    public void setCollectionId(String name) {
        for (IODataCollection collection : KMFApplication.getServiceDocument().getAllCollections()) {
            if (collection.getMemberTitle().equals(name)) {
                mCollectionId = collection.getId();
                break;
            }
        }
    }

    /**
     * Get key property names.
     *
     * @return return list with key property names.
     */
    public List<String> getKeyPropertyNames() {
        return mKeyPropertyNames;
    }

    /**
     * Get function import({@link IODataFunctionImport}).
     *
     * @return Function import if entity is function, otherwise {@code null}.
     */
    public IODataFunctionImport getFunctionImport() {
        return mFunctionImport;
    }

    /**
     * Get function import name.
     *
     * @return name of function import.
     */
    public String getFunctionImportName() {
        return mFunctionImport.getName();
    }

    /**
     * Get entity set({@link KMFODataEntity#mEntitySet})
     *
     * @return {@link KMFODataEntity#mEntitySet} if entity is function and
     * entity set({@link com.sap.mobile.lib.parser.IODataFunctionImport#getEntitySet()})
     * is not {@code null}, otherwise entity is complex type and return {@code null}.
     */
    public String getEntitySet() {
        return mEntitySet;
    }

    /**
     * Get complex type({@link IODataComplexType}).
     *
     * @return {@link IODataComplexType} if entity is function and
     * entity set({@link com.sap.mobile.lib.parser.IODataFunctionImport#getEntitySet()})
     * is {@code null}, otherwise entity is not complex type and return {@code null}.
     */
    public IODataComplexType getComplexType() {
        return mComplexType;
    }

    /**
     * Check if entity is function import.
     *
     * @return
     */
    public boolean isFunctionImport() {
        return mFunctionImport != null;
    }

    /**
     * Set function import from meta data using name (function import name).
     * Function import can return data as collection or complex type and is used for:
     * - creating request, f.e. URL ".../service/CONVERT?MATERIAL='1'&QUANTITY='2'&QUNIT_IN='KS'&QUNIT_OUT='KG'"
     * , where "CONVERT" is function import name, convert material from KS to KG
     * - parsing returned data from server
     * When function import have set entity set:
     * - parsing data is same as data for collection (collection ID = entity set)
     * ({@link com.sap.mobile.lib.parser.Parser} and parseODataFeed)
     * - otherwise data is parsed using this function import and returned as complex type
     * ({@link com.sap.mobile.lib.parser.Parser} and parseFunctionImportResult)
     *
     * @param name
     */
    protected void setFunctionImport(String name) {
        for (IODataFunctionImport functionImport : KMFApplication.getMetaDocument().getFunctionImports()) {
            if (functionImport.getName().equals(name)) {
                mFunctionImport = functionImport;
                mEntitySet = functionImport.getEntitySet();
                setComplexType(mReturnType = functionImport.getReturnType());
            }
        }
    }

    /**
     * Check if entity is function import and return complex type.
     *
     * @return
     */
    public boolean isComplexType() {
        return mComplexType != null;
    }

    /**
     * Set complex type for return type. When function import return complex type, data returned
     * from server must be parsed using import function ({@link com.sap.mobile.lib.parser.Parser} and parseFunctionImportResult).
     *
     * @param returnType function import return type({@link com.sap.mobile.lib.parser.IODataFunctionImport#getReturnType()})
     */
    protected void setComplexType(String returnType) {
        for (IODataComplexType complexType : KMFApplication.getMetaDocument().getComplexTypes()) {
            if (returnType.contains(complexType.getName()))
                mComplexType = complexType;
        }
    }

    /**
     * Check whether entity ID and colection ID is set.
     *
     * @return
     */
    public boolean isEntryValid() {
        return getEntryCollectionId() != null && getEntryId() != null;
    }

    /**
     * Return entry data.
     *
     * @return
     */
    public IODataEntry getEntry() {
        return mEntry;
    }

    /**
     * Set entry data.
     *
     * @param entry
     */
    public void setEntry(IODataEntry entry) {
        mEntry = entry;
    }

    /**
     * Return entry id.
     *
     * @return
     */
    public String getEntryId() {
        return mEntry.getId();
    }

    /**
     * Set entry id.
     *
     * @param id
     */
    public void setEntryId(String id) {
        mEntry.putId(id);
    }

    /**
     * TODO
     */
    public void setEntryId() {
        //TODO - tady uz lze vygenerovat, klicove property jsou ulozene v mKeyPropertyNames
        // getKeyPropertyNames() vraci uz seznam klicu
        // FIXME - prozatim resim tak, ze vezmu entry collection id + entry propery ID
        mEntry.putId(getEntryCollectionId() + getEntryPropertyValue("ID"));
    }

    /**
     * Return entry collection ID.
     *
     * @return entry collection ID
     */
    public String getEntryCollectionId() {
        return mEntry.getCollectionId();
    }

    /**
     * Set entry {@code collectionId}.
     *
     * @param collectionId entry collection ID
     */
    public void setEntryCollectionId(String collectionId) {
        mEntry.setCollectionId(collectionId);
    }

    /**
     * Return entry title.
     *
     * @return entry title
     */
    public String getEntryTitle() {
        return mEntry.getTitle();
    }

    /**
     * Set entry title.
     *
     * @param title entry title
     */
    public void setEntryTitle(String title) {
        mEntry.setTitle(title);
    }

    /**
     * Return entry value for defined property.
     *
     * @param property entry property
     * @return
     */
    public String getEntryPropertyValue(String property) {
        return mEntry.getPropertyValue(property);
    }

    /**
     * Return entry value for defined property with replacing null.
     *
     * @param property entry property
     * @return
     */
    public String getEntryPropertyValueReplaceNull(String property) {
        String propertyValue = mEntry.getPropertyValue(property);

        if (propertyValue == null)
            return new String("");
        else
            return propertyValue;
    }

    /**
     * Put into entry property and value using {@link com.sap.mobile.lib.parser.IODataEntry#putPropertyValue(String, String)}.
     *
     * @param property entry property({})nazev  popois  server
     * @param value    entry value
     */
    public void setEntryProperty(String property, String value) {
        mEntry.putPropertyValue(property, value);
    }

    /**
     * Put into entry property and value using {@link com.sap.mobile.lib.parser.IODataEntry#putPropertyValue(String, String)}.
     *
     * @param property entry property({})nazev  popois  server
     * @param value    entry value
     */
    public void setEntryProperty(String property, Boolean value) {
        mEntry.putPropertyValue(property, value.toString());
    }

    /**
     * Put into entry property and value using {@link com.sap.mobile.lib.parser.IODataEntry#putPropertyValue(String, String)}.
     *
     * @param property entry property({})nazev  popois  server
     * @param value    entry value
     */
    public void setEntryProperty(String property, Float value) {
        mEntry.putPropertyValue(property, value.toString());
    }

    /**
     * Put into entry property and value using {@link com.sap.mobile.lib.parser.IODataEntry#putPropertyValue(String, String)}.
     *
     * @param property entry property({})nazev  popois  server
     * @param value    entry value
     */
    public void setEntryProperty(String property, Integer value) {
        mEntry.putPropertyValue(property, value.toString());
    }

    /**
     * Check if entry cache time stamp({@link #getEntryCacheState()}) is not null.
     *
     * @return true if cache time stamp is not null, otherwise false.
     */
    public boolean isEntryCached() {
        return getEntryCacheTimeStamp() != null;
    }

    /**
     * Check if entry saved in local cache.
     *
     * @return true if entry is saved in local cache, otherwise false.
     */
    public boolean isEntryCachedLocal() {
        return mEntry.getIsLocal();
    }

    /**
     * Get entry cache state({@link com.sap.mobile.lib.parser.IODataEntry.cacheState}) using method {@link com.sap.mobile.lib.parser.IODataEntry.cacheState#toString()}.
     *
     * @return Cache state in string({@link java.lang.String}).
     */
    public IODataEntry.cacheState getEntryCacheState() {
        if (mEntry == null)
            return null;

        return mEntry.getCachestate();
    }

    /**
     * Get entry cache time stamp {@link com.sap.mobile.lib.parser.IODataEntry#getCacheTimestamp()}.
     *
     * @return Entry cache time stamp.
     */
    public Long getEntryCacheTimeStamp() {
        return mEntry.getCacheTimestamp();
    }

    /**
     * Get entry cache time stamp.
     *
     * @param simpleDateFormat date format({@link java.lang.String})
     * @return Formated cache time stamp into string({@link java.lang.String}).
     */
    public String getEntryCacheTimeStamp(String simpleDateFormat) {
        if (isEntryCached())
            return KMFHelperString.formatUtilDateToStringUsingFormat(KMFHelperUtilDate.getTime(getEntryCacheTimeStamp()), simpleDateFormat);
        else
            return null;

    }

    /**
     * Create entry for inserting or updating data on server.
     *
     * @return
     */
    public ODataEntry createEntry() {
        ODataEntry entry = new ODataEntry();
        entry.setSchema(KMFApplication.getMetaDocument());
        entry.setTitle(getCollectionId());
        entry.setCollectionId(getCollectionId());
        for (IODataProperty property : KMFApplication.getMetaDocument().getEntityType(mName).getProperties()) {
            String value = this.mEntry.getPropertyValue(property.getName());

            if (value != null)
                entry.putPropertyValue(property.getName(), value);
        }
        return entry;
    }

    /**
     * Create entry for deep inserting or updating data on server.
     *
     * @param entries link entries
     * @return
     */
    public ODataEntry createEntry(List<?> entries) {
        ODataEntry entry = createEntry();
        if (entries.size() > 0) {
            ODataLink itemsLink = new ODataLink();
            itemsLink.setSchema(KMFApplication.getMetaDocument());
            itemsLink.putAttribute(mCollectionId, "href");
            itemsLink.putAttribute(IParserDocument.XMLNS_DATASERVICES_URI + "/related/" + ((KMFODataEntity) entries.get(0)).getCollectionId(), ODataLink.ATTRIBUTE_REL);
            itemsLink.putAttribute("application/atom+xml;type=feed", "type");

            for (KMFODataEntity entity : (List<KMFODataEntity>) entries) {
                ODataEntry subEntry = new ODataEntry();

                for (IODataProperty property : KMFApplication.getMetaDocument().getEntityType(entity.getName()).getProperties()) {
                    String value = entity.getEntry().getPropertyValue(property.getName());

                    if (value != null)
                        subEntry.putPropertyValue(property.getName(), value);
                }

                itemsLink.putDocument(subEntry.getDocument(), "m:inline", "atom:feed");
            }
            entry.putDocument(itemsLink.getDocument());
        }
        return entry;
    }
}
