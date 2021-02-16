package org.egov.fsm.calculator.repository.querybuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.egov.fsm.calculator.config.BillingSlabConfig;
import org.egov.fsm.calculator.web.models.BillingSlab;
import org.egov.fsm.calculator.web.models.BillingSlabSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
public class BillingSlabQueryBuilder {

	private static final String QUERY_BILLINGSLAB_COMBINATION_COUNT = "SELECT count(*) FROM eg_billing_slab where tenantid=? AND capacityfrom=? AND capacityto=? AND propertytype=? AND slum=?";
	private static final String QUERY_BILLINGSLAB_COMBINATION_For_UPDATE_COUNT = "SELECT count(*) FROM eg_billing_slab where tenantid=? AND capacityfrom=? AND capacityto=? AND propertytype=? AND slum=? AND id!=?";
	private static final String QUERY_BILLINGSLAB_EXIST = "SELECT count(*) FROM eg_billing_slab where id =?";
	private static final String QUERY_BILLING_SLAB_SEARCH = "SELECT * FROM eg_billing_slab where tenantid=?";
	private final String paginationWrapper = "{} {orderby} OFFSET ? LIMIT ?";
	private static final String QUERY_BILLINGSLAB_PRICE = "SELECT price FROM eg_billing_slab where tenantid=? AND capacityto>=? AND capacityfrom<=?";
	private static final String QUERY_PARAM_FOR_SLUM = " AND slum=?";

	@Autowired
	private BillingSlabConfig config;

	public String getBillingSlabPriceQuery(String tenantId, Double capacity, String slumName, List<Object> preparedStmtList) {
		String query = QUERY_BILLINGSLAB_PRICE;
		preparedStmtList.add(tenantId);
		preparedStmtList.add(capacity);
		preparedStmtList.add(capacity);
		if(org.apache.commons.lang3.StringUtils.isEmpty(slumName)) {
			query = query + QUERY_PARAM_FOR_SLUM;
			preparedStmtList.add("NO");
		}else {
			query = query + QUERY_PARAM_FOR_SLUM;
			preparedStmtList.add("YES");
		}
		return QUERY_BILLINGSLAB_PRICE;
	} 
	
	public String getBillingSlabCombinationCountQuery(String tenantId, BigDecimal capacityFrom, BigDecimal capacityTo, String propertType,
			String slum, List<Object> preparedStmtList) {
		preparedStmtList.add(tenantId);
		preparedStmtList.add(capacityFrom);
		preparedStmtList.add(capacityTo);
		preparedStmtList.add(propertType);
		preparedStmtList.add(slum);
		return QUERY_BILLINGSLAB_COMBINATION_COUNT;
		
	}

	public String getBillingSlabCombinationCountForUpdateQuery(String tenantId, BigDecimal capacityFrom, BigDecimal capacityTo,
			String propertType, String slum, String id, List<Object> preparedStmtList) {
		preparedStmtList.add(tenantId);
		preparedStmtList.add(capacityFrom);
		preparedStmtList.add(capacityTo);
		preparedStmtList.add(propertType);
		preparedStmtList.add(slum);
		preparedStmtList.add(id);
		return QUERY_BILLINGSLAB_COMBINATION_For_UPDATE_COUNT;
	}

	private String convertListToString(List<String> namesList) {
		return String.join(",", namesList.stream().map(name -> ("'" + name + "'")).collect(Collectors.toList()));
	}

	public String getBillingSlabExistQuery(String id, List<Object> preparedStmtList) {
		preparedStmtList.add(id);
		return QUERY_BILLINGSLAB_EXIST;
	}
	
	public String getBillingSlabSearchQuery(BillingSlabSearchCriteria criteria, List<Object> preparedStmtList) {
		StringBuilder query = new StringBuilder(String.format(QUERY_BILLING_SLAB_SEARCH, criteria.getTenantId()));
		if (!CollectionUtils.isEmpty(criteria.getIds())) {
			query.append(" AND id IN(").append(convertListToString(criteria.getIds())).append(")");
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(criteria.getPropertyType())) {
			query.append(" AND propertytype=?");
			preparedStmtList.add(criteria.getPropertyType());
		}
		if(criteria.getCapacity() != null) {
			query.append(" AND capacityto>=? AND capacityfrom<=?");
			preparedStmtList.add(criteria.getCapacity());
			preparedStmtList.add(criteria.getCapacity());
		}
		
		if(criteria.getSlum() != null) {
			query.append(" AND slum=?");
			preparedStmtList.add(criteria.getSlum().toString());
		}
		return addPaginationWrapper(query.toString(), preparedStmtList,  criteria);
	}

	private String addPaginationWrapper(String query, List<Object> preparedStmtList, BillingSlabSearchCriteria criteria) {

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
			finalQuery = finalQuery.replace("OFFSET ? LIMIT ?", "");
		} else {
			preparedStmtList.add(offset);
			preparedStmtList.add(limit + offset);
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
	
	private void addToPreparedStatement(List<Object> preparedStmtList, List<String> ids) {
		ids.forEach(id -> {
			preparedStmtList.add(id);
		});

	}

	private Object createQuery(List<String> ids) {
		StringBuilder builder = new StringBuilder();
		int length = ids.size();
		for (int i = 0; i < length; i++) {
			builder.append(" ?");
			if (i != length - 1)
				builder.append(",");
		}
		return builder.toString();
	}

}
