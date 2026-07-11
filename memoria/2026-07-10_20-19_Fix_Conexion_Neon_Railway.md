# App Reemplazos - Bitácora de Memoria

## Fecha: 10 de Julio de 2026
## Tema: Resolución Crítica de Conexión entre Railway y NeonDB

### Síntomas Iniciales
- La aplicación devolvía sistemáticamente el error "Usuario o contraseña incorrectos" al intentar hacer login con el usuario `Walter`.
- El dashboard de métricas de NeonDB mostraba estado "Idle" y "Archived" con 0 actividad reciente.
- La aplicación no presentaba errores 500 en Railway, solo errores 4xx y redirecciones 3xx.

### Diagnóstico e Investigación
1. **Teoría de Hibernación (Timeout):** Se detectó que NeonDB entra en suspensión profunda por inactividad. El arranque en frío tarda ~45s, pero HikariCP (Spring Boot) tiene un timeout de 30s. Esto provocaba que Java abortara la conexión antes de que Neon despertara, y Spring Security ocultaba el error devolviendo un fallo genérico de autenticación.
2. **Prueba de conexión manual:** Se despertó NeonDB a través de su SQL Editor y se ejecutó `SELECT * FROM usuarios;`. Los datos estaban intactos, confirmando que la base de datos no se había borrado. Se le inyectó el hash de bcrypt de `admin123` al usuario `Walter`.
3. **Descubrimiento del Problema Raíz:** A pesar de tener Neon despierto, el login seguía fallando y Neon no registraba la actividad. Al revisar el historial de Git (`git log application.yml`), se descubrió que un commit del 27 de abril modificó el código para exigir las variables `DATABASE_URL`, `DB_USERNAME` y `DB_PASSWORD` (preparando la app para Render). Sin embargo, el entorno de Railway seguía usando las variables antiguas `SPRING_DATASOURCE_URL`. Al no encontrar la variable nueva, la app intentaba conectarse a `localhost`, fallando silenciosamente.

### Solución Implementada
- Se actualizaron/renombraron las variables de entorno directamente en el panel de Railway para que coincidieran exactamente con las que exige el código de `application.yml`:
  - `DATABASE_URL` = `jdbc:postgresql://ep...`
  - `DB_USERNAME` = `neondb_owner`
  - `DB_PASSWORD` = `[REDACTED]`
- Se desencadenó un nuevo despliegue automático en Railway.
- Tras el despliegue exitoso, la conexión a NeonDB se restauró y el login funcionó correctamente conservando todas las claves y datos históricos.

### Próximos Pasos Recomendados
- Iniciar revisión del código fuente para próximas mejoras o nuevas funcionalidades en el Dashboard.
