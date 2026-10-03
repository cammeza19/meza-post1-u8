package com.example.auditoria.domain.entity;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.domain.valueobject.PlanRemediacion;
import com.example.auditoria.domain.valueobject.Severidad;
import com.example.auditoria.domain.valueobject.TransicionInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas del Aggregate Root sin @SpringBootTest: el dominio se instancia como Java puro.
 */
class HallazgoAuditoriaTest {

    private static final PlanRemediacion PLAN =
            new PlanRemediacion("Equipo de Infraestructura", LocalDate.of(2026, 8, 20), "Rotar credenciales");

    private HallazgoAuditoria nuevoHallazgo() {
        return new HallazgoAuditoria(HallazgoId.nuevo(), "Contrasenas por defecto en servidor de pruebas",
                "El servidor QA usa credenciales por defecto del fabricante", "Infraestructura",
                Severidad.ALTA, LocalDate.of(2026, 8, 1));
    }

    @Test
    @DisplayName("Un hallazgo nuevo inicia en estado ABIERTO y sin plan")
    void nuevoHallazgoIniciaAbierto() {
        HallazgoAuditoria hallazgo = nuevoHallazgo();

        assertThat(hallazgo.getEstado()).isEqualTo(EstadoHallazgo.ABIERTO);
        assertThat(hallazgo.getPlanRemediacion()).isNull();
        assertThat(hallazgo.getFechaCierre()).isNull();
    }

    @Test
    @DisplayName("Recorre el ciclo completo ABIERTO -> EN_REMEDIACION -> CERRADO -> REABIERTO -> EN_REMEDIACION")
    void recorreElCicloDeVidaCompleto() {
        HallazgoAuditoria hallazgo = nuevoHallazgo();

        assertThat(hallazgo.iniciarRemediacion(PLAN)).isEqualTo(EstadoHallazgo.ABIERTO);
        assertThat(hallazgo.getEstado()).isEqualTo(EstadoHallazgo.EN_REMEDIACION);
        assertThat(hallazgo.getPlanRemediacion()).isEqualTo(PLAN);

        assertThat(hallazgo.cerrar()).isEqualTo(EstadoHallazgo.EN_REMEDIACION);
        assertThat(hallazgo.getEstado()).isEqualTo(EstadoHallazgo.CERRADO);
        assertThat(hallazgo.getFechaCierre()).isEqualTo(LocalDate.now());

        assertThat(hallazgo.reabrir()).isEqualTo(EstadoHallazgo.CERRADO);
        assertThat(hallazgo.getEstado()).isEqualTo(EstadoHallazgo.REABIERTO);
        assertThat(hallazgo.getFechaCierre()).isNull();

        assertThat(hallazgo.iniciarRemediacion(PLAN)).isEqualTo(EstadoHallazgo.REABIERTO);
        assertThat(hallazgo.getEstado()).isEqualTo(EstadoHallazgo.EN_REMEDIACION);
    }

    @Test
    @DisplayName("No se puede cerrar un hallazgo que nunca estuvo en remediacion")
    void noSePuedeCerrarSinPlan() {
        HallazgoAuditoria hallazgo = nuevoHallazgo();

        assertThatThrownBy(hallazgo::cerrar).isInstanceOf(IllegalStateException.class);
        assertThat(hallazgo.getEstado()).isEqualTo(EstadoHallazgo.ABIERTO);
    }

    @Test
    @DisplayName("No se puede reabrir un hallazgo que sigue abierto")
    void noSePuedeReabrirUnHallazgoAbierto() {
        HallazgoAuditoria hallazgo = nuevoHallazgo();

        assertThatThrownBy(hallazgo::reabrir)
                .isInstanceOf(TransicionInvalidaException.class)
                .hasMessage("No se puede transicionar de ABIERTO a REABIERTO");
    }

    @Test
    @DisplayName("No se puede iniciar remediacion dos veces seguidas")
    void noSePuedeIniciarRemediacionDosVeces() {
        HallazgoAuditoria hallazgo = nuevoHallazgo();
        hallazgo.iniciarRemediacion(PLAN);

        assertThatThrownBy(() -> hallazgo.iniciarRemediacion(PLAN))
                .isInstanceOf(TransicionInvalidaException.class);
    }

    @Test
    @DisplayName("El titulo y el area responsable son obligatorios")
    void validaInvariantesDeCreacion() {
        assertThatThrownBy(() -> new HallazgoAuditoria(HallazgoId.nuevo(), " ", null, "TI",
                Severidad.BAJA, LocalDate.now())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new HallazgoAuditoria(HallazgoId.nuevo(), "Titulo", null, "",
                Severidad.BAJA, LocalDate.now())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("El plan de remediacion exige responsable y fecha limite")
    void validaPlanDeRemediacion() {
        assertThatThrownBy(() -> new PlanRemediacion("", LocalDate.now(), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PlanRemediacion("Equipo", null, null))
                .isInstanceOf(NullPointerException.class);
    }
}
