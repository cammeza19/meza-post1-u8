package com.example.auditoria.domain.valueobject;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class EstadoHallazgoTest {

    @ParameterizedTest(name = "{0} -> {1} es valida: {2}")
    @CsvSource({
            "ABIERTO, EN_REMEDIACION, true",
            "ABIERTO, CERRADO, false",
            "ABIERTO, REABIERTO, false",
            "EN_REMEDIACION, CERRADO, true",
            "EN_REMEDIACION, ABIERTO, false",
            "EN_REMEDIACION, REABIERTO, false",
            "CERRADO, REABIERTO, true",
            "CERRADO, EN_REMEDIACION, false",
            "CERRADO, ABIERTO, false",
            "REABIERTO, EN_REMEDIACION, true",
            "REABIERTO, CERRADO, false",
            "REABIERTO, ABIERTO, false"
    })
    void validaLaMaquinaDeEstados(EstadoHallazgo origen, EstadoHallazgo destino, boolean esperado) {
        assertThat(origen.puedeTransicionarA(destino)).isEqualTo(esperado);
    }
}
