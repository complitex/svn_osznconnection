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
import org.complitex.osznconnection.file.entity.PaymentAndBenefitData;
import org.complitex.osznconnection.file.service_provider.util.OracleErrors;

/**
 *
 * @author Artem
 */
public class PaymentAndBenefitDataHandler extends BaseTypeHandler<List<PaymentAndBenefitData>> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<PaymentAndBenefitData> parameter, JdbcType jdbcType)
            throws SQLException {
        throw new UnsupportedOperationException("Only procedure call supported");
    }

    @Override
    public List<PaymentAndBenefitData> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        throw new UnsupportedOperationException("Only procedure call supported");
    }

    @Override
    public List<PaymentAndBenefitData> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("Only procedure call supported");
    }

    @Override
    public List<PaymentAndBenefitData> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        ResultSet rs = null;
        List<PaymentAndBenefitData> paymentAndBenefitDatas = Lists.newArrayList();
        try {
            rs = (ResultSet) cs.getObject(columnIndex);

            while (rs.next()) {
                PaymentAndBenefitData paymentAndBenefitData = new PaymentAndBenefitData();

                paymentAndBenefitData.setLodgerCount(rs.getInt("lodg_cnt"));
                paymentAndBenefitData.setUserCount(rs.getInt("usr_cnt"));
                paymentAndBenefitData.setPercent(rs.getBigDecimal("pct"));
                paymentAndBenefitData.setApartmentFeeCharge(rs.getBigDecimal("charge"));
                paymentAndBenefitData.setNormCharge(rs.getBigDecimal("norm_charge"));
                paymentAndBenefitData.setSaldo(rs.getBigDecimal("saldo_in"));
                paymentAndBenefitData.setReducedArea(rs.getBigDecimal("ts"));
                paymentAndBenefitData.setRoomCount(rs.getInt("rc"));
                paymentAndBenefitData.setOwnership(rs.getString("own"));
                paymentAndBenefitData.setApartmentFeeTarif(rs.getBigDecimal("b_tarif"));
                paymentAndBenefitData.setHeatingTarif(rs.getBigDecimal("b_tarif_ot"));
                paymentAndBenefitData.setHotWaterTarif(rs.getBigDecimal("b_tarif_gv"));
                paymentAndBenefitData.setColdWaterTarif(rs.getBigDecimal("b_tarif_hv"));
                paymentAndBenefitData.setGasTarif(rs.getBigDecimal("b_tarif_gas"));
                paymentAndBenefitData.setPowerTarif(rs.getBigDecimal("b_tarif_en"));
                paymentAndBenefitData.setGarbageDisposalTarif(rs.getBigDecimal("b_tarif_tr"));
                paymentAndBenefitData.setDrainageTarif(rs.getBigDecimal("b_tarif_gvo"));
                paymentAndBenefitData.setHeatingArea(rs.getBigDecimal("hs"));
                paymentAndBenefitData.setChargeHotWater(rs.getBigDecimal("charge_gv"));
                paymentAndBenefitData.setChargeColdWater(rs.getBigDecimal("charge_hv"));
                paymentAndBenefitData.setChargeGas(rs.getBigDecimal("charge_gas"));
                paymentAndBenefitData.setChargePower(rs.getBigDecimal("charge_en"));
                paymentAndBenefitData.setChargeGarbageDisposal(rs.getBigDecimal("charge_tr"));
                paymentAndBenefitData.setChargeDrainage(rs.getBigDecimal("charge_gvo"));

                paymentAndBenefitDatas.add(paymentAndBenefitData);
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
        return paymentAndBenefitDatas;
    }
}
