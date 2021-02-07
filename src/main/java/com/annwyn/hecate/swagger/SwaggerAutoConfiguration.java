package com.annwyn.hecate.swagger;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.List;


@Configuration
@EnableOpenApi
@EnableConfigurationProperties(SwaggerProperties.class)
@ConditionalOnProperty(prefix = "com.annwyn.swagger", name = "enable", havingValue = "true")
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class SwaggerAutoConfiguration {

    private final Logger logger = LoggerFactory.getLogger(SwaggerAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(Docket.class)
    public Docket docket(ApiInfo apiInfo, SwaggerProperties swaggerProperties, List<SecurityContext> securityContexts) {
        this.logger.info("启动swagger, 扫描包路径: {}. 访问路径{}.", swaggerProperties.getBasePackage(), "swagger-ui/index.html");
        return new Docket(DocumentationType.SWAGGER_2) //
                .enable(swaggerProperties.isEnable()) //
                .apiInfo(apiInfo) //
                .securityContexts(securityContexts) //
                .select() //
                .apis(RequestHandlerSelectors.basePackage(swaggerProperties.getBasePackage())) //
                .paths(PathSelectors.any()) //
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(ApiInfo.class)
    public ApiInfo apiInfo(SwaggerProperties swaggerProperties) {
        this.logger.info("配置swagger, {}: {}. 扫描包: {}, 是否启用: {}", swaggerProperties.getTitle(),
                swaggerProperties.getVersion(), swaggerProperties.getBasePackage(), swaggerProperties.isEnable());

        return new ApiInfoBuilder() //
                .title(swaggerProperties.getTitle())
                .version(swaggerProperties.getVersion()) //
                .description(swaggerProperties.getDescription()) //
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "securityContexts")
    public List<SecurityContext> securityContexts() {
        return Collections.emptyList();
    }
}
