package org.egov.swcalculation.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swcalculation.config.SWCalculationConfiguration;
import org.egov.swcalculation.constants.SWCalculationConstant;
import org.egov.swcalculation.producer.SWCalculationProducer;
import org.egov.swcalculation.repository.BillGeneratorDao;
import org.egov.swcalculation.repository.SewerageCalculatorDao;
import org.egov.swcalculation.util.SWCalculationUtil;
import org.egov.swcalculation.web.models.AdhocTaxReq;
import org.egov.swcalculation.web.models.BillGenerationSearchCriteria;
import org.egov.swcalculation.web.models.BillGeneratorReq;
import org.egov.swcalculation.web.models.BillScheduler;
import org.egov.swcalculation.web.models.BillScheduler.StatusEnum;
import org.egov.swcalculation.web.models.Calculation;
import org.egov.swcalculation.web.models.CalculationCriteria;
import org.egov.swcalculation.web.models.CalculationReq;
import org.egov.swcalculation.web.models.Property;
import org.egov.swcalculation.web.models.RequestInfoWrapper;
import org.egov.swcalculation.web.models.SewerageConnection;
import org.egov.swcalculation.web.models.SewerageConnectionRequest;
import org.egov.swcalculation.web.models.TaxHeadCategory;
import org.egov.swcalculation.web.models.TaxHeadEstimate;
import org.egov.swcalculation.web.models.TaxHeadMaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableSet;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SWCalculationServiceImpl implements SWCalculationService {

	@Autowired
	private MasterDataService mDataService;

	@Autowired
	private EstimationService estimationService;

	@Autowired
	private PayService payService;

	@Autowired
	private DemandService demandService;

	@Autowired
	private SewerageCalculatorDao sewerageCalculatorDao;
	
	@Autowired
	private SWCalculationUtil sWCalculationUtil;
	
	@Autowired
	private BillGeneratorService billGeneratorService;

	@Autowired
	private SWCalculationProducer producer;

	@Autowired
	private SWCalculationConfiguration configs;

	@Autowired
	private BillGeneratorDao billGeneratorDao;

	/**
	 * Get CalculationReq and Calculate the Tax Head on Sewerage Charge
	 * @param request  calculation request
	 * @return Returns the list of Calculation objects
	 */
	public List<Calculation> getCalculation(CalculationReq request) {
		List<Calculation> calculations;

		if (request.getIsconnectionCalculation()) {
			// Calculate and create demand for connection
			Map<String, Object> masterMap = mDataService.loadMasterData(request.getRequestInfo(),
					request.getCalculationCriteria().get(0).getTenantId());
			calculations = getCalculations(request, masterMap);
			demandService.generateDemand(request, calculations, masterMap,
					request.getIsconnectionCalculation());
			unsetSewerageConnection(calculations);
		} else {
			// Calculate and create demand for application
			Map<String, Object> masterData = mDataService.loadExemptionMaster(request.getRequestInfo(),
					request.getCalculationCriteria().get(0).getTenantId());
			calculations = getFeeCalculation(request, masterData);
			demandService.generateDemand(request, calculations, masterData,
					request.getIsconnectionCalculation());
			unsetSewerageConnection(calculations);
		}
		
		return calculations;
	}

	/**
	 * 
	 * @param requestInfo - Request Info
	 * @param criteria - Criteria
	 * @param estimatesAndBillingSlabs - List of estimates
	 * @param masterMap - MDMS Master Data
	 * @return - Returns Calculation object
	 * 
	 */
	public Calculation getCalculation(RequestInfo requestInfo, CalculationCriteria criteria,
			Map<String, List> estimatesAndBillingSlabs, Map<String, Object> masterMap, boolean isConnectionFee) {

		@SuppressWarnings("unchecked")
		List<TaxHeadEstimate> estimates = estimatesAndBillingSlabs.get("estimates");
		@SuppressWarnings("unchecked")
		List<String> billingSlabIds = estimatesAndBillingSlabs.get("billingSlabIds");
		SewerageConnection sewerageConnection = criteria.getSewerageConnection();
		
		Property property = sWCalculationUtil.getProperty(SewerageConnectionRequest.builder()
				.sewerageConnection(sewerageConnection).requestInfo(requestInfo).build());

		String tenantId = null != property.getTenantId() ? property.getTenantId() : criteria.getTenantId();

		@SuppressWarnings("unchecked")
		Map<String, TaxHeadCategory> taxHeadCategoryMap = ((List<TaxHeadMaster>) masterMap
				.get(SWCalculationConstant.TAXHEADMASTER_MASTER_KEY)).stream()
						.collect(Collectors.toMap(TaxHeadMaster::getCode, TaxHeadMaster::getCategory, (OldValue, NewValue) -> NewValue));

		BigDecimal taxAmt = BigDecimal.ZERO;
		BigDecimal sewerageCharge = BigDecimal.ZERO;
		BigDecimal penalty = BigDecimal.ZERO;
		BigDecimal exemption = BigDecimal.ZERO;
		BigDecimal rebate = BigDecimal.ZERO;
		BigDecimal fee = BigDecimal.ZERO;

		for (TaxHeadEstimate estimate : estimates) {

			TaxHeadCategory category = taxHeadCategoryMap.get(estimate.getTaxHeadCode());
			estimate.setCategory(category);

			switch (category) {

			case CHARGES:
				sewerageCharge = sewerageCharge.add(estimate.getEstimateAmount());
				break;

			case PENALTY:
				penalty = penalty.add(estimate.getEstimateAmount());
				break;

			case REBATE:
				rebate = rebate.add(estimate.getEstimateAmount());
				break;

			case EXEMPTION:
				exemption = exemption.add(estimate.getEstimateAmount());
				break;

			case FEE:
				fee = fee.add(estimate.getEstimateAmount());
				break;

			default:
				taxAmt = taxAmt.add(estimate.getEstimateAmount());
				break;
			}
			
		}

		TaxHeadEstimate decimalEstimate = payService.roundOfDecimals(taxAmt.add(penalty).add(fee).add(sewerageCharge),
				rebate.add(exemption), isConnectionFee);
		if (null != decimalEstimate) {
			decimalEstimate.setCategory(taxHeadCategoryMap.get(decimalEstimate.getTaxHeadCode()));
			estimates.add(decimalEstimate);
			if (decimalEstimate.getEstimateAmount().compareTo(BigDecimal.ZERO) >= 0)
				taxAmt = taxAmt.add(decimalEstimate.getEstimateAmount());
			else
				rebate = rebate.add(decimalEstimate.getEstimateAmount());
		}

		BigDecimal totalAmount = taxAmt.add(penalty).add(rebate).add(exemption).add(sewerageCharge).add(fee);
		return Calculation.builder().totalAmount(totalAmount).taxAmount(taxAmt).penalty(penalty).exemption(exemption)
				.charge(sewerageCharge).fee(fee).sewerageConnection(sewerageConnection).rebate(rebate)
				.tenantId(tenantId).taxHeadEstimates(estimates).billingSlabIds(billingSlabIds)
				.connectionNo(criteria.getConnectionNo()).applicationNO(criteria.getApplicationNo()).build();
	}

	/**
	 * Generate Demand Based on Time (Monthly, Quarterly, Yearly)
	 */
	public void generateDemandBasedOnTimePeriod(RequestInfo requestInfo) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime date = LocalDateTime.now();
		log.info("Time schedule start for sewerage demand generation on : " + date.format(dateTimeFormatter));
//		List<String> tenantIds = sewerageCalculatorDao.getTenantId();
		List<String> tenantIds = new ArrayList<>();
		tenantIds.add("pb.fazilka");
		tenantIds.add("pb.testing");
		tenantIds.add("pb.amritsar");
		tenantIds.add("pb.itpatiala");
		tenantIds.add("pb.bassipathana");
		tenantIds.add("pb.amargarh");
		tenantIds.add("pb.nadala");
		tenantIds.add("pb.bhadson");
		tenantIds.add("pb.shahkot");
		tenantIds.add("pb.mamdot");
		tenantIds.add("pb.ahmedgarh");
		tenantIds.add("pb.bhawanigarh");
		tenantIds.add("pb.balachaur");
		tenantIds.add("pb.talwara");
		tenantIds.add("pb.dirba");
		tenantIds.add("pb.khamano");
		tenantIds.add("pb.joga");
		
		if (tenantIds.isEmpty())
			return;
		log.info("Tenant Ids : " + tenantIds.toString());
		for (String tenantId : tenantIds) {
			try {
				demandService.generateDemandForTenantId(tenantId, requestInfo);
			} catch (Exception e) {
				log.error("Exception occurred while generating demand for tenant: {} : " , tenantId);
				log.error("Exception: {} : " , e);
				e.printStackTrace();
				continue;

			}
		}
	}

	/**
	 * 
	 * @param request
	 *            would be calculations request
	 * @param masterMap
	 *            master data
	 * @return all calculations including sewerage charge and taxhead on that
	 */
	List<Calculation> getCalculations(CalculationReq request, Map<String, Object> masterMap) {
		List<Calculation> calculations = new ArrayList<>(request.getCalculationCriteria().size());
		for (CalculationCriteria criteria : request.getCalculationCriteria()) {
			Map<String, List> estimationMap = estimationService.getEstimationMap(criteria, request,
					masterMap);
			ArrayList<?> billingFrequencyMap = (ArrayList<?>) masterMap
					.get(SWCalculationConstant.Billing_Period_Master);
			mDataService.enrichBillingPeriod(criteria, billingFrequencyMap, masterMap);
			Calculation calculation = getCalculation(request.getRequestInfo(), criteria, estimationMap, masterMap,
					true);
			calculation.setTaxPeriodFrom(criteria.getFrom());
			calculation.setTaxPeriodTo(criteria.getTo());
			calculations.add(calculation);
		}
		return calculations;
	}

	/**
	 * 
	 * 
	 * @param request - Calculation Request
	 * @return List of calculation.
	 */
	public List<Calculation> bulkDemandGeneration(CalculationReq request, Map<String, Object> masterMap) {
		List<Calculation> calculations = getCalculations(request, masterMap);
		demandService.generateDemandForBillingCycleInBulk(request, calculations, masterMap, true);
		return calculations;
	}

	/**
	 * 
	 * @param request - Calculation Request
	 * @return list of calculation based on request
	 */
	public List<Calculation> getEstimation(CalculationReq request) {
		Map<String, Object> masterData = mDataService.loadExemptionMaster(request.getRequestInfo(),
				request.getCalculationCriteria().get(0).getTenantId());
		List<Calculation> calculations = getFeeCalculation(request, masterData);
		unsetSewerageConnection(calculations);
		return calculations;
	}

	/**
	 * 
	 * @param request - Calculation Request
	 * @param masterMap - MDMS Master Data
	 * @return list of calculation based on estimation criteria
	 */
	List<Calculation> getFeeCalculation(CalculationReq request, Map<String, Object> masterMap) {
		List<Calculation> calculations = new ArrayList<>(request.getCalculationCriteria().size());
		for (CalculationCriteria criteria : request.getCalculationCriteria()) {
			Map<String, List> estimationMap = estimationService.getFeeEstimation(criteria, request.getRequestInfo(),
					masterMap);
			mDataService.enrichBillingPeriodForFee(masterMap);
			Calculation calculation = getCalculation(request.getRequestInfo(), criteria, estimationMap, masterMap,
					false);
			calculations.add(calculation);
		}
		return calculations;
	}

	public void unsetSewerageConnection(List<Calculation> calculation) {
		calculation.forEach(cal -> cal.setSewerageConnection(null));
	}
	
	/**
	 * Add adhoc tax to demand
	 * @param adhocTaxReq - Adhoc Tax Request Object
	 * @return List of Calculation
	 */
	public List<Calculation> applyAdhocTax(AdhocTaxReq adhocTaxReq) {
		List<TaxHeadEstimate> estimates = new ArrayList<>();
		if (!(adhocTaxReq.getAdhocpenalty().compareTo(BigDecimal.ZERO) == 0))
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(SWCalculationConstant.SW_ADHOC_PENALTY)
					.estimateAmount(adhocTaxReq.getAdhocpenalty().setScale(2, 2)).build());
		if (!(adhocTaxReq.getAdhocrebate().compareTo(BigDecimal.ZERO) == 0))
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(SWCalculationConstant.SW_ADHOC_REBATE)
					.estimateAmount(adhocTaxReq.getAdhocrebate().setScale(2, 2).negate()).build());
		Calculation calculation = Calculation.builder()
				.tenantId(adhocTaxReq.getRequestInfo().getUserInfo().getTenantId())
				.connectionNo(adhocTaxReq.getConsumerCode()).taxHeadEstimates(estimates).build();
		List<Calculation> calculations = Collections.singletonList(calculation);
		return demandService.updateDemandForAdhocTax(adhocTaxReq.getRequestInfo(), calculations);
	}
	
	/**
	 * Generate bill Based on Time (Monthly, Quarterly, Yearly)
	 */
	public void generateBillBasedLocality(RequestInfo requestInfo) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime date = LocalDateTime.now();
		log.info("Time schedule start for water bill generation on : " + date.format(dateTimeFormatter));

		BillGenerationSearchCriteria criteria = new BillGenerationSearchCriteria();
		criteria.setStatus(SWCalculationConstant.INITIATED_CONST);

		List<BillScheduler> billSchedularList = billGeneratorService.getBillGenerationDetails(criteria);
		if (billSchedularList != null && billSchedularList.isEmpty())
			return;
		log.info("billSchedularList count : " + billSchedularList.size());
		for (BillScheduler billSchedular : billSchedularList) {
			try {
				billGeneratorDao.updateBillSchedularStatus(billSchedular.getId(), StatusEnum.INPROGRESS);

				requestInfo.getUserInfo().setTenantId(billSchedular.getTenantId() != null ? billSchedular.getTenantId() : requestInfo.getUserInfo().getTenantId());
				RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();

				List<String> connectionNos = sewerageCalculatorDao.getConnectionsNoByLocality( billSchedular.getTenantId(), SWCalculationConstant.nonMeterdConnection, billSchedular.getLocality());

				//testing purpose added three consumercodes
//				connectionNos.add("0603000001");
//				connectionNos.add("0603000002");
//				connectionNos.add("0603009718");
				
				if (connectionNos == null || connectionNos.isEmpty()) {
					billGeneratorDao.updateBillSchedularStatus(billSchedular.getId(), StatusEnum.COMPLETED);
					continue;
				}

				Collection<List<String>> partitionConectionNoList = partitionBasedOnSize(connectionNos, configs.getBulkBillGenerateCount());
				int threadSleepCount = 1;
				
				log.info("partitionConectionNoList size: {}, Producer ConsumerCodes size : {} and BulkBillGenerateCount: {}",partitionConectionNoList.size(), connectionNos.size(), configs.getBulkBillGenerateCount());
				int count = 1;

				for (List<String>  conectionNoList : partitionConectionNoList) {

					BillGeneratorReq billGeneraterReq = BillGeneratorReq
							.builder()
							.requestInfoWrapper(requestInfoWrapper)
							.tenantId(billSchedular.getTenantId())
							.consumerCodes(ImmutableSet.copyOf(conectionNoList))
							.billSchedular(billSchedular)
							.build();

					producer.push(configs.getBillGenerateSchedulerTopic(), billGeneraterReq);
					log.info("Bill Scheduler pushed connections size:{} to kafka topic of batch no: ", conectionNoList.size(), count++);

					if(threadSleepCount == 2) {
						//Pausing controller for every three batches.
						Thread.sleep(10000);
						threadSleepCount=1;
					}
					threadSleepCount++;
				}
				billGeneratorDao.updateBillSchedularStatus(billSchedular.getId(), StatusEnum.COMPLETED);

			}catch (Exception e) {
				e.printStackTrace();
				log.error("Execptio occured while generating bills for tenant"+billSchedular.getTenantId()+" and locality: "+billSchedular.getLocality());
			}

		}
	}
	
	static <T> Collection<List<T>> partitionBasedOnSize(List<T> inputList, int size) {
        final AtomicInteger counter = new AtomicInteger(0);
        return inputList.stream()
                    .collect(Collectors.groupingBy(s -> counter.getAndIncrement()/size))
                    .values();
	}


}
