package org.acme.service.requests;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.model.response.requests.RequestCommonData;
import org.acme.service.settings.AssociationSettingsService;
import org.acme.service.settings.ItemSettingsService;
import org.bson.types.ObjectId;

@ApplicationScoped
public class CommonRequestService {
    @Inject
    AssociationSettingsService associationSettingsService;
    @Inject
    ItemSettingsService itemSettingsService;


    public RequestCommonData getCommonData() {
        var commonData = new RequestCommonData();
        commonData.setAssociationsList(associationSettingsService.listAll());
        commonData.setItemsList(itemSettingsService.getItems());
        return commonData;
    }

    public ObjectId getObjectId(String requestId) {
        return new ObjectId(requestId);
    }
}
