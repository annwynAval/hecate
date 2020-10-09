package com.annwyn.hecate.mybatis.logger;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 已知错误: 当sql参数中包含#时, 再打印sql时出现错误.
 * @author annwyn
 * @version 1.0.0
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class MybatisLoggerInterceptor implements Interceptor {

    private static final String MYBATIS_PARAMETER_NULL_VALUE = "NULL";
    private static final String MYBATIS_PARAMETER_REGEXP_VALUE = "'%s'";
    private static final String MYBATIS_LOGGING_REGEXP_VALUE = "[sqlId: {}] [耗时: {}ms] {}";

    private final Logger logger = LoggerFactory.getLogger(MybatisLoggerInterceptor.class);
    private final MybatisLoggerProperties mybatisLoggerProperties;
    private final Map<String, Boolean> needPrintSqlCache = new ConcurrentHashMap<>(16);
    private final Pattern mapperMethodPattern = Pattern.compile("^(?<className>.+)\\.(?<methodName>\\w+)$");

    protected MybatisLoggerInterceptor(MybatisLoggerProperties properties) {
        this.mybatisLoggerProperties = properties;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public Object intercept(Invocation invocation)throws Throwable {
        if(!this.mybatisLoggerProperties.isEnable()) {
            return invocation.proceed();
        }
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        if(this.checkIfNeedPrintSql(mappedStatement.getId())) { // 判断是否需要打印SQL
            return invocation.proceed();
        }

        long startMills = System.currentTimeMillis();
        Object returnValue = invocation.proceed();
        long time = System.currentTimeMillis() - startMills;

        this.showSql(mappedStatement.getId(), this.getSql(mappedStatement, invocation), time);
        return returnValue;
    }

    public boolean checkIfNeedPrintSql(String sqlId) throws ClassNotFoundException {
        // 强制开启
        if(this.mybatisLoggerProperties.isForcePrintSQL()) {
            return true;
        }

        if(this.needPrintSqlCache.containsKey(sqlId)) {
            return this.needPrintSqlCache.get(sqlId);
        }

        boolean needPrintSql = this.judgeIfNeedPrintSql(sqlId);
        this.needPrintSqlCache.put(sqlId, needPrintSql);
        return needPrintSql;
    }

    private boolean judgeIfNeedPrintSql(String sqlId) throws ClassNotFoundException {
        Matcher matcher = this.mapperMethodPattern.matcher(sqlId);
        if(!matcher.matches()) {
            return false;
        }
        String className = matcher.group("className"), methodName = matcher.group("methodName");
        if(StringUtils.isEmpty(className) || StringUtils.isEmpty(methodName)) {
            return false;
        }

        Class<?> clazz = Class.forName(className);
        Optional<Method> optional = Arrays.stream(clazz.getMethods()) //
                .filter(item -> item.getName().equals(methodName)).findFirst();

        if(!optional.isPresent()) {
            return false;
        }
        PrintSql printSql = AnnotationUtils.findAnnotation(optional.get(), PrintSql.class);
        return printSql != null && printSql.value();
    }

    private String getSql(MappedStatement mappedStatement, Invocation invocation) {
        BoundSql boundSql = mappedStatement.getBoundSql(invocation.getArgs()[1]);
        Configuration configuration = mappedStatement.getConfiguration();

        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");

        if(parameterObject == null || CollectionUtils.isEmpty(parameterMappings)) {
            return sql;
        }

        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        if(typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
            return sql.replaceFirst("\\?", this.getParameterValue(parameterObject));
        }

        MetaObject metaobject = configuration.newMetaObject(parameterObject);
        for(ParameterMapping parameterMapping : parameterMappings) {
            String propertyName = parameterMapping.getProperty();
            if(metaobject.hasGetter(propertyName)) {
                sql = sql.replaceFirst("\\?", getParameterValue(metaobject.getValue(propertyName)));
            } else if(boundSql.hasAdditionalParameter(propertyName)) {
                sql = sql.replaceFirst("\\?", getParameterValue(boundSql.getAdditionalParameter(propertyName)));
            }
        }
        return sql;
    }

    private void showSql(String sqlId, String sql, long time) {
        this.logger.info(MYBATIS_LOGGING_REGEXP_VALUE, sqlId, time, sql);
    }

    private String getParameterValue(Object object) {
        if(object == null) {
            return MYBATIS_PARAMETER_NULL_VALUE;
        }
        if(object instanceof String) {
            return String.format(MYBATIS_PARAMETER_REGEXP_VALUE, object.toString());
        }
        return object.toString();
    }

}
