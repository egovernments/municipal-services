package org.egov.land.validator;

import javax.validation.Valid;

import org.egov.bpa.validator.MDMSValidator;
import org.egov.land.web.models.LandRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LandValidator {

	@Autowired
	private MDMSValidator mdmsValidator;
	
	
	public void validateCreate(@Valid LandRequest landRequest, Object mdmsData) {
		mdmsValidator.validateMdmsData(landRequest, mdmsData);
		validateApplicationDocuments(landRequest, mdmsData, null);
		validateUser(landRequest);
		
	}

	private void validateUser(@Valid LandRequest landRequest) {
		landRequest.getLandInfo().getOwners().forEach(owner->{
			if (org.springframework.util.StringUtils.isEmpty(owner.getRelationship())) {
				throw new CustomException("BPA.CREATE.USER", " Owner relation ship is mandatory " + owner.toString());
			}
		});		
	}

	private void validateApplicationDocuments(@Valid LandRequest landRequest, Object mdmsData, Object currentState) {
		// TODO Auto-generated method stub
		
	}
}
