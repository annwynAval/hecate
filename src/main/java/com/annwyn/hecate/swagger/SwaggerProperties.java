package com.annwyn.hecate.swagger;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;


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
     * swagger扫描包
     */
    private String basePackage;

    /**
     * 共同请求参数
     */
    private List<SwaggerParameter> parameters;

    @Getter
    @Setter
    public static final class SwaggerParameter {

        /**
         * 参数名称
         */
        private String name;

        /**
         * 参数说明
         */
        private String description;

        /**
         * 是否必须项
         */
        private boolean required;

        /**
         * 参数位置, header, cookie, body, query
         */
        private String parameterType;

        /**
         * 参数类型
         */
        private String modelReference;

    }

}
