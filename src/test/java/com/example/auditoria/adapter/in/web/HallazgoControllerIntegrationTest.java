package com.example.auditoria.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica de extremo a extremo los checkpoints de la Parte 1 (Paso 6).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:integraciondb;DB_CLOSE_DELAY=-1")
class HallazgoControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void cicloCompletoDeTransiciones() throws Exception {
        String id = registrar("Infraestructura", "CRITICA", LocalDate.now().minusDays(10));

        mvc.perform(patch("/api/hallazgos/{id}/iniciar-remediacion", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"responsable\":\"Equipo TI\",\"fechaLimite\":\"2026-12-31\",\"notas\":\"Parchear\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_REMEDIACION"));

        mvc.perform(patch("/api/hallazgos/{id}/cerrar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADO"));

        mvc.perform(get("/api/hallazgos/{id}", id))
                .andExpect(jsonPath("$.fechaCierre").value(LocalDate.now().toString()));

        mvc.perform(patch("/api/hallazgos/{id}/reabrir", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"El hallazgo reaparecio\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REABIERTO"));

        mvc.perform(get("/api/hallazgos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REABIERTO"))
                .andExpect(jsonPath("$.planRemediacion.responsable").value("Equipo TI"));
    }

    @Test
    void registrarRetorna201ConUuid() throws Exception {
        String id = registrar("Infraestructura", "ALTA", LocalDate.of(2026, 8, 1));

        assertThat(UUID.fromString(id)).isNotNull();
        mvc.perform(get("/api/hallazgos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ABIERTO"))
                .andExpect(jsonPath("$.severidad").value("ALTA"));
    }

    @Test
    void cerrarUnHallazgoAbiertoRetorna400() throws Exception {
        String id = registrar("Seguridad", "MEDIA", LocalDate.of(2026, 8, 1));

        mvc.perform(patch("/api/hallazgos/{id}/cerrar", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mvc.perform(get("/api/hallazgos/{id}", id))
                .andExpect(jsonPath("$.estado").value("ABIERTO"));
    }

    @Test
    void reabrirUnHallazgoAbiertoRetorna400() throws Exception {
        String id = registrar("Seguridad", "MEDIA", LocalDate.of(2026, 8, 1));

        mvc.perform(patch("/api/hallazgos/{id}/reabrir", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Prueba\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No se puede transicionar de ABIERTO a REABIERTO"));
    }

    @Test
    void hallazgoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/hallazgos/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/api/hallazgos/{id}/cerrar", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void cuerpoInvalidoRetorna400() throws Exception {
        mvc.perform(post("/api/hallazgos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"areaResponsable\":\"TI\",\"severidad\":\"ALTA\",\"fechaDeteccion\":\"2026-08-01\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/hallazgos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"X\",\"areaResponsable\":\"TI\",\"severidad\":\"URGENTE\",\"fechaDeteccion\":\"2026-08-01\"}"))
                .andExpect(status().isBadRequest());
    }

    private String registrar(String area, String severidad, LocalDate fechaDeteccion) throws Exception {
        String body = """
                {"titulo":"Hallazgo de prueba","descripcion":"Detalle","areaResponsable":"%s",
                 "severidad":"%s","fechaDeteccion":"%s"}
                """.formatted(area, severidad, fechaDeteccion);
        String respuesta = mvc.perform(post("/api/hallazgos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(respuesta).get("hallazgoId").asText();
    }
}
