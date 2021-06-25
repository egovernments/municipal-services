package org.egov.pt.repository.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.Property;
import org.egov.pt.models.PropertyCriteria;
import org.egov.pt.web.contracts.FuzzySearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class FuzzySearchQueryBuilder {


    private ObjectMapper mapper;

    private PropertyConfiguration config;


    @Autowired
    public FuzzySearchQueryBuilder(ObjectMapper mapper, PropertyConfiguration config) {
        this.mapper = mapper;
        this.config = config;
    }


    private static final String BASE_QUERY = "{\n" +
            "  \"from\": {{OFFSET}},\n" +
            "  \"size\": {{LIMIT}},\n" +
            "  \"sort\": {\n" +
            "    \"_score\": {\n" +
            "      \"order\": \"desc\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"query\": {\n" +
            "  }\n" +
            "}";

    private static final String innerQueryTemplate = "{\n" +
            "          \"match\": {\n" +
            "            \"{{VAR}}\": {\n" +
            "              \"query\": \"{{PARAM}}\",\n" +
            "              \"fuzziness\": \"{{FUZZINESS}}\"\n" +
            "            }\n" +
            "          }\n" +
            "        }";

    private static final String filterTemplate   = "\"filter\": { " +
            "      }";

    /**
     * Builds a elasticsearch search query based on the fuzzy search criteria
     * @param criteria
     * @return
     */
    public String getFuzzySearchQuery(PropertyCriteria criteria, List<String> ids){

        String finalQuery;

        try {
            String baseQuery = addPagination(criteria);
            JsonNode node = mapper.readTree(baseQuery);
            ObjectNode insideMatch = (ObjectNode)node.get("query");
            List<JsonNode> fuzzyClauses = new LinkedList<>();

            if(criteria.getName() != null){
                String innerQuery = innerQueryTemplate.replace("{{PARAM}}",criteria.getName());
                innerQuery = innerQuery.replace("{{FUZZINESS}}", config.getNameFuziness());
                innerQuery = innerQuery.replace("{{VAR}}","Data.ownerNames");
                JsonNode innerNode = mapper.readTree(innerQuery);
                fuzzyClauses.add(innerNode);
            }

            if(criteria.getDoorNo() != null){
                String innerQuery = innerQueryTemplate.replace("{{PARAM}}",criteria.getDoorNo());
                innerQuery = innerQuery.replace("{{FUZZINESS}}", config.getDoorNoFuziness());
                innerQuery = innerQuery.replace("{{VAR}}","Data.doorNo");
                JsonNode innerNode = mapper.readTree(innerQuery);
                fuzzyClauses.add(innerNode);
            }

            if(criteria.getOldPropertyId() != null){
                String innerQuery = innerQueryTemplate.replace("{{PARAM}}",criteria.getOldPropertyId());
                innerQuery = innerQuery.replace("{{FUZZINESS}}", config.getOldPropertyIdFuziness());
                innerQuery = innerQuery.replace("{{VAR}}","Data.oldPropertyId");
                JsonNode innerNode = mapper.readTree(innerQuery);
                fuzzyClauses.add(innerNode);
            }

            JsonNode mustNode = mapper.convertValue(new HashMap<String, List<JsonNode>>(){{put("must",fuzzyClauses);}}, JsonNode.class);

            insideMatch.put("bool",mustNode);
            ObjectNode boolNode = (ObjectNode)insideMatch.get("bool");


            if(!CollectionUtils.isEmpty(ids)){
                JsonNode jsonNode = mapper.convertValue(new HashMap<String, List<String>>(){{put("Data.id.keyword",ids);}}, JsonNode.class);
                ObjectNode parentNode = mapper.createObjectNode();
                parentNode.put("terms",jsonNode);
                boolNode.put("filter", parentNode);
            }

            finalQuery = mapper.writeValueAsString(node);

        }
        catch (Exception e){
            throw new CustomException("JSONNODE_ERROR","Failed to build json query for fuzzy search");
        }

        return finalQuery;

    }

    private String addPagination(PropertyCriteria criteria) {


        Long limit = config.getDefaultLimit();
        Long offset = config.getDefaultOffset();

        if (criteria.getLimit() != null && criteria.getLimit() <= config.getMaxSearchLimit())
            limit = criteria.getLimit();

        if (criteria.getLimit() != null && criteria.getLimit() > config.getMaxSearchLimit())
            limit = config.getMaxSearchLimit();

        if (criteria.getOffset() != null)
            offset = criteria.getOffset();

        String baseQuery = BASE_QUERY.replace("{{OFFSET}}", offset.toString());
        baseQuery = baseQuery.replace("{{LIMIT}}", limit.toString());

        return baseQuery;
    }

}
