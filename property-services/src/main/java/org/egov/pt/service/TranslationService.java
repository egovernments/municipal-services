package org.egov.pt.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.models.Assessment;
import org.egov.pt.models.Property;
import org.egov.pt.models.oldProperty.OldProperty;
import org.egov.pt.web.contracts.AssessmentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TranslationService {


    private ObjectMapper mapper;


    @Autowired
    public TranslationService(ObjectMapper mapper) {
        this.mapper = mapper;
    }


    public Map<String, Object> translate(AssessmentRequest assessmentRequest, Property property){

        RequestInfo requestInfo = assessmentRequest.getRequestInfo();
        Assessment assessment = assessmentRequest.getAssessment();

        Map<String, Object> propertyMap = new HashMap<>();
        Map<String, Object> propertyDetail = new HashMap<>();
        Map<String, Object> auditDetails = new HashMap<>();

        Map<String, Object> addressMap = new HashMap<>();
        Map<String, Object> localityMap = new HashMap<>();
        localityMap.put("area",property.getAddress().getLocality().getArea());
        localityMap.put("code",property.getAddress().getLocality().getCode());
        addressMap.put("locality",localityMap);

        propertyMap.put("address", addressMap);
        propertyMap.put("propertyId",property.getPropertyId());
        propertyMap.put("tenantId", property.getTenantId());
        propertyMap.put("acknowldgementNumber", property.getAcknowldgementNumber());
        propertyMap.put("oldPropertyId", property.getOldPropertyId());
        propertyMap.put("status", property.getStatus());
        propertyMap.put("creationReason", property.getCreationReason());
        propertyMap.put("occupancyDate", null);

        auditDetails.put("createdBy", property.getAuditDetails().getCreatedBy());
        auditDetails.put("lastModifiedBy", property.getAuditDetails().getLastModifiedBy());
        auditDetails.put("createdTime", property.getAuditDetails().getCreatedTime());
        auditDetails.put("lastModifiedTime", property.getAuditDetails().getLastModifiedTime());
        propertyMap.put("auditDetails", auditDetails);


        String[] propertyTypeMasterData = property.getPropertyType().split("\\.");
        String propertyType = null,propertySubType = null;
        propertyType = propertyTypeMasterData[0];
        if(propertyTypeMasterData.length > 1)
            propertySubType = propertyTypeMasterData[1];


        if(property.getUsageCategory()!=null){

            String[] usageCategoryMasterData = property.getUsageCategory().split("\\.");
            String usageCategoryMajor = null,usageCategoryMinor = null;
            usageCategoryMajor = usageCategoryMasterData[0];
            if(usageCategoryMasterData.length > 1)
                usageCategoryMinor = usageCategoryMasterData[1];

            propertyDetail.put("usageCategoryMajor", usageCategoryMajor);
            propertyDetail.put("usageCategoryMinor", usageCategoryMinor);

        }


        if(property.getOwnershipCategory()!=null){
            String[] ownershipCategoryMasterData  = property.getOwnershipCategory().split("\\.");
            String ownershipCategory = null,subOwnershipCategory = null;
            ownershipCategory = ownershipCategoryMasterData[0];
            if(ownershipCategoryMasterData.length > 1)
                subOwnershipCategory = ownershipCategoryMasterData[1];

            propertyDetail.put("ownershipCategory", ownershipCategory);
            propertyDetail.put("subOwnershipCategory", subOwnershipCategory);
        }


        propertyDetail.put("noOfFloors", property.getNoOfFloors());
        propertyDetail.put("landArea", property.getLandArea());
        propertyDetail.put("buildUpArea", property.getSuperBuiltUpArea());
        propertyDetail.put("financialYear", assessment.getFinancialYear());
        propertyDetail.put("propertyType", propertyType);
        propertyDetail.put("propertySubType", propertySubType);
        propertyDetail.put("assessmentNumber", assessment.getAssessmentNumber());
        propertyDetail.put("assessmentDate", assessment.getAssessmentDate());

       // propertyDetail.put("adhocExemption", );
        // propertyDetail.put("adhocPenalty",);

        List<Map<String, Object>> owners = new LinkedList<>();

        property.getOwners().forEach(ownerInfo -> {
            Map<String, Object> owner = mapper.convertValue(ownerInfo,  new TypeReference<Map<String, Object>>() {});
            owners.add(owner);
        });

        List<Map<String, Object>> units = new LinkedList<>();

        if(!CollectionUtils.isEmpty(property.getUnits())){
            property.getUnits().forEach(unit -> {
                Map<String, Object> unitMap = new HashMap<>();
                unitMap.put("id",unit.getId());
                unitMap.put("floorNo", unit.getFloorNo());
                unitMap.put("unitArea", unit.getConstructionDetail().getBuiltUpArea());
                unitMap.put("arv", unit.getArv());
                unitMap.put("occupancyType", unit.getOccupancyType());

                String[] masterData = unit.getUsageCategory().split("\\.");

                if(masterData.length >= 1)
                    unitMap.put("usageCategoryMajor", masterData[0]);

                if(masterData.length >= 2)
                    unitMap.put("usageCategoryMinor", masterData[1]);

                if(masterData.length >= 3)
                    unitMap.put("usageCategorySubMinor", masterData[2]);

                if(masterData.length >= 4)
                    unitMap.put("usageCategoryDetail",masterData[3]);

                unitMap.put("additionalDetails", unit.getAdditionalDetails());
                units.add(unitMap);

            });
        }

        propertyDetail.put("owners", owners);
        propertyDetail.put("units", units);

        propertyMap.put("propertyDetails", Collections.singletonList(propertyDetail));

        Map<String, Object> calculationCriteria = new HashMap<>();
        calculationCriteria.put("property", propertyMap);
        calculationCriteria.put("tenantId", property.getTenantId());

        Map<String, Object> calculationReq = new HashMap<>();
        calculationReq.put("RequestInfo", requestInfo);
        calculationReq.put("CalculationCriteria", Collections.singletonList(calculationCriteria));

        return calculationReq;

    }

    public OldProperty getOldProperty(AssessmentRequest assessmentRequest, Property property) {
        Map<String, Object> oldPropertyObjectMap = translate(assessmentRequest, property);
        List<Map<String, Object>> calculationCriteriaArray = (List<Map<String, Object>>) oldPropertyObjectMap.get("CalculationCriteria");
        OldProperty oldProperty = mapper.convertValue(calculationCriteriaArray.get(0).get("property"), OldProperty.class);
        return oldProperty;
    }
}
