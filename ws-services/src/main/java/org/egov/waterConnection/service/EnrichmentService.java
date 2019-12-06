package org.egov.waterConnection.service;

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
import org.egov.waterConnection.config.WSConfiguration;
import org.egov.waterConnection.model.Property;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.model.Idgen.IdResponse;
import org.egov.waterConnection.repository.IdGenRepository;
import org.egov.waterConnection.model.SearchCriteria;
import org.egov.waterConnection.util.WaterServicesUtil;
import org.egov.waterConnection.validator.ValidateProperty;
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
	WSConfiguration config;

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
	public void enrichWaterSearch(List<WaterConnection> waterConnectionList, RequestInfo requestInfo,
			SearchCriteria waterConnectionSearchCriteria) {
		waterConnectionList.forEach(waterConnection -> {
			List<Property> propertyList;
			if (waterConnection.getProperty().getPropertyId() == null
					|| waterConnection.getProperty().getPropertyId().isEmpty()) {
				throw new CustomException("INVALID SEARCH",
						"PROPERTY ID NOT FOUND FOR " + waterConnection.getId() + " WATER CONNECTION ID");
			}
			if (waterConnection.getProperty().getPropertyId() != null) {
				Set<String> propertyIds = new HashSet<>();
				propertyIds.add(waterConnection.getProperty().getPropertyId());

				propertyList = waterServicesUtil.propertySearchOnCriteria(waterConnectionSearchCriteria, requestInfo);
				if (propertyList == null || propertyList.isEmpty()) {
					throw new CustomException("INVALID SEARCH",
							"NO PROPERTY FOUND FOR " + waterConnection.getId() + " WATER CONNECTION ID");
				}
				waterConnection.setProperty(propertyList.get(0));
			}
		});
	}

	/**
	 * 
	 * @param waterConnectionRequest
	 * @param true
	 *            for create and false for update
	 */
	public void enrichWaterConnection(WaterConnectionRequest waterConnectionRequest, boolean isCreate) {
		validateProperty.enrichPropertyForWaterConnection(waterConnectionRequest);
		if (isCreate) {
			waterConnectionRequest.getWaterConnection().setId(UUID.randomUUID().toString());
			setWaterConnectionIdgenIds(waterConnectionRequest);
		}
	}

	/**
	 * 
	 * @param waterConnectionRequest
	 * @param MDMS
	 *            Data
	 */
	public void enrichWaterConnectionWithMDMSData(WaterConnectionRequest waterConnectionRequest,
			List<Property> propertyList) {
		if (propertyList != null && !propertyList.isEmpty())
			waterConnectionRequest.getWaterConnection().setProperty(propertyList.get(0));
	}

	/**
	 * Sets the WaterConnectionId for given WaterConnectionRequest
	 *
	 * @param request
	 *            WaterConnectionRequest which is to be created
	 */
	private void setWaterConnectionIdgenIds(WaterConnectionRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = request.getRequestInfo().getUserInfo().getTenantId();
		WaterConnection waterConnection = request.getWaterConnection();

		List<String> applicationNumbers = getIdList(requestInfo, tenantId, config.getWaterConnectionIdGenName(),
				config.getWaterConnectionIdGenFormat(), 1);
		ListIterator<String> itr = applicationNumbers.listIterator();

		Map<String, String> errorMap = new HashMap<>();
		if (applicationNumbers.size() != 1) {
			errorMap.put("IDGEN ERROR ",
					"The Id of WaterConnection returned by idgen is not equal to number of WaterConnection");
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
		waterConnection.setConnectionNo(itr.next());

	}

	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, String idformat, int count) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count)
				.getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}
}
