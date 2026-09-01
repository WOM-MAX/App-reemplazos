# App Reemplazos - Bitácora de Memoria

## Fecha: 1 de Septiembre de 2026
## Tema: Optimización de HikariCP para Auto-suspend en Neon Serverless

### Problema Identificado
- NeonDB reportó un consumo excesivo de horas-CU (>80% al final del mes anterior y 3.66 horas consumidas en el primer día del mes), a pesar de no haber tráfico activo de usuarios en la aplicación.
- Causa raíz: HikariCP mantenía los valores por defecto (`minimum-idle = 10`), conservando 10 conexiones TCP abiertas de forma permanente hacia PostgreSQL y realizando reciclos periódicos de conexiones.
- Al no haber 5 minutos continuos con 0 conexiones, la característica de suspensión automática (auto-suspend) de Neon Serverless nunca se activaba, manteniendo la instancia a 0.25 CU las 24 horas del día (consumo de ~180 horas-CU mensuales contra el límite de 100 horas-CU del plan gratuito).

### Solución Implementada
- Se configuró el bloque `spring.datasource.hikari` en `application.yml`:
  - `minimum-idle: 0`: Permite al pool cerrar todas las conexiones cuando no hay peticiones activas.
  - `idle-timeout: 60000`: Cierra conexiones inactivas tras 60 segundos (1 minuto).
  - `maximum-pool-size: 5`: Limita el número de conexiones concurrentes máximas.
  - `max-lifetime: 600000`: Tiempo de vida máximo de 10 minutos por conexión.
  - `connection-timeout: 60000`: Tiempo de espera de 60 segundos para permitir el arranque en frío (cold start) de NeonDB al despertar de la suspensión.
  - `keepalive-time: 0`: Deshabilita pings continuos de keepalive para no interrumpir el temporizador de inactividad de Neon.

### Validación
- Compilación ejecutada exitosamente con `./mvnw.cmd test-compile`.

### Siguiente Paso
- Realizar commit y push a GitHub para que Railway ejecute el nuevo despliegue con la configuración actualizada.
