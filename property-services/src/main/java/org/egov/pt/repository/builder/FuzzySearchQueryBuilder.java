package org.egov.pt.repository.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.egov.pt.web.contracts.FuzzySearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FuzzySearchQueryBuilder {


    private ObjectMapper mapper;


    @Autowired
    public FuzzySearchQueryBuilder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    private static final String BASE_QUERY = "{\n" +
            "  \"_source\": [\"Data.propertyId\",\"Data.tenantData.code\"],\n" +
            "  \"query\": {\n" +
            "    \"match\": {\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final String innerQueryTemplate = "{\n" +
            "        \"query\": \"{{PARAM}}\",\n" +
            "        \"fuzziness\": \"7\"\n" +
            "      }";

    /**
     * Builds a elasticsearch search query based on the fuzzy search criteria
     * @param criteria
     * @return
     */
    public String getFuzzySearchQuery(FuzzySearchCriteria criteria){

        String finalQuery;

        try {
            JsonNode node = mapper.readTree(BASE_QUERY);
            ObjectNode insideMatch = (ObjectNode)node.get("query").get("match");

            if(criteria.getName() != null){
                String innerQuery = innerQueryTemplate.replace("{{PARAM}}",criteria.getName());
                JsonNode innerNode = mapper.readTree(innerQuery);
                insideMatch.put("name",innerNode);
            }

            if(criteria.getDoorNo() != null){
                String innerQuery = innerQueryTemplate.replace("{{PARAM}}",criteria.getDoorNo());
                JsonNode innerNode = mapper.readTree(innerQuery);
                insideMatch.put("Data.doorNo",innerNode);
            }

            if(criteria.getStreet() != null){
                String innerQuery = innerQueryTemplate.replace("{{PARAM}}",criteria.getStreet());
                JsonNode innerNode = mapper.readTree(innerQuery);
                insideMatch.put("Data.street",innerNode);
            }

            finalQuery = mapper.writeValueAsString(node);

        }
        catch (Exception e){
            throw new CustomException("JSONNODE_ERROR","Failed to build json query for fuzzy search");
        }

        return finalQuery;

    }

}
