# Runbooks y Procedimientos de Respuesta a Incidentes

## 1. Alertas Críticas

### 1.1 PostgresDBDown (Base de Datos Caída)
**Severidad:** Crítica (P1)
**Descripción:** El servicio `postgres` no responde o las conexiones fallan.
**Procedimiento:**
1. Verificar logs del contenedor: `docker logs intellicart-db`.
2. Verificar espacio en disco del volumen `postgres_data`.
3. Revisar uso de memoria/CPU del contenedor.
4. Si está bloqueado, reiniciar el contenedor: `docker restart intellicart-db`.
**Escalamiento:** Inmediato al DBA o Tech Lead.

### 1.2 HighOrderFailureRate (Tasa Alta de Fallos en Órdenes)
**Severidad:** Alta (P2)
**Descripción:** Más del 5% de las órdenes están fallando o entran en rollback (SAGA).
**Procedimiento:**
1. Revisar los traces de Jaeger filtrando por `order-service` y tags `error=true`.
2. Verificar si el cuello de botella es `payment-service` o el publicador de eventos de Kafka.
3. Chequear el lag del consumidor de Kafka en el topic `order-events` via Kafka UI.
**Escalamiento:** Equipo de Backend.

### 1.3 ApiGatewayHighLatency (Latencia Alta en Gateway)
**Severidad:** Media (P3)
**Descripción:** El P95 del `api-gateway` excede los 500ms durante más de 5 minutos.
**Procedimiento:**
1. Verificar dashboards de Grafana (Service Dashboard) para identificar el servicio back-end lento.
2. Comprobar si hay recolección de basura prolongada (GC Pauses) en el servicio implicado.

---

## 2. Procedimientos Comunes

- **Reinicio Seguro de Servicios:** `docker-compose stop <service> && docker-compose up -d <service>`.
- **Limpieza de Tópicos Kafka:** A través de Kafka UI (`http://localhost:8080`), seleccionar el tópico y dar "Clear Messages".
