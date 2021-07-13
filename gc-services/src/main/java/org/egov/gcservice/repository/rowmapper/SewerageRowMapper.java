package org.egov.gcservice.repository.rowmapper;

import org.egov.gcservice.util.GCConstants;
import org.egov.gcservice.web.models.*;
import org.egov.gcservice.web.models.GarbageConnection.StatusEnum;
import org.egov.gcservice.web.models.workflow.ProcessInstance;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SewerageRowMapper implements ResultSetExtractor<List<GarbageConnection>> {
	
	
	@Autowired
	private ObjectMapper mapper;
	
	@Override
    public List<GarbageConnection> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, GarbageConnection> connectionListMap = new HashMap<>();
        GarbageConnection sewarageConnection = new GarbageConnection();
        while (rs.next()) {
            String Id = rs.getString("connection_Id");
            if (connectionListMap.getOrDefault(Id, null) == null) {
                sewarageConnection = new GarbageConnection();
                sewarageConnection.setTenantId(rs.getString("tenantid"));
                sewarageConnection.setId(rs.getString("connection_Id"));
                sewarageConnection.setApplicationNo(rs.getString("applicationNo"));
                sewarageConnection.setStatus(rs.getString("status"));
                sewarageConnection.setConnectionNo(rs.getString("connectionNo"));
                sewarageConnection.setOldConnectionNo(rs.getString("oldConnectionNo"));
                //sewarageConnection.setConnectionExecutionDate(rs.getLong("connectionExecutionDate"));
                sewarageConnection.setPropertyType(rs.getString("propertyType"));
                sewarageConnection.setIslegacy(rs.getBoolean("islegacy"));
                // get property id and get property object
                PGobject pgObj = (PGobject) rs.getObject("additionaldetails");
				ObjectNode addtionalDetails = null;
				if (pgObj != null) {

					try {
						addtionalDetails = mapper.readValue(pgObj.getValue(), ObjectNode.class);
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						throw new CustomException("PARSING ERROR", "The additionalDetail json cannot be parsed");
					}
				} else {
					addtionalDetails = mapper.createObjectNode();
				}
               // HashMap<String, Object> addtionalDetails = new HashMap<>();
                addtionalDetails.put(GCConstants.ADHOC_PENALTY, rs.getBigDecimal("adhocpenalty"));
                addtionalDetails.put(GCConstants.ADHOC_REBATE, rs.getBigDecimal("adhocrebate"));
                addtionalDetails.put(GCConstants.ADHOC_PENALTY_REASON, rs.getString("adhocpenaltyreason"));
                addtionalDetails.put(GCConstants.ADHOC_PENALTY_COMMENT, rs.getString("adhocpenaltycomment"));
                addtionalDetails.put(GCConstants.ADHOC_REBATE_REASON, rs.getString("adhocrebatereason"));
                addtionalDetails.put(GCConstants.ADHOC_REBATE_COMMENT, rs.getString("adhocrebatecomment"));
                addtionalDetails.put(GCConstants.APP_CREATED_DATE, rs.getBigDecimal("appCreatedDate"));
                addtionalDetails.put(GCConstants.DETAILS_PROVIDED_BY, rs.getString("detailsprovidedby"));
                addtionalDetails.put(GCConstants.ESTIMATION_FILESTORE_ID, rs.getString("estimationfileStoreId"));
                addtionalDetails.put(GCConstants.SANCTION_LETTER_FILESTORE_ID, rs.getString("sanctionfileStoreId"));
                addtionalDetails.put(GCConstants.ESTIMATION_DATE_CONST, rs.getBigDecimal("estimationLetterDate"));
                addtionalDetails.put(GCConstants.LOCALITY, rs.getString("locality"));
                sewarageConnection.setAdditionalDetails(addtionalDetails);
                sewarageConnection.setProcessInstance(ProcessInstance.builder().action((rs.getString("action"))).build());
                sewarageConnection.setApplicationType(rs.getString("applicationType"));
                sewarageConnection.setEffectiveFrom(rs.getLong("effectiveFrom"));
                sewarageConnection.setPropertyId(rs.getString("property_id"));

                AuditDetails auditdetails = AuditDetails.builder()
                        .createdBy(rs.getString("sw_createdBy"))
                        .createdTime(rs.getLong("sw_createdTime"))
                        .lastModifiedBy(rs.getString("sw_lastModifiedBy"))
                        .lastModifiedTime(rs.getLong("sw_lastModifiedTime"))
                        .build();
                sewarageConnection.setAuditDetails(auditdetails);

                // Add documents id's
                connectionListMap.put(Id, sewarageConnection);
            }
			addDocumentToGarbageConnection(rs, sewarageConnection);
			addPlumberInfoToGarbageConnection(rs, sewarageConnection);
			addHoldersDeatilsToGarbageConnection(rs, sewarageConnection);
            addRoadCuttingInfotToGarbageConnection(rs, sewarageConnection);
        }
        return new ArrayList<>(connectionListMap.values());
    }

    private void addDocumentToGarbageConnection(ResultSet rs, GarbageConnection GarbageConnection) throws SQLException {
        String document_Id = rs.getString("doc_Id");
        String isActive = rs.getString("doc_active");
        boolean documentActive = false;
        if (isActive != null) {
            documentActive = Status.ACTIVE.name().equalsIgnoreCase(isActive);
        }
        if (document_Id != null && documentActive) {
            Document applicationDocument = new Document();
            applicationDocument.setId(document_Id);
            applicationDocument.setDocumentType(rs.getString("documenttype"));
            applicationDocument.setFileStoreId(rs.getString("filestoreid"));
            applicationDocument.setDocumentUid(rs.getString("doc_Id"));
            applicationDocument.setStatus(Status.fromValue(isActive));
            //GarbageConnection.addDocumentsItem(applicationDocument);
        }
    }

    private void addRoadCuttingInfotToGarbageConnection(ResultSet rs, GarbageConnection GarbageConnection) throws SQLException {
        String roadcutting_id = rs.getString("roadcutting_id");
        String isActive = rs.getString("roadcutting_active");
        boolean roadCuttingInfoActive = false;
        if (!org.apache.commons.lang3.StringUtils.isEmpty(isActive)) {
            roadCuttingInfoActive = Status.ACTIVE.name().equalsIgnoreCase(isActive);
        }
        if (!org.apache.commons.lang3.StringUtils.isEmpty(roadcutting_id) && roadCuttingInfoActive) {
            RoadCuttingInfo roadCuttingInfo = new RoadCuttingInfo();
            roadCuttingInfo.setId(roadcutting_id);
            roadCuttingInfo.setRoadType(rs.getString("roadcutting_roadtype"));
            roadCuttingInfo.setRoadCuttingArea(rs.getFloat("roadcutting_roadcuttingarea"));
            roadCuttingInfo.setStatus(Status.fromValue(isActive));
            //GarbageConnection.addRoadCuttingInfoList(roadCuttingInfo);
        }
    }

    private void addPlumberInfoToGarbageConnection(ResultSet rs, GarbageConnection GarbageConnection) throws SQLException {
        String plumber_id = rs.getString("plumber_id");
        if (plumber_id != null) {
            PlumberInfo plumber = new PlumberInfo();
            plumber.setId(plumber_id);
            plumber.setName(rs.getString("plumber_name"));
            plumber.setGender(rs.getString("plumber_gender"));
            plumber.setLicenseNo(rs.getString("licenseno"));
            plumber.setMobileNumber(rs.getString("plumber_mobileNumber"));
            plumber.setRelationship(rs.getString("relationship"));
            plumber.setCorrespondenceAddress(rs.getString("correspondenceaddress"));
            plumber.setFatherOrHusbandName(rs.getString("fatherorhusbandname"));
         //   GarbageConnection.addPlumberInfoItem(plumber);
        }
    }

    private void addHoldersDeatilsToGarbageConnection(ResultSet rs, GarbageConnection GarbageConnection) throws SQLException {
        String uuid = rs.getString("userid");
        List<OwnerInfo> connectionHolders = GarbageConnection.getConnectionHolders();
        if (!CollectionUtils.isEmpty(connectionHolders)) {
            for (OwnerInfo connectionHolderInfo : connectionHolders) {
                if (!StringUtils.isEmpty(connectionHolderInfo.getUuid()) && !StringUtils.isEmpty(uuid) && connectionHolderInfo.getUuid().equals(uuid))
                    return;
            }
        }
        if (!StringUtils.isEmpty(uuid)) {
            Double holderShipPercentage = rs.getDouble("holdershippercentage");
            if (rs.wasNull()) {
                holderShipPercentage = null;
            }
            Boolean isPrimaryOwner = rs.getBoolean("isprimaryholder");
            if (rs.wasNull()) {
                isPrimaryOwner = null;
            }
            OwnerInfo connectionHolderInfo = OwnerInfo.builder()
                    .relationship(Relationship.fromValue(rs.getString("holderrelationship")))
                    .status(Status.fromValue(rs.getString("holderstatus")))
                    .tenantId(rs.getString("holdertenantid")).ownerType(rs.getString("connectionholdertype"))
                    .isPrimaryOwner(isPrimaryOwner).uuid(uuid).build();
            GarbageConnection.addConnectionHolderInfo(connectionHolderInfo);
        }
    }
}
