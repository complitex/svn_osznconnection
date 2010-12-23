/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter.mybatis.handler;

import com.google.common.collect.Lists;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
public class AccountDetailsByAccCodeHandler implements TypeHandler {

    private static final Logger log = LoggerFactory.getLogger(AccountDetailsByAccCodeHandler.class);

    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getResult(ResultSet rs, String columnName) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
        ResultSet rs = null;
        try {
            rs = (ResultSet) cs.getObject(columnIndex);
        } catch (SQLException e) {
            log.debug("", e);
        }

        if (rs == null) {
            return null;
        }

        List<AccountDetail> accountDetails = Lists.newArrayList();
        while (rs.next()) {
            AccountDetail detail = new AccountDetail();
            detail.setAccountNumber(rs.getString("mn_code"));
            detail.setOwnerName(rs.getString("FIO"));
            detail.setMegabankAccountNumber(rs.getString("ERC_CODE"));
            detail.setOwnNumSr(rs.getString("zheu_code"));
            detail.setStreetType(rs.getString("STREET_SORT"));
            detail.setStreet(rs.getString("STREET_NAME"));
            detail.setBuildingNumber(rs.getString("HOUSE_NAME"));
            detail.setBuildingCorp(rs.getString("HOUSE_PART"));
            detail.setApartment(rs.getString("FLAT"));
            accountDetails.add(detail);
        }

        return accountDetails;
    }
}
