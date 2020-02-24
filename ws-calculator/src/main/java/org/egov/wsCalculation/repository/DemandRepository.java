package org.egov.wsCalculation.repository;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.wsCalculation.config.WSCalculationConfiguration;
import org.egov.wsCalculation.model.Demand;
import org.egov.wsCalculation.model.DemandRequest;
import org.egov.wsCalculation.model.DemandResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;


@Repository
public class DemandRepository {


    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private WSCalculationConfiguration config;

    @Autowired
    private ObjectMapper mapper;


    /**
     * Creates demand
     * @param requestInfo The RequestInfo of the calculation Request
     * @param demands The demands to be created
     * @return The list of demand created
     */
    public List<Demand> saveDemand(RequestInfo requestInfo, List<Demand> demands){
        StringBuilder url = new StringBuilder(config.getBillingServiceHost());
        url.append(config.getDemandCreateEndPoint());
        DemandRequest request = new DemandRequest(requestInfo,demands);
        Object result = serviceRequestRepository.fetchResult(url,request);
        try{
           return  mapper.convertValue(result,DemandResponse.class).getDemands();
        }
        catch(IllegalArgumentException e){
            throw new CustomException("PARSING ERROR","Failed to parse response of create demand");
        }
    }


    /**
     * Updates the demand
     * @param requestInfo The RequestInfo of the calculation Request
     * @param demands The demands to be updated
     * @return The list of demand updated
     */
    public List<Demand> updateDemand(RequestInfo requestInfo, List<Demand> demands){
        StringBuilder url = new StringBuilder(config.getBillingServiceHost());
        url.append(config.getDemandUpdateEndPoint());
        DemandRequest request = new DemandRequest(requestInfo,demands);
        Object result = serviceRequestRepository.fetchResult(url,request);
        try{
            return mapper.convertValue(result,DemandResponse.class).getDemands();
        }
        catch(IllegalArgumentException e){
            throw new CustomException("PARSING ERROR","Failed to parse response of update demand");
        }
    }


}
