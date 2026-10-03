package com.example.auditoria.adapter.out.persistence;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * Registro de la bitacora de cambios de estado. Append-only: no tiene setters,
 * todas sus columnas son no actualizables y Hibernate la trata como inmutable.
 * No se usa para reconstruir el estado del agregado.
 */
@Entity
@Immutable
@Table(name = "historial_cambios_estado",
        indexes = @Index(name = "idx_historial_hallazgo", columnList = "hallazgoId"))
public class HistorialCambioEstadoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, length = 36)
    private String hallazgoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private EstadoHallazgo estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private EstadoHallazgo estadoNuevo;

    @Column(updatable = false, length = 1000)
    private String motivo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    protected HistorialCambioEstadoJpaEntity() {
        // constructor vacio exigido por JPA
    }

    public HistorialCambioEstadoJpaEntity(String hallazgoId, EstadoHallazgo estadoAnterior,
                                          EstadoHallazgo estadoNuevo, String motivo, LocalDateTime fecha) {
        this.hallazgoId = hallazgoId;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.motivo = motivo;
        this.fecha = fecha;
    }

    public Long getId() { return id; }

    public String getHallazgoId() { return hallazgoId; }

    public EstadoHallazgo getEstadoAnterior() { return estadoAnterior; }

    public EstadoHallazgo getEstadoNuevo() { return estadoNuevo; }

    public String getMotivo() { return motivo; }

    public LocalDateTime getFecha() { return fecha; }
}
