package com.personalfinance.config;

import com.personalfinance.mcp.FinanceTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider tools(FinanceTools financeTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(financeTools)
                .build();
    }
}