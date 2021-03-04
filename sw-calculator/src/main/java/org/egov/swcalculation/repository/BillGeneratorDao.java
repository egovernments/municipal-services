package org.egov.swcalculation.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.egov.swcalculation.producer.SWCalculationProducer;
import org.egov.swcalculation.repository.builder.SWCalculatorQueryBuilder;
import org.egov.swcalculation.repository.rowMapper.BillGenerateSchedulerRowMapper;
import org.egov.swcalculation.web.models.BillGenerationRequest;
import org.egov.swcalculation.web.models.BillGenerationSearchCriteria;
import org.egov.swcalculation.web.models.BillScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class BillGeneratorDao {

	@Autowired
	private SWCalculationProducer sWCalculationProducer;

	@Autowired
	private BillGenerateSchedulerRowMapper billGenerateSchedulerRowMapper;

	@Autowired
	private SWCalculatorQueryBuilder queryBuilder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Value("${egov.sw.billgenerate.scheduler}")
	private String createSWBillScheduler;

	public void saveBillGenertaionDetails(BillGenerationRequest billGenerationReq) {
		sWCalculationProducer.push(createSWBillScheduler, billGenerationReq);
	}

	public List<BillScheduler> getBillGenerationDetails(BillGenerationSearchCriteria criteria) {
		List<Object> preparedStatement = new ArrayList<>();

		String query = queryBuilder.getBillGenerationSchedulerQuery(criteria, preparedStatement);
		if (query == null)
			return Collections.emptyList();
		log.debug("Prepared Statement" + preparedStatement.toString());
		return jdbcTemplate.query(query, preparedStatement.toArray(), billGenerateSchedulerRowMapper);
	}
}
