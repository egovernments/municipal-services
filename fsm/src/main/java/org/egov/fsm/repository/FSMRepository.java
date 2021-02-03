package org.egov.fsm.repository;


import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.fsm.config.FSMConfiguration;
import org.egov.fsm.producer.Producer;
import org.egov.fsm.repository.querybuilder.FSMQueryBuilder;
import org.egov.fsm.repository.rowmapper.FSMRowMapper;
import org.egov.fsm.web.model.FSM;
import org.egov.fsm.web.model.FSMRequest;
import org.egov.fsm.web.model.FSMSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class FSMRepository {

	@Autowired
	private FSMConfiguration config;
	
    @Autowired
	private Producer producer;
    
    @Autowired
    private FSMQueryBuilder fsmQueryBuilder;
    
    @Autowired
	private JdbcTemplate jdbcTemplate;
    
    
    @Autowired
	private FSMRowMapper FSMrowMapper;
	
	
	public void save(FSMRequest fsmRequest) {
		producer.push(config.getSaveTopic(), fsmRequest);
	}
	
	public void update(FSMRequest fsmRequest, boolean isStateUpdatable) {
		RequestInfo requestInfo = fsmRequest.getRequestInfo();

		FSM fsmForStatusUpdate = null;
		FSM fsmForUpdate = null;

		FSM fsm = fsmRequest.getFsm();

		if (isStateUpdatable) {
			fsmForUpdate = fsm;
		} else {
			fsmForStatusUpdate = fsm;
		}
		if (fsmForUpdate != null)
			producer.push(config.getUpdateTopic(), new FSMRequest(requestInfo, fsmForUpdate, null));

		if (fsmForStatusUpdate != null)
			producer.push(config.getUpdateWorkflowTopic(), new FSMRequest(requestInfo, fsmForStatusUpdate,null));

	}
	
	
	
	public List<FSM> getFSMData(FSMSearchCriteria fsmSearchCriteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = fsmQueryBuilder.getFSMSearchQuery(fsmSearchCriteria, preparedStmtList);
		List<FSM> FSMData = jdbcTemplate.query(query, preparedStmtList.toArray(), FSMrowMapper);
		return FSMData;
	}
}
