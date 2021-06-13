package org.egov.fsm.service;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.fsm.repository.DatamartRepository;
import org.egov.fsm.web.model.DataMartModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataMartService {

	@Autowired
	DatamartRepository dataMartRepository;

	public List<DataMartModel> getFsmDataMartData(RequestInfo requestInfo) {
		List<DataMartModel> modelList = dataMartRepository.getData(requestInfo);

		return modelList;
	}

}
