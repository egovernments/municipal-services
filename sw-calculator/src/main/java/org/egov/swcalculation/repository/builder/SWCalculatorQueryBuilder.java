package org.egov.swcalculation.repository.builder;

import java.util.List;

import org.egov.swcalculation.constants.SWCalculationConstant;
import org.egov.swcalculation.web.models.BillGenerationSearchCriteria;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

@Controller
public class SWCalculatorQueryBuilder {
	
	private static final String connectionNoListQuery = "SELECT distinct(conn.connectionno),sw.connectionexecutiondate FROM eg_sw_connection conn INNER JOIN eg_sw_service sw ON conn.id = sw.connection_id";
	
	private static final String distinctTenantIdsCriteria = "SELECT distinct(tenantid) FROM eg_sw_connection sw";

	private static final String billGenerationSchedulerSearchQuery = "SELECT * from eg_sw_scheduler ";

	private static final String BILL_SCHEDULER_STATUS_UPDATE_QUERY = "UPDATE eg_sw_scheduler SET status=? where id=?";

	private static final String connectionNoByLocality = "SELECT distinct(conn.connectionno) FROM eg_sw_connection conn INNER JOIN eg_sw_service ws ON conn.id = ws.connection_id ";

	private static final String fiterConnectionBasedOnTaxPeriod =" AND conn.connectionno not in (select distinct consumercode from egbs_demand_v1 d ";

	private static final String BILL_SCHEDULER_STATUS_SEARCH_QUERY = "select status from eg_sw_scheduler ";
	
	private static final String LAST_DEMAND_GEN_FOR_CONN =" SELECT d.taxperiodfrom FROM egbs_demand_v1 d ";
	
	private static final String isConnectionDemandAvailableForBillingCycle ="select EXISTS (select 1 from egbs_demand_v1 d ";

	public String getDistinctTenantIds() {
		return distinctTenantIdsCriteria;
	}

	public String getConnectionNumberList(String tenantId, String connectionType, String status, Long taxPeriodFrom, Long taxPeriodTo, List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(connectionNoListQuery);
		// Add connection type
		addClauseIfRequired(preparedStatement, query);
		query.append(" sw.connectiontype = ? ");
		preparedStatement.add(connectionType);
		
		//Add status
		addClauseIfRequired(preparedStatement, query);
		query.append(" conn.status = ? ");
		preparedStatement.add(status);
		
		//Get the activated connections status	
		addClauseIfRequired(preparedStatement, query);
		query.append(" conn.applicationstatus = ? ");
		preparedStatement.add(SWCalculationConstant.CONNECTION_ACTIVATED);
		

		// add tenantid
		addClauseIfRequired(preparedStatement, query);
		query.append(" conn.tenantid = ? ");
		preparedStatement.add(tenantId);
		
		//Added connection number for testing Anonymous User issue
		addClauseIfRequired(preparedStatement, query);
		query.append(" conn.connectionno ='0603001817' ");
		
		//Add not null condition
		addClauseIfRequired(preparedStatement, query);
		query.append(" conn.connectionno is not null");
		
		query.append(fetchConnectionsToBeGenerate(tenantId, taxPeriodFrom, taxPeriodTo, preparedStatement));

		return query.toString();
	}
	
	public String fetchConnectionsToBeGenerate(String tenantId, Long taxPeriodFrom, Long taxPeriodTo, List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(fiterConnectionBasedOnTaxPeriod);

		query.append(" WHERE d.tenantid = ? ");
		preparedStatement.add(tenantId);
		
		addClauseIfRequired(preparedStatement, query);
		query.append(" d.status = 'ACTIVE' ");
		
		addClauseIfRequired(preparedStatement, query);
		query.append(" d.businessservice = ? ");
		preparedStatement.add(SWCalculationConstant.SERVICE_FIELD_VALUE_SW);

		addClauseIfRequired(preparedStatement, query);
		query.append(" d.taxPeriodFrom = ? ");
		preparedStatement.add(taxPeriodFrom);
		
		addClauseIfRequired(preparedStatement, query);
		query.append(" d.taxPeriodTo = ? ) ");
		preparedStatement.add(taxPeriodTo);

		return query.toString();
	}

	private void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" AND ");
		}
	}
	
	public String getBillGenerationSchedulerQuery(BillGenerationSearchCriteria criteria,
			List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(billGenerationSchedulerSearchQuery);
		if (!StringUtils.isEmpty(criteria.getTenantId())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" tenantid= ? ");
			preparedStatement.add(criteria.getTenantId());
		}
		if (criteria.getLocality() != null) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" locality = ? ");
			preparedStatement.add(criteria.getLocality());
		}
		if (criteria.getStatus() != null) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" status = ? ");
			preparedStatement.add(criteria.getStatus());
		}
		if (criteria.getBillingcycleStartdate() != null) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" billingcyclestartdate >= ? ");
			preparedStatement.add(criteria.getBillingcycleStartdate());
		}
		if (criteria.getBillingcycleEnddate() != null) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" billingcycleenddate <= ? ");
			preparedStatement.add(criteria.getBillingcycleEnddate());
		}
		
		query.append(" ORDER BY createdtime ");

		return query.toString();
	}
	
	/**
	 * Bill expire query builder
	 * 
	 * @param billIds
	 * @param preparedStmtList
	 */
	public String getBillSchedulerUpdateQuery(String schedulerId, List<Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(BILL_SCHEDULER_STATUS_UPDATE_QUERY);

		return builder.toString();
	}

	public String getConnectionsNoByLocality(String tenantId, String connectionType,String status,String locality, List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(connectionNoByLocality);
		// add tenantid
		if(tenantId != null) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.tenantid = ? ");
			preparedStatement.add(tenantId);
		}
		
		// Add connection type
		if(connectionType != null) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" ws.connectiontype = ? ");
			preparedStatement.add(connectionType);
		}

		//Active status	
		if(status != null) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.status = ? ");
			preparedStatement.add(status);			
		}

//		addClauseIfRequired(preparedStatement, query);
//		query.append(" conn.connectionno = ? ");
//		preparedStatement.add("SW/107/2020-21/000018");

		if (locality != null) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" locality = ? ");
			preparedStatement.add(locality);
		}
		
		//Getting only non exempted connection to generate bill
		addClauseIfRequired(preparedStatement, query);
		query.append(" (conn.additionaldetails->>'isexempted')::boolean is false ");

		addClauseIfRequired(preparedStatement, query);
		query.append(" conn.connectionno is not null");
		return query.toString();

	}
	
	public String getBillSchedulerSearchQuery(String locality, Long billFromDate, Long billToDate, String tenantId,
			List<Object> preparedStmtList) {

		StringBuilder query = new StringBuilder(BILL_SCHEDULER_STATUS_SEARCH_QUERY);

		addClauseIfRequired(preparedStmtList, query);
		query.append(" tenantid = ? ");
		preparedStmtList.add(tenantId);

		if (locality != null) {
			addClauseIfRequired(preparedStmtList, query);
			query.append(" locality = ? ");
			preparedStmtList.add(locality);
		}
		if (billFromDate != null) {
			addClauseIfRequired(preparedStmtList, query);
			query.append(" billingcyclestartdate = ? ");
			preparedStmtList.add(billFromDate);
		}
		if (billToDate != null) {
			addClauseIfRequired(preparedStmtList, query);
			query.append(" billingcycleenddate = ? ");
			preparedStmtList.add(billToDate);
		}
		

		return query.toString();
	}
	
	public String searchLastDemandGenFromDate(String consumerCode, String tenantId, List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(LAST_DEMAND_GEN_FOR_CONN);

		addClauseIfRequired(preparedStatement, query);
		query.append(" d.businessservice = ? ");
		preparedStatement.add(SWCalculationConstant.SERVICE_FIELD_VALUE_SW);

		addClauseIfRequired(preparedStatement, query);
		query.append(" d.tenantid = ? ");
		preparedStatement.add(tenantId);
		
		addClauseIfRequired(preparedStatement, query);
		query.append(" d.consumercode = ? ");
		preparedStatement.add(consumerCode);
		
		addClauseIfRequired(preparedStatement, query);
		query.append(" d.status = 'ACTIVE' ");
		
		query.append(" ORDER BY d.taxperiodfrom desc limit 1 ");
		
		return query.toString();
	}
	
	public String isConnectionDemandAvailableForBillingCycle(String tenantId, Long taxPeriodFrom, Long taxPeriodTo, String consumerCode, List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(isConnectionDemandAvailableForBillingCycle);

		addClauseIfRequired(preparedStatement, query);
		query.append(" d.tenantid = ? ");
		preparedStatement.add(tenantId);
		
		addClauseIfRequired(preparedStatement, query);
		query.append(" d.consumercode = ? ");
		preparedStatement.add(consumerCode);
		
		addClauseIfRequired(preparedStatement, query);
		query.append(" d.status = 'ACTIVE' ");
		
		addClauseIfRequired(preparedStatement, query);
		query.append(" d.taxPeriodFrom = ? ");
		preparedStatement.add(taxPeriodFrom);
		
		addClauseIfRequired(preparedStatement, query);
		query.append(" d.taxPeriodTo = ? ");
		preparedStatement.add(taxPeriodTo);
		
		addClauseIfRequired(preparedStatement, query);
		query.append(" d.businessservice = ? ) ");
		preparedStatement.add(SWCalculationConstant.SERVICE_FIELD_VALUE_SW);

		
		return query.toString();
	}
}
