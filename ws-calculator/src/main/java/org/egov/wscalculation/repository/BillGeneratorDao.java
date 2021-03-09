package org.egov.wscalculation.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.egov.wscalculation.producer.WSCalculationProducer;
import org.egov.wscalculation.repository.builder.WSCalculatorQueryBuilder;
import org.egov.wscalculation.repository.rowmapper.BillGenerateSchedulerRowMapper;
import org.egov.wscalculation.web.models.BillGenerationReq;
import org.egov.wscalculation.web.models.BillGenerationSearchCriteria;
import org.egov.wscalculation.web.models.BillScheduler;
import org.egov.wscalculation.web.models.BillScheduler.StatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class BillGeneratorDao {

	@Autowired
	private WSCalculationProducer wSCalculationProducer;

	@Autowired
	private BillGenerateSchedulerRowMapper billGenerateSchedulerRowMapper;

	@Autowired
	private WSCalculatorQueryBuilder queryBuilder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Value("${egov.billgenerate.scheduler}")
	private String createBillScheduler;

	public void saveBillGenertaionDetails(BillGenerationReq billGenerationReq) {
		wSCalculationProducer.push(createBillScheduler, billGenerationReq);
	}

	public List<BillScheduler> getBillGenerationDetails(BillGenerationSearchCriteria criteria) {
		List<Object> preparedStatement = new ArrayList<>();

		String query = queryBuilder.getBillGenerationSchedulerQuery(criteria, preparedStatement);
		if (query == null)
			return Collections.emptyList();
		log.debug("Prepared Statement" + preparedStatement.toString());
		return jdbcTemplate.query(query, preparedStatement.toArray(), billGenerateSchedulerRowMapper);
	}
	
	/**
	 * executes query to update bill scheduler status 
	 * @param billIds
	 */
	public void updateBillSchedularStatus(String schedulerId, StatusEnum status) {

		List<Object> preparedStmtList = new ArrayList<>();
		preparedStmtList.add(status.toString());
		preparedStmtList.add(schedulerId.toString());
		String queryStr = queryBuilder.getBillSchedulerUpdateQuery(schedulerId, preparedStmtList);
		jdbcTemplate.update(queryStr, preparedStmtList.toArray());
	}
}
