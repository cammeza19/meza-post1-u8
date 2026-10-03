package com.example.auditoria.usecase.port;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.HallazgoId;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida hacia la persistencia de hallazgos.
 */
public interface HallazgoRepositoryPort {

    void guardar(HallazgoAuditoria hallazgo);

    Optional<HallazgoAuditoria> buscarPorId(HallazgoId id);

    List<HallazgoAuditoria> buscarTodos();
}
