package com.annwyn.hecate.mybatis.logger;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "com.annwyn.mybatis.logger")
public class MybatisLoggerProperties {

    /**
     * 是否启用
     */
    private boolean enable;
    /**
     * 是否强制打印所有SQL
     */
    private boolean forcePrintSql;

}
