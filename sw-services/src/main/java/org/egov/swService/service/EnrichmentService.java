package org.egov.swService.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.swService.config.SWConfiguration;
import org.egov.swService.model.Property;
import org.egov.swService.model.SewerageConnection;
import org.egov.swService.model.SewerageConnectionRequest;
import org.egov.swService.model.Idgen.IdResponse;
import org.egov.swService.repository.IdGenRepository;
import org.egov.swService.util.WaterServicesUtil;
import org.egov.swService.model.SearchCriteria;
import org.egov.swService.validator.ValidateProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class EnrichmentService {

	@Autowired
	WaterServicesUtil waterServicesUtil;
	
	@Autowired
	IdGenRepository idGenRepository;
	
	@Autowired
	SWConfiguration config;
	
	@Autowired
	ValidateProperty validateProperty;

	

	/**
	 * 
	 * @param waterConnectionList
	 *            List of water connection for enriching the water connection
	 *            with property.
	 * @param requestInfo
	 *            is RequestInfo from request
	 */

	public void enrichSewerageSearch(List<SewerageConnection> sewerageConnectionList, RequestInfo requestInfo) {
		sewerageConnectionList.forEach(sewerageConnection -> {
			List<Property> propertyList;
			if (sewerageConnection.getProperty().getId() == null
					|| sewerageConnection.getProperty().getId().isEmpty()) {
				throw new CustomException("INVALID SEARCH",
						"PROPERTY ID NOT FOUND FOR " + sewerageConnection.getId() + " SEWERAGE CONNECTION ID");
			}
			if (sewerageConnection.getProperty().getId() != null) {
				Set<String> propertyIds = new HashSet<>();
				propertyIds.add(sewerageConnection.getProperty().getId());
				SearchCriteria searchCriteria = SearchCriteria.builder()
						.ids(propertyIds).build();
				propertyList = waterServicesUtil.propertySearchOnCriteria(searchCriteria, requestInfo);
				if (propertyList == null || propertyList.isEmpty()) {
					throw new CustomException("INVALID SEARCH",
							"NO PROPERTY FOUND FOR " + sewerageConnection.getId() + " SEWERAGE CONNECTION ID");
				}
				sewerageConnection.setProperty(propertyList.get(0));
			}
		});
	}

	
	
	/**
	 * 
	 * @param waterConnectionRequest
	 * @param propertyList
	 */

	public void enrichSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest, boolean isCreate) {
		validateProperty.enrichPropertyForSewerageConnection(sewerageConnectionRequest);
		if (isCreate)
			sewerageConnectionRequest.getSewerageConnection().setId(UUID.randomUUID().toString());
		setSewarageConnectionIdgenIds(sewerageConnectionRequest);
	}
	


	/**
	 * Sets the SewarageConnectionId for given SewerageConnectionRequest
	 *
	 * @param request SewerageConnectionRequest which is to be created
	 */
	private void setSewarageConnectionIdgenIds(SewerageConnectionRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = request.getRequestInfo().getUserInfo().getTenantId();
		SewerageConnection waterConnection = request.getSewerageConnection();

		List<String> applicationNumbers = getIdList(requestInfo, tenantId, config.getSewerageIdGenName(),
				config.getSewerageIdGenFormat(), 1);
		ListIterator<String> itr = applicationNumbers.listIterator();

		Map<String, String> errorMap = new HashMap<>();
		if (applicationNumbers.size() != 1) {
			errorMap.put("IDGEN ERROR ",
					"The Id of SewerageConnection returned by idgen is not equal to number of SewerageConnection");
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
		waterConnection.setId(itr.next());
	}

	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, String idformat, int count) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count)
				.getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}
}
