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
import org.complitex.osznconnection.file.entity.PaymentAndBenefitData;

/**
 *
 * @author Artem
 */
public class PaymentAndBenefitDataHandler implements TypeHandler {

    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getResult(ResultSet rs, String columnName) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<PaymentAndBenefitData> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        ResultSet rs = null;
        try {
            rs = (ResultSet) cs.getObject(columnIndex);
        } catch (SQLException e) {
            String message = e.getMessage();
            if (OracleErrors.CURSOR_IS_CLOSED_ERROR.equals(message)) {
                // do nothing. It is expected behaviour.
            } else {
                throw e;
            }
        }

        if (rs == null) {
            return null;
        }

        List<PaymentAndBenefitData> paymentAndBenefitDatas = Lists.newArrayList();
        while (rs.next()) {
            PaymentAndBenefitData paymentAndBenefitData = new PaymentAndBenefitData();
            paymentAndBenefitData.setLodgerCount(rs.getInt("lodg_cnt"));
            paymentAndBenefitData.setUserCount(rs.getInt("usr_cnt"));
            paymentAndBenefitData.setPercent(rs.getBigDecimal("pct"));
            paymentAndBenefitData.setCharge(rs.getBigDecimal("charge"));
            paymentAndBenefitData.setNormCharge(rs.getBigDecimal("norm_charge"));
            paymentAndBenefitData.setSaldo(rs.getBigDecimal("saldo_in"));
            paymentAndBenefitData.setReducedArea(rs.getBigDecimal("ts"));
            paymentAndBenefitData.setRoomCount(rs.getInt("rc"));
            paymentAndBenefitData.setOwnership(rs.getString("own"));
            paymentAndBenefitData.setTarif(rs.getDouble("b_tarif"));
            paymentAndBenefitDatas.add(paymentAndBenefitData);
        }

        return paymentAndBenefitDatas;
    }
}
