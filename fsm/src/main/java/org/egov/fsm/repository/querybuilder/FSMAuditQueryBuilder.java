package org.egov.fsm.repository.querybuilder;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.fsm.config.FSMConfiguration;
import org.egov.fsm.web.model.FSMAuditSearchCriteria;
import org.egov.fsm.web.model.FSMSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FSMAuditQueryBuilder {

	@Autowired
	private FSMConfiguration config;

	private static final String Query = "select fsm.*,fsm_address.*,fsm_geo.*,fsm_pit.*,fsm.id as fsm_id, fsm.createdby as fsm_createdby,"
			+ "  fsm.lastmodifiedby as fsm_lastmodifiedby, fsm.createdtime as fsm_createdtime, fsm.lastmodifiedtime as fsm_lastmodifiedtime,"
			+ "	 fsm.additionaldetails,fsm_address.id as fsm_address_id,fsm_geo.id as fsm_geo_id,"
			+ "	 fsm_pit.id as fsm_pit_id" + "	 FROM eg_fsm_application fsm"
			+ "	 INNER JOIN   eg_fsm_address fsm_address on fsm_address.fsm_id = fsm.id"
			+ "	 LEFT OUTER JOIN  eg_fsm_geolocation fsm_geo on fsm_geo.address_id = fsm_address.id"
			+ "	 LEFT OUTER JOIN  eg_fsm_pit_detail fsm_pit on fsm_pit.fsm_id = fsm.id where fsm.tenantid='%s'";
	
	private static final String FSM_ID = " AND fsm.id='%s'";
	private static final String APPLICATION_NO = " AND fsm.applicationno='%s'";

	public String getFSMActualDataQuery(FSMAuditSearchCriteria criteria) {
		return generateQuery(criteria);
	}
	
	private String generateQuery(FSMAuditSearchCriteria criteria) {
		StringBuilder fsmDataQuery = new StringBuilder(String.format(Query, criteria.getTenantId()));
		if (StringUtils.isNotEmpty(criteria.getId())) {
			fsmDataQuery = fsmDataQuery.append(String.format(FSM_ID, criteria.getId()));
		}
		if (StringUtils.isNotEmpty(criteria.getApplicationNumber())) {
			fsmDataQuery = fsmDataQuery.append(String.format(APPLICATION_NO, criteria.getApplicationNumber()));
		}
		fsmDataQuery.append(" order by fsm.lastmodifiedtime desc");
		return fsmDataQuery.toString();
	}
	
	public String getFSMAuditDataQuery(FSMAuditSearchCriteria criteria) {
		return generateQuery(criteria).replace("eg_fsm_application", "eg_fsm_application_auditlog").replace("eg_fsm_address", "eg_fsm_address_auditlog").replace("eg_fsm_geolocation", "eg_fsm_geolocation_auditlog").replace("eg_fsm_pit_detail", "eg_fsm_pit_detail_auditlog");
	}

}
