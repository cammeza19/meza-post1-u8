package com.example.auditoria.domain.valueobject;

/**
 * Clasificacion del impacto de un hallazgo.
 * Enum simple: no encapsula reglas de negocio propias (ver README, decision de diseno 1).
 */
public enum Severidad {
    CRITICA, ALTA, MEDIA, BAJA
}
