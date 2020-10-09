package com.annwyn.hecate.mybatis.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AutoConvertEnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

    private final BaseTypeHandler<E> delegate;

    public AutoConvertEnumTypeHandler(Class<E> clazz) {
        this.delegate = DefaultEnumTypeHandler.DefaultEnum.class.isAssignableFrom(clazz) ? new DefaultEnumTypeHandler<>(clazz) : new EnumOrdinalTypeHandler<>(clazz);
    }

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, E e, JdbcType jdbcType) throws SQLException {
        this.delegate.setNonNullParameter(preparedStatement, i, e, jdbcType);
    }

    @Override
    public E getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return this.delegate.getNullableResult(resultSet, s);
    }

    @Override
    public E getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return this.delegate.getNullableResult(resultSet, i);
    }

    @Override
    public E getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return this.delegate.getNullableResult(callableStatement, i);
    }

}
