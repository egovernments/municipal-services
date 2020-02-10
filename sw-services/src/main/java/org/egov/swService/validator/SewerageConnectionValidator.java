package org.egov.swService.validator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.egov.tracer.model.CustomException;
import org.egov.swService.model.SewerageConnection;
import org.egov.swService.model.SewerageConnectionRequest;
import org.egov.swService.repository.SewarageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SewerageConnectionValidator {

	@Autowired
	private SewarageDao sewarageDao;

	/**
	 * 
	 * @param sewarageConnectionRequest
	 *            SewarageConnectionRequest is request for create or update
	 *            sewarage connection
	 * @param isUpdate
	 *            True for update and false for create
	 */
	public void validateSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest, boolean isUpdate) {
		SewerageConnection sewerageConnection = sewerageConnectionRequest.getSewerageConnection();
		Map<String, String> errorMap = new HashMap<>();
//		if (isUpdate && sewerageConnection.getConnectionNo() != null && !sewerageConnection.getConnectionNo().isEmpty()) {
//			int n = sewarageDao.isSewerageConnectionExist(Arrays.asList(sewerageConnection.getConnectionNo()));
//			if (n == 0) {
//				errorMap.put("INVALID SEWARAGE CONNECTION NUMBER", "Sewarage connection id not present");
//			}
//		}
//		if (sewerageConnection.getConnectionType() == null || sewerageConnection.getConnectionType().isEmpty()) {
//			errorMap.put("INVALID SEWERAGE CONNECTION TYPE",
//					"SewerageConnection cannot be created  without connection type");
//		}
		
		if (sewerageConnection.getProperty().getUsageCategory() == null || sewerageConnection.getProperty().getUsageCategory().isEmpty()) {
			errorMap.put("INVALID SEWERAGE CONNECTION PROPERTY USAGE TYPE", "SewerageConnection cannot be created without property usage type");
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

}
