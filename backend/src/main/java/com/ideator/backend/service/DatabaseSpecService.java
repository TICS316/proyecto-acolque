// SCRUM-69: Servicio que consume ideator-db-agent en Microsoft Foundry para generar el modelo relacional físico.
package com.ideator.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SCRUM-69 – DatabaseSpecService
 *
 * Invoca al agente especializado ideator-db-agent (fase DB) a través de
 * {@link FoundryAgentOrchestratorService} para transformar las decisiones de la
 * fase A en una propuesta de modelo relacional físico (tablas, columnas, tipos, PK/FK)
 * serializada en JSON.
 *
 * Las directivas y reglas del modelo físico están encapsuladas directamente en el agente
 * configurado en Microsoft Azure AI Foundry, eliminando prompts hardcodeados en el backend.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseSpecService {

    private final FoundryAgentOrchestratorService orchestratorService;

    /**
     * Genera la especificación física de la base de datos a partir del análisis
     * interno recibido de la fase A invocando al agente ideator-db-agent.
     *
     * @param analysisInternal el texto de análisis interno de la fase A
     * @return JSON con la propuesta de modelo relacional
     */
    public String generatePhysicalModel(String analysisInternal) {
        try {
            String response = orchestratorService.callAgent(AgentPhase.DB, analysisInternal);
            return (response != null && !response.isBlank()) ? response : "{\"tables\": []}";
        } catch (Exception e) {
            log.error("Error al generar especificación de BD con ideator-db-agent", e);
            return "{\"tables\": []}";
        }
    }
}
