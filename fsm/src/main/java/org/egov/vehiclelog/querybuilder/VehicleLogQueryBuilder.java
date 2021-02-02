package org.egov.vehiclelog.querybuilder;

import java.util.List;
import java.util.stream.Collectors;

import org.egov.fsm.web.model.FSMSearchCriteria;
import org.egov.vehiclelog.config.VehicleLogConfiguration;
import org.egov.vehiclelog.web.model.VehicleLogSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
public class VehicleLogQueryBuilder {

	private static final String QUERY_FSM_APPLICATION_COUNT = "SELECT count(id) FROM eg_fsm_application where id IN (%s)";
	private static final String QUERY_VEHICLE_LOG_EXIST = "SELECT count(*) FROM eg_vehicle_log where where id ='%s'";
	private static final String QUERY_VEHICLE_LOG_EXIST_FOR_VEHICLE = "SELECT count(*) FROM eg_vehicle_log where where vehicle_id ='%s' AND status='ACTIVE'";
	private static final String Query_SEARCH_VEHICLE_LOG = "select * from eg_vehicle_log inner join  eg_vehicle_log_fsm as vehicleLog_fsm on id=vehicleLog_fsm.vehicle_log_id where tenantid='%s'";
	private final String paginationWrapper = "{} {orderby} OFFSET %s LIMIT %s";

	@Autowired
	private VehicleLogConfiguration config;

	public String getFSMApplicationCountQuery(List<String> applicationNos) {
		return String.format(QUERY_FSM_APPLICATION_COUNT, convertListToString(applicationNos));
	}

	private String convertListToString(List<String> namesList) {
		return String.join(",", namesList.stream().map(name -> ("'" + name + "'")).collect(Collectors.toList()));
	}

	public String getVehicleLogExistQueryForVehicle(String vehicleId) {
		return String.format(QUERY_VEHICLE_LOG_EXIST_FOR_VEHICLE, vehicleId);
	}

	public String getVehicleLogExistQuery(String vehicleLogId) {
		return String.format(QUERY_VEHICLE_LOG_EXIST, vehicleLogId);
	}

	public String getVehicleLogSearchQuery(VehicleLogSearchCriteria criteria) {
		StringBuilder query = new StringBuilder(String.format(Query_SEARCH_VEHICLE_LOG, criteria.getTenantId()));
		if (!CollectionUtils.isEmpty(criteria.getIds())) {
			query.append(" AND id IN(").append(convertListToString(criteria.getIds())).append(")");
		}
		if (!CollectionUtils.isEmpty(criteria.getVehicleIds())) {
			query.append(" AND vehicle_id IN(").append(convertListToString(criteria.getVehicleIds())).append(")");
		}
		if (!CollectionUtils.isEmpty(criteria.getDsoIds())) {
			query.append(" AND dso_id IN(").append(convertListToString(criteria.getDsoIds())).append(")");
		}
		if (!CollectionUtils.isEmpty(criteria.getApplicationStatus())) {
			query.append(" AND applicationstatus IN(").append(convertListToString(criteria.getApplicationStatus()))
					.append(")");
		}
		if (!CollectionUtils.isEmpty(criteria.getFsmIds())) {
			query.append(" AND fsm_id IN(").append(convertListToString(criteria.getFsmIds())).append(")");
		}
		return addPaginationWrapper(query.toString(), criteria);
	}

	private String addPaginationWrapper(String query, VehicleLogSearchCriteria criteria) {

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
			finalQuery = String.format(finalQuery, offset, offset+limit);
		}

		return finalQuery;

	}
	
	private void addOrderByClause(StringBuilder builder, VehicleLogSearchCriteria criteria){

        if(StringUtils.isEmpty(criteria.getSortBy()))
            builder.append( " ORDER BY lastmodifiedtime ");

        else if(criteria.getSortBy()== VehicleLogSearchCriteria.SortBy.applicationStatus)
            builder.append(" ORDER BY applicationStatus ");

        else if(criteria.getSortBy()== VehicleLogSearchCriteria.SortBy.dso)
            builder.append(" ORDER BY dso_id ");
        
        else if(criteria.getSortBy()== VehicleLogSearchCriteria.SortBy.fsm)
            builder.append(" ORDER BY fsm_id ");
        
        else if(criteria.getSortBy()== VehicleLogSearchCriteria.SortBy.vehicle)
            builder.append(" ORDER BY vehicle_id ");
        
        else if(criteria.getSortBy()== VehicleLogSearchCriteria.SortBy.createdTime)
            builder.append(" ORDER BY createdtime ");

        if(criteria.getSortOrder()== VehicleLogSearchCriteria.SortOrder.ASC)
            builder.append(" ASC ");
        else builder.append(" DESC ");

    }

}
