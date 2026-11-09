# AutoTech Manager

Sistema integral de gestión para el taller mecánico **AutoTech Motors S. de R.L.**
Administra clientes, vehículos, catálogo de servicios, mecánicos, inventario de repuestos,
órdenes de trabajo, diagnósticos, presupuestos, sustitución de motores, pagos y facturación.

Proyecto del Segundo Parcial de **Programación Avanzada II**.

## Tecnologías

- Java (Maven, NetBeans) con interfaz gráfica Swing
- JDBC con Microsoft SQL Server (Azure SQL)
- Arquitectura por capas: presentación (`vista`), negocio (`servicio`) y datos (`dao`, `conexion`)

## Cómo ejecutarlo

1. Ejecutar `database/AutoTechDB.sql` sobre la base **AutoTechDB** (SSMS o VS Code con la extensión mssql).
   Si la base ya existía antes del inicio de sesión, basta con ejecutar `database/02_Usuarios_Login.sql`.
2. Copiar `config.properties.example` como `config.properties` y escribir los datos de conexión.
3. Abrir el proyecto en NetBeans, hacer *Clean and Build* y ejecutar.
4. Iniciar sesión con el usuario asignado a cada integrante (`mairon`, `cristhian` o `cristopher`).
   Las contraseñas se entregan aparte; en la base solo se guarda su huella cifrada (PBKDF2-SHA256).

## Estructura

```
database/   Script SQL (tablas, procedimientos, vistas, triggers, datos de prueba y usuarios)
docs/       Diagrama entidad-relación, diagramas UML (Diagramas_AutoTech.pdf) y arquitectura
src/main/java
├── conexion      ConexionDB
├── dao           Contratos de persistencia (y dao/impl con JDBC)
├── servicio      Reglas de negocio y transacciones
├── hilos         Cierre de órdenes en segundo plano, monitor de stock y concurrencia
├── modelo        Entidades y enums
├── excepciones   Excepciones del sistema
├── util          Configuración, validaciones, sesión y cifrado de contraseñas
└── vista         Inicio de sesión, menú principal (tablero), formularios y estilos (EstiloUI)
```

## Integrantes

- 
- 
-
