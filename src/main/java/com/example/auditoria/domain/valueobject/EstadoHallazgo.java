package com.example.auditoria.domain.valueobject;

/**
 * Ciclo de vida de un hallazgo de auditoria.
 * Enum con comportamiento: es el unico responsable de decidir que transiciones son validas.
 *
 * <pre>
 * ABIERTO -> EN_REMEDIACION -> CERRADO -> REABIERTO -> EN_REMEDIACION
 * </pre>
 */
public enum EstadoHallazgo {
    ABIERTO, EN_REMEDIACION, CERRADO, REABIERTO;

    public boolean puedeTransicionarA(EstadoHallazgo destino) {
        return switch (this) {
            case ABIERTO -> destino == EN_REMEDIACION;
            case EN_REMEDIACION -> destino == CERRADO;
            case CERRADO -> destino == REABIERTO;
            case REABIERTO -> destino == EN_REMEDIACION;
        };
    }
}
