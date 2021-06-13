package org.egov.fsm.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.fsm.web.model.DataMartTenantModel;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class DataMartTenantRowMapper  implements ResultSetExtractor<List<DataMartTenantModel>>{

	@Override
	public List<DataMartTenantModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
		// TODO Auto-generated method stub
		List<DataMartTenantModel> dataMartTenantList=new ArrayList<DataMartTenantModel>();
		while(rs.next()) {
			DataMartTenantModel dataMartTenantModel= new DataMartTenantModel();
			dataMartTenantModel.setTenantId(rs.getString("tenantid"));
			dataMartTenantModel.setCount(rs.getInt("count"));
			dataMartTenantList.add(dataMartTenantModel);
		}
		
		return dataMartTenantList;
	}

}
