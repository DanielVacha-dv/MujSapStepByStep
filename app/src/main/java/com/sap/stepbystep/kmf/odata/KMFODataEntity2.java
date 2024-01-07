package com.sap.stepbystep.kmf.odata;

import android.util.Log;

import com.sap.mobile.lib.parser.IParserDocument;
import com.sap.mobile.lib.parser.ODataEntry;
import com.sap.mobile.lib.parser.ODataFunctionImport;
import com.sap.mobile.lib.parser.ODataLink;
import com.sap.stepbystep.kmf.android.KMFApplication;
import com.sap.stepbystep.kmf.android.KMFApplication2;

import java.util.ArrayList;
import java.util.List;

public class KMFODataEntity2 extends KMFODataEntity {
    public KMFODataEntity2(String name, String collection) {
        super();

        if(!KMFApplication.getConfig().getConfigOData().useOnlineStore()) {
            super.constructor(name);
            return;
        }

        mName = name;

        if (KMFApplication2.getMetadataDocument() == null) {
            Log.d(TAG, "KMFApplication2.getMetadataDocument() is null");
            return;
        }

        if (KMFApplication2.getMetadataDocument().getMetaEntity(mName) != null) {
            mKeyPropertyNames = new ArrayList<>();
            mKeyPropertyNames.addAll(KMFApplication2.getMetadataDocument().getMetaEntity(mName).getKeyPropertyNames());
            setCollectionId(collection); // Collection ID is set if meta data countains entity with coresponding name.
        } else {
            setFunctionImport(collection); // Function import is set if meta data do not countains entity with coresponding name.
        }
    }


    /**
     * Create entry for inserting or updating data on server.
     *
     * @return
     */
    @Override
    public ODataEntry createEntry() {
        if(!KMFApplication.getConfig().getConfigOData().useOnlineStore()) {
            return super.createEntry();
        }

        ODataEntry entry = new ODataEntry();
        entry.setSchema(KMFApplication.getMetaDocument());
        entry.setTitle(mName);
        entry.setCollectionId(getCollectionId());

        for (String propertyName :
                KMFApplication2.getMetadataDocument().getMetaEntity(mName).getPropertyNames()) {
            String value = this.mEntry.getPropertyValue(propertyName);

            if (value != null) {
                entry.putPropertyValue(propertyName, value);
            }
        }

        return entry;
    }

    @Override
    public ODataEntry createEntry(List<?> entries) {
        if(!KMFApplication.getConfig().getConfigOData().useOnlineStore()) {
            return super.createEntry();
        }

        ODataEntry entry = createEntry();
        if (entries.size() > 0) {
            ODataLink itemsLink = new ODataLink();
            itemsLink.setSchema(KMFApplication.getMetaDocument());
            itemsLink.putAttribute(mCollectionId, "href");
            itemsLink.putAttribute(IParserDocument.XMLNS_DATASERVICES_URI + "/related/" + ((KMFODataEntity2) entries.get(0)).getCollectionId(), ODataLink.ATTRIBUTE_REL);
            itemsLink.putAttribute("application/atom+xml;type=feed", "type");

            for (KMFODataEntity2 entity : (List<KMFODataEntity2>) entries) {
                ODataEntry subEntry = new ODataEntry();
                subEntry.setSchema(KMFApplication.getMetaDocument());
                subEntry.setTitle(entity.getName());
                subEntry.setCollectionId(entity.getCollectionId());

                for (String propertyName :
                        KMFApplication2.getMetadataDocument().getMetaEntity(entity.getName()).getPropertyNames()) {
                    String value = entity.getEntry().getPropertyValue(propertyName);

                    if (value != null) {
                        subEntry.putPropertyValue(propertyName, value);
                    }
                }

                itemsLink.putDocument(subEntry.getDocument(), "m:inline", "atom:feed");
            }
            entry.putDocument(itemsLink.getDocument());
        }


        return entry;
    }

    @Override
    protected void setFunctionImport(String functionImport) {
        if(!KMFApplication.getConfig().getConfigOData().useOnlineStore()) {
            super.setFunctionImport(functionImport);
            return;
        }

        mFunctionImport = new ODataFunctionImport();
        setCollectionId(functionImport);
    }

    @Override
    public String getFunctionImportName() {
        if(!KMFApplication.getConfig().getConfigOData().useOnlineStore()) {
            return super.getFunctionImportName();
        }

        return getCollectionId();
    }

    @Override
    public void setCollectionId(String collectionId) {
        if(!KMFApplication.getConfig().getConfigOData().useOnlineStore()) {
            super.setCollectionId(collectionId);
            return;
        }

        mCollectionId = collectionId;
    }
}