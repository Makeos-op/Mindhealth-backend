package com.upc.mind_health.services;

import com.upc.mind_health.dtos.G6_MH_PoliticaResponseDTO;
import com.upc.mind_health.entities.G6_MH_PoliticaDatos;
import com.upc.mind_health.repositories.G6_MH_PoliticaDatosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class G6_MH_PoliticaDatosService {

    private final G6_MH_PoliticaDatosRepository politicaRepository;

    @Transactional(readOnly = true)
    public G6_MH_PoliticaResponseDTO consultarPoliticaVigente() {
        G6_MH_PoliticaDatos politica = politicaRepository.findFirstByOrderByUltimaActualizacionDesc()
                .orElseGet(this::generarPoliticaPorDefecto);

        return G6_MH_PoliticaResponseDTO.builder()
                .titulo(politica.getTitulo())
                .contenidoHtml(politica.getContenidoHtml())
                .version(politica.getVersion())
                .ultimaActualizacion(politica.getUltimaActualizacion())
                .build();
    }

    private G6_MH_PoliticaDatos generarPoliticaPorDefecto() {
        return G6_MH_PoliticaDatos.builder()
                .titulo("Política de Protección de Datos Emocionales")
                .contenidoHtml("<p>En Mind Health, tus datos clínicos y emocionales están protegidos bajo estándares de confidencialidad médica. Almacenamos tus registros de forma segura y no los compartimos con terceros sin tu consentimiento explícito.</p>")
                .version("v1.0")
                .ultimaActualizacion(LocalDateTime.now())
                .build();
    }
}