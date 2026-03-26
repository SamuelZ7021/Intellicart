# Service Level Objectives (SLOs) y Service Level Indicators (SLIs)

Este documento define los compromisos de fiabilidad para la plataforma Intellicart.

## 1. API Gateway

### SLIs (Indicadores)
- **Disponibilidad:** Porcentaje de peticiones HTTP exitosas (status `200`-`4xx` vs `5xx`).
- **Latencia:** Tiempo de respuesta P95 de las peticiones HTTP.

### SLOs (Objetivos)
- **Disponibilidad:** 99.9% de las peticiones en un periodo de 30 días deben ser exitosas.
- **Latencia:** El 95% de las peticiones deben ser atendidas en menos de 200ms.

### Error Budget
- **Disponibilidad:** 0.1% de error permitido (~43 minutos de inactividad por mes).

---

## 2. Order Service

### SLIs
- **Disponibilidad de Creación de Órdenes:** Porcentaje de éxito en el endpoint `POST /api/orders`.
- **Latencia de Creación de Órdenes:** Tiempo P95 para procesar una nueva orden.

### SLOs
- **Disponibilidad:** 99.95% de órdenes procesadas sin errores del servidor.
- **Latencia:** P95 < 500ms para asegurar fluidez en el checkout.

### Error Budget
- **Disponibilidad:** 0.05% de fallos permitidos al mes (~21 minutos/mes).

---

## 3. ML Service (Recomendaciones)

### SLIs
- **Disponibilidad gRPC:** Porcentaje de llamadas gRPC exitosas desde Order/User Service a ML Service.
- **Latencia gRPC:** Tiempo de inferencia (P99).

### SLOs
- **Disponibilidad:** 99.5% de disponibilidad (la degradación de recomendaciones es aceptable).
- **Latencia:** P99 < 300ms.

### Error Budget
- 0.5% de fallos permitidos (~3 horas/mes).
