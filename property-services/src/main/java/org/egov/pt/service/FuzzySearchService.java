package org.egov.pt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.egov.pt.models.Property;
import org.egov.pt.repository.ElasticSearchRepository;
import org.egov.pt.web.contracts.FuzzySearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.egov.pt.util.PTConstants.ES_DATA_PATH;

@Component
public class FuzzySearchService {

    private ElasticSearchRepository elasticSearchRepository;

    private ObjectMapper mapper;

    @Autowired
    public FuzzySearchService(ElasticSearchRepository elasticSearchRepository, ObjectMapper mapper) {
        this.elasticSearchRepository = elasticSearchRepository;
        this.mapper = mapper;
    }


    public List<Property> getProperties(FuzzySearchCriteria criteria) {

        Object esResponse = elasticSearchRepository.fuzzySearchProperties(criteria);

        Map<String, List<String>> tenantIdToPropertyId = getTenantIdToPropertyIdMap(esResponse);

        System.out.println(tenantIdToPropertyId);

        List<Property> properties = new LinkedList<>();

        // Add property search code


        return properties;
    }


    /**
     * Creates a map of tenantId to propertyIds from es response
     * @param esResponse
     * @return
     */
    private Map<String, List<String>> getTenantIdToPropertyIdMap(Object esResponse) {

        List<Map<String, Object>> data;
        Map<String, List<String>> tenantIdToPropertyIds = new HashMap<>();


        try {
            data = JsonPath.read(esResponse, ES_DATA_PATH);


            if (!CollectionUtils.isEmpty(data)) {

                for (Map<String, Object> map : data) {

                    String tenantId = JsonPath.read(map, "$.tenantData.code");
                    String propertyId = JsonPath.read(map, "$.propertyId");

                    if (tenantIdToPropertyIds.containsKey(tenantId))
                        tenantIdToPropertyIds.get(tenantId).add(propertyId);
                    else {
                        List<String> propertyIds = new LinkedList<>();
                        propertyIds.add(propertyId);
                        tenantIdToPropertyIds.put(tenantId, propertyIds);
                    }

                }

            }

        } catch (Exception e) {
            throw new CustomException("PARSING_ERROR","Failed to extract propertyIds from es response");
        }

        return tenantIdToPropertyIds;
    }



}
