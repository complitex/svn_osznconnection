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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
public class PaymentAndBenefitDataHandler implements TypeHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentAndBenefitDataHandler.class);

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
            log.debug("", e);
        }

        if (rs == null) {
            return null;
        }

        List<PaymentAndBenefitData> paymentAndBenefitDatas = Lists.newArrayList();
        while (rs.next()) {
            PaymentAndBenefitData paymentAndBenefitData = new PaymentAndBenefitData();
            paymentAndBenefitData.setLodgerCount(rs.getInt("lodg_cnt"));
            paymentAndBenefitData.setUserCount(rs.getInt("usr_cnt"));
            paymentAndBenefitData.setPercent(rs.getDouble("pct"));
            paymentAndBenefitData.setCharge(rs.getDouble("charge"));
            paymentAndBenefitData.setNormCharge(rs.getDouble("norm_charge"));
            paymentAndBenefitData.setSaldo(rs.getDouble("saldo_in"));
            paymentAndBenefitData.setReducedArea(rs.getDouble("ts"));
            paymentAndBenefitData.setRoomCount(rs.getInt("rc"));
            paymentAndBenefitData.setOwnership(rs.getString("own"));
            paymentAndBenefitData.setTarif(rs.getDouble("b_tarif"));
            paymentAndBenefitDatas.add(paymentAndBenefitData);
        }

        return paymentAndBenefitDatas;
    }
}
