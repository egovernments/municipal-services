package org.egov.fsm.calculator.repository.querybuilder;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.egov.fsm.calculator.config.BillingSlabConfig;
import org.egov.fsm.calculator.web.models.BillingSlabSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
public class BillingSlabQueryBuilder {

	private static final String QUERY_BILLINGSLAB_COMBINATION_COUNT = "SELECT count(*) FROM eg_billing_slab where capacityfrom=%s AND capacityto=%s AND propertytype='%s' AND slum='%s'";
	private static final String QUERY_BILLINGSLAB_COMBINATION_For_UPDATE_COUNT = "SELECT count(*) FROM eg_billing_slab where capacityfrom=%s AND capacityto=%s AND propertytype='%s' AND slum='%s' AND id!='%s'";
	private static final String QUERY_BILLINGSLAB_EXIST = "SELECT count(*) FROM eg_billing_slab where id ='%s'";
	private static final String QUERY_BILLING_SLAB_SEARCH = "SELECT * FROM eg_billing_slab where tenantid='%s'";
	private final String paginationWrapper = "{} {orderby} OFFSET %s LIMIT %s";

	@Autowired
	private BillingSlabConfig config;

	public String getBillingSlabCombinationCountQuery(Integer capacityFrom, Integer capacityTo, String propertType,
			String slum) {
		return String.format(QUERY_BILLINGSLAB_COMBINATION_COUNT, capacityFrom, capacityTo, propertType, slum);
	}

	public String getBillingSlabCombinationCountForUpdateQuery(Integer capacityFrom, Integer capacityTo,
			String propertType, String slum, String id) {
		return String.format(QUERY_BILLINGSLAB_COMBINATION_For_UPDATE_COUNT, capacityFrom, capacityTo, propertType,
				slum, id);
	}

	private String convertListToString(List<String> namesList) {
		return String.join(",", namesList.stream().map(name -> ("'" + name + "'")).collect(Collectors.toList()));
	}

	public String getBillingSlabExistQuery(String id) {
		return String.format(QUERY_BILLINGSLAB_EXIST, id);
	}
	
	public String getBillingSlabSearchQuery(BillingSlabSearchCriteria criteria) {
		StringBuilder query = new StringBuilder(String.format(QUERY_BILLING_SLAB_SEARCH, criteria.getTenantId()));
		if (!CollectionUtils.isEmpty(criteria.getIds())) {
			query.append(" AND id IN(").append(convertListToString(criteria.getIds())).append(")");
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(criteria.getPropertyType())) {
			query.append(String.format(" AND propertytype='%s'", criteria.getPropertyType()));
		}
		if(criteria.getCapacity() != null) {
			query.append(String.format(" AND capacityto>=%s AND capacityfrom<=%s", criteria.getCapacity(), criteria.getCapacity()));
		}
		return addPaginationWrapper(query.toString(), criteria);
	}

	private String addPaginationWrapper(String query, BillingSlabSearchCriteria criteria) {

		int limit = config.getDefaultLimit();
		int offset = config.getDefaultOffset();
		String finalQuery = paginationWrapper.replace("{}", query);

		if (criteria.getLimit() != null && criteria.getLimit() <= config.getMaxSearchLimit())
			limit = criteria.getLimit();

		if (criteria.getLimit() != null && criteria.getLimit() > config.getMaxSearchLimit()) {
			limit = config.getMaxSearchLimit();
		}

		if (criteria.getOffset() != null)
			offset = criteria.getOffset();

		StringBuilder orderQuery = new StringBuilder();
		addOrderByClause(orderQuery, criteria);
		finalQuery = finalQuery.replace("{orderby}", orderQuery.toString());

		if (limit == -1) {
			finalQuery = finalQuery.replace("OFFSET %s LIMIT %s", "");
		} else {
			finalQuery = String.format(finalQuery, offset, offset + limit);
		}

		return finalQuery;

	}

	private void addOrderByClause(StringBuilder builder, BillingSlabSearchCriteria criteria) {

		if (StringUtils.isEmpty(criteria.getSortBy()))
			builder.append(" ORDER BY lastmodifiedtime ");

		else if (criteria.getSortBy() == BillingSlabSearchCriteria.SortBy.id)
			builder.append(" ORDER BY id ");

		else if (criteria.getSortBy() == BillingSlabSearchCriteria.SortBy.propertyType)
			builder.append(" ORDER BY propertytype ");

		if (criteria.getSortOrder() == BillingSlabSearchCriteria.SortOrder.ASC)
			builder.append(" ASC ");
		else
			builder.append(" DESC ");

	}

}
