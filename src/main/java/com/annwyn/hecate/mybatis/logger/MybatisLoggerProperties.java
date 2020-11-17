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
    private boolean enable = false;

    /**
     * 未找到注解时, 是否进行打印
     * ture: 未找到注解时, 打印sql
     * false: 未找到注解时, 不打印sql
     */
    private boolean printSqlIfMissing = false;

}
