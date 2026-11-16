// SCRUM-71: Servicio que consume ideator-ui-agent en Microsoft Foundry para generar el mapa de UI.
package com.ideator.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SCRUM-71 – UiMapService
 *
 * Invoca al agente especializado ideator-ui-agent (fase UI) a través de
 * {@link FoundryAgentOrchestratorService} para generar la jerarquía de pantallas,
 * componentes y mockups en formato JSON.
 *
 * Las directivas de navegación y diseño están encapsuladas directamente en el agente
 * configurado en Microsoft Azure AI Foundry, eliminando prompts hardcodeados en el backend.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UiMapService {

    private final FoundryAgentOrchestratorService orchestratorService;

    /**
     * Genera el mapa conceptual de interfaces para el proyecto dado invocando a ideator-ui-agent.
     *
     * @param projectId identificador del proyecto
     * @return JSON con las pantallas y componentes de la interfaz
     */
    public String generateUiMap(Long projectId) {
        try {
            String response = orchestratorService.callAgent(AgentPhase.UI, "{\"projectId\": " + projectId + "}");
            return (response != null && !response.isBlank()) ? response : "{\"screens\": []}";
        } catch (Exception e) {
            log.error("Error al generar mapa de UI con ideator-ui-agent", e);
            return "{\"screens\": []}";
        }
    }
}
