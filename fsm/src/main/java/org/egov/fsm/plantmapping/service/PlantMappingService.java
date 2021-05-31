package org.egov.fsm.plantmapping.service;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.fsm.plantmapping.repository.PlantMappingRepository;
import org.egov.fsm.plantmapping.util.PlantMappingUtil;
import org.egov.fsm.plantmapping.validator.PlantMappingValidator;
import org.egov.fsm.plantmapping.web.model.PlantMapping;
import org.egov.fsm.plantmapping.web.model.PlantMappingRequest;
import org.egov.fsm.plantmapping.web.model.PlantMappingResponse;
import org.egov.fsm.plantmapping.web.model.PlantMappingSearchCriteria;
import org.egov.fsm.util.FSMErrorConstants;
import org.egov.fsm.web.model.FSM;
import org.egov.fsm.web.model.FSMResponse;
import org.egov.fsm.web.model.FSMSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlantMappingService {

	@Autowired
	private PlantMappingUtil util;
	
	@Autowired
	private PlantMappingValidator validaor;
	
	@Autowired
	private PlantMappingEnrichmentService enrichmentService;
	
	@Autowired
	private PlantMappingRepository repository;
	
	public PlantMapping create(@Valid PlantMappingRequest request) {
		// TODO Auto-generated method stub
		RequestInfo requestInfo = request.getRequestInfo();
		Object mdmsData = util.mDMSCall(requestInfo, request.getPlantMapping().getTenantId());
		if (request.getPlantMapping().getTenantId().split("\\.").length == 1) {
			throw new CustomException(FSMErrorConstants.INVALID_TENANT, "Application Request cannot be create at StateLevel");
		}
		validaor.validateCreateOrUpdate(request, mdmsData);
		enrichmentService.enrichCreateRequest(request, mdmsData);
		return request.getPlantMapping();
	}

	public PlantMapping update(@Valid PlantMappingRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		
		PlantMapping plantMap = request.getPlantMapping();
		Object mdmsData = util.mDMSCall(requestInfo, request.getPlantMapping().getTenantId());
		
		
		if (plantMap.getId() == null) {
			throw new CustomException(FSMErrorConstants.UPDATE_ERROR, "Application Not found in the System" + plantMap);
		}
		
		List<String> ids = new ArrayList<String>();
		ids.add( plantMap.getId());
		PlantMappingSearchCriteria criteria = PlantMappingSearchCriteria.builder().tenantId(plantMap.getTenantId()).build();
		
		PlantMappingResponse response = repository.getPlantMappingData(criteria);
		List<PlantMapping> plantMaps = response.getPlantMapping();
		
		
		validaor.validateCreateOrUpdate(request, mdmsData);
		enrichmentService.enrichCreateRequest(request, mdmsData);
		return request.getPlantMapping();
	}

	public PlantMappingResponse search(@Valid PlantMappingSearchCriteria criteria, RequestInfo requestInfo) {

		validaor.validateSearch(criteria,requestInfo);
		PlantMappingResponse response = repository.getPlantMappingData(criteria);
		return response;
	}

}
