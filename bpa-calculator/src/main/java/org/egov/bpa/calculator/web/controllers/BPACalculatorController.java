package org.egov.bpa.calculator.web.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.egov.bpa.calculator.services.CalculationService;
import org.egov.bpa.calculator.services.DemandService;
import org.egov.bpa.calculator.web.models.Calculation;
import org.egov.bpa.calculator.web.models.CalculationReq;
import org.egov.bpa.calculator.web.models.CalculationRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
//@RequestMapping("/v1")
@Slf4j
public class BPACalculatorController {

	private ObjectMapper objectMapper;

	private HttpServletRequest request;

	private CalculationService calculationService;

	private DemandService demandService;

	@Autowired
	public BPACalculatorController(ObjectMapper objectMapper, HttpServletRequest request,
								CalculationService calculationService,DemandService demandService) {
		this.objectMapper = objectMapper;
		this.request = request;
		this.calculationService=calculationService;
		this.demandService=demandService;
	}

	/**
	 * Calulates the tradeLicense fee and creates Demand
	 * @param calculationReq The calculation Request
	 * @return Calculation Response
	 */
	@RequestMapping(value = "/_calculate", method = RequestMethod.POST)
	public ResponseEntity<CalculationRes> calculate(@Valid @RequestBody CalculationReq calculationReq) {
		log.info("CalculationReaquest:: " + calculationReq);
		 List<Calculation> calculations = calculationService.calculate(calculationReq);
		 CalculationRes calculationRes = CalculationRes.builder().calculations(calculations).build();
		 return new ResponseEntity<CalculationRes>(calculationRes,HttpStatus.OK);
	}


}
