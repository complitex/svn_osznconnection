/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.complitex.osznconnection.file.entity.EnumCodeManager;
import org.complitex.osznconnection.file.entity.IEnumCode;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Artem
 */
public class EnumCodeTypeHandler extends BaseTypeHandler<IEnumCode> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, IEnumCode parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public IEnumCode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int code = rs.getInt(columnName);
        if (!rs.wasNull()) {
            return EnumCodeManager.valueOf(code);
        }
        return null;
    }

    @Override
    public IEnumCode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }
}
