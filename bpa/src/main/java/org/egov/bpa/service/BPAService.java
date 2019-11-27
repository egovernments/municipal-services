package org.egov.bpa.service;

import org.egov.bpa.repository.BPARepository;
import org.egov.bpa.web.models.BPARequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BPAService {
	
	@Autowired
	private BPARepository bpaRequestInfoDao;

	public BPARequest create(BPARequest bpaRequest) {
//		 bpaRequestInfoDao.insert(bpaRequest);
	
		 return bpaRequest;
	}
}