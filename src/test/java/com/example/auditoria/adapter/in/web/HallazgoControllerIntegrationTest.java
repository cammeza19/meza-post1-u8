package com.example.auditoria.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
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
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica de extremo a extremo los checkpoints de la Parte 1 (Paso 6) y de la Parte 2 (Paso 11).
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
    void cicloCompletoConHistorialYDashboard() throws Exception {
        String area = "Area-" + UUID.randomUUID();
        String id = registrar(area, "CRITICA", LocalDate.now().minusDays(10));

        mvc.perform(patch("/api/hallazgos/{id}/iniciar-remediacion", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"responsable\":\"Equipo TI\",\"fechaLimite\":\"2026-12-31\",\"notas\":\"Parchear\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_REMEDIACION"));

        mvc.perform(patch("/api/hallazgos/{id}/cerrar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADO"));

        mvc.perform(patch("/api/hallazgos/{id}/reabrir", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"El hallazgo reaparecio\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REABIERTO"));

        mvc.perform(get("/api/hallazgos/{id}/historial", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].estadoAnterior").value("ABIERTO"))
                .andExpect(jsonPath("$[0].estadoNuevo").value("EN_REMEDIACION"))
                .andExpect(jsonPath("$[1].estadoNuevo").value("CERRADO"))
                .andExpect(jsonPath("$[2].estadoNuevo").value("REABIERTO"))
                .andExpect(jsonPath("$[2].motivo").value("El hallazgo reaparecio"));

        String otroArea = "Area-" + UUID.randomUUID();
        String otroId = registrar(otroArea, "BAJA", LocalDate.now().minusDays(4));
        mvc.perform(patch("/api/hallazgos/{id}/iniciar-remediacion", otroId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"responsable\":\"Equipo TI\",\"fechaLimite\":\"2026-12-31\"}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/hallazgos/{id}/cerrar", otroId)).andExpect(status().isOk());

        JsonNode dashboard = mapper.readTree(mvc.perform(get("/api/hallazgos/dashboard"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        JsonNode listado = mapper.readTree(mvc.perform(get("/api/hallazgos"))
                .andReturn().getResponse().getContentAsString());

        assertThat(sumarTotales(dashboard.get("porSeveridad"))).isEqualTo(listado.size());
        assertThat(sumarTotales(dashboard.get("porEstado"))).isEqualTo(listado.size());
        assertThat(promedioDe(dashboard, otroArea)).isEqualTo(4.0);
        assertThat(promedioDe(dashboard, area)).isNull();
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
    void cerrarUnHallazgoAbiertoRetorna400SinRegistrarHistorial() throws Exception {
        String id = registrar("Seguridad", "MEDIA", LocalDate.of(2026, 8, 1));

        mvc.perform(patch("/api/hallazgos/{id}/cerrar", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mvc.perform(get("/api/hallazgos/{id}/historial", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
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
        mvc.perform(get("/api/hallazgos/{id}/historial", UUID.randomUUID()))
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

    private static long sumarTotales(JsonNode conteos) {
        long suma = 0;
        for (JsonNode conteo : conteos) {
            suma += conteo.get("total").asLong();
        }
        return suma;
    }

    private static Double promedioDe(JsonNode dashboard, String area) {
        for (JsonNode promedio : dashboard.get("promedioDiasCierrePorArea")) {
            if (promedio.get("categoria").asText().equals(area)) {
                return promedio.get("promedioDias").asDouble();
            }
        }
        return null;
    }
}
