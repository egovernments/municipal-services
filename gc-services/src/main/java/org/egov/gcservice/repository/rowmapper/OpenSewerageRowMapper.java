package org.egov.gcservice.repository.rowmapper;

import org.egov.gcservice.util.GCConstants;
import org.egov.gcservice.web.models.*;
import org.egov.gcservice.web.models.GarbageConnection.StatusEnum;
import org.egov.gcservice.web.models.workflow.ProcessInstance;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenSewerageRowMapper implements ResultSetExtractor<List<GarbageConnection>> {
	
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
                sewarageConnection
                        .setStatus(rs.getString("status"));
               // sewarageConnection.setStatus(StatusEnum.fromValue(rs.getString("status")));
                sewarageConnection.setConnectionNo(rs.getString("connectionNo"));
                sewarageConnection.setOldConnectionNo(rs.getString("oldConnectionNo"));
                sewarageConnection.setIslegacy(rs.getBoolean("islegacy"));
                // get property id and get property object
                HashMap<String, Object> addtionalDetails = new HashMap<>();
                addtionalDetails.put(GCConstants.APP_CREATED_DATE, rs.getBigDecimal("appCreatedDate"));
                addtionalDetails.put(GCConstants.LOCALITY, rs.getString("locality"));
                //sewarageConnection.setAdditionalDetails(addtionalDetails);
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

			addHoldersDeatilsToGarbageConnection(rs, sewarageConnection);
        }
        return new ArrayList<>(connectionListMap.values());
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
