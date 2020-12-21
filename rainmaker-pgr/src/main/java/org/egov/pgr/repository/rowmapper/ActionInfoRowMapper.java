package org.egov.pgr.repository.rowmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.pgr.model.ActionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ActionInfoRowMapper implements ResultSetExtractor<List<ActionInfo>> {

    @Autowired
    ObjectMapper objectMapper;

    public List<ActionInfo> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<ActionInfo> actionInfoList = new ArrayList<>();
        while(rs.next()){
            ActionInfo actionInfo = ActionInfo.builder()
                    .uuid(rs.getString("uuid"))
                    .tenantId(rs.getString("tenantid"))
                    .by(rs.getString("by"))
                    .isInternal(rs.getBoolean("isinternal"))
                    .when(rs.getLong("when"))
                    .businessKey(rs.getString("businesskey"))
                    .action(rs.getString("action"))
                    .status(rs.getString("status"))
                    .assignee(rs.getString("assignee"))
                    .build();
            actionInfoList.add(actionInfo);
        }
        return actionInfoList;
    }
}
