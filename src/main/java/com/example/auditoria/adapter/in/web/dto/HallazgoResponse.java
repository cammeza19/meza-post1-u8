package com.example.auditoria.adapter.in.web.dto;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.PlanRemediacion;

import java.time.LocalDate;

/**
 * DTO de salida: el dominio nunca se serializa directamente hacia HTTP.
 */
public record HallazgoResponse(
        String id,
        String titulo,
        String descripcion,
        String areaResponsable,
        String severidad,
        String estado,
        LocalDate fechaDeteccion,
        LocalDate fechaCierre,
        PlanRemediacionResponse planRemediacion
) {

    public record PlanRemediacionResponse(String responsable, LocalDate fechaLimite, String notas) {
    }

    public static HallazgoResponse desde(HallazgoAuditoria h) {
        PlanRemediacion plan = h.getPlanRemediacion();
        return new HallazgoResponse(
                h.getId().toString(),
                h.getTitulo(),
                h.getDescripcion(),
                h.getAreaResponsable(),
                h.getSeveridad().name(),
                h.getEstado().name(),
                h.getFechaDeteccion(),
                h.getFechaCierre(),
                plan == null ? null
                        : new PlanRemediacionResponse(plan.responsable(), plan.fechaLimite(), plan.notas()));
    }
}
