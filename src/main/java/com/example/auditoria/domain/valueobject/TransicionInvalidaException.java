package com.example.auditoria.domain.valueobject;

/**
 * Se lanza cuando se intenta una transicion que la maquina de estados de EstadoHallazgo no permite.
 */
public class TransicionInvalidaException extends RuntimeException {

    public TransicionInvalidaException(EstadoHallazgo actual, EstadoHallazgo destino) {
        super("No se puede transicionar de " + actual + " a " + destino);
    }
}
