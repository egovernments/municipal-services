package org.egov.vehicle.repository.querybuilder;

import java.util.List;

import javax.validation.Valid;

import org.egov.vehicle.config.VehicleConfiguration;
import org.egov.vehicle.web.model.Vehicle;
import org.egov.vehicle.web.model.VehicleRequest;
import org.egov.vehicle.web.model.VehicleSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class QueryBuilder {

	@Autowired
	VehicleConfiguration config;
	
	
	private final String paginationWrapper = "{} {orderby} {pagination}";
	private static final String Query = " SELECT count(*) OVER() AS full_count, * FROM eg_vehicle ";
	private static final String VEH_EXISTS_QUERY=" SELECT COUNT(*) FROM eg_vehicle WHERE tenantid=? AND registrationNumber=?";
	
	/**
	 * 
	 * @param query
	 *            prepared Query
	 * @param preparedStmtList
	 *            values to be replased on the query
	 * @param criteria
	 *            fsm search criteria
	 * @return the query by replacing the placeholders with preparedStmtList
	 */
	private String addPaginationWrapper(String query, List<Object> preparedStmtList, VehicleSearchCriteria criteria) {
		
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
		addOrderByClause(orderQuery,criteria);
		finalQuery = finalQuery.replace("{orderby}", orderQuery.toString()); 
		
		if (limit == -1) {
			finalQuery = finalQuery.replace("{pagination}", ""); 
		} else {
			finalQuery = finalQuery.replace("{pagination}", " offset ?  limit ?  "); 
			preparedStmtList.add(offset);
			preparedStmtList.add(limit );
		}
		
		return finalQuery;

	}

	
	/**
	 * 
	 * @param builder
	 * @param criteria
	 */
	 private void addOrderByClause(StringBuilder builder, VehicleSearchCriteria criteria){

	        if(StringUtils.isEmpty(criteria.getSortBy()))
	            builder.append( " ORDER BY lastmodifiedtime ");

	        else if(criteria.getSortBy()== VehicleSearchCriteria.SortBy.type)
	            builder.append(" ORDER BY type ");

	        else if(criteria.getSortBy()== VehicleSearchCriteria.SortBy.model)
	            builder.append(" ORDER BY model ");
	        
	        else if(criteria.getSortBy()== VehicleSearchCriteria.SortBy.suctionType)
	            builder.append(" ORDER BY  suctionType");
	        
	        else if(criteria.getSortBy()== VehicleSearchCriteria.SortBy.createdTime)
	            builder.append(" ORDER BY createdTime ");
	        
	        else if(criteria.getSortBy()== VehicleSearchCriteria.SortBy.fitnessValidTill)
	            builder.append(" ORDER BY fitnessValidTill ");
	        
	        else if(criteria.getSortBy()== VehicleSearchCriteria.SortBy.InsuranceCertValidTill)
	            builder.append(" ORDER BY InsuranceCertValidTill ");


	        else if(criteria.getSortBy()== VehicleSearchCriteria.SortBy.pollutionCertiValidTill)
	            builder.append(" ORDER BY pollutionCertiValidTill ");
	        

	        else if(criteria.getSortBy()== VehicleSearchCriteria.SortBy.roadTaxPaidTill)
	            builder.append(" ORDER BY roadTaxPaidTill ");

	        else if(criteria.getSortBy()== VehicleSearchCriteria.SortBy.tankCapicity)
	            builder.append(" ORDER BY tankCapicity ");
	        
	        
	        if(criteria.getSortOrder()== VehicleSearchCriteria.SortOrder.ASC)
	            builder.append(" ASC ");
	        else builder.append(" DESC ");

	    }
	private void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" AND");
		}
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

	public String getSearchQuery(@Valid VehicleSearchCriteria criteria, List<Object> preparedStmtList) {
		
		StringBuilder builder = new StringBuilder(Query);
		if(criteria.getTenantId() != null) {
			if(criteria.getTenantId().split("\\.").length == 1) {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" tenantid like ?");
				preparedStmtList.add('%' + criteria.getTenantId() + '%');
			}else {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" tenantid=? ");
				preparedStmtList.add(criteria.getTenantId());
			}
		}
		
		if( criteria.getTankCapacity() !=null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" tankcapicity = ?");
			preparedStmtList.add(criteria.getTankCapacity());
		}
		
		List<String> ownerIds = criteria.getOwnerId();
		if (!CollectionUtils.isEmpty(ownerIds)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" owner_id IN (").append(createQuery(ownerIds)).append(")");
			addToPreparedStatement(preparedStmtList, ownerIds);
		}
		
		
		
		List<String> suctionTypes = criteria.getSuctionType();
		if (!CollectionUtils.isEmpty(suctionTypes)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" suctionType IN (").append(createQuery(suctionTypes)).append(")");
			addToPreparedStatement(preparedStmtList, suctionTypes);
		}
		
		List<String> model = criteria.getModel();
		if (!CollectionUtils.isEmpty(model)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" model IN (").append(createQuery(model)).append(")");
			addToPreparedStatement(preparedStmtList, model);
		}
		
		List<String> type = criteria.getType();
		if (!CollectionUtils.isEmpty(type)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" type IN (").append(createQuery(type)).append(")");
			addToPreparedStatement(preparedStmtList, type);
		}
		
		List<String> registrationNumber = criteria.getRegistrationNumber();
		if (!CollectionUtils.isEmpty(registrationNumber)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" registrationNumber IN (").append(createQuery(registrationNumber)).append(")");
			addToPreparedStatement(preparedStmtList, registrationNumber);
		}
		
		List<String> ids = criteria.getIds();
		if (!CollectionUtils.isEmpty(ids)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" id IN (").append(createQuery(ids)).append(")");
			addToPreparedStatement(preparedStmtList, ids);
		}
		
		
		
		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);
	}
	
	public String vehicleExistsQuery(VehicleRequest vehicleReq, List<Object> preparedStmtList) {
		Vehicle vehicle = vehicleReq.getVehicle();
		preparedStmtList.add(vehicle.getTenantId());
		preparedStmtList.add(vehicle.getRegistrationNumber());
		return VEH_EXISTS_QUERY;
	}
}
