/**
 * Fehlerbehandlung.
 *
 * <p><b>Aufgabe:</b> Eigene Exceptions für fachliche Fehler und eine zentrale Stelle, die daraus
 * einheitliche HTTP-Antworten macht.
 *
 * <p><b>Gehört hierher:</b>
 * <ul>
 *   <li>Exceptions wie {@code ResourceNotFoundException} (404) oder
 *       {@code AbsenceOverlapException} (409, Doppelbuchung)</li>
 *   <li>{@code GlobalExceptionHandler} mit {@code @RestControllerAdvice}, der Exceptions in ein
 *       einheitliches Fehlerformat umwandelt</li>
 * </ul>
 *
 * <p><b>Regeln:</b> Services werfen Exceptions, Controller fangen sie nicht selbst ab.
 */
package de.hs_esslingen.provacasy_backend.exception;
