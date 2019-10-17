package org.egov.pt.calculator.repository.rowmapper;

        import java.sql.ResultSet;
        import java.sql.SQLException;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

        import org.egov.pt.calculator.web.models.MutationBillingSlab;
        import org.egov.pt.calculator.web.models.property.AuditDetails;
        import org.springframework.dao.DataAccessException;
        import org.springframework.jdbc.core.ResultSetExtractor;
        import org.springframework.stereotype.Component;

@Component
public class MutationBillingSlabRowMapper implements ResultSetExtractor<List<MutationBillingSlab>> {

    @Override
    public List<MutationBillingSlab> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, MutationBillingSlab> billingSlabMap = new HashMap<>();
        while (rs.next()) {
            String currentId = rs.getString("id");
            MutationBillingSlab currentBillingSlab = billingSlabMap.get(currentId);
            if (null == currentBillingSlab) {
                AuditDetails auditDetails = AuditDetails.builder().createdBy(rs.getString("createdby"))
                        .createdTime(rs.getLong("createdTime")).lastModifiedBy(rs.getString("lastmodifiedby"))
                        .lastModifiedTime(rs.getLong("lastmodifiedtime")).build();

                currentBillingSlab = MutationBillingSlab.builder().id(rs.getString("id"))
                        .tenantId(rs.getString("tenantId"))
                        .ownerShipType(rs.getString("ownerShipType"))
                        .usageType(rs.getString("usageType"))
                        .areaType(rs.getString("areaType"))
                        .currentMarketValue(rs.getString("currentMarketValue"))
                        .cmvPercent(rs.getDouble("cmvPercent"))
                        .fixedAmount(rs.getDouble("fixedAmount"))
                        .auditDetails(auditDetails).build();

                billingSlabMap.put(currentId, currentBillingSlab);
            }

        }

        return new ArrayList<>(billingSlabMap.values());

    }

}

