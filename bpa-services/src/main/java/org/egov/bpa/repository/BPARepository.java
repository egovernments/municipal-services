package org.egov.bpa.repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.producer.Producer;
import org.egov.bpa.repository.querybuilder.BPAQueryBuilder;
import org.egov.bpa.repository.rowmapper.BPARowMapper;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.BPASearchCriteria;
import org.egov.bpa.web.models.Document;
import org.egov.bpa.web.models.User;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

@Slf4j
@Repository
public class BPARepository {

	@Autowired
	private BPAConfiguration config;

	@Autowired
	private Producer producer;

	@Autowired
	private BPAQueryBuilder queryBuilder;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private BPARowMapper rowMapper;
	
	
	/**
	 * Pushes the request on save topic through kafka
	 *
	 * @param bpaRequest
	 *            The bpa create request
	 */
	public void save(BPARequest bpaRequest) {
		producer.push(config.getSaveTopic(), bpaRequest);
	}
	
	public void update(BPARequest bpaRequest, boolean isStateUpdatable) {
        RequestInfo requestInfo = bpaRequest.getRequestInfo();
        

        BPA bpaForStatusUpdate =null;
        BPA bpaForUpdate =null;
        BPA bpaForAdhocChargeUpdate =null;

        BPA bpa = bpaRequest.getBPA();

            if (isStateUpdatable) {
            		bpaForUpdate = bpa;
            }
            else {
                bpaForStatusUpdate = bpa;
            }
        if (bpaForUpdate != null)
            producer.push(config.getUpdateTopic(), new BPARequest(requestInfo, bpaForUpdate));

        if (bpaForStatusUpdate != null)
            producer.push(config.getUpdateWorkflowTopic(), new BPARequest(requestInfo, bpaForStatusUpdate));

    }
	
	
	 /**
     * Searhces bpa in databse
     *
     * @param criteria The bpa Search criteria
     * @return List of bpa from seach
     */
    public List<BPA> getBPAData(BPASearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getBPASearchQuery(criteria, preparedStmtList);        
        List<BPA> BPAData = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper); 		
        sortChildObjectsById(BPAData);
        return BPAData;
    }
    
    /**
     * Sorts the child objects by  there ids
     * @param bpa oF BPA DATA
     */
    private void sortChildObjectsById(List<BPA> bpaData){
        if(CollectionUtils.isEmpty(bpaData))
            return;
        bpaData.forEach(bpa -> {
        	bpa.getOwners().sort(Comparator.comparing(User::getUuid));
            if(!CollectionUtils.isEmpty(bpa.getDocuments()))
                bpa.getDocuments().sort(Comparator.comparing(Document::getId));
        });
    }
}
