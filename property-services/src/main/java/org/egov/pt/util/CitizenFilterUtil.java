package org.egov.pt.util;


import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.models.OwnerInfo;
import org.egov.pt.models.Property;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

@Component
public class CitizenFilterUtil {


    /**
     *
     * @param requestInfo
     * @param properties
     */
    public void filtersForCitizen(RequestInfo requestInfo, List<Property> properties){

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

            if(!allowedUUIDs.contains(uuid) && !allowedMobileNumbers.contains(mobileNumber)){
                property.setDocuments(null);
                maskMobileNumber(property);
            }

        }

    }

    /**
     * Mask the mobilenumber of the owners of the property
     * @param property
     */
    private void maskMobileNumber(Property property){

        List<OwnerInfo> owners = property.getOwners();
        String maskedMobileNumber;

        if(!CollectionUtils.isEmpty(owners)){

            for(OwnerInfo owner : owners){

                maskedMobileNumber = owner.getMobileNumber().replaceAll("\\d(?=(?:\\D*\\d){4})", "*");
                owner.setMobileNumber(maskedMobileNumber);

            }

        }

    }


}
