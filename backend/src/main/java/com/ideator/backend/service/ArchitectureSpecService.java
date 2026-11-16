// SCRUM-72: Servicio que consume ideator-arch-agent en Microsoft Foundry para justificación de arquitectura y despliegue.
package com.ideator.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * SCRUM-72 – ArchitectureSpecService
 *
 * Invoca al agente especializado ideator-arch-agent (fase ARCHITECTURE) a través de
 * {@link FoundryAgentOrchestratorService} para generar la justificación del stack tecnológico
 * y la estrategia de despliegue con contenedores Docker.
 */
@Service
@Slf4j
public class ArchitectureSpecService {

    private final FoundryAgentOrchestratorService orchestratorService;

    public ArchitectureSpecService() {
        this.orchestratorService = null;
    }

    @Autowired
    public ArchitectureSpecService(@Autowired(required = false) FoundryAgentOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    /**
     * Genera la especificación de arquitectura para el proyecto dado invocando a ideator-arch-agent.
     *
     * @param projectId identificador del proyecto
     * @return JSON con la justificación técnica y el plan de despliegue
     */
    public String generateArchitectureSpec(Long projectId) {
        if (orchestratorService != null) {
            try {
                String response = orchestratorService.callAgent(AgentPhase.ARCHITECTURE, "{\"projectId\": " + projectId + "}");
                if (response != null && !response.isBlank()) {
                    return response;
                }
            } catch (Exception e) {
                log.warn("Error al invocar ideator-arch-agent, aplicando especificación base: {}", e.getMessage());
            }
        }

        String justification = "The stack combines Spring Boot 3 for a robust, type-safe backend with JWT security, " +
                "React + TypeScript for a modern, component-driven UI, PostgreSQL as a reliable relational " +
                "database, Docker for reproducible builds, and Azure OpenAI to provide AI-assisted features. " +
                "This combination ensures rapid development, easy scaling, and maintainability.";

        String deploymentPlan = "Docker Compose orchestrates the backend, frontend and PostgreSQL containers. " +
                "The backend image is built from the `backend` module, exposing port 8080. " +
                "The frontend is built with Vite and served via Nginx on port 80. " +
                "PostgreSQL runs with persisted volume `pgdata`. " +
                "For production, the compose file can be deployed to Azure Container Instances " +
                "or Azure Kubernetes Service. Environment variables for Azure OpenAI credentials are injected " +
                "through a `.env` file or Azure Key Vault.";

        return "{\"justification\": \"" + justification.replace("\"", "\\\"") + "\", \"deploymentPlan\": \"" + deploymentPlan.replace("\"", "\\\"") + "\"}";
    }
}
