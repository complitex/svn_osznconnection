/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider.handler;

import com.google.common.collect.Lists;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.service_provider.util.OracleErrors;

/**
 *
 * @author Artem
 */
public class AccountDetailsByAddressHandler implements TypeHandler<List<AccountDetail>> {

    @Override
    public void setParameter(PreparedStatement ps, int i, List<AccountDetail> parameter, JdbcType jdbcType) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<AccountDetail> getResult(ResultSet rs, String columnName) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<AccountDetail> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        ResultSet rs = null;
        List<AccountDetail> accountDetails = Lists.newArrayList();
        try {
            rs = (ResultSet) cs.getObject(columnIndex);

            while (rs.next()) {
                AccountDetail detail = new AccountDetail();
                detail.setAccountNumber(rs.getString("acc_code"));
                detail.setOwnerName(rs.getString("owner_fio"));
                detail.setOwnerINN(rs.getString("owner_inn"));
                detail.setServiceProviderAccountNumberInfo(rs.getString("erc_zheu_code"));
                detail.setMegabankAccountNumber(rs.getString("erc_code"));
                accountDetails.add(detail);
            }
        } catch (SQLException e) {
            String message = e.getMessage();
            if (OracleErrors.CURSOR_IS_CLOSED_ERROR.equals(message)) {
                // do nothing. It is expected behaviour.
            } else {
                throw e;
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return accountDetails;
    }
}
