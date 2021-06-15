package org.egov.pt.repository.builder;

import org.egov.pt.web.contracts.FuzzySearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class FuzzySearchQueryBuilder {


    private static final String BASE_QUERY = "{\n" +
            "  \"_source\": \"propertyid\",\n" +
            "  \"query\": {\n" +
            "    \"match\": {\n" +
            "      \"name\": {\n" +
            "        \"query\": \"ani\",\n" +
            "        \"fuzziness\": \"7\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    /**
     * Builds a elasticsearch search query based on the fuzzy search criteria
     * @param criteria
     * @return
     */
    public String getFuzzySearchQuery(FuzzySearchCriteria criteria){

        StringBuilder builder = new StringBuilder(BASE_QUERY);

        return builder.toString();

    }

}
