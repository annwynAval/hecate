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

    private boolean enable;

    private String title;
    private String version;
    private String description;

    private String basePackage;

}
