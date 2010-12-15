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
public class AccountDetailHandler implements TypeHandler {

    private static final Logger log = LoggerFactory.getLogger(AccountDetailHandler.class);

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
            detail.setAccountNumber(rs.getString("acc_code"));
            detail.setOwnerName(rs.getString("owner_fio"));
            detail.setOwnerINN(rs.getString("owner_inn"));
            accountDetails.add(detail);
        }

        return accountDetails;
    }
}
