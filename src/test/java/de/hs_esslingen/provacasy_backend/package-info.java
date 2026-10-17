/**
 * ProVaCaSy (Professoren Vacation Calendar System), Backend.
 *
 * <p>Der Quellcode ist nach technischen Schichten in Pakete gegliedert. Jedes Paket hat genau eine
 * Aufgabe. Abhängigkeiten zeigen nur nach unten:
 *
 * <pre>
 * controller  -&gt;  service  -&gt;  repository  -&gt;  model
 *     |             |
 *     v             v
 *    dto  &lt;-----  mapper
 * </pre>
 *
 * <p>Querschnittsthemen liegen in eigenen Paketen: {@code security} (Keycloak, Rollen),
 * {@code config} (technische Konfiguration) und {@code exception} (zentrale Fehlerbehandlung).
 *
 * <p>Eine ausführliche Beschreibung mit Beispielen steht in {@code docs/architecture.md}.
 */
package de.hs_esslingen.provacasy_backend;
