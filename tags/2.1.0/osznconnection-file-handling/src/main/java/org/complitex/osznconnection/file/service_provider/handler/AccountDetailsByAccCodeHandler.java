package org.complitex.osznconnection.file.service_provider.handler;

import com.google.common.collect.Lists;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.dictionary.oracle.OracleErrors;

/**
 *
 * @author Artem
 */
@Deprecated
public class AccountDetailsByAccCodeHandler extends BaseTypeHandler<List<AccountDetail>> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<AccountDetail> parameter, JdbcType jdbcType)
            throws SQLException {
        throw new UnsupportedOperationException("Only procedure call supported");
    }

    @Override
    public List<AccountDetail> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        throw new UnsupportedOperationException("Only procedure call supported");
    }

    @Override
    public List<AccountDetail> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("Only procedure call supported");
    }

    @Override
    public List<AccountDetail> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        ResultSet rs = null;
        List<AccountDetail> accountDetails = Lists.newArrayList();
        try {
            rs = (ResultSet) cs.getObject(columnIndex);

            while (rs.next()) {
                AccountDetail detail = new AccountDetail();
                detail.setAccountNumber(rs.getString("mn_code"));
                detail.setOwnerName(rs.getString("FIO"));
                detail.setMegabankAccountNumber(rs.getString("ERC_CODE"));
                detail.setServiceProviderAccountNumberInfo(rs.getString("zheu_code"));
                detail.setStreetType(rs.getString("STREET_SORT"));
                detail.setStreet(rs.getString("STREET_NAME"));
                detail.setBuildingNumber(rs.getString("HOUSE_NAME"));
                detail.setBuildingCorp(rs.getString("HOUSE_PART"));
                detail.setApartment(rs.getString("FLAT"));
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
