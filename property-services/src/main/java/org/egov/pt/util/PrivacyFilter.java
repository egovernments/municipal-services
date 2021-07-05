package org.egov.pt.util;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.egov.pt.models.Property;
import org.egov.pt.web.contracts.PropertyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrivacyFilter {


    @Autowired
    private ObjectMapper mapper;



    private String propertyStr = "{\n" +
            "    \"ResponseInfo\": {\n" +
            "        \"apiId\": \"Mihy\",\n" +
            "        \"ver\": \".01\",\n" +
            "        \"ts\": null,\n" +
            "        \"resMsgId\": \"uief87324\",\n" +
            "        \"msgId\": \"20170310130900|en_IN\",\n" +
            "        \"status\": \"successful\"\n" +
            "    },\n" +
            "    \"Properties\": [\n" +
            "        {\n" +
            "            \"id\": \"5d6c8ce9-0af6-424c-bbd4-a1e262522aa5\",\n" +
            "            \"propertyId\": \"PB-PT-2020-02-18-003735\",\n" +
            "            \"surveyId\": null,\n" +
            "            \"linkedProperties\": null,\n" +
            "            \"tenantId\": \"pb.amritsar\",\n" +
            "            \"accountId\": \"04956309-87cd-4526-b4e6-48123abd4f3d\",\n" +
            "            \"oldPropertyId\": null,\n" +
            "            \"status\": \"ACTIVE\",\n" +
            "            \"address\": {\n" +
            "                \"tenantId\": \"pb.amritsar\",\n" +
            "                \"doorNo\": \"417/j\",\n" +
            "                \"plotNo\": null,\n" +
            "                \"id\": \"85099ed4-605c-4d91-84fa-1203f03783cc\",\n" +
            "                \"landmark\": \" next to grand mercure exit gate\",\n" +
            "                \"city\": \"amritsar\",\n" +
            "                \"district\": null,\n" +
            "                \"region\": null,\n" +
            "                \"state\": null,\n" +
            "                \"country\": null,\n" +
            "                \"pincode\": \"560064\",\n" +
            "                \"buildingName\": \"jumbedweepa\",\n" +
            "                \"street\": \"bakery street\",\n" +
            "                \"locality\": {\n" +
            "                    \"code\": \"SUN11\",\n" +
            "                    \"name\": \"Back Side 33 KVA Grid Patiala Road - Area1\",\n" +
            "                    \"label\": \"Locality\",\n" +
            "                    \"latitude\": null,\n" +
            "                    \"longitude\": null,\n" +
            "                    \"area\": \"Area1\",\n" +
            "                    \"children\": [],\n" +
            "                    \"materializedPath\": null\n" +
            "                },\n" +
            "                \"geoLocation\": {\n" +
            "                    \"latitude\": 0.0,\n" +
            "                    \"longitude\": 0.0\n" +
            "                },\n" +
            "                \"additionalDetails\": null\n" +
            "            },\n" +
            "            \"acknowldgementNumber\": \"PB-AC-2020-02-18-003017\",\n" +
            "            \"propertyType\": \"BUILTUP.SHAREDPROPERTY\",\n" +
            "            \"ownershipCategory\": \"INDIVIDUAL.SINGLEOWNER\",\n" +
            "            \"owners\": [\n" +
            "                {\n" +
            "                    \"id\": null,\n" +
            "                    \"uuid\": \"917425b5-037f-4e97-840a-f39d45c9a031\",\n" +
            "                    \"userName\": \"25a6563b-1bef-4056-a860-25b67d60448c\",\n" +
            "                    \"password\": null,\n" +
            "                    \"salutation\": null,\n" +
            "                    \"name\": \"Abhishek\",\n" +
            "                    \"gender\": \"FEMALE\",\n" +
            "                    \"mobileNumber\": \"7829727713\",\n" +
            "                    \"emailId\": null,\n" +
            "                    \"altContactNumber\": \"123456\",\n" +
            "                    \"pan\": null,\n" +
            "                    \"aadhaarNumber\": null,\n" +
            "                    \"permanentAddress\": null,\n" +
            "                    \"permanentCity\": null,\n" +
            "                    \"permanentPinCode\": null,\n" +
            "                    \"correspondenceCity\": null,\n" +
            "                    \"correspondencePinCode\": null,\n" +
            "                    \"correspondenceAddress\": null,\n" +
            "                    \"active\": true,\n" +
            "                    \"dob\": null,\n" +
            "                    \"pwdExpiryDate\": 1587889734000,\n" +
            "                    \"locale\": null,\n" +
            "                    \"type\": \"CITIZEN\",\n" +
            "                    \"signature\": null,\n" +
            "                    \"accountLocked\": false,\n" +
            "                    \"roles\": [\n" +
            "                        {\n" +
            "                            \"id\": null,\n" +
            "                            \"name\": \"Citizen\",\n" +
            "                            \"code\": \"CITIZEN\",\n" +
            "                            \"tenantId\": \"pb\"\n" +
            "                        }\n" +
            "                    ],\n" +
            "                    \"fatherOrHusbandName\": \"HUSBAND NAME\",\n" +
            "                    \"bloodGroup\": null,\n" +
            "                    \"identificationMark\": null,\n" +
            "                    \"photo\": null,\n" +
            "                    \"createdBy\": \"1560\",\n" +
            "                    \"createdDate\": 1580117590000,\n" +
            "                    \"lastModifiedBy\": \"1\",\n" +
            "                    \"lastModifiedDate\": 1582713524000,\n" +
            "                    \"tenantId\": \"pb\",\n" +
            "                    \"ownerInfoUuid\": \"676de9b8-5fc6-4b68-af2e-6b5d6dbba95e\",\n" +
            "                    \"isPrimaryOwner\": true,\n" +
            "                    \"ownerShipPercentage\": null,\n" +
            "                    \"ownerType\": \"NONE\",\n" +
            "                    \"institutionId\": null,\n" +
            "                    \"status\": \"ACTIVE\",\n" +
            "                    \"documents\": null,\n" +
            "                    \"relationship\": \"HUSBAND\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"institution\": null,\n" +
            "            \"creationReason\": null,\n" +
            "            \"usageCategory\": \"NONRESIDENTIAL\",\n" +
            "            \"noOfFloors\": 1,\n" +
            "            \"landArea\": 1000.0,\n" +
            "            \"superBuiltUpArea\": null,\n" +
            "            \"source\": \"MUNICIPAL_RECORDS\",\n" +
            "            \"channel\": \"SYSTEM\",\n" +
            "            \"documents\": null,\n" +
            "            \"units\": [\n" +
            "                {\n" +
            "                    \"id\": \"e65453f9-15a8-47ff-ae0e-1ddbe0b95245\",\n" +
            "                    \"tenantId\": null,\n" +
            "                    \"floorNo\": 0,\n" +
            "                    \"unitType\": null,\n" +
            "                    \"usageCategory\": \"NONRESIDENTIAL.COMMERCIAL.RETAIL.SHOWROOM\",\n" +
            "                    \"occupancyType\": \"SELFOCCUPIED\",\n" +
            "                    \"active\": true,\n" +
            "                    \"occupancyDate\": 0,\n" +
            "                    \"constructionDetail\": {\n" +
            "                        \"carpetArea\": null,\n" +
            "                        \"builtUpArea\": 100.00,\n" +
            "                        \"plinthArea\": null,\n" +
            "                        \"superBuiltUpArea\": null,\n" +
            "                        \"constructionType\": null,\n" +
            "                        \"constructionDate\": null,\n" +
            "                        \"dimensions\": null\n" +
            "                    },\n" +
            "                    \"additionalDetails\": null,\n" +
            "                    \"auditDetails\": null,\n" +
            "                    \"arv\": null\n" +
            "                }\n" +
            "            ],\n" +
            "            \"additionalDetails\": null,\n" +
            "            \"auditDetails\": {\n" +
            "                \"createdBy\": \"04956309-87cd-4526-b4e6-48123abd4f3d\",\n" +
            "                \"lastModifiedBy\": \"04956309-87cd-4526-b4e6-48123abd4f3d\",\n" +
            "                \"createdTime\": 1582019077955,\n" +
            "                \"lastModifiedTime\": 1582019077955\n" +
            "            },\n" +
            "            \"workflow\": null\n" +
            "        }\n" +
            "    ]\n" +
            "}";


    public PropertyResponse getMaskedProperty(){
        PropertyResponse response = null;
        try {
             response = mapper.readValue(propertyStr, PropertyResponse.class);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        DocumentContext json = JsonPath.parse(propertyStr);
        DocumentContext result = json.map("$..name", (currentValue, configuration) -> {
            return maskField(currentValue);
        });
        System.out.println(result.jsonString());
        return response;
    }


    private String maskField(Object str){
        return  str.toString().replaceAll("\\S", "X");
    }



}
