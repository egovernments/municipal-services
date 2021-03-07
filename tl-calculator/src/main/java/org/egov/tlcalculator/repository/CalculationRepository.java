package org.egov.tlcalculator.repository;

import java.util.List;

import org.egov.tlcalculator.repository.rowmapper.CalculationRowMapper;
import org.egov.tlcalculator.web.models.BillingSlabIds;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Repository
public class CalculationRepository {


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CalculationRowMapper calculationRowMapper;

    /**
     * Executes the argument query on db
     * @param query The query to be executed
     * @param preparedStmtList The parameter values for the query
     * @return BillingSlabIds
     */
    public BillingSlabIds getDataFromDB(String query, List<Object> preparedStmtList){
        BillingSlabIds billingSlabIds = null;
        try {
            billingSlabIds = jdbcTemplate.query(query, preparedStmtList.toArray(), calculationRowMapper);
        }catch(CustomException e) {
            log.error("Exception while fetching from DB: " + e);
            return billingSlabIds;
        }

        return billingSlabIds;
    }


}
