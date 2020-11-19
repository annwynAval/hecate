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
    private final Map<String, Boolean> needPrintSqlCache = new ConcurrentHashMap<>(16);
    private final Map<String, Class<?>> mapperClassCache = new ConcurrentHashMap<>(16);
    private final Pattern mapperMethodPattern = Pattern.compile("^(?<className>.+)\\.(?<methodName>\\w+)$");

    private final MybatisLoggerProperties mybatisLoggerProperties;

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
        if(!this.checkIfNeedPrintSql(mappedStatement.getId())) { // 判断是否需要打印SQL
            return invocation.proceed();
        }

        long startMills = System.currentTimeMillis();
        Object returnValue = invocation.proceed();
        long time = System.currentTimeMillis() - startMills;

        this.showSql(mappedStatement.getId(), this.getSql(mappedStatement, invocation), time);
        return returnValue;
    }

    public boolean checkIfNeedPrintSql(String sqlId) throws ClassNotFoundException {
        Boolean needPrintSql = this.needPrintSqlCache.get(sqlId);
        if(needPrintSql == null) {
            this.needPrintSqlCache.put(sqlId, needPrintSql = this.judgeIfNeedPrintSql(sqlId));
        }
        return needPrintSql;
    }

    private boolean judgeIfNeedPrintSql(String sqlId) {
        Matcher matcher = this.mapperMethodPattern.matcher(sqlId);
        if(!matcher.matches()) {
            return false;
        }
        String className = matcher.group("className"), methodName = matcher.group("methodName");
        if(StringUtils.isEmpty(className) || StringUtils.isEmpty(methodName)) {
            return false;
        }

        Class<?> clazz = this.getMapperClass(className);
        if(clazz == null) {
            return false;
        }

        Optional<Method> optional = Arrays.stream(clazz.getMethods()) //
                .filter(item -> item.getName().equals(methodName)).findFirst();
        if(!optional.isPresent()) {
            this.logger.error("method not found. className: {}, methodName: {}", className, methodName);
            return false;
        }
        PrintSql printSql = AnnotationUtils.findAnnotation(optional.get(), PrintSql.class);
        return printSql == null ? this.mybatisLoggerProperties.isPrintSqlIfMissing() : printSql.value();
    }

    private Class<?> getMapperClass(String className) {
        if(this.mapperClassCache.containsKey(className)) {
            return this.mapperClassCache.get(className);
        }
        synchronized(MybatisLoggerInterceptor.class) {
            if(this.mapperClassCache.containsKey(className)) {
                return this.mapperClassCache.get(className);
            }

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if(classLoader == null) {
                classLoader = MybatisLoggerInterceptor.class.getClassLoader();
            }
            Class<?> clazz = this.getMapperClass(className, classLoader);
            this.mapperClassCache.put(className, clazz);
            return clazz;
        }
    }

    private Class<?> getMapperClass(String className, ClassLoader classLoader) {
        try {
            Class<?> clazz = Class.forName(className, false, classLoader);
            if(clazz.isInterface()) {
                return clazz;
            }
            this.logger.error("can't found interface, but found class. {}", className);
        } catch(ClassNotFoundException e) {
            this.logger.error("can't found class.", e);
        }
        return null;
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
