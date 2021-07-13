package org.egov.echallan.expense.consumer;


import static org.egov.echallan.expense.util.ExpenseConstants.KEY_NAME;
import static org.egov.echallan.expense.util.ExpenseConstants.KEY_PDF_ENTITY_ID;
import static org.egov.echallan.expense.util.ExpenseConstants.KEY_PDF_FILESTOREID;
import static org.egov.echallan.expense.util.ExpenseConstants.KEY_PDF_JOBS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.egov.echallan.expense.model.Expense;
import org.egov.echallan.expense.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class ExpenseFileStoreConsumer {


    @Autowired
    private ExpenseRepository expenseRepository;

    @KafkaListener(topics = { "${kafka.topics.filestore}" })
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    	try {
        List<Map<String,Object>> jobMaps = (List<Map<String,Object>>)record.get(KEY_PDF_JOBS);

        List<Expense> expenses = new ArrayList<>();
        jobMaps.forEach(job -> {
            if(job.get(KEY_NAME).toString().equalsIgnoreCase("mcollect-challan")) {
            	Expense challan = new Expense();
            	challan.setId((String) job.get(KEY_PDF_ENTITY_ID));
            	challan.setFilestoreid(StringUtils.join((List<String>)job.get(KEY_PDF_FILESTOREID),','));
            	expenses.add(challan);
            	log.info("Updating filestorid for: "+expenses);
            }
        });


        expenseRepository.updateFileStoreId(expenses);
    	 } catch (final Exception e) {
             log.error("Error while listening to value: " + record + " on topic: " + topic + ": ", e.getMessage());
         }

    }



}
