package com.sap.stepbystep.smf.data.entity;

import android.content.ContentValues;

import com.sap.smp.client.odata.ODataEntity;
import com.sap.smp.client.odata.ODataPropMap;
import com.sap.smp.client.odata.ODataProperty;
import com.sap.stepbystep.kmf.odata.KMFODataEntity;
import com.sap.stepbystep.kmf.odata.KMFODataEntity2;
import com.sap.stepbystep.smf.data.constant.SMFODataService;

public class SMFCodeList {

    public static final String TAG = SMFCodeList.class.getName();

    public static final String ENTITY_NAME = "CodeList";
    public static final String ENTITY_TYPE = SMFODataService.NAMESPACE + ENTITY_NAME;
    public static final String COLLECTION = ENTITY_NAME + "Set";

    /**
     * KeyProperties:
     */
    public static final String CODE_LIST_ID = "CodeListId";
    public static final String CODE_TYPE = "CodeType";
    /**
     * Properties:
     */
    public static final String DESCRIPTION = "Description";

    private Integer mFieldEntryId;
    private String mCodeListId;
    private String mCodeType;
    private String mDescription;

    public SMFCodeList(KMFODataEntity oDataEntity) {
        if (oDataEntity == null) {
            return;
        }

        setCodeListId(oDataEntity.getEntryPropertyValueReplaceNull(CODE_LIST_ID));
        setCodeType(oDataEntity.getEntryPropertyValueReplaceNull(CODE_TYPE));
        setDescription(oDataEntity.getEntryPropertyValueReplaceNull(DESCRIPTION));
    }

    public SMFCodeList(ODataEntity entity) {
        if (entity == null) {
            return;
        }

        ODataPropMap properties = entity.getProperties();

        ODataProperty property = properties.get(CODE_LIST_ID);
        setCodeListId((String) property.getValue());

        property = properties.get(CODE_TYPE);
        setCodeType((String) property.getValue());

        property = properties.get(DESCRIPTION);
        setDescription((String) property.getValue());
    }

    public SMFCodeList(ContentValues contentValues) {
        if (contentValues == null) {
            return;
        }

        setCodeListId(contentValues.getAsString(CODE_LIST_ID));
        setCodeType(contentValues.getAsString(CODE_TYPE));
        setDescription(contentValues.getAsString(DESCRIPTION));
    }

    /**
     * Get empty oData entity.
     *
     * @return {@link KMFODataEntity}
     */
    public static KMFODataEntity getEmptyODataEntity() {
        return new KMFODataEntity(ENTITY_NAME);
    }

    /**
     * Get empty oData entity.
     *
     * @return {@link KMFODataEntity2}
     */
    public static KMFODataEntity2 getEmptyODataEntity2() {
        return new KMFODataEntity2(ENTITY_TYPE, COLLECTION);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SMFCodeList)) {
            return false;
        }

        SMFCodeList objectCodeList = (SMFCodeList) object;
        return getCodeListId().equals(objectCodeList.getCodeListId())
                && getCodeType().equals(objectCodeList.getCodeType());
    }

    /**
     * Get field entry ID.
     *
     * @return {@link Integer}
     */
    public Integer getFieldEntryId() {
        return mFieldEntryId;
    }

    /**
     * Set field entry ID.
     *
     * @param fieldEntryID field entry ID
     */
    public void setFieldEntryId(Integer fieldEntryID) {
        mFieldEntryId = fieldEntryID;
    }

    /**
     * Get code list ID.
     *
     * @return {@link java.lang.String}
     */
    public String getCodeListId() {
        return mCodeListId;
    }

    /**
     * Set code list ID.
     *
     * @param codeListId code list ID
     */
    public void setCodeListId(String codeListId) {
        mCodeListId = codeListId;
    }

    /**
     * Get code type.
     *
     * @return {@link java.lang.String}
     */
    public String getCodeType() {
        return mCodeType;
    }

    /**
     * Set code type.
     *
     * @param codeType code type
     */
    public void setCodeType(String codeType) {
        mCodeType = codeType;
    }

    /**
     * Get description.
     *
     * @return {@link java.lang.String}
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Set description.
     *
     * @param description description
     */
    public void setDescription(String description) {
        mDescription = description;
    }

    /**
     * Get oData entity.
     *
     * @return {@link KMFODataEntity}
     */
    public KMFODataEntity getODataEntity() {
        KMFODataEntity oDataEntity = getEmptyODataEntity();
        oDataEntity.setEntryCollectionId(oDataEntity.getCollectionId());
        oDataEntity.setEntryProperty(CODE_LIST_ID, getCodeListId());
        oDataEntity.setEntryProperty(CODE_TYPE, getCodeType());
        oDataEntity.setEntryProperty(DESCRIPTION, getDescription());
        return oDataEntity;
    }

    /**
     * Get content values.
     *
     * @return {@link android.content.ContentValues}
     */
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CODE_LIST_ID, getCodeListId());
        contentValues.put(CODE_TYPE, getCodeType());
        contentValues.put(DESCRIPTION, getDescription());
        return contentValues;
    }
}
