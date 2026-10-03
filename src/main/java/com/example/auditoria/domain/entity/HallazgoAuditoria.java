package com.example.auditoria.domain.entity;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.domain.valueobject.PlanRemediacion;
import com.example.auditoria.domain.valueobject.Severidad;
import com.example.auditoria.domain.valueobject.TransicionInvalidaException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Aggregate Root del circulo Entities.
 * Su estado solo cambia a traves de metodos de dominio que validan la maquina de estados;
 * no expone setters. No depende de ningun framework.
 */
public class HallazgoAuditoria {

    private final HallazgoId id;
    private final String titulo;
    private final String descripcion;
    private final String areaResponsable;
    private final Severidad severidad;
    private final LocalDate fechaDeteccion;
    private EstadoHallazgo estado;
    private PlanRemediacion planRemediacion;
    private LocalDate fechaCierre;

    public HallazgoAuditoria(HallazgoId id, String titulo, String descripcion,
                             String areaResponsable, Severidad severidad, LocalDate fechaDeteccion) {
        Objects.requireNonNull(id, "El id del hallazgo es obligatorio");
        if (titulo == null || titulo.isBlank())
            throw new IllegalArgumentException("El titulo es obligatorio");
        if (areaResponsable == null || areaResponsable.isBlank())
            throw new IllegalArgumentException("El area responsable es obligatoria");
        Objects.requireNonNull(severidad, "La severidad es obligatoria");
        Objects.requireNonNull(fechaDeteccion, "La fecha de deteccion es obligatoria");
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.areaResponsable = areaResponsable;
        this.severidad = severidad;
        this.fechaDeteccion = fechaDeteccion;
        this.estado = EstadoHallazgo.ABIERTO;
    }

    /**
     * Reconstituye un hallazgo ya existente desde su representacion persistida,
     * conservando su estado, plan y fecha de cierre reales (sin reejecutar transiciones).
     * Lo usa exclusivamente el adaptador de persistencia.
     */
    public static HallazgoAuditoria reconstituir(HallazgoId id, String titulo, String descripcion,
                                                 String areaResponsable, Severidad severidad,
                                                 LocalDate fechaDeteccion, EstadoHallazgo estado,
                                                 PlanRemediacion planRemediacion, LocalDate fechaCierre) {
        HallazgoAuditoria hallazgo = new HallazgoAuditoria(
                id, titulo, descripcion, areaResponsable, severidad, fechaDeteccion);
        hallazgo.estado = Objects.requireNonNull(estado, "El estado es obligatorio");
        hallazgo.planRemediacion = planRemediacion;
        hallazgo.fechaCierre = fechaCierre;
        return hallazgo;
    }

    public EstadoHallazgo iniciarRemediacion(PlanRemediacion plan) {
        Objects.requireNonNull(plan, "El plan de remediacion es obligatorio");
        EstadoHallazgo anterior = transicionar(EstadoHallazgo.EN_REMEDIACION);
        this.planRemediacion = plan;
        return anterior;
    }

    public EstadoHallazgo cerrar() {
        if (planRemediacion == null)
            throw new IllegalStateException("No se puede cerrar un hallazgo sin plan de remediacion");
        EstadoHallazgo anterior = transicionar(EstadoHallazgo.CERRADO);
        this.fechaCierre = LocalDate.now();
        return anterior;
    }

    public EstadoHallazgo reabrir() {
        EstadoHallazgo anterior = transicionar(EstadoHallazgo.REABIERTO);
        this.fechaCierre = null;
        return anterior;
    }

    private EstadoHallazgo transicionar(EstadoHallazgo destino) {
        if (!estado.puedeTransicionarA(destino))
            throw new TransicionInvalidaException(estado, destino);
        EstadoHallazgo anterior = this.estado;
        this.estado = destino;
        return anterior;
    }

    // getters (sin setters: el estado solo cambia mediante los metodos de dominio)

    public HallazgoId getId() { return id; }

    public String getTitulo() { return titulo; }

    public String getDescripcion() { return descripcion; }

    public String getAreaResponsable() { return areaResponsable; }

    public Severidad getSeveridad() { return severidad; }

    public LocalDate getFechaDeteccion() { return fechaDeteccion; }

    public EstadoHallazgo getEstado() { return estado; }

    public PlanRemediacion getPlanRemediacion() { return planRemediacion; }

    public LocalDate getFechaCierre() { return fechaCierre; }
}
