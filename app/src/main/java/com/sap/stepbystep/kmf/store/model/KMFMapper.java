package com.sap.stepbystep.kmf.store.model;


import android.text.TextUtils;

import com.sap.mobile.lib.parser.IODataEntry;
import com.sap.mobile.lib.parser.IODataLink;
import com.sap.mobile.lib.parser.IParserDocument;
import com.sap.mobile.lib.parser.ODataLink;
import com.sap.mobile.lib.parser.ParserDocument;
import com.sap.smp.client.odata.ODataEntity;
import com.sap.smp.client.odata.ODataNavigationProperty;
import com.sap.smp.client.odata.exception.ODataException;
import com.sap.smp.client.odata.impl.ODataEntityDefaultImpl;
import com.sap.smp.client.odata.impl.ODataEntitySetDefaultImpl;
import com.sap.smp.client.odata.impl.ODataPropertyDefaultImpl;
import com.sap.smp.client.odata.online.OnlineODataStore;
import com.sap.stepbystep.kmf.android.KMFApplication2;
import com.sap.stepbystep.kmf.store.listener.KMFOnlineStoreOpenListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by popelkat on 7.11.2016.
 */
public class KMFMapper {
    public static ODataEntity oDataEntry2oDataEntity(IODataEntry entry) throws ODataException {
        ODataEntity entity = null;
        OnlineODataStore store = KMFOnlineStoreOpenListener.getInstance().getStore();

        if (store != null) {
            entity = new ODataEntityDefaultImpl(entry.getTitle());
//            store.allocateProperties(entity, ODataStore.PropMode.All);
            store.allocateNavigationProperties(entity);


            String propertyType;
            for (String propertyName :
                    KMFApplication2.getMetadataDocument().getMetaEntity(entry.getTitle()).getPropertyNames()) {
                if (entry.getPropertyValue(propertyName) != null) {
                    Object value;
                    propertyType =
                            KMFApplication2.getMetadataDocument().getMetaEntity(entry.getTitle()).getProperty(propertyName).getType().name();

                    if (TextUtils.isEmpty(propertyType)) {
                        value = entry.getPropertyValue(propertyName);
                    } else {
                        if (propertyType.equals("Boolean")) {
                            value = Boolean.valueOf(entry.getPropertyValue(propertyName));
                        } else if (propertyType.equals("Decimal")) {
                            value = BigDecimal.valueOf(Double.parseDouble(entry.getPropertyValue(propertyName)));
                        } else if (propertyType.equals("BigDecimal")) {
                            value = BigDecimal.valueOf(Double.parseDouble(entry.getPropertyValue(propertyName)));
                        } else {
                            value = entry.getPropertyValue(propertyName);
                        }
                    }

                    entity.getProperties().put(propertyName, new ODataPropertyDefaultImpl(propertyName, value));
                }
            }

            String navigationPropertyName;
            if (entry.getSchema() != null) {
                String prefix = "atom";
                List<IParserDocument> linkDocuments = entry.getDocuments(new String[]{prefix + ":" + "link"});
                List<IODataLink> links = new ArrayList();
                Iterator i$ = linkDocuments.iterator();

                while(i$.hasNext()) {
                    IParserDocument linkDocument = (IParserDocument)i$.next();
                    ODataLink link = new ODataLink((ParserDocument)linkDocument);
                    links.add(link);
                }

                for (IODataLink link : links) {
                    navigationPropertyName = link.getRel().replaceFirst(".*/(\\w+)", "$1");

                    if (!entity.getNavigationPropertyNames().contains(navigationPropertyName)) {
                        continue;
                    }

                    ODataNavigationProperty navigationProperty = entity.getNavigationProperty(navigationPropertyName);
                    ODataEntitySetDefaultImpl entitySet = new ODataEntitySetDefaultImpl(
                            link.getInlineEntries().size(),
                            navigationPropertyName,
                            null
                    );
                    entitySet.setResourcePath(navigationPropertyName);

                    for (IODataEntry inlineEntry : link.getInlineEntries()) {
                        entitySet.getEntities().add(KMFMapper.oDataEntry2oDataEntity(inlineEntry));
                    }

                    navigationProperty.setNavigationContent(entitySet);
                    navigationProperty.setAssociationResourcePath(navigationPropertyName);
                    entity.setNavigationProperty(navigationPropertyName, navigationProperty);
                }
            }
        }

        return entity;
    }
}

