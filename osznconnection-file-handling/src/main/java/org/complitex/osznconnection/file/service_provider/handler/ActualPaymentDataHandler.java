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

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.complitex.osznconnection.file.entity.ActualPaymentData;
import org.complitex.osznconnection.file.service_provider.util.OracleErrors;

/**
 *
 * @author Artem
 */
@Deprecated
public class ActualPaymentDataHandler extends BaseTypeHandler<List<ActualPaymentData>> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<ActualPaymentData> parameter, JdbcType jdbcType) throws SQLException {
        throw new UnsupportedOperationException("Only procedure call supported");
    }

    @Override
    public List<ActualPaymentData> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        throw new UnsupportedOperationException("Only procedure call supported");
    }

    @Override
    public List<ActualPaymentData> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("Only procedure call supported");
    }

    @Override
    public List<ActualPaymentData> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        ResultSet rs = null;
        List<ActualPaymentData> actualPaymentDatas = Lists.newArrayList();
        try {
            rs = (ResultSet) cs.getObject(columnIndex);

            while (rs.next()) {
                ActualPaymentData actualPaymentData = new ActualPaymentData();
                actualPaymentData.setApartmentFeeCharge(rs.getBigDecimal("fact_charge"));
                actualPaymentData.setApartmentFeeTarif(rs.getBigDecimal("fact_tarif"));
                actualPaymentData.setHeatingCharge(rs.getBigDecimal("fact_charge_ot"));
                actualPaymentData.setHeatingTarif(rs.getBigDecimal("fact_tarif_ot"));
                actualPaymentData.setHotWaterCharge(rs.getBigDecimal("fact_charge_gv"));
                actualPaymentData.setHotWaterTarif(rs.getBigDecimal("fact_tarif_gv"));
                actualPaymentData.setColdWaterCharge(rs.getBigDecimal("fact_charge_hv"));
                actualPaymentData.setColdWaterTarif(rs.getBigDecimal("fact_tarif_hv"));
                actualPaymentData.setGasCharge(rs.getBigDecimal("fact_charge_gas"));
                actualPaymentData.setGasTarif(rs.getBigDecimal("fact_tarif_gas"));
                actualPaymentData.setPowerCharge(rs.getBigDecimal("fact_charge_en"));
                actualPaymentData.setPowerTarif(rs.getBigDecimal("fact_tarif_en"));
                actualPaymentData.setGarbageDisposalCharge(rs.getBigDecimal("fact_charge_tr"));
                actualPaymentData.setGarbageDisposalTarif(rs.getBigDecimal("fact_tarif_tr"));
                actualPaymentData.setDrainageCharge(rs.getBigDecimal("fact_charge_gvo"));
                actualPaymentData.setDrainageTarif(rs.getBigDecimal("fact_tarif_gvo"));
                actualPaymentDatas.add(actualPaymentData);
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
        return actualPaymentDatas;
    }
}
