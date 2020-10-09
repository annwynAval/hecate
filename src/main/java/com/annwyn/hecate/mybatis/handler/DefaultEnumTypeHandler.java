package com.annwyn.hecate.mybatis.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.util.CollectionUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class DefaultEnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

    private final Map<Integer, E> defaultEnums = new HashMap<>(4);

    public DefaultEnumTypeHandler(Class<E> clazz) {
        if(!DefaultEnum.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("can't register DefaultEnumTypeHandler with class: " + clazz);
        }
        for(E e : EnumSet.allOf(clazz)) {
            this.defaultEnums.put(((DefaultEnum) e).convert(), e);
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, ((DefaultEnum) parameter).convert());
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return rs.wasNull() ? null : this.searchAbstractEnum(rs.getInt(columnName));
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.wasNull() ? null : this.searchAbstractEnum(rs.getInt(columnIndex));
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.wasNull() ? null : this.searchAbstractEnum(cs.getInt(columnIndex));
    }

    private E searchAbstractEnum(int value) {
        if(CollectionUtils.isEmpty(this.defaultEnums)) {
            return null;
        }
        return this.defaultEnums.getOrDefault(value, null);
    }

    public interface DefaultEnum {
        int convert();
    }
}