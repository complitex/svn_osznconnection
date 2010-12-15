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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
public class BenefitDataHandler implements TypeHandler {

    private static final Logger log = LoggerFactory.getLogger(BenefitDataHandler.class);

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
        try {
            rs = (ResultSet)cs.getObject(columnIndex);
        } catch (SQLException e) {
            log.debug("", e);
        }

        if(rs == null){
            return null;
        }

        List<BenefitData> benefitDatas = Lists.newArrayList();
        while(rs.next()){
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

        return benefitDatas;
    }

}
