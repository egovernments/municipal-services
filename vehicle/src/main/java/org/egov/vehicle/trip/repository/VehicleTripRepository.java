package org.egov.vehicle.trip.repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.vehicle.producer.VehicleProducer;
import org.egov.common.contract.request.RequestInfo;
import org.egov.vehicle.config.VehicleConfiguration;
import org.egov.vehicle.trip.querybuilder.VehicleTripQueryBuilder;
import org.egov.vehicle.trip.repository.rowmapper.TripDetailRowMapper;
import org.egov.vehicle.trip.repository.rowmapper.VehicleTripRowMapper;
import org.egov.vehicle.trip.web.model.VehicleTrip;
import org.egov.vehicle.trip.web.model.VehicleTripDetail;
import org.egov.vehicle.trip.web.model.VehicleTripRequest;
import org.egov.vehicle.trip.web.model.VehicleTripSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VehicleTripRepository {
	
	@Autowired
	private VehicleProducer producer;
	
	@Autowired
	private VehicleConfiguration config;
    
    @Autowired
	private JdbcTemplate jdbcTemplate;
    
    @Autowired
   	private VehicleTripRowMapper mapper;
    
    @Autowired
   	private TripDetailRowMapper detailMapper;
    
    @Autowired
    private VehicleTripQueryBuilder queryBuilder;
	
	public void save(VehicleTripRequest request) {
		producer.push(config.getSaveVehicleLogTopic(), request);
	}
	
	public void update(VehicleTripRequest request, boolean isStateUpdatable) {
		RequestInfo requestInfo = request.getRequestInfo();

		VehicleTrip tripForStatusUpdate = null;
		VehicleTrip tripForUpdate = null;

		VehicleTrip trip = request.getVehicleTrip();

		if (isStateUpdatable) {
			tripForUpdate = trip;
		} else {
			tripForStatusUpdate = trip;
		}
		if (tripForUpdate != null)
			producer.push(config.getUpdateVehicleLogTopic(), new VehicleTripRequest(requestInfo, tripForUpdate, null));

		if (tripForStatusUpdate != null)
			producer.push(config.getUpdateWorkflowVehicleLogTopic(), new VehicleTripRequest(requestInfo, tripForStatusUpdate,null));
	}
	
	public Integer getDataCount(String query) {
		Integer count = null;
		try {
			count = jdbcTemplate.queryForObject(query, Integer.class);
		} catch (Exception e) {
			throw e;
		}
		return count;
	}
	
	public List<VehicleTrip> getVehicleLogData(VehicleTripSearchCriteria criteria) {
		List<VehicleTrip> vehicleTrips = null;
		String query = queryBuilder.getVehicleLogSearchQuery(criteria);
		try {
			vehicleTrips = jdbcTemplate.query(query, mapper);
		} catch (Exception e) {
			throw e;
		}
		return vehicleTrips;
	}
	
	public List<VehicleTripDetail> getTrpiDetails(String tripId){
		List<VehicleTripDetail> tripDetails = null;
		String query = queryBuilder.getTripDetailSarchQuery(tripId);
		try {
			tripDetails = jdbcTemplate.query(query, detailMapper);
		} catch (Exception e) {
			throw e;
		}
		
		return tripDetails;
	}

	public List<String> getTripFromRefrences(List<String> refernceNos) {
		
		List<String> ids = null;
		String query = queryBuilder.getTripIdFromReferenceNosQuery(refernceNos);
		try {
			ids = jdbcTemplate.queryForList(query,String.class);
		} catch (Exception e) {
			throw e;
		}
		
		return ids;
	}

}
