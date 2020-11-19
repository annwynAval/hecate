package com.annwyn.hecate.swagger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableSwagger2
@EnableConfigurationProperties(SwaggerProperties.class)
@ConditionalOnProperty(prefix = "com.annwyn.swagger", name = "enable", havingValue = "true")
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class SwaggerAutoConfiguration {

    private final Logger logger = LoggerFactory.getLogger(SwaggerAutoConfiguration.class);

    @Resource
    private SwaggerProperties swaggerProperties;

    @Bean
    public Docket restDocket() {
        this.logger.info("启动swagger, {}: {}. 扫描包: {}, 访问路径: {}", this.swaggerProperties.getTitle(),
                this.swaggerProperties.getVersion(), this.swaggerProperties.getBasePackage(), "swagger-ui.html");
        final ApiInfo apiInfo = new ApiInfoBuilder() //
                .title(this.swaggerProperties.getTitle()) //
                .version(this.swaggerProperties.getVersion()) //
                .description(this.swaggerProperties.getDescription()) //
                .build();
        return new Docket(DocumentationType.SWAGGER_2) //
                .enable(this.swaggerProperties.isEnable())
                .apiInfo(apiInfo) //
                .select() //
                .apis(RequestHandlerSelectors.basePackage(this.swaggerProperties.getBasePackage())) //
                .paths(PathSelectors.any()) //
                .build()
                .globalOperationParameters(this.buildParameters());
    }

    private List<Parameter> buildParameters() {
        if(CollectionUtils.isEmpty(this.swaggerProperties.getParameters())) {
            return Collections.emptyList();
        }

        List<Parameter> parameters = new ArrayList<>(this.swaggerProperties.getParameters().size());
        for(SwaggerProperties.SwaggerParameter template : this.swaggerProperties.getParameters()) {
            ParameterBuilder parameterBuilder = new ParameterBuilder() //
                    .name(template.getName())
                    .description(template.getDescription())
                    .modelRef(new ModelRef(template.getModelReference()))
                    .parameterType(template.getParameterType())
                    .required(template.isRequired());
            parameters.add(parameterBuilder.build());
        }
        return parameters;
    }

    // /**
    //  * 此处不配置也依旧可以正常访问, 应该是swagger内部有做映射, 不过暂时不清楚是哪处做的配置.
    //  * @return {@link WebMvcConfigurer}
    //  */
    // @Bean
    // public WebMvcConfigurer swaggerWebMvcConfigurer() {
    //     return new WebMvcConfigurer() {
    //         @Override
    //         public void addResourceHandlers(ResourceHandlerRegistry registry) {
    //             registry.addResourceHandler("/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
    //             registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    //         }
    //     };
    // }
}
