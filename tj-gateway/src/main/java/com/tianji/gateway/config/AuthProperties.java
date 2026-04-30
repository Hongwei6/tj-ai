package com.tianji.gateway.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@Data
@Component
@ConfigurationProperties(prefix = "tj.auth")
public class AuthProperties implements InitializingBean {

    private Set<String> excludePath = new java.util.HashSet<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        // 添加默认不拦截的路径（格式为 METHOD:PATH 或 **:PATH）
        excludePath.add("*:/error/**");
        excludePath.add("GET:/jwks");
        excludePath.add("*:/as/accounts/login");
        excludePath.add("*:/as/accounts/admin/login");
        excludePath.add("*:/as/accounts/refresh");
    }
}
