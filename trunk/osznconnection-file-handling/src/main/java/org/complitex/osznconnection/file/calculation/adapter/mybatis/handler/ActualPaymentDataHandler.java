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
import org.complitex.osznconnection.file.entity.ActualPaymentData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
public class ActualPaymentDataHandler implements TypeHandler {

    private static final Logger log = LoggerFactory.getLogger(ActualPaymentDataHandler.class);

    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getResult(ResultSet rs, String columnName) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ActualPaymentData> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        ResultSet rs = null;
        try {
            rs = (ResultSet) cs.getObject(columnIndex);
        } catch (SQLException e) {
            log.debug("", e);
        }

        if (rs == null) {
            return null;
        }

        List<ActualPaymentData> actualPaymentDatas = Lists.newArrayList();
        while (rs.next()) {
            ActualPaymentData actualPaymentData = new ActualPaymentData();
            actualPaymentData.setCharge(rs.getBigDecimal("fact_charge"));
            actualPaymentData.setTarif(rs.getBigDecimal("fact_tarif"));
            actualPaymentDatas.add(actualPaymentData);
        }

        return actualPaymentDatas;
    }
}
