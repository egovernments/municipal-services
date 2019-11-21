package org.egov.wsCalculation.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.egov.wsCalculation.model.Calculation;
import org.egov.wsCalculation.model.CalculationReq;
import org.egov.wsCalculation.model.CalculationRes;
import org.egov.wsCalculation.model.Demand;
import org.egov.wsCalculation.service.DemandService;
import org.egov.wsCalculation.service.WSCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
@RestController
@RequestMapping("/waterCalculation")
public class WSCalculationController {
	
	
	@Autowired
	private DemandService demandService;
	
	@Autowired
	private WSCalculationService wSCalculationService;
	
	
	
	@PostMapping("/_estimate")
	public ResponseEntity<CalculationRes> getTaxEstimation(@RequestBody @Valid CalculationReq calculationReq) {
		return new ResponseEntity<>(wSCalculationService.getTaxCalculation(calculationReq), HttpStatus.OK);
	}

	@PostMapping("/_calculate")
	public ResponseEntity<Map<String, Calculation>> generateDemands(@RequestBody @Valid CalculationReq calculationReq) {
		return new ResponseEntity<>(demandService.generateDemands(calculationReq), HttpStatus.OK);
	}
	
	@PostMapping("/_update")
	public ResponseEntity<List<Demand>> updateDemands(@RequestBody @Valid CalculationReq calculationReq) {
		return new ResponseEntity<>(demandService.updateDemands(calculationReq), HttpStatus.OK);
	}

}
