package com.sap.stepbystep.kmf.data;

import com.sap.stepbystep.kmf.odata.KMFODataEntity;
import com.sap.stepbystep.kmf.odata.KMFODataEntity2;
import com.sap.stepbystep.kmf.odata.KMFODataRequestKeys;

public class KMFUserData extends KMFBaseUserData {

    public KMFODataRequestKeys getRequestKeys() {
        return new KMFODataRequestKeys(
                USER_NAME
                , mUserName.toUpperCase()
        ).addKey(
                USER_PIN
                , mUserPIN
        );
    }

    public static KMFODataEntity getEntity() {
        return new KMFODataEntity(FUNCTION_IMPORT_NAME);
    }

    public static KMFODataEntity2 getEntity2(String namespace){
        KMFODataEntity2 oDataEntity = new KMFODataEntity2(namespace + FUNCTION_IMPORT_NAME, FUNCTION_IMPORT_NAME);
        oDataEntity.setEntryCollectionId(oDataEntity.getCollectionId());
        return oDataEntity;
    }
}

