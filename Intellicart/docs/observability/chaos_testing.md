# Plan de Testing de Resiliencia y Chaos Engineering

Este documento describe las validaciones para asegurar el comportamiento del sistema ante fallos (Day 5).

## 1. Simulación de Fail-overs (Chaos Básico)

### 1.1 Caída de la Base de Datos
- **Acción:** `docker stop intellicart-db` durante carga de trabajo.
- **Expectativa:** 
  - `order-service` y `user-service` reportan fallos inmediatos de healthcheck.
  - Alertas correspondientes se disparan.
  - Al reiniciar (`docker start intellicart-db`), los servicios se recuperan exitosamente o entran en un ciclo de re-intento seguro.

### 1.2 Caída del Broker de Eventos (Kafka)
- **Acción:** `docker stop kafka`.
- **Expectativa:**
  - Patrón **Transactional Outbox** almacena eventos en base de datos.
  - Al reiniciar Kafka, los eventos pendientes son leídos, serializados y enviados sin pérdida de mensajes.

### 1.3 Latencia Inducida en ML Service
- **Acción:** Usar una herramienta proxy toxiproxy o añadir `time.sleep(2)` en el endpoint de FastAPI.
- **Expectativa:**
  - Resilience4J Circuit Breaker en el cliente gRPC Java se abre.
  - Devuelve recomendaciones por defecto de la caché en Redis (fallback mechanism).
  - Grafana refleja error rate o tiempos de caída que no impactan al resto de la plataforma.

## 2. Validación de Alertas

- Disparar peticiones con status 500 al API Gateway artificialmente mediante scripts.
- Verificar mediante endpoint dummy de Prometheus Alertmanager que el webhook de notificación es lanzado exitosamente de acuerdo con las reglas configuradas.
