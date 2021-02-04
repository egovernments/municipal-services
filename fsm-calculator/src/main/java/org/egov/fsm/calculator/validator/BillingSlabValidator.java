package org.egov.fsm.calculator.validator;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.fsm.calculator.repository.BillingSlabRepository;
import org.egov.fsm.calculator.repository.querybuilder.BillingSlabQueryBuilder;
import org.egov.fsm.calculator.utils.BillingSlabUtil;
import org.egov.fsm.calculator.utils.CalculatorConstants;
import org.egov.fsm.calculator.web.models.BillingSlab;
import org.egov.fsm.calculator.web.models.BillingSlabRequest;
import org.egov.fsm.calculator.web.models.BillingSlabSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BillingSlabValidator {

	@Autowired
	private BillingSlabQueryBuilder queryBuilder;

	@Autowired
	private BillingSlabRepository repository;

	@Autowired
	private BillingSlabUtil util;

	@Autowired
	private MDMSValidator mdmsValidator;

	public void validateCreate(BillingSlabRequest request) {
		validateInputs(request);
		String query = queryBuilder.getBillingSlabCombinationCountQuery(request.getBillingSlab().getCapacityFrom(),
				request.getBillingSlab().getCapacityTo(), request.getBillingSlab().getPropertyType(),
				request.getBillingSlab().getSlum());
		int count = repository.getDataCount(query);
		if (count >= 1) {
			throw new CustomException(CalculatorConstants.INVALID_BILLING_SLAB_ERROR,
					"Billing Slab already exits with the given combination of capacityType, capacityFrom, propertyType and slum");
		}
	}

	public void validateUpdate(BillingSlabRequest request) {
		if (StringUtils.isEmpty(request.getBillingSlab().getId())) {
			throw new CustomException(CalculatorConstants.INVALID_BILLING_SLAB_ERROR, "id is mandatory");
		}
		
		validateInputs(request);
		String queryForBillingSlab = queryBuilder.getBillingSlabExistQuery(request.getBillingSlab().getId());
		int count = repository.getDataCount(queryForBillingSlab);
		if (count <= 0) {
			throw new CustomException(CalculatorConstants.INVALID_BILLING_SLAB_ERROR, "Billing Slab not found");
		}

		String query = queryBuilder.getBillingSlabCombinationCountForUpdateQuery(
				request.getBillingSlab().getCapacityFrom(), request.getBillingSlab().getCapacityTo(),
				request.getBillingSlab().getPropertyType(), request.getBillingSlab().getSlum(),
				request.getBillingSlab().getId());
		int combinationCount = repository.getDataCount(query);
		if (combinationCount >= 1) {
			throw new CustomException(CalculatorConstants.INVALID_BILLING_SLAB_ERROR,
					"Billing Slab already exits with the given combination of capacityType, capacityFrom, propertyType and slum");
		}
	}

	public void validateInputs(BillingSlabRequest request) {
		
		if (StringUtils.isEmpty(request.getBillingSlab().getTenantId())) {
			throw new CustomException(CalculatorConstants.INVALID_BILLING_SLAB_ERROR, "TenantId is mandatory");
		}

		if (request.getBillingSlab().getCapacityFrom() == null) {
			throw new CustomException(CalculatorConstants.INVALID_BILLING_SLAB_ERROR, "CapacityFrom is mandatory");
		}

		if (request.getBillingSlab().getCapacityTo() == null) {
			throw new CustomException(CalculatorConstants.INVALID_BILLING_SLAB_ERROR, "CapacityTo is mandatory");
		}

		if (StringUtils.isEmpty(request.getBillingSlab().getPropertyType())) {
			throw new CustomException(CalculatorConstants.INVALID_BILLING_SLAB_ERROR, "PropertyType is mandatory");
		}

		if (request.getBillingSlab().getPrice() == null) {
			throw new CustomException(CalculatorConstants.INVALID_BILLING_SLAB_ERROR, "Price is mandatory");
		}

		if (StringUtils.isEmpty(request.getBillingSlab().getSlum())) {
			throw new CustomException(CalculatorConstants.INVALID_BILLING_SLAB_ERROR, "Slum is mandatory");
		}

		if (request.getBillingSlab().getCapacityFrom() < request.getBillingSlab().getCapacityTo()) {
			throw new CustomException(CalculatorConstants.INVALID_BILLING_SLAB_ERROR,
					"CapacityFrom cannot be less than capacityTo");
		}

		Object mdmsData = util.mDMSCall(request.getRequestInfo(), request.getBillingSlab().getTenantId());
		mdmsValidator.validateMdmsData(mdmsData);
		mdmsValidator.validatePropertyType(request.getBillingSlab().getPropertyType());
	}
	
	
	public void validateSearch(BillingSlabSearchCriteria criteria, RequestInfo requestInfo) {
		if (StringUtils.isEmpty(criteria.getTenantId())) {
			throw new CustomException(CalculatorConstants.INVALID_BILLING_SLAB_ERROR, "TenantId is mandatory");
		}
		
		Object mdmsData = util.mDMSCall(requestInfo, criteria.getTenantId());
		mdmsValidator.validateMdmsData(mdmsData);
		mdmsValidator.validatePropertyType(criteria.getPropertyType());
	}

}
