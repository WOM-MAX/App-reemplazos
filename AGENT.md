# App Reemplazos - Contexto Principal

Este documento sirve como el punto de anclaje de contexto para el desarrollo del proyecto **App Reemplazos**.

## Descripción del Proyecto
Sistema de gestión para el Colegio Acrópolis encargado del seguimiento de reemplazos, atrasos y gestión de personal.

## Arquitectura y Stack Tecnológico
- **Backend:** Java Spring Boot
- **Base de Datos:** PostgreSQL alojada en NeonDB
- **Despliegue:** Railway (Producción)
- **Frontend:** Archivos estáticos en `src/main/resources/static/` (HTML, CSS con Tailwind, Javascript Vanilla).

## Variables de Entorno y Conexión (Crítico)
El archivo `application.yml` fue preparado para despliegues externos. Las variables de entorno que el servidor DEBE inyectar son:
- `DATABASE_URL` (Debe ser formato `jdbc:postgresql://...`)
- `DB_USERNAME`
- `DB_PASSWORD`
*(Las variables antiguas `SPRING_DATASOURCE_URL` ya no son reconocidas de forma segura por el código actual).*

## Memoria y Continuidad
Para mantener el historial de trabajo, todos los arreglos, implementaciones y diagnósticos deben quedar registrados en la carpeta `memoria/` siguiendo el formato `YYYY-MM-DD_HH-MM_Tema.md`.
