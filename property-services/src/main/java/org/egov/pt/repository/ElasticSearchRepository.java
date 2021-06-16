package org.egov.pt.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.repository.builder.FuzzySearchQueryBuilder;
import org.egov.pt.web.contracts.FuzzySearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ElasticSearchRepository {


    private PropertyConfiguration config;

    private FuzzySearchQueryBuilder queryBuilder;

    private RestTemplate restTemplate;

    private ObjectMapper mapper;

    @Autowired
    public ElasticSearchRepository(PropertyConfiguration config, FuzzySearchQueryBuilder queryBuilder, RestTemplate restTemplate, ObjectMapper mapper) {
        this.config = config;
        this.queryBuilder = queryBuilder;
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }


    /**
     * Searches records from elasticsearch based on the fuzzy search criteria
     *
     * @param criteria
     * @return
     */
    public Object fuzzySearchProperties(FuzzySearchCriteria criteria) {


        String url = getESURL();

        String searchQuery = queryBuilder.getFuzzySearchQuery(criteria);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(searchQuery, headers);
        ResponseEntity response = null;
        try {
             response = restTemplate.postForEntity(url, requestEntity, Object.class);

        } catch (Exception e) {

            throw new CustomException("ES_ERROR","Failed to fetch data from ES");
        }

        return response.getBody();

    }


    /**
     * Generates elasticsearch search url from application properties
     *
     * @return
     */
    private String getESURL() {

        StringBuilder builder = new StringBuilder(config.getEsHost());
        builder.append(config.getEsPTIndex());
        builder.append(config.getEsSearchEndpoint());

        return builder.toString();
    }


}
