package org.egov.wsCalculation.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.wsCalculation.constants.WSCalculationConstant;
import org.egov.wsCalculation.model.Assessment;
import org.egov.wsCalculation.model.AuditDetails;
import org.egov.wsCalculation.model.Demand;
import org.egov.wsCalculation.repository.AssessmentRepository;
import org.egov.wsCalculation.util.WSCalculationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssessmentService {
	@Autowired
	private AssessmentRepository repository;

	@Autowired
	private WSCalculationUtil utils;

	/**
	 * Returns the latest assessment for the given criteria
	 * 
	 * @param assessment
	 * @return
	 */
	public List<Assessment> getMaxAssessment(Assessment assessment) {

		List<Object> preparedStatementList = new ArrayList<>();
		String query = utils.getMaxAssessmentQuery(assessment, preparedStatementList);
		return repository.getAssessments(query, preparedStatementList.toArray());
	}

	/**
	 * persists the assessments
	 * 
	 * adds the data to the respective kafka topic
	 * 
	 * @param demands
	 * @param info
	 */
	public List<Assessment> saveAssessments(List<Demand> demands, Map<String, String> consumerCodeFinYearMap,
			RequestInfo info) {

		List<Assessment> assessments = new ArrayList<>();

		AuditDetails details = utils.getAuditDetails(info.getUserInfo().getId().toString(), true);
		demands.forEach(demand -> {

			String[] consumerCodeSplitArray = demand.getConsumerCode()
					.split(WSCalculationConstant.WS_CONSUMER_CODE_SEPARATOR);
			assessments.add(Assessment.builder().connectionId(consumerCodeSplitArray[0])
					.assessmentYear(consumerCodeFinYearMap.get(demand.getConsumerCode()))
					.uuid(UUID.randomUUID().toString()).assessmentNumber(consumerCodeSplitArray[1])
					.tenantId(demand.getTenantId()).demandId(demand.getId()).auditDetails(details).build());
		});
		return repository.saveAssessments(assessments, info);
	}

}
