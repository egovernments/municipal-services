package org.egov.echallan.expense.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.echallan.config.ChallanConfiguration;
import org.egov.echallan.expense.model.Expense;
import org.egov.echallan.expense.model.ExpenseRequest;
import org.egov.echallan.expense.web.models.calculation.CalulationCriteria;
import org.egov.echallan.model.Challan;
import org.egov.echallan.repository.ServiceRequestRepository;
import org.egov.echallan.web.models.calculation.Calculation;
import org.egov.echallan.expense.web.models.calculation.CalculationReq;
import org.egov.echallan.web.models.calculation.CalculationRes;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class ExpenseCalculationService {


    private ServiceRequestRepository serviceRequestRepository;

    private ObjectMapper mapper;

    private ChallanConfiguration config;
    
    @Autowired
    public ExpenseCalculationService(ServiceRequestRepository serviceRequestRepository, ObjectMapper mapper,ChallanConfiguration config) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.mapper = mapper;
        this.config = config;
    }

	public Expense addCalculation(ExpenseRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		Expense expense = request.getExpense();

		if (expense == null)
			throw new CustomException("INVALID REQUEST", "The request for calculation cannot be empty or null");

		CalculationRes response = getCalculation(requestInfo, expense);
		List<Calculation> calculations = response.getCalculations();
		Map<String, Calculation> applicationNumberToCalculation = new HashMap<>();
		calculations.forEach(calculation -> {
			applicationNumberToCalculation.put(calculation.getChallan().getChallanNo(), calculation);
			calculation.setChallan(null);
		});

		expense.setCalculation(applicationNumberToCalculation.get(expense.getChallanNo()));

		return expense;
	}

    private CalculationRes getCalculation(RequestInfo requestInfo,Expense expense){
    	
    	StringBuilder uri = new StringBuilder();
        uri.append(config.getCalculatorHost());
        uri.append(config.getCalculateEndpoint());
        List<CalulationCriteria> criterias = new LinkedList<>();

         criterias.add(new CalulationCriteria(expense,expense.getChallanNo(),expense.getTenantId()));

        CalculationReq request = CalculationReq.builder().calulationCriteria(criterias)
                .requestInfo(requestInfo)
                .build();

        Object result = serviceRequestRepository.fetchResult(uri,request);
        CalculationRes response = null;
        try{
            response = mapper.convertValue(result,CalculationRes.class);
        }
        catch (IllegalArgumentException e){
            throw new CustomException("PARSING ERROR","Failed to parse response of calculate");
        } 
        return response;
    }

}
