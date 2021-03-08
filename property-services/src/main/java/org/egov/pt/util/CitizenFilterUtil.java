package org.egov.pt.util;


import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.models.Property;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class CitizenFilterUtil {


    /**
     *
     * @param requestInfo
     * @param properties
     */
    public void filterOwnerDocument(RequestInfo requestInfo, List<Property> properties){

        String uuid = requestInfo.getUserInfo().getUuid();
        String mobileNumber = requestInfo.getUserInfo().getUserName();

        for(Property property : properties){

            List<String> allowedUUIDs = new LinkedList<>();

            List<String> allowedMobileNumbers = new LinkedList<>();

            property.getOwners().forEach(ownerInfo -> {
                allowedUUIDs.add(ownerInfo.getUuid());
                allowedMobileNumbers.add(ownerInfo.getMobileNumber());
            });

            allowedUUIDs.add(property.getAccountId());

            if(!allowedUUIDs.contains(uuid) && !allowedMobileNumbers.contains(mobileNumber))
                property.setDocuments(null);

        }

    }


}
