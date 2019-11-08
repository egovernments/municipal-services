package org.egov.waterConnection.validator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.model.SewerageConnection;
import org.egov.waterConnection.model.SewerageConnectionRequest;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.repository.SewarageDao;
import org.egov.waterConnection.repository.WaterDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class SewerageConnectionValidator {
	
	@Autowired
	SewarageDao sewarageDao;

	/**
	 * 
	 * @param waterConnectionRequest WaterConnectionRequest is request for create or update water connection
	 * @param isUpdate True for update and false for create
	 */
	public void validateSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest, boolean isUpdate) {
		SewerageConnection sewerageConnection = sewerageConnectionRequest.getSewerageConnection();
		Map<String, String> errorMap = new HashMap<>();
		if (isUpdate && (sewerageConnection.getId() == null || sewerageConnection.getId().isEmpty())) {
			errorMap.put("INVALID SEWARAGE CONNECTION", "SewarageConnection cannot be updated without connection id");
		}
		if (isUpdate && sewerageConnection.getId() != null && !sewerageConnection.getId().isEmpty()) {
			int n = sewarageDao.isSewerageConnectionExist(Arrays.asList(sewerageConnection.getId()));
			if (n == 0) {
				errorMap.put("INVALID SEWARAGE CONNECTION", "Sewarage Id not present");
			}
		}
		
      if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}


}
