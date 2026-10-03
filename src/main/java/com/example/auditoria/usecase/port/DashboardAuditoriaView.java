package com.example.auditoria.usecase.port;

import java.util.List;

/**
 * Vista consolidada que el comite de auditoria consulta antes de cada reunion mensual.
 */
public record DashboardAuditoriaView(
        List<ConteoCategoria> porSeveridad,
        List<ConteoCategoria> porEstado,
        List<PromedioCategoria> promedioDiasCierrePorArea
) {
}
