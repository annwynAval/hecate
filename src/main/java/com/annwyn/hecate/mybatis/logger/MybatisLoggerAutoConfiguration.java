package com.annwyn.hecate.mybatis.logger;


import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;

@Configuration
@ConditionalOnBean(SqlSessionFactory.class)
@ConditionalOnProperty(prefix = "com.annwyn.mybatis.logger", name = "enable", havingValue = "true")
@EnableConfigurationProperties(MybatisLoggerProperties.class)
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class MybatisLoggerAutoConfiguration implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(MybatisLoggerAutoConfiguration.class);

    @Resource
    private List<SqlSessionFactory> sqlSessionFactories;

    @Resource
    private MybatisLoggerProperties mybatisLoggerProperties;

    @Override
    public void afterPropertiesSet() {
        if(this.mybatisLoggerProperties.isEnable()) {
            this.logger.info("激活mybatis自动打印SQL配置, forcePrintSQL: {}", this.mybatisLoggerProperties.isForcePrintSql());
            final MybatisLoggerInterceptor interceptor = new MybatisLoggerInterceptor(this.mybatisLoggerProperties);
            this.sqlSessionFactories.forEach(item -> item.getConfiguration().addInterceptor(interceptor));
        }
    }
}
