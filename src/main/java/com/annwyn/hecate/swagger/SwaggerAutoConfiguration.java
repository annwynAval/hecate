package com.annwyn.hecate.swagger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.Resource;

@Configuration
@EnableSwagger2
@ConditionalOnBean(SwaggerProperties.class)
@EnableConfigurationProperties(SwaggerProperties.class)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class SwaggerAutoConfiguration {

    private final Logger logger = LoggerFactory.getLogger(SwaggerAutoConfiguration.class);

    @Resource
    private SwaggerProperties swaggerProperties;

    @Bean
    public Docket createRestApi() {
        this.logger.info("启动swagger, {}: {}. 扫描包: {}, 访问路径: {}", this.swaggerProperties.getTitle(),
                this.swaggerProperties.getVersion(), this.swaggerProperties.getBasePackage(), "swagger-ui.html");
        final ApiInfo apiInfo = new ApiInfoBuilder() //
                .title(this.swaggerProperties.getTitle()) //
                .version(this.swaggerProperties.getVersion()) //
                .description(this.swaggerProperties.getDescription()) //
                .build();
        return new Docket(DocumentationType.SWAGGER_2) //
                .apiInfo(apiInfo) //
                .select() //
                .apis(RequestHandlerSelectors.basePackage(this.swaggerProperties.getBasePackage())) //
                .paths(PathSelectors.any()) //
                .build();
    }
}
