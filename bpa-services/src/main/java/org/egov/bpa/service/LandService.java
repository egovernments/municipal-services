package org.egov.bpa.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.land.web.models.LandInfo;
import org.egov.land.web.models.LandRequest;
import org.egov.land.web.models.LandSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LandService {

	@Autowired
	private BPAConfiguration config;
	
	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;
	
	public void addLandInfoToBPA(BPARequest bpaRequest) {
		// TODO Auto-generated method stub
		StringBuilder uri = new StringBuilder(config.getLandInfoHost());
		uri.append(config.getLandInfoCreate());
		
		LandRequest landRequest = new LandRequest();
		landRequest.setRequestInfo(bpaRequest.getRequestInfo());
		landRequest.setLandInfo(bpaRequest.getBPA().getLandInfo());
		LinkedHashMap responseMap = null;
		try {
			responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri,landRequest);
			}catch(ServiceCallException se) {
				throw new CustomException("LandInfo ERROR", " Invalid Land data");
			}
		ArrayList<LandInfo> landInfo = new ArrayList<LandInfo>();
		
		landInfo = (ArrayList<LandInfo>) responseMap.get("LandInfo");
		LandInfo landData = mapper.convertValue(landInfo.get(0), LandInfo.class);
		bpaRequest.getBPA().setLandInfo(landData);
		bpaRequest.getBPA().setLandId(landData.getId());
	}
	



	public void updateLandInfo(BPARequest bpaRequest) {
		// TODO Auto-generated method stub
		StringBuilder uri = new StringBuilder(config.getLandInfoHost());
		uri.append(config.getLandInfoUpdate());
		
		LandRequest landRequest = new LandRequest();
		landRequest.setRequestInfo(bpaRequest.getRequestInfo());
		landRequest.setLandInfo(bpaRequest.getBPA().getLandInfo());
		LinkedHashMap responseMap = null;
		try {
			responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri,landRequest);
			}catch(ServiceCallException se) {
				throw new CustomException("LandInfo ERROR", " Invalid Land data");
			}
		ArrayList<LandInfo> landInfo = new ArrayList<LandInfo>();
		
		landInfo = (ArrayList<LandInfo>) responseMap.get("LandInfo");
		LandInfo landData = mapper.convertValue(landInfo.get(0), LandInfo.class);
		bpaRequest.getBPA().setLandInfo(landData);
		bpaRequest.getBPA().setLandId(landData.getId());
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<LandInfo> searchLandInfoToBPA(RequestInfo requestInfo, LandSearchCriteria landcriteria) {
		// TODO Auto-generated method stub
		
	
		StringBuilder url = getLandSerchURLWithParams(requestInfo, landcriteria);
		
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		LinkedHashMap responseMap = null;
		responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(url, requestInfoWrapper);
		ArrayList<LandInfo> landInfo = new ArrayList<LandInfo>();
		landInfo = (ArrayList<LandInfo>) responseMap.get("LandInfo");
		ArrayList<LandInfo> landData = new ArrayList<LandInfo>(); 
		if(landInfo.size()>0){
		for(int i=0; i<landInfo.size(); i++){
			landData.add(mapper.convertValue(landInfo.get(i), LandInfo.class));
		}
		}
		System.out.println("LAND DATA from the service request repository is" + landInfo);
		
		
		return landData;
	}
	
	private StringBuilder getLandSerchURLWithParams(RequestInfo requestInfo, LandSearchCriteria landcriteria) {
		// TODO Auto-generated method stub
		StringBuilder uri = new StringBuilder(config.getLandInfoHost());
		uri.append(config.getLandInfoSearch());
		uri.append("?tenantId=");
		uri.append(landcriteria.getTenantId());
		LandSearchCriteria landSearchCriteria = new LandSearchCriteria();
		LandRequest landRequest = new LandRequest();
		landRequest.setRequestInfo(requestInfo);
		if (landcriteria.getMobileNumber() != null) {
			landSearchCriteria.setMobileNumber(landcriteria.getMobileNumber());
		} else {
			landSearchCriteria.setIds(landcriteria.getIds());
		}
		return uri;
	}
}
