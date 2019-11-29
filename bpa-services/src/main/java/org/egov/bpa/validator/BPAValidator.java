package org.egov.bpa.validator;

import lombok.extern.slf4j.Slf4j;

import org.egov.bpa.web.models.BPARequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BPAValidator {

	@Autowired
	private MDMSValidator mdmsValidator;

	public void validateCreate(BPARequest bpaRequest, Object mdmsData) {
		// TODO Auto-generated method stub
		valideDates(bpaRequest, mdmsData);
		validateInstitution(bpaRequest);
		validateDuplicateDocuments(bpaRequest);
		/*propertyValidator.validateProperty(bpaRequest);*/
		mdmsValidator.validateMdmsData(bpaRequest, mdmsData);
	}

	private void validateDuplicateDocuments(BPARequest request) {
		/*
		 * List<String> documentFileStoreIds = new LinkedList(); BPA bpa =
		 * request.getBPA(); if(bpa.getDocuments()!=null){
		 * bpa.getDocuments().forEach( document -> {
		 * if(documentFileStoreIds.contains(document.getFileStore())) throw new
		 * CustomException
		 * ("DUPLICATE_DOCUMENT ERROR","Same document cannot be used multiple times"
		 * ); else documentFileStoreIds.add(document.getFileStore()); } ); }
		 */
	}

	/**
	 * Validates the details if subOwnersipCategory is institutional
	 * 
	 * @param request
	 *            The input TradeLicenseRequest Object
	 */
	private void validateInstitution(BPARequest request) {

	}

	private void valideDates(BPARequest request, Object mdmsData) {

	}
}
