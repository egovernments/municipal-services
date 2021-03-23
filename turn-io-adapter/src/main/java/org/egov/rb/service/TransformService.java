package org.egov.rb.service;




import org.egov.rb.contract.ServiceResponse;
import org.egov.rb.model.MessageRequest;
import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;

@org.springframework.stereotype.Service
@Slf4j
public class TransformService {
	
	public ServiceResponse transform(MessageRequest messageRequest) {
		log.info("Service layer for createss");
		//enrichserviceRequestForcreate(request);
		//RBProducer.push(saveTopic, request);
		//RBProducer.push(saveIndexTopic, dataTranformationForIndexer(request, true));
		return null;
		
	}

}
