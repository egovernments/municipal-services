package org.egov.gcservice.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.gcservice.config.GCConfiguration;
import org.egov.gcservice.repository.rowmapper.OpenSewerageRowMapper;
import org.egov.gcservice.web.models.SearchCriteria;
import org.egov.gcservice.web.models.GarbageConnection;
import org.egov.gcservice.web.models.GarbageConnectionRequest;
import org.egov.gcservice.producer.SewarageConnectionProducer;
import org.egov.gcservice.repository.builder.SWQueryBuilder;
import org.egov.gcservice.repository.rowmapper.SewerageRowMapper;
import org.egov.gcservice.util.GCConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SewerageDaoImpl implements SewerageDao {

	@Autowired
	private SewarageConnectionProducer sewarageConnectionProducer;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private SWQueryBuilder swQueryBuilder;

	@Autowired
	private SewerageRowMapper sewarageRowMapper;

	@Autowired
	private OpenSewerageRowMapper openSewerageRowMapper;

	@Autowired
	private GCConfiguration swConfiguration;

	@Value("${egov.sewarageservice.createconnection.topic}")
	private String createSewarageConnection;

	@Value("${egov.sewarageservice.updateconnection.topic}")
	private String updateSewarageConnection;

	@Override
	public void saveGarbageConnection(GarbageConnectionRequest garbageConnectionRequest) {
		sewarageConnectionProducer.push(createSewarageConnection, garbageConnectionRequest);
	}

	@Override
	public List<GarbageConnection> getGarbageConnectionList(SearchCriteria criteria, RequestInfo requestInfo) {
		List<Object> preparedStatement = new ArrayList<>();
		String query = swQueryBuilder.getSearchQueryString(criteria, preparedStatement, requestInfo);
		if (query == null)
			return Collections.emptyList();
		Boolean isOpenSearch = isSearchOpen(requestInfo.getUserInfo());
		List<GarbageConnection> GarbageConnectionList = new ArrayList<>();
		if(isOpenSearch)
			GarbageConnectionList = jdbcTemplate.query(query, preparedStatement.toArray(),
					openSewerageRowMapper);
		else
			GarbageConnectionList = jdbcTemplate.query(query, preparedStatement.toArray(),
					sewarageRowMapper);

		if (GarbageConnectionList == null) {
			return Collections.emptyList();
		}
		return GarbageConnectionList;
	}

	public Boolean isSearchOpen(User userInfo) {

		return userInfo.getType().equalsIgnoreCase("SYSTEM")
				&& userInfo.getRoles().stream().map(Role::getCode).collect(Collectors.toSet()).contains("ANONYMOUS");
	}

	public void updateGarbageConnection(GarbageConnectionRequest garbageConnectionRequest,
			boolean isStateUpdatable) {
		if (isStateUpdatable) {
			sewarageConnectionProducer.push(updateSewarageConnection, garbageConnectionRequest);
		} else {
			sewarageConnectionProducer.push(swConfiguration.getWorkFlowUpdateTopic(), garbageConnectionRequest);
		}
	}

	/**
	 * push object for edit notification
	 * 
	 * @param garbageConnectionRequest - Sewerage GarbageConnection Request Object
	 */
	public void pushForEditNotification(GarbageConnectionRequest garbageConnectionRequest) {
		if (!GCConstants.EDIT_NOTIFICATION_STATE
				.contains(garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAction())) {
			sewarageConnectionProducer.push(swConfiguration.getEditNotificationTopic(), garbageConnectionRequest);
		}
	}

	/**
	 * Enrich file store Id's
	 * 
	 * @param garbageConnectionRequest - Sewerage GarbageConnection Request Object
	 */
	public void enrichFileStoreIds(GarbageConnectionRequest garbageConnectionRequest) {
		sewarageConnectionProducer.push(swConfiguration.getFileStoreIdsTopic(), garbageConnectionRequest);
	}

	/**
	 * Save file store Id's
	 * 
	 * @param garbageConnectionRequest - Sewerage GarbageConnection Request Object
	 */
	public void saveFileStoreIds(GarbageConnectionRequest garbageConnectionRequest) {
		sewarageConnectionProducer.push(swConfiguration.getSaveFileStoreIdsTopic(), garbageConnectionRequest);
	}

}
