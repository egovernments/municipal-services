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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BPALandService {

	@Autowired
	private BPAConfiguration config;
	
	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addLandInfoToBPA(BPARequest bpaRequest) {
		StringBuilder uri = new StringBuilder(config.getLandInfoHost());
		uri.append(config.getLandInfoCreate());
		
		LandRequest landRequest = new LandRequest();
		landRequest.setRequestInfo(bpaRequest.getRequestInfo());
		landRequest.setLandInfo(bpaRequest.getBPA().getLandInfo());
		LinkedHashMap responseMap = null;
		try {
			responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri, landRequest);
		} catch (Exception se) {
			throw new CustomException("LANDINFO_EXCEPTION", " LandInfo service call failed.");
		} 
		ArrayList<LandInfo> landInfo = new ArrayList<LandInfo>();
		
		landInfo = (ArrayList<LandInfo>) responseMap.get("LandInfo");
		LandInfo landData = mapper.convertValue(landInfo.get(0), LandInfo.class);
		bpaRequest.getBPA().setLandInfo(landData);
		bpaRequest.getBPA().setLandId(landData.getId());
	}
	



	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void updateLandInfo(BPARequest bpaRequest) {
		StringBuilder uri = new StringBuilder(config.getLandInfoHost());
		uri.append(config.getLandInfoUpdate());
		
		LandRequest landRequest = new LandRequest();
		landRequest.setRequestInfo(bpaRequest.getRequestInfo());
		landRequest.setLandInfo(bpaRequest.getBPA().getLandInfo());
		LinkedHashMap responseMap = null;
		try {
			responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri, landRequest);
		} catch (Exception se) {
			throw new CustomException("LANDINFO_EXCEPTION", " LandInfo service call failed.");
		}
		ArrayList<LandInfo> landInfo = new ArrayList<LandInfo>();
		
		landInfo = (ArrayList<LandInfo>) responseMap.get("LandInfo");
		LandInfo landData = mapper.convertValue(landInfo.get(0), LandInfo.class);
		bpaRequest.getBPA().setLandInfo(landData);
		bpaRequest.getBPA().setLandId(landData.getId());
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<LandInfo> searchLandInfoToBPA(RequestInfo requestInfo, LandSearchCriteria landcriteria) {
		
	
		StringBuilder url = getLandSerchURLWithParams(requestInfo, landcriteria);
		
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		LinkedHashMap responseMap = null;
		responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(url, requestInfoWrapper);
		ArrayList<LandInfo> landInfo = new ArrayList<LandInfo>();
		if (responseMap != null && responseMap.get("LandInfo") != null)
			landInfo = (ArrayList<LandInfo>) responseMap.get("LandInfo");
		ArrayList<LandInfo> landData = new ArrayList<LandInfo>(); 
		if(landInfo.size()>0){
		for(int i=0; i<landInfo.size(); i++){
			landData.add(mapper.convertValue(landInfo.get(i), LandInfo.class));
		}
		}
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
			uri.append("&").append("&mobileNumber=");
			uri.append(landcriteria.getMobileNumber());
		} else {
			landSearchCriteria.setIds(landcriteria.getIds());
			uri.append("&").append("ids=");
			for (int i = 0; i < landcriteria.getIds().size(); i++) {
				if(i!=0){
					uri.append(",");
				}
				uri.append(landcriteria.getIds().get(i));
			}
		}
		return uri;
	}
}
