# AutoTech Manager

Sistema integral de gestión para el taller mecánico **AutoTech Motors S. de R.L.**
Administra clientes, vehículos, catálogo de servicios, mecánicos, inventario de repuestos,
órdenes de trabajo, diagnósticos, presupuestos, sustitución de motores, pagos y facturación.

Proyecto del Segundo Parcial de **Programación Avanzada II**.

## Tecnologías

- Java (Maven, NetBeans) con interfaz gráfica Swing
- JDBC con Microsoft SQL Server (Azure SQL)
- Arquitectura por capas: presentación (`vista`), negocio (`negocio`) y datos (`dao`, `conexion`)

## Cómo ejecutarlo

1. Ejecutar `database/AutoTechDB.sql` sobre la base **AutoTechDB** (SSMS o VS Code con la extensión mssql).
2. Copiar `config.properties.example` como `config.properties` y escribir los datos de conexión.
3. Abrir el proyecto en NetBeans, hacer *Clean and Build* y ejecutar.

## Estructura

```
database/   Script SQL (tablas, procedimientos, vistas, triggers y datos de prueba)
docs/       Diagrama entidad-relación, diagramas UML y arquitectura
src/main/java
├── conexion      ConexionDB
├── dao           Contratos de persistencia (y dao/impl con JDBC)
├── negocio       Reglas de negocio y transacciones
├── hilos         Procesamiento en segundo plano
├── modelo        Entidades y enums
├── excepciones   Excepciones del sistema
├── util          Configuración y validaciones
└── vista         Menú principal y formularios
```

## Integrantes

- 
- 
- 
