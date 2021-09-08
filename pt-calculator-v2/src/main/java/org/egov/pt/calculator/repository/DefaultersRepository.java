package org.egov.pt.calculator.repository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.pt.calculator.repository.rowmapper.DefaultersRowMapper;
import org.egov.pt.calculator.web.models.DefaultersInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

@Repository
@Slf4j
public class DefaultersRepository {

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private DefaultersRowMapper defaultersRowMapper;

	private static final String INNER_QUERY = "select pt.propertyid,usr.name ownername,usr.mobilenumber,sum(dd.taxamount - dd.collectionamount) balance from eg_pt_property pt,eg_pt_owner ownr,eg_user usr,egbs_demanddetail_v1 dd, egbs_demand_v1 d "
			+ " where ownr.propertyid = pt.id and ownr.tenantid=pt.tenantid and usr.uuid=ownr.userid and dd.demandid=d.id and d.consumercode = pt.propertyid and d.tenantid = pt.tenantid and pt.status='ACTIVE' and d.status = 'ACTIVE'";
	private static final String OUTER_QUERY = "select result.propertyid,result.ownername,result.mobilenumber,result.balance from ({duequery}) as result where result.balance > 0";

	private static final String GROUP_BY_CLAUSE = " group by pt.propertyid,usr.name,usr.mobilenumber";

	private static final String LIMIT = " limit 10";

	public List<DefaultersInfo> fetchAllDefaulterDetailsForFY(String finYear, String tenantId) {

		final Map<String, Object> params = new HashMap<>();
		List<DefaultersInfo> defaultersInfo = new ArrayList<>();
		StringBuilder dueQuery = new StringBuilder(INNER_QUERY);
		if (StringUtils.isNotBlank(finYear)) {
			Map<String, Long> map = getFYStartAndEndDates(finYear);
			dueQuery.append(" and d.taxperiodfrom >=:fromDate and d.taxperiodto <=:toDate ");
			params.put("fromDate", map.get("startDate"));
			params.put("toDate", map.get("endDate"));
		}
		dueQuery.append(" and pt.tenantId=:tenantId");
		params.put("tenantId", tenantId);
		dueQuery.append(GROUP_BY_CLAUSE);
		dueQuery.append(LIMIT);

		log.info("Due query " + dueQuery.toString());
		log.info("Params " + params);

		String mainQuery = OUTER_QUERY.replace("{duequery}", dueQuery);
		try {
			defaultersInfo = namedParameterJdbcTemplate.query(mainQuery, params, defaultersRowMapper);
		} catch (Exception ex) {
			log.info("exception while fetching PT defaulters-Due SMS " + ex.getMessage());
		}
		return defaultersInfo;
	}

	private Map<String, Long> getFYStartAndEndDates(String finYear) {
		Map<String, Long> map = new HashMap<>();

		Calendar cal = Calendar.getInstance();
		cal.clear();
		Integer startDay = Integer.valueOf(1);
		Integer startMonth = Integer.valueOf(3);
		Integer startYear = Integer.valueOf(finYear.split("-")[0]);
		cal.set(startYear, startMonth, startDay);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 00);
		map.put("startDate", cal.getTimeInMillis());

		cal.clear();
		Integer endDay = Integer.valueOf(31);
		Integer endMonth = Integer.valueOf(2);
		Integer endYear = Integer.valueOf(startYear + 1);
		cal.set(endYear, endMonth, endDay);
		cal.set(Calendar.HOUR_OF_DAY, 17);
		cal.set(Calendar.MINUTE, 30);
		map.put("endDate", cal.getTimeInMillis());

		return map;
	}

}
