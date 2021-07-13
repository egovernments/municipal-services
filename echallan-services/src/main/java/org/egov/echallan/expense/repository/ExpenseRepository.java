package org.egov.echallan.expense.repository;

import static org.egov.echallan.repository.builder.ChallanQueryBuilder.FILESTOREID_UPDATE_SQL;

import java.util.ArrayList;
import java.util.List;

import org.egov.echallan.config.ChallanConfiguration;
import org.egov.echallan.expense.model.Expense;
import org.egov.echallan.expense.model.ExpenseRequest;
import org.egov.echallan.expense.repository.builder.ExpenseQueryBuilder;
import org.egov.echallan.expense.repository.rowmapper.ExpenseRowMapper;
import org.egov.echallan.expense.model.SearchCriteria;
import org.egov.echallan.expense.producer.ExpenseProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Repository
public class ExpenseRepository {

    private ExpenseProducer expenseProducer;
    
    private ChallanConfiguration config;

    private JdbcTemplate jdbcTemplate;

    private ExpenseQueryBuilder queryBuilder;

    private ExpenseRowMapper rowMapper;
    
    private RestTemplate restTemplate;

    @Value("${egov.filestore.host}")
    private String fileStoreHost;

    @Value("${egov.filestore.setinactivepath}")
	private String fileStoreInactivePath;

    @Autowired
	private ObjectMapper mapper; 
    @Autowired
    public ExpenseRepository(ExpenseProducer producer, ChallanConfiguration	 config,ExpenseQueryBuilder queryBuilder,
    		JdbcTemplate jdbcTemplate,ExpenseRowMapper rowMapper,RestTemplate restTemplate) {
        this.expenseProducer = producer;
        this.config = config;
        this.jdbcTemplate = jdbcTemplate;
        this.queryBuilder = queryBuilder ; 
        this.rowMapper = rowMapper;
        this.restTemplate = restTemplate;
    }



    /**
     * Pushes the request on save topic
     *
     * @param ExpenseRequest The expense create request
     */
    public void save(ExpenseRequest	 expenseRequest) {
    	
        expenseProducer.push(config.getSaveExpenseTopic(), expenseRequest);
    }
    
    /**
     * Pushes the request on update topic
     *
     * @param ExpenseRequest The expense create request
     */
    public void update(ExpenseRequest expenseRequest) {
    	
        expenseProducer.push(config.getUpdateExpenseTopic(), expenseRequest);
    }
    
    
    public List<Expense> getExpenses(SearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getExpenseSearchQuery(criteria, preparedStmtList);
        List<Expense> expenses =  jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        return expenses;
    }



	public void updateFileStoreId(List<Expense> expenses) {
		List<Object[]> rows = new ArrayList<>();

        expenses.forEach(expense -> {
        	rows.add(new Object[] {expense.getFilestoreid(),
        			expense.getId()}
        	        );
        });

        jdbcTemplate.batchUpdate(FILESTOREID_UPDATE_SQL,rows);
		
	}
	
	 public void setInactiveFileStoreId(String tenantId, List<String> fileStoreIds)  {
			String idLIst = fileStoreIds.toString().substring(1, fileStoreIds.toString().length() - 1).replace(", ", ",");
			String Url = fileStoreHost + fileStoreInactivePath + "?tenantId=" + tenantId + "&fileStoreIds=" + idLIst;
			try {
				  restTemplate.postForObject(Url, null, String.class) ;
			} catch (Exception e) {
				log.error("Error in calling fileStore "+e.getMessage());
			}
			 
		}



	/*public void updateChallanOnCancelReceipt(HashMap<String, Object> record) {
		// TODO Auto-generated method stub

		PaymentRequest paymentRequest = mapper.convertValue(record, PaymentRequest.class);
		RequestInfo requestInfo = paymentRequest.getRequestInfo();

		List<PaymentDetail> paymentDetails = paymentRequest.getPayment().getPaymentDetails();
		String tenantId = paymentRequest.getPayment().getTenantId();
		List<Object[]> rows = new ArrayList<>();
		for (PaymentDetail paymentDetail : paymentDetails) {
			Bill bill = paymentDetail.getBill();
			rows.add(new Object[] {bill.getConsumerCode(),
        			bill.getBusinessService()}
        	        );
		}
		jdbcTemplate.batchUpdate(CANCEL_RECEIPT_UPDATE_SQL,rows);
		
	}*/
    
}
