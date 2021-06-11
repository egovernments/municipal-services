package org.egov.pt.util;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.models.Property;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class EmployeeFilterUtil {



    /**
     *
     * @param requestInfo
     * @param properties
     */
    public void filtersForEmployee(RequestInfo requestInfo, List<Property> properties){

        filterUnits(properties);
        filterAddress(properties);

    }


    /**
     * Removes Units from properties
     * @param properties
     */
    private void filterUnits(List<Property> properties){

        properties.forEach(property -> {
            property.setUnits(null);
        });

    }

    /**
     * Removes Address from properties
     * @param properties
     */
    private void filterAddress(List<Property> properties){

        properties.forEach(property -> {
            property.setAddress(null);
        });

    }


    /**
     * Removes Address from properties
     * @param properties
     */
    private void maskAddress(List<Property> properties){

        for(Property property : properties){
            property.getAddress().setBuildingName(maskString(property.getAddress().getBuildingName()));
            property.getAddress().setPlotNo(maskString(property.getAddress().getPlotNo()));
            property.getAddress().setStreet(maskString(property.getAddress().getStreet()));
            property.getAddress().setDoorNo(maskString(property.getAddress().getDoorNo()));
        }

    }

    private String maskString(String str){
        return  str.replaceAll("\\S", "X");
    }

}
