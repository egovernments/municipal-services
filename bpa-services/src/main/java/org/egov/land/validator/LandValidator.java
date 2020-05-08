package org.egov.land.validator;

import java.util.LinkedList;
import java.util.List;

import javax.validation.Valid;

import org.egov.land.web.models.LandRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class LandValidator {

//	@Autowired
//	private MDMSValidator mdmsValidator;
	
	
	public void validateCreate(@Valid LandRequest landRequest, Object mdmsData) {
//		mdmsValidator.validateMdmsData(landRequest, mdmsData);
		validateApplicationDocuments(landRequest, mdmsData, null);
		validateUser(landRequest);
		
	}

	private void validateUser(@Valid LandRequest landRequest) {
		landRequest.getLandInfo().getOwners().forEach(owner->{
			if (StringUtils.isEmpty(owner.getRelationship())) {
				throw new CustomException("BPA.CREATE.USER", " Owner relation ship is mandatory " + owner.toString());
			}
		});		
	}

	private void validateApplicationDocuments(@Valid LandRequest landRequest, Object mdmsData, Object currentState) {
		if (landRequest.getLandInfo().getDocuments() != null) {
			List<String> documentFileStoreIds = new LinkedList<String>();
			landRequest.getLandInfo().getDocuments().forEach(document -> {
				if (documentFileStoreIds.contains(document.getFileStore()))
					throw new CustomException("BPA_DUPLICATE_DOCUMENT", "Same document cannot be used multiple times");
				else
					documentFileStoreIds.add(document.getFileStore());
			});
		}
	}
}
