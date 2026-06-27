package com.rndmodgames.futtoboru.harness;

/**
 * Thrown when headless simulation cannot progress (mandatory gate, max days exceeded, etc.).
 */
public class SimulationDeadlockException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SimulationDeadlockException(String message) {
        super(message);
    }

    public SimulationDeadlockException(String message, Throwable cause) {
        super(message, cause);
    }
}
