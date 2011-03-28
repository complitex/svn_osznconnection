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
import org.complitex.osznconnection.file.entity.BenefitData;

/**
 *
 * @author Artem
 */
public class BenefitDataHandler implements TypeHandler {

    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getResult(ResultSet rs, String columnName) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<BenefitData> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        ResultSet rs = null;
        List<BenefitData> benefitDatas = Lists.newArrayList();
        try {
            rs = (ResultSet) cs.getObject(columnIndex);

            while (rs.next()) {
                BenefitData benefitData = new BenefitData();
                benefitData.setLastName(rs.getString("ln"));
                benefitData.setFirstName(rs.getString("fn"));
                benefitData.setMiddleName(rs.getString("mn"));
                benefitData.setInn(rs.getString("inn"));
                benefitData.setPassportNumber(rs.getString("pn"));
                benefitData.setPassportSerial(rs.getString("ps"));
                benefitData.setOrderFamily(rs.getString("ord"));
                benefitData.setCode(rs.getString("cc"));
                benefitData.setUserCount(rs.getString("uc"));
                benefitDatas.add(benefitData);
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
        return benefitDatas;
    }
}
