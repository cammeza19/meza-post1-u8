package com.example.auditoria.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repositorio Spring Data. En la Parte 2 se extiende con proyecciones de lectura para el dashboard
 * sobre el mismo esquema: no hay base de datos ni modelo de lectura separados.
 */
public interface HallazgoJpaRepository extends JpaRepository<HallazgoJpaEntity, String> {

    @Query("SELECT h.severidad AS categoria, COUNT(h) AS total FROM HallazgoJpaEntity h GROUP BY h.severidad")
    List<ConteoProjection> contarPorSeveridad();

    @Query("SELECT h.estado AS categoria, COUNT(h) AS total FROM HallazgoJpaEntity h GROUP BY h.estado")
    List<ConteoProjection> contarPorEstado();

    @Query("SELECT h.areaResponsable AS categoria, " +
            "AVG(DATEDIFF(DAY, h.fechaDeteccion, h.fechaCierre)) AS promedio " +
            "FROM HallazgoJpaEntity h WHERE h.estado = 'CERRADO' GROUP BY h.areaResponsable")
    List<PromedioProjection> promedioDiasCierrePorArea();

    interface ConteoProjection {
        String getCategoria();

        Long getTotal();
    }

    interface PromedioProjection {
        String getCategoria();

        Double getPromedio();
    }
}
