package org.egov.bpa.validator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.web.models.BPARequest;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;

@Component
@Slf4j
public class MDMSValidator {

	/**
	 * method to validate the mdms data in the request
	 *
	 * @param bpaRequest
	 */
	public void validateMdmsData(BPARequest request, Object mdmsData) {

		Map<String, String> errorMap = new HashMap<>();

		

		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
	}


}
