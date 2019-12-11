package org.egov.swCalculation.controller;

import java.util.List;

import javax.validation.Valid;

import org.egov.swCalculation.model.CalculationReq;
import org.egov.swCalculation.model.CalculationRes;
import org.egov.swCalculation.model.DemandResponse;
import org.egov.swCalculation.model.GetBillCriteria;
import org.egov.swCalculation.model.RequestInfoWrapper;
import org.egov.swCalculation.service.DemandService;
import org.egov.swCalculation.service.SWCalculationService;
import org.egov.swCalculation.util.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
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
@RequestMapping("/sewerageCalculator")
public class SWCalculationController {
	
	@Autowired
	SWCalculationService sWCalculationService;
	
	@Autowired
	DemandService demandService;
	
	@Autowired
	ResponseInfoFactory responseInfoFactory;
	
	
	
	@PostMapping("/_calculate")
	public ResponseEntity<CalculationRes> calculate(@RequestBody @Valid CalculationReq calculationReq) {
		return new ResponseEntity<>(sWCalculationService.getCalculation(calculationReq), HttpStatus.OK);
	}
	
	@PostMapping("/_updateDemand")
	public ResponseEntity<DemandResponse> updateDemands(@RequestBody @Valid RequestInfoWrapper requestInfoWrapper,
			@ModelAttribute @Valid GetBillCriteria getBillCriteria) {
		return new ResponseEntity<>(demandService.updateDemands(getBillCriteria, requestInfoWrapper), HttpStatus.OK);
	}

}
