package org.egov.bpa.service;

import java.util.List;

import javax.validation.Valid;

import org.egov.bpa.web.model.LandInfo;
import org.egov.bpa.web.model.LandRequest;
import org.egov.bpa.web.model.LandSearchCriteria;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Service;

@Service
public class LandService {
	
	public List<LandInfo> create(@Valid LandRequest landRequest) {
		return null;
	}
	
	public List<LandInfo> update(@Valid LandRequest landRequest) {
		return null;
	}

	private List<LandInfo> getLandInfoWithOwnerInfo(@Valid LandRequest landRequest) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<LandInfo> search(@Valid LandSearchCriteria criteria, RequestInfo requestInfo) {
		return null;
	}

	private LandInfo getLandInfoFromMobileNumber(@Valid LandSearchCriteria criteria, RequestInfo requestInfo) {
		// TODO Auto-generated method stub
		return null;
	}
}
