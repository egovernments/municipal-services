package org.egov.vehicle.trip.querybuilder;

import java.util.List;
import java.util.stream.Collectors;

import org.egov.vehicle.config.VehicleConfiguration;
import org.egov.vehicle.trip.web.model.VehicleTripSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
public class VehicleTripQueryBuilder {

	private static final String QUERY_VEHICLE_LOG_EXIST = "SELECT count(*) FROM eg_vehicle_trip where id =? AND status='ACTIVE'";
	private static final String Query_SEARCH_VEHICLE_LOG = "select * from eg_vehicle_trip WHERE tenantid=?";
	private final String paginationWrapper = "{} {orderby}  OFFSET ? LIMIT ?";
	private static final String QUERY_TRIP_FROM_REF= "SELECT trip_id from eg_vehicle_trip_detail WHERE referenceno in ( %s )";
	private static final String QUERY_TRIP_DTL= "SELECT * FROM eg_vehicle_trip_detail WHERE trip_id = ? ";
	

	@Autowired
	private VehicleConfiguration config;



	private String convertListToString(List<String> namesList) {
		return String.join(",", namesList.stream().map(name -> ("'" + name + "'")).collect(Collectors.toList()));
	}



	public String getVehicleLogExistQuery(String vehicleLogId, List<Object> preparedStmtList) {
		preparedStmtList.add(vehicleLogId);
		return QUERY_VEHICLE_LOG_EXIST;
	}

	public String getVehicleLogSearchQuery(VehicleTripSearchCriteria criteria, List<Object> preparedStmtList) {
		StringBuilder query = new StringBuilder(String.format(Query_SEARCH_VEHICLE_LOG, criteria.getTenantId()));
		if (!CollectionUtils.isEmpty(criteria.getIds())) {
			query.append(" AND id IN(").append(convertListToString(criteria.getIds())).append(")");
		}
		if (!CollectionUtils.isEmpty(criteria.getVehicleIds())) {
			query.append(" AND vehicle_id IN(").append(convertListToString(criteria.getVehicleIds())).append(")");
		}
		
		if (!CollectionUtils.isEmpty(criteria.getDriverIds())) {
			query.append(" AND driver_id IN(").append(convertListToString(criteria.getDriverIds())).append(")");
		}
		
		if (!CollectionUtils.isEmpty(criteria.getTripOwnerIds())) {
			query.append(" AND owner_id IN(").append(convertListToString(criteria.getTripOwnerIds())).append(")");
		}
		
		if (!CollectionUtils.isEmpty(criteria.getApplicationNos())) {
			query.append(" AND applicationno IN(").append(convertListToString(criteria.getApplicationNos())).append(")");
		}
		
		if (!StringUtils.isEmpty(criteria.getBusinessService())) {
			query.append(" AND businessservice = ").append(criteria.getBusinessService());
		}
	
		if (!CollectionUtils.isEmpty(criteria.getApplicationStatus())) {
			query.append(" AND applicationstatus IN(").append(convertListToString(criteria.getApplicationStatus()))
					.append(")");
		}
		
		return addPaginationWrapper(query.toString(), criteria, preparedStmtList);
	}

	private String addPaginationWrapper(String query, VehicleTripSearchCriteria criteria, List<Object> preparedStmtList) {

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
	
	private void addOrderByClause(StringBuilder builder, VehicleTripSearchCriteria criteria){

        if(StringUtils.isEmpty(criteria.getSortBy()))
            builder.append( " ORDER BY lastmodifiedtime ");

        else if(criteria.getSortBy()== VehicleTripSearchCriteria.SortBy.applicationStatus)
            builder.append(" ORDER BY applicationStatus ");


        
        else if(criteria.getSortBy()== VehicleTripSearchCriteria.SortBy.vehicle)
            builder.append(" ORDER BY vehicle_id ");
        
        else if(criteria.getSortBy()== VehicleTripSearchCriteria.SortBy.createdTime)
            builder.append(" ORDER BY createdtime ");

        if(criteria.getSortOrder()== VehicleTripSearchCriteria.SortOrder.ASC)
            builder.append(" ASC ");
        else builder.append(" DESC ");

    }



	public String getTripIdFromReferenceNosQuery(List<String> refernceNos) {
		return String.format(QUERY_TRIP_FROM_REF,convertListToString(refernceNos));
	}



	public String getTripDetailSarchQuery(String tripId, List<Object> preparedStmtList) {
		preparedStmtList.add(tripId);
		return QUERY_TRIP_DTL;
	}

}
