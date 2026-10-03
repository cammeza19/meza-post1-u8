package com.example.auditoria.adapter.out.persistence;

import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * Repositorio append-only: extiende Repository (no JpaRepository) para exponer
 * unicamente insercion y lectura; no existe ningun metodo de actualizacion ni de borrado.
 */
public interface HistorialCambioEstadoJpaRepository extends Repository<HistorialCambioEstadoJpaEntity, Long> {

    HistorialCambioEstadoJpaEntity save(HistorialCambioEstadoJpaEntity registro);

    List<HistorialCambioEstadoJpaEntity> findByHallazgoIdOrderByFechaAscIdAsc(String hallazgoId);
}
