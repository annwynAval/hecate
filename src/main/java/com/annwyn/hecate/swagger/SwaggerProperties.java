package com.annwyn.hecate.swagger;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@Setter
@ConfigurationProperties(prefix = "com.annwyn.swagger")
public class SwaggerProperties {

    /**
     * 是否启用
     */
    private boolean enable;

    /**
     * swagger title
     */
    private String title;

    /**
     * swagger version
     */
    private String version;

    /**
     * swagger description
     */
    private String description;

    /**
     * 请求地址
     */
    private String servletPath;

    /**
     * swagger扫描包
     */
    private String basePackage;


}
