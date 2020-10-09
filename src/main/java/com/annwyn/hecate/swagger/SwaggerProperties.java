package com.annwyn.hecate.swagger;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@Setter
@ConditionalOnProperty(prefix = "com.annwyn.swagger", name = "enable", havingValue = "true")
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
     * swagger扫描包
     */
    private String basePackage;

}
