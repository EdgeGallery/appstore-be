package org.edgegallery.appstore.domain.model.releases;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class EnumStatusHandler extends BaseTypeHandler<EnumExperienceStatus> {
    /**
     *Set the conversion class and enumeration class content.
     * set by the configuration file for more convenient and efficient implementation by other methods.
     * @param type type.
     */
    public EnumStatusHandler(Class<EnumExperienceStatus> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, EnumExperienceStatus parameter, JdbcType jdbcType)
        throws SQLException {
        ps.setInt(i, parameter.getProgress());
    }

    @Override
    public EnumExperienceStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int i = rs.getInt(columnName);
        if (rs.wasNull()) {
            return null;
        } else {
            return locateEnum(i);
        }
    }

    @Override
    public EnumExperienceStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int i = rs.getInt(columnIndex);
        if (rs.wasNull()) {
            return null;
        } else {
            return locateEnum(i);
        }
    }

    @Override
    public EnumExperienceStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int i = cs.getInt(columnIndex);
        if (cs.wasNull()) {
            return null;
        } else {
            return locateEnum(i);
        }
    }

    private EnumExperienceStatus locateEnum(int value) {
        for (EnumExperienceStatus status : EnumExperienceStatus.values()) {
            if (status.getProgress() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("unknown parameter：" + value);
    }
}


