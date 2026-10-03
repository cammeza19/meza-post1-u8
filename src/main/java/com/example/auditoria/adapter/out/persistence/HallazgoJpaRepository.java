package com.example.auditoria.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data de la entidad JPA de hallazgos.
 */
public interface HallazgoJpaRepository extends JpaRepository<HallazgoJpaEntity, String> {
}
