package com.ideator.backend.config;

import com.ideator.backend.service.AgentPhase;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azure.foundry")
@Data
public class AzureFoundryProperties {

    private String projectEndpoint = "https://ideator-colque-resource.services.ai.azure.com/api/projects/ideator-colque";
    private String apiVersion = "2025-05-15-preview";
    private Agents agents = new Agents();

    @Data
    public static class Agents {
        private String context = "ideator-context-agent";
        private String db = "ideator-db-agent";
        private String api = "ideator-api-agent";
        private String ui = "ideator-ui-agent";
        private String architecture = "ideator-arch-agent";
    }

    public String getAgentName(AgentPhase phase) {
        if (phase == null) {
            return null;
        }
        return switch (phase) {
            case CONTEXT -> agents != null && agents.getContext() != null ? agents.getContext() : "ideator-context-agent";
            case DB -> agents != null && agents.getDb() != null ? agents.getDb() : "ideator-db-agent";
            case API -> agents != null && agents.getApi() != null ? agents.getApi() : "ideator-api-agent";
            case UI -> agents != null && agents.getUi() != null ? agents.getUi() : "ideator-ui-agent";
            case ARCHITECTURE -> agents != null && agents.getArchitecture() != null ? agents.getArchitecture() : "ideator-arch-agent";
        };
    }
}
