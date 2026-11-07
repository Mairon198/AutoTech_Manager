/* =====================================================================
   AutoTech Manager - Base de datos AutoTechDB (SQL Server / Azure SQL)
   Programación Avanzada II - Segundo Parcial
   ---------------------------------------------------------------------
   Cómo ejecutarlo:
   1. Crear la base AutoTechDB (en Azure: portal > SQL databases > Create,
      en el mismo servidor, plan Basic).
   2. Conectarse a AutoTechDB con SSMS o con VS Code (extensión mssql)
      y ejecutar este archivo completo (F5).
   ATENCIÓN: la sección 0 BORRA todas las tablas y las vuelve a crear,
   así que el script se puede ejecutar varias veces durante el desarrollo.
   ---------------------------------------------------------------------
   Contenido:
     0. Limpieza
     1. Catálogos (estados, impuestos, especialidades)
     2. Clientes y vehículos
     3. Mecánicos
     4. Proveedores, repuestos e inventario
     5. Catálogo de servicios
     6. Órdenes de trabajo y detalle
     7. Diagnóstico y presupuesto
     8. Actividades, consumos y control de calidad
     9. Motores y cambio de motor
    10. Garantías, pagos y facturas
    11. Auditoría
    12. Funciones
    13. Triggers de auditoría
    14. Procedimientos almacenados
    15. Vistas administrativas
    16. Datos de prueba
    17. Consultas de ejemplo (JOIN, GROUP BY, subconsultas)
   ===================================================================== */


/* =====================================================================
   0. LIMPIEZA (orden inverso a las dependencias)
   ===================================================================== */
DROP VIEW IF EXISTS dbo.vw_HistorialVehiculo;
DROP VIEW IF EXISTS dbo.vw_OrdenesResumen;
DROP VIEW IF EXISTS dbo.vw_InventarioBajoMinimo;
DROP VIEW IF EXISTS dbo.vw_ProductividadMecanicos;
DROP VIEW IF EXISTS dbo.vw_ServiciosMasSolicitados;
DROP PROCEDURE IF EXISTS dbo.sp_ConsumirRepuesto;
DROP PROCEDURE IF EXISTS dbo.sp_RegistrarEntradaInventario;
DROP PROCEDURE IF EXISTS dbo.sp_AsignarMecanico;
DROP PROCEDURE IF EXISTS dbo.sp_CambiarEstadoOrden;
DROP PROCEDURE IF EXISTS dbo.sp_RegistrarCambioMotor;
DROP PROCEDURE IF EXISTS dbo.sp_RegistrarPago;
DROP PROCEDURE IF EXISTS dbo.sp_GenerarFactura;
DROP PROCEDURE IF EXISTS dbo.sp_HistorialVehiculo;
DROP FUNCTION IF EXISTS dbo.fn_TotalesOrden;
DROP FUNCTION IF EXISTS dbo.fn_MotorCompatible;

DROP TABLE IF EXISTS dbo.Auditoria;
DROP TABLE IF EXISTS dbo.Facturas;
DROP TABLE IF EXISTS dbo.Pagos;
DROP TABLE IF EXISTS dbo.Garantias;
DROP TABLE IF EXISTS dbo.CambiosMotor;
DROP TABLE IF EXISTS dbo.CompatibilidadMotor;
DROP TABLE IF EXISTS dbo.Motores;
DROP TABLE IF EXISTS dbo.ControlesCalidad;
DROP TABLE IF EXISTS dbo.MovimientosInventario;
DROP TABLE IF EXISTS dbo.ConsumoRepuestos;
DROP TABLE IF EXISTS dbo.ActividadesTrabajo;
DROP TABLE IF EXISTS dbo.DetallePresupuesto;
DROP TABLE IF EXISTS dbo.Presupuestos;
DROP TABLE IF EXISTS dbo.EvidenciasTecnicas;
DROP TABLE IF EXISTS dbo.Diagnosticos;
DROP TABLE IF EXISTS dbo.AsignacionMecanicos;
DROP TABLE IF EXISTS dbo.DetalleOrden;
DROP TABLE IF EXISTS dbo.OrdenesTrabajo;
DROP TABLE IF EXISTS dbo.ServicioRepuestos;
DROP TABLE IF EXISTS dbo.Servicios;
DROP TABLE IF EXISTS dbo.Repuestos;
DROP TABLE IF EXISTS dbo.Proveedores;
DROP TABLE IF EXISTS dbo.MecanicoEspecialidades;
DROP TABLE IF EXISTS dbo.Mecanicos;
DROP TABLE IF EXISTS dbo.Especialidades;
DROP TABLE IF EXISTS dbo.Vehiculos;
DROP TABLE IF EXISTS dbo.Clientes;
DROP TABLE IF EXISTS dbo.TransicionesEstadoOrden;
DROP TABLE IF EXISTS dbo.EstadosOrden;
DROP TABLE IF EXISTS dbo.ConfiguracionImpuestos;
GO


/* =====================================================================
   1. CATÁLOGOS
   ===================================================================== */

-- Estados posibles de una orden (mismo flujo que el enum EstadoOrden de Java)
CREATE TABLE dbo.EstadosOrden (
    Codigo       VARCHAR(25)  NOT NULL CONSTRAINT PK_EstadosOrden PRIMARY KEY,
    Descripcion  VARCHAR(50)  NOT NULL,
    Secuencia    TINYINT      NOT NULL,
    EsFinal      BIT          NOT NULL CONSTRAINT DF_EstadosOrden_EsFinal DEFAULT 0
);
GO

-- Cambios de estado permitidos (tabla puente estado -> estado)
CREATE TABLE dbo.TransicionesEstadoOrden (
    EstadoActual     VARCHAR(25) NOT NULL CONSTRAINT FK_Trans_Actual    REFERENCES dbo.EstadosOrden(Codigo),
    EstadoSiguiente  VARCHAR(25) NOT NULL CONSTRAINT FK_Trans_Siguiente REFERENCES dbo.EstadosOrden(Codigo),
    CONSTRAINT PK_TransicionesEstadoOrden PRIMARY KEY (EstadoActual, EstadoSiguiente),
    CONSTRAINT CK_Trans_Distinto CHECK (EstadoActual <> EstadoSiguiente)
);
GO

-- Impuestos configurables (ej.: ISV 15 %). Solo uno puede ser el predeterminado.
CREATE TABLE dbo.ConfiguracionImpuestos (
    IdImpuesto       INT IDENTITY(1,1) CONSTRAINT PK_ConfiguracionImpuestos PRIMARY KEY,
    Nombre           VARCHAR(50)   NOT NULL CONSTRAINT UQ_Impuestos_Nombre UNIQUE,
    Porcentaje       DECIMAL(5,2)  NOT NULL CONSTRAINT CK_Impuestos_Porcentaje CHECK (Porcentaje BETWEEN 0 AND 100),
    EsPredeterminado BIT           NOT NULL CONSTRAINT DF_Impuestos_Predet DEFAULT 0,
    Activo           BIT           NOT NULL CONSTRAINT DF_Impuestos_Activo DEFAULT 1
);
GO
CREATE UNIQUE INDEX UX_Impuestos_Predeterminado ON dbo.ConfiguracionImpuestos(EsPredeterminado) WHERE EsPredeterminado = 1;
GO

CREATE TABLE dbo.Especialidades (
    IdEspecialidad INT IDENTITY(1,1) CONSTRAINT PK_Especialidades PRIMARY KEY,
    Nombre         VARCHAR(60)   NOT NULL CONSTRAINT UQ_Especialidades_Nombre UNIQUE,
    Descripcion    VARCHAR(200)  NULL,
    Activo         BIT           NOT NULL CONSTRAINT DF_Especialidades_Activo DEFAULT 1
);
GO


/* =====================================================================
   2. CLIENTES Y VEHÍCULOS (Módulo 1)
   ===================================================================== */
CREATE TABLE dbo.Clientes (
    IdCliente      INT IDENTITY(1,1) CONSTRAINT PK_Clientes PRIMARY KEY,
    Identidad      VARCHAR(20)   NOT NULL CONSTRAINT UQ_Clientes_Identidad UNIQUE,
    Nombre         VARCHAR(80)   NOT NULL,
    Apellido       VARCHAR(80)   NULL,
    Telefono       VARCHAR(20)   NULL,
    Correo         VARCHAR(120)  NULL,
    Direccion      VARCHAR(200)  NULL,
    Activo         BIT           NOT NULL CONSTRAINT DF_Clientes_Activo DEFAULT 1,
    FechaCreacion  DATETIME2(0)  NOT NULL CONSTRAINT DF_Clientes_Fecha  DEFAULT SYSDATETIME(),
    CONSTRAINT CK_Clientes_Nombre CHECK (LEN(LTRIM(Nombre)) > 0),
    CONSTRAINT CK_Clientes_Correo CHECK (Correo IS NULL OR Correo LIKE '%_@_%._%')
);
GO

CREATE TABLE dbo.Vehiculos (
    IdVehiculo     INT IDENTITY(1,1) CONSTRAINT PK_Vehiculos PRIMARY KEY,
    IdCliente      INT           NOT NULL CONSTRAINT FK_Vehiculos_Clientes REFERENCES dbo.Clientes(IdCliente),
    Placa          VARCHAR(10)   NOT NULL CONSTRAINT UQ_Vehiculos_Placa UNIQUE,
    VIN            CHAR(17)      NOT NULL CONSTRAINT UQ_Vehiculos_VIN   UNIQUE,
    Marca          VARCHAR(40)   NOT NULL,
    Modelo         VARCHAR(40)   NOT NULL,
    Anio           SMALLINT      NOT NULL,
    Color          VARCHAR(30)   NULL,
    Kilometraje    INT           NOT NULL CONSTRAINT DF_Vehiculos_Km DEFAULT 0,
    Combustible    VARCHAR(15)   NOT NULL,
    Activo         BIT           NOT NULL CONSTRAINT DF_Vehiculos_Activo DEFAULT 1,
    FechaCreacion  DATETIME2(0)  NOT NULL CONSTRAINT DF_Vehiculos_Fecha  DEFAULT SYSDATETIME(),
    CONSTRAINT CK_Vehiculos_Placa       CHECK (Placa LIKE '[A-Z][A-Z][A-Z] [0-9][0-9][0-9][0-9]'),
    CONSTRAINT CK_Vehiculos_VIN         CHECK (LEN(VIN) = 17 AND VIN NOT LIKE '%[IOQ]%'),
    CONSTRAINT CK_Vehiculos_Anio        CHECK (Anio BETWEEN 1950 AND 2100),
    CONSTRAINT CK_Vehiculos_Km          CHECK (Kilometraje >= 0),
    CONSTRAINT CK_Vehiculos_Combustible CHECK (Combustible IN ('GASOLINA','DIESEL','HIBRIDO','ELECTRICO','GLP')),
    -- Permite que OrdenesTrabajo verifique que el vehículo pertenece al cliente
    CONSTRAINT UQ_Vehiculos_IdCliente UNIQUE (IdVehiculo, IdCliente)
);
GO
CREATE INDEX IX_Vehiculos_Cliente ON dbo.Vehiculos(IdCliente);
GO


/* =====================================================================
   3. MECÁNICOS (Módulo 4)
   ===================================================================== */
CREATE TABLE dbo.Mecanicos (
    IdMecanico      INT IDENTITY(1,1) CONSTRAINT PK_Mecanicos PRIMARY KEY,
    CodigoEmpleado  VARCHAR(10)   NOT NULL CONSTRAINT UQ_Mecanicos_Codigo    UNIQUE,
    Identidad       VARCHAR(20)   NOT NULL CONSTRAINT UQ_Mecanicos_Identidad UNIQUE,
    Nombre          VARCHAR(80)   NOT NULL,
    Apellido        VARCHAR(80)   NULL,
    Telefono        VARCHAR(20)   NULL,
    Correo          VARCHAR(120)  NULL,
    Direccion       VARCHAR(200)  NULL,
    TarifaHora      DECIMAL(10,2) NOT NULL CONSTRAINT DF_Mecanicos_Tarifa DEFAULT 0,
    Disponible      BIT           NOT NULL CONSTRAINT DF_Mecanicos_Disp   DEFAULT 1,
    Activo          BIT           NOT NULL CONSTRAINT DF_Mecanicos_Activo DEFAULT 1,
    FechaCreacion   DATETIME2(0)  NOT NULL CONSTRAINT DF_Mecanicos_Fecha  DEFAULT SYSDATETIME(),
    CONSTRAINT CK_Mecanicos_Tarifa CHECK (TarifaHora >= 0)
);
GO

-- Tabla puente: un mecánico tiene varias especialidades
CREATE TABLE dbo.MecanicoEspecialidades (
    IdMecanico      INT NOT NULL CONSTRAINT FK_MecEsp_Mecanico     REFERENCES dbo.Mecanicos(IdMecanico),
    IdEspecialidad  INT NOT NULL CONSTRAINT FK_MecEsp_Especialidad REFERENCES dbo.Especialidades(IdEspecialidad),
    CONSTRAINT PK_MecanicoEspecialidades PRIMARY KEY (IdMecanico, IdEspecialidad)
);
GO


/* =====================================================================
   4. PROVEEDORES, REPUESTOS E INVENTARIO (Módulo 5)
   ===================================================================== */
CREATE TABLE dbo.Proveedores (
    IdProveedor    INT IDENTITY(1,1) CONSTRAINT PK_Proveedores PRIMARY KEY,
    RTN            VARCHAR(20)   NOT NULL CONSTRAINT UQ_Proveedores_RTN UNIQUE,
    Nombre         VARCHAR(100)  NOT NULL,
    Contacto       VARCHAR(80)   NULL,
    Telefono       VARCHAR(20)   NULL,
    Correo         VARCHAR(120)  NULL,
    Direccion      VARCHAR(200)  NULL,
    Activo         BIT           NOT NULL CONSTRAINT DF_Proveedores_Activo DEFAULT 1,
    FechaCreacion  DATETIME2(0)  NOT NULL CONSTRAINT DF_Proveedores_Fecha  DEFAULT SYSDATETIME()
);
GO

CREATE TABLE dbo.Repuestos (
    IdRepuesto     INT IDENTITY(1,1) CONSTRAINT PK_Repuestos PRIMARY KEY,
    Codigo         VARCHAR(20)   NOT NULL CONSTRAINT UQ_Repuestos_Codigo UNIQUE,
    Nombre         VARCHAR(100)  NOT NULL,
    Categoria      VARCHAR(15)   NOT NULL,
    Marca          VARCHAR(40)   NULL,
    Stock          INT           NOT NULL CONSTRAINT DF_Repuestos_Stock    DEFAULT 0,
    StockMinimo    INT           NOT NULL CONSTRAINT DF_Repuestos_StockMin DEFAULT 0,
    CostoUnitario  DECIMAL(12,2) NOT NULL CONSTRAINT DF_Repuestos_Costo    DEFAULT 0,
    PrecioVenta    DECIMAL(12,2) NOT NULL CONSTRAINT DF_Repuestos_Precio   DEFAULT 0,
    IdProveedor    INT           NULL CONSTRAINT FK_Repuestos_Proveedores REFERENCES dbo.Proveedores(IdProveedor),
    Activo         BIT           NOT NULL CONSTRAINT DF_Repuestos_Activo DEFAULT 1,
    FechaCreacion  DATETIME2(0)  NOT NULL CONSTRAINT DF_Repuestos_Fecha  DEFAULT SYSDATETIME(),
    CONSTRAINT CK_Repuestos_Stock     CHECK (Stock >= 0),          -- nunca stock negativo
    CONSTRAINT CK_Repuestos_StockMin  CHECK (StockMinimo >= 0),
    CONSTRAINT CK_Repuestos_Costo     CHECK (CostoUnitario >= 0),
    CONSTRAINT CK_Repuestos_Precio    CHECK (PrecioVenta >= 0),
    CONSTRAINT CK_Repuestos_Categoria CHECK (Categoria IN ('ACEITE','FILTRO','BUJIA','BATERIA','FRENOS','RADIADOR',
                                                          'TRANSMISION','MOTOR','ELECTRICO','SUSPENSION','OTRO'))
);
GO


/* =====================================================================
   5. CATÁLOGO DE SERVICIOS (Módulo 2)
   ===================================================================== */
CREATE TABLE dbo.Servicios (
    IdServicio          INT IDENTITY(1,1) CONSTRAINT PK_Servicios PRIMARY KEY,
    Codigo              VARCHAR(10)   NOT NULL CONSTRAINT UQ_Servicios_Codigo UNIQUE,
    Nombre              VARCHAR(100)  NOT NULL,
    Descripcion         VARCHAR(300)  NULL,
    Nivel               TINYINT       NOT NULL,   -- 1 básico ... 5 reparación mayor
    CostoManoObra       DECIMAL(12,2) NOT NULL,   -- costo base vigente (el histórico queda en DetalleOrden)
    TiempoEstimadoHoras DECIMAL(6,2)  NOT NULL,
    Activo              BIT           NOT NULL CONSTRAINT DF_Servicios_Activo DEFAULT 1,
    FechaCreacion       DATETIME2(0)  NOT NULL CONSTRAINT DF_Servicios_Fecha  DEFAULT SYSDATETIME(),
    CONSTRAINT CK_Servicios_Nivel  CHECK (Nivel BETWEEN 1 AND 5),
    CONSTRAINT CK_Servicios_Costo  CHECK (CostoManoObra >= 0),
    CONSTRAINT CK_Servicios_Tiempo CHECK (TiempoEstimadoHoras > 0)
);
GO

-- Tabla puente: repuestos asociados a cada servicio
CREATE TABLE dbo.ServicioRepuestos (
    IdServicio        INT NOT NULL CONSTRAINT FK_ServRep_Servicio REFERENCES dbo.Servicios(IdServicio),
    IdRepuesto        INT NOT NULL CONSTRAINT FK_ServRep_Repuesto REFERENCES dbo.Repuestos(IdRepuesto),
    CantidadSugerida  INT NOT NULL CONSTRAINT DF_ServRep_Cantidad DEFAULT 1,
    CONSTRAINT PK_ServicioRepuestos PRIMARY KEY (IdServicio, IdRepuesto),
    CONSTRAINT CK_ServRep_Cantidad CHECK (CantidadSugerida > 0)
);
GO


/* =====================================================================
   6. ÓRDENES DE TRABAJO (Módulo 3)
   ===================================================================== */
CREATE TABLE dbo.OrdenesTrabajo (
    IdOrden                INT IDENTITY(1,1) CONSTRAINT PK_OrdenesTrabajo PRIMARY KEY,
    -- Número único y legible, calculado a partir del Id: OT-000001
    Numero                 AS ('OT-' + RIGHT('000000' + CAST(IdOrden AS VARCHAR(10)), 6)) PERSISTED,
    IdCliente              INT           NOT NULL,
    IdVehiculo             INT           NOT NULL,
    FechaIngreso           DATETIME2(0)  NOT NULL CONSTRAINT DF_Ordenes_FechaIngreso DEFAULT SYSDATETIME(),
    KilometrajeIngreso     INT           NOT NULL,
    Sintomas               VARCHAR(500)  NOT NULL,
    DiagnosticoPreliminar  VARCHAR(500)  NULL,
    Estado                 VARCHAR(25)   NOT NULL CONSTRAINT DF_Ordenes_Estado DEFAULT 'RECIBIDA'
                                         CONSTRAINT FK_Ordenes_Estado REFERENCES dbo.EstadosOrden(Codigo),
    FechaEntrega           DATETIME2(0)  NULL,
    Activo                 BIT           NOT NULL CONSTRAINT DF_Ordenes_Activo DEFAULT 1,
    FechaCreacion          DATETIME2(0)  NOT NULL CONSTRAINT DF_Ordenes_Fecha  DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Ordenes_Numero UNIQUE (Numero),
    CONSTRAINT FK_Ordenes_Clientes  FOREIGN KEY (IdCliente) REFERENCES dbo.Clientes(IdCliente),
    -- El vehículo debe pertenecer al cliente de la orden
    CONSTRAINT FK_Ordenes_VehiculoCliente FOREIGN KEY (IdVehiculo, IdCliente) REFERENCES dbo.Vehiculos(IdVehiculo, IdCliente),
    CONSTRAINT CK_Ordenes_Km        CHECK (KilometrajeIngreso >= 0),
    CONSTRAINT CK_Ordenes_Sintomas  CHECK (LEN(LTRIM(Sintomas)) > 0),
    CONSTRAINT CK_Ordenes_Entrega   CHECK (FechaEntrega IS NULL OR FechaEntrega >= FechaIngreso)
);
GO
CREATE INDEX IX_Ordenes_Vehiculo ON dbo.OrdenesTrabajo(IdVehiculo);
CREATE INDEX IX_Ordenes_Estado   ON dbo.OrdenesTrabajo(Estado);
GO

-- Servicios por orden. PrecioUnitario guarda el precio del momento (histórico)
CREATE TABLE dbo.DetalleOrden (
    IdDetalle       INT IDENTITY(1,1) CONSTRAINT PK_DetalleOrden PRIMARY KEY,
    IdOrden         INT           NOT NULL CONSTRAINT FK_DetalleOrden_Orden    REFERENCES dbo.OrdenesTrabajo(IdOrden),
    IdServicio      INT           NOT NULL CONSTRAINT FK_DetalleOrden_Servicio REFERENCES dbo.Servicios(IdServicio),
    Cantidad        INT           NOT NULL CONSTRAINT DF_DetalleOrden_Cantidad DEFAULT 1,
    PrecioUnitario  DECIMAL(12,2) NOT NULL,
    Observacion     VARCHAR(200)  NULL,
    CONSTRAINT UQ_DetalleOrden UNIQUE (IdOrden, IdServicio),
    CONSTRAINT CK_DetalleOrden_Cantidad CHECK (Cantidad > 0),
    CONSTRAINT CK_DetalleOrden_Precio   CHECK (PrecioUnitario >= 0)
);
GO

-- Tabla puente orden - mecánico. Solo un responsable principal por orden.
CREATE TABLE dbo.AsignacionMecanicos (
    IdOrden          INT          NOT NULL CONSTRAINT FK_Asignacion_Orden    REFERENCES dbo.OrdenesTrabajo(IdOrden),
    IdMecanico       INT          NOT NULL CONSTRAINT FK_Asignacion_Mecanico REFERENCES dbo.Mecanicos(IdMecanico),
    EsResponsable    BIT          NOT NULL CONSTRAINT DF_Asignacion_Resp  DEFAULT 0,
    FechaAsignacion  DATETIME2(0) NOT NULL CONSTRAINT DF_Asignacion_Fecha DEFAULT SYSDATETIME(),
    CONSTRAINT PK_AsignacionMecanicos PRIMARY KEY (IdOrden, IdMecanico)
);
GO
CREATE UNIQUE INDEX UX_Asignacion_UnResponsable ON dbo.AsignacionMecanicos(IdOrden) WHERE EsResponsable = 1;
GO


/* =====================================================================
   7. DIAGNÓSTICO Y PRESUPUESTO (Módulo 6)
   ===================================================================== */
CREATE TABLE dbo.Diagnosticos (
    IdDiagnostico        INT IDENTITY(1,1) CONSTRAINT PK_Diagnosticos PRIMARY KEY,
    IdOrden              INT           NOT NULL CONSTRAINT FK_Diagnosticos_Orden    REFERENCES dbo.OrdenesTrabajo(IdOrden),
    IdMecanico           INT           NOT NULL CONSTRAINT FK_Diagnosticos_Mecanico REFERENCES dbo.Mecanicos(IdMecanico),
    Fecha                DATETIME2(0)  NOT NULL CONSTRAINT DF_Diagnosticos_Fecha DEFAULT SYSDATETIME(),
    Descripcion          VARCHAR(1000) NOT NULL,
    CausaRaiz            VARCHAR(500)  NULL,
    Recomendacion        VARCHAR(500)  NULL,
    RequiereCambioMotor  BIT           NOT NULL CONSTRAINT DF_Diagnosticos_Motor DEFAULT 0
);
GO

CREATE TABLE dbo.EvidenciasTecnicas (
    IdEvidencia    INT IDENTITY(1,1) CONSTRAINT PK_EvidenciasTecnicas PRIMARY KEY,
    IdDiagnostico  INT           NOT NULL CONSTRAINT FK_Evidencias_Diagnostico REFERENCES dbo.Diagnosticos(IdDiagnostico),
    Tipo           VARCHAR(40)   NOT NULL,   -- Compresión, Temperatura, Escáner, Fotografía...
    Descripcion    VARCHAR(500)  NOT NULL,
    ValorMedido    VARCHAR(100)  NULL,
    RutaArchivo    VARCHAR(260)  NULL
);
GO

CREATE TABLE dbo.Presupuestos (
    IdPresupuesto           INT IDENTITY(1,1) CONSTRAINT PK_Presupuestos PRIMARY KEY,
    IdOrden                 INT           NOT NULL CONSTRAINT FK_Presupuestos_Orden    REFERENCES dbo.OrdenesTrabajo(IdOrden),
    Fecha                   DATETIME2(0)  NOT NULL CONSTRAINT DF_Presupuestos_Fecha DEFAULT SYSDATETIME(),
    Subtotal                DECIMAL(12,2) NOT NULL CONSTRAINT DF_Presupuestos_Subtotal DEFAULT 0,
    Descuento               DECIMAL(12,2) NOT NULL CONSTRAINT DF_Presupuestos_Desc     DEFAULT 0,
    AutorizadoDescuentoPor  VARCHAR(80)   NULL,
    IdImpuesto              INT           NULL CONSTRAINT FK_Presupuestos_Impuesto REFERENCES dbo.ConfiguracionImpuestos(IdImpuesto),
    MontoImpuesto           DECIMAL(12,2) NOT NULL CONSTRAINT DF_Presupuestos_Imp      DEFAULT 0,
    Total                   DECIMAL(12,2) NOT NULL CONSTRAINT DF_Presupuestos_Total    DEFAULT 0,
    Estado                  VARCHAR(10)   NOT NULL CONSTRAINT DF_Presupuestos_Estado   DEFAULT 'PENDIENTE',
    FechaRespuesta          DATETIME2(0)  NULL,
    ObservacionCliente      VARCHAR(300)  NULL,
    CONSTRAINT CK_Presupuestos_Montos   CHECK (Subtotal >= 0 AND Descuento >= 0 AND MontoImpuesto >= 0 AND Total >= 0),
    CONSTRAINT CK_Presupuestos_Desc     CHECK (Descuento <= Subtotal),
    -- Un descuento solo es válido si alguien lo autorizó
    CONSTRAINT CK_Presupuestos_DescAut  CHECK (Descuento = 0 OR AutorizadoDescuentoPor IS NOT NULL),
    CONSTRAINT CK_Presupuestos_Estado   CHECK (Estado IN ('PENDIENTE','APROBADO','RECHAZADO')),
    CONSTRAINT CK_Presupuestos_Resp     CHECK (Estado = 'PENDIENTE' OR FechaRespuesta IS NOT NULL)
);
GO

-- Desglose del presupuesto: mano de obra (servicio + horas) o repuestos
CREATE TABLE dbo.DetallePresupuesto (
    IdDetallePresupuesto  INT IDENTITY(1,1) CONSTRAINT PK_DetallePresupuesto PRIMARY KEY,
    IdPresupuesto         INT           NOT NULL CONSTRAINT FK_DetPres_Presupuesto REFERENCES dbo.Presupuestos(IdPresupuesto),
    TipoLinea             VARCHAR(10)   NOT NULL,
    IdServicio            INT           NULL CONSTRAINT FK_DetPres_Servicio REFERENCES dbo.Servicios(IdServicio),
    IdRepuesto            INT           NULL CONSTRAINT FK_DetPres_Repuesto REFERENCES dbo.Repuestos(IdRepuesto),
    Descripcion           VARCHAR(200)  NOT NULL,
    Horas                 DECIMAL(6,2)  NOT NULL CONSTRAINT DF_DetPres_Horas    DEFAULT 0,
    Cantidad              INT           NOT NULL CONSTRAINT DF_DetPres_Cantidad DEFAULT 1,
    PrecioUnitario        DECIMAL(12,2) NOT NULL,
    CONSTRAINT CK_DetPres_Tipo     CHECK (TipoLinea IN ('MANO_OBRA','REPUESTO')),
    CONSTRAINT CK_DetPres_Ref      CHECK ((TipoLinea = 'MANO_OBRA' AND IdServicio IS NOT NULL AND IdRepuesto IS NULL)
                                       OR (TipoLinea = 'REPUESTO'  AND IdRepuesto IS NOT NULL AND IdServicio IS NULL)),
    CONSTRAINT CK_DetPres_Valores  CHECK (Horas >= 0 AND Cantidad > 0 AND PrecioUnitario >= 0)
);
GO


/* =====================================================================
   8. ACTIVIDADES, CONSUMOS, MOVIMIENTOS Y CONTROL DE CALIDAD
   ===================================================================== */
CREATE TABLE dbo.ActividadesTrabajo (
    IdActividad    INT IDENTITY(1,1) CONSTRAINT PK_ActividadesTrabajo PRIMARY KEY,
    IdOrden        INT           NOT NULL CONSTRAINT FK_Actividades_Orden    REFERENCES dbo.OrdenesTrabajo(IdOrden),
    IdMecanico     INT           NOT NULL CONSTRAINT FK_Actividades_Mecanico REFERENCES dbo.Mecanicos(IdMecanico),
    Descripcion    VARCHAR(300)  NOT NULL,
    Horas          DECIMAL(6,2)  NOT NULL CONSTRAINT DF_Actividades_Horas  DEFAULT 0,
    Estado         VARCHAR(12)   NOT NULL CONSTRAINT DF_Actividades_Estado DEFAULT 'PENDIENTE',
    Inicio         DATETIME2(0)  NULL,
    Fin            DATETIME2(0)  NULL,
    Observaciones  VARCHAR(300)  NULL,
    CONSTRAINT CK_Actividades_Horas  CHECK (Horas >= 0),
    CONSTRAINT CK_Actividades_Estado CHECK (Estado IN ('PENDIENTE','EN_PROCESO','COMPLETADA')),
    CONSTRAINT CK_Actividades_Fechas CHECK (Fin IS NULL OR (Inicio IS NOT NULL AND Fin >= Inicio))
);
GO

-- Tabla puente orden - repuesto (repuestos consumidos en la orden)
CREATE TABLE dbo.ConsumoRepuestos (
    IdConsumo               INT IDENTITY(1,1) CONSTRAINT PK_ConsumoRepuestos PRIMARY KEY,
    IdOrden                 INT           NOT NULL CONSTRAINT FK_Consumo_Orden    REFERENCES dbo.OrdenesTrabajo(IdOrden),
    IdRepuesto              INT           NOT NULL CONSTRAINT FK_Consumo_Repuesto REFERENCES dbo.Repuestos(IdRepuesto),
    Cantidad                INT           NOT NULL,
    PrecioUnitario          DECIMAL(12,2) NOT NULL,
    Fecha                   DATETIME2(0)  NOT NULL CONSTRAINT DF_Consumo_Fecha DEFAULT SYSDATETIME(),
    RegistradoEnInventario  BIT           NOT NULL CONSTRAINT DF_Consumo_Reg   DEFAULT 0,
    CONSTRAINT CK_Consumo_Cantidad CHECK (Cantidad > 0),
    CONSTRAINT CK_Consumo_Precio   CHECK (PrecioUnitario >= 0)
);
GO

CREATE TABLE dbo.MovimientosInventario (
    IdMovimiento    INT IDENTITY(1,1) CONSTRAINT PK_MovimientosInventario PRIMARY KEY,
    IdRepuesto      INT              NOT NULL CONSTRAINT FK_Movimientos_Repuesto REFERENCES dbo.Repuestos(IdRepuesto),
    Tipo            VARCHAR(10)      NOT NULL,
    Cantidad        INT              NOT NULL,
    CostoUnitario   DECIMAL(12,2)    NOT NULL CONSTRAINT DF_Movimientos_Costo DEFAULT 0,
    Fecha           DATETIME2(0)     NOT NULL CONSTRAINT DF_Movimientos_Fecha DEFAULT SYSDATETIME(),
    Referencia      VARCHAR(100)     NULL,
    IdOrden         INT              NULL CONSTRAINT FK_Movimientos_Orden REFERENCES dbo.OrdenesTrabajo(IdOrden),
    Usuario         VARCHAR(60)      NOT NULL CONSTRAINT DF_Movimientos_Usuario DEFAULT SUSER_SNAME(),
    -- Clave única que envía Java por cada operación: si llega dos veces, no se descuenta dos veces
    ClaveOperacion  UNIQUEIDENTIFIER NULL,
    CONSTRAINT CK_Movimientos_Tipo     CHECK (Tipo IN ('ENTRADA','SALIDA','AJUSTE')),
    CONSTRAINT CK_Movimientos_Cantidad CHECK ((Tipo IN ('ENTRADA','SALIDA') AND Cantidad > 0) OR (Tipo = 'AJUSTE' AND Cantidad <> 0)),
    CONSTRAINT CK_Movimientos_Costo    CHECK (CostoUnitario >= 0)
);
GO
CREATE UNIQUE INDEX UX_Movimientos_Clave ON dbo.MovimientosInventario(ClaveOperacion) WHERE ClaveOperacion IS NOT NULL;
CREATE INDEX IX_Movimientos_Repuesto ON dbo.MovimientosInventario(IdRepuesto, Fecha);
GO

CREATE TABLE dbo.ControlesCalidad (
    IdControl      INT IDENTITY(1,1) CONSTRAINT PK_ControlesCalidad PRIMARY KEY,
    IdOrden        INT           NOT NULL CONSTRAINT FK_Controles_Orden     REFERENCES dbo.OrdenesTrabajo(IdOrden),
    IdInspector    INT           NOT NULL CONSTRAINT FK_Controles_Inspector REFERENCES dbo.Mecanicos(IdMecanico),
    Fecha          DATETIME2(0)  NOT NULL CONSTRAINT DF_Controles_Fecha DEFAULT SYSDATETIME(),
    Aprobado       BIT           NOT NULL,
    Checklist      VARCHAR(1000) NULL,
    Observaciones  VARCHAR(500)  NULL,
    -- Si se rechaza, se debe explicar por qué
    CONSTRAINT CK_Controles_Obs CHECK (Aprobado = 1 OR Observaciones IS NOT NULL)
);
GO


/* =====================================================================
   9. MOTORES Y CAMBIO DE MOTOR (Módulo 7)
   ===================================================================== */
CREATE TABLE dbo.Motores (
    IdMotor        INT IDENTITY(1,1) CONSTRAINT PK_Motores PRIMARY KEY,
    NumeroSerie    VARCHAR(30)   NOT NULL CONSTRAINT UQ_Motores_Serie UNIQUE,
    Marca          VARCHAR(40)   NOT NULL,
    ModeloMotor    VARCHAR(40)   NOT NULL,
    Cilindrada     VARCHAR(10)   NOT NULL,
    Combustible    VARCHAR(15)   NOT NULL,
    Estado         VARCHAR(12)   NOT NULL CONSTRAINT DF_Motores_Estado DEFAULT 'DISPONIBLE',
    EsNuevo        BIT           NOT NULL CONSTRAINT DF_Motores_Nuevo  DEFAULT 1,
    Costo          DECIMAL(12,2) NOT NULL CONSTRAINT DF_Motores_Costo  DEFAULT 0,
    IdRepuesto     INT           NULL CONSTRAINT FK_Motores_Repuesto  REFERENCES dbo.Repuestos(IdRepuesto),
    IdProveedor    INT           NULL CONSTRAINT FK_Motores_Proveedor REFERENCES dbo.Proveedores(IdProveedor),
    Activo         BIT           NOT NULL CONSTRAINT DF_Motores_Activo DEFAULT 1,
    FechaCreacion  DATETIME2(0)  NOT NULL CONSTRAINT DF_Motores_Fecha  DEFAULT SYSDATETIME(),
    CONSTRAINT CK_Motores_Estado      CHECK (Estado IN ('DISPONIBLE','RESERVADO','INSTALADO','RETIRADO')),
    CONSTRAINT CK_Motores_Combustible CHECK (Combustible IN ('GASOLINA','DIESEL','HIBRIDO','ELECTRICO','GLP')),
    CONSTRAINT CK_Motores_Costo       CHECK (Costo >= 0)
);
GO

-- Reglas de compatibilidad: el motor sirve para marca + modelo + rango de años
CREATE TABLE dbo.CompatibilidadMotor (
    IdCompatibilidad INT IDENTITY(1,1) CONSTRAINT PK_CompatibilidadMotor PRIMARY KEY,
    IdMotor          INT          NOT NULL CONSTRAINT FK_Compat_Motor REFERENCES dbo.Motores(IdMotor),
    Marca            VARCHAR(40)  NOT NULL,
    Modelo           VARCHAR(40)  NOT NULL,
    AnioDesde        SMALLINT     NOT NULL,
    AnioHasta        SMALLINT     NOT NULL,
    CONSTRAINT UQ_Compat UNIQUE (IdMotor, Marca, Modelo, AnioDesde),
    CONSTRAINT CK_Compat_Anios CHECK (AnioDesde BETWEEN 1950 AND 2100 AND AnioHasta >= AnioDesde)
);
GO

CREATE TABLE dbo.CambiosMotor (
    IdCambioMotor         INT IDENTITY(1,1) CONSTRAINT PK_CambiosMotor PRIMARY KEY,
    IdOrden               INT           NOT NULL CONSTRAINT FK_CambiosMotor_Orden    REFERENCES dbo.OrdenesTrabajo(IdOrden),
    IdVehiculo            INT           NOT NULL CONSTRAINT FK_CambiosMotor_Vehiculo REFERENCES dbo.Vehiculos(IdVehiculo),
    SerieMotorAnterior    VARCHAR(30)   NOT NULL,
    IdMotorNuevo          INT           NOT NULL CONSTRAINT FK_CambiosMotor_Motor    REFERENCES dbo.Motores(IdMotor),
    DestinoMotorRetirado  VARCHAR(20)   NOT NULL,
    FechaDesmontaje       DATETIME2(0)  NOT NULL,
    FechaInstalacion      DATETIME2(0)  NOT NULL,
    ResultadoPruebas      VARCHAR(1000) NOT NULL,
    PruebasAprobadas      BIT           NOT NULL CONSTRAINT DF_CambiosMotor_Pruebas DEFAULT 0,
    AutorizadoCliente     BIT           NOT NULL,
    Observaciones         VARCHAR(500)  NULL,
    CONSTRAINT UQ_CambiosMotor_Orden UNIQUE (IdOrden),
    CONSTRAINT UQ_CambiosMotor_Motor UNIQUE (IdMotorNuevo),     -- un motor se instala una sola vez
    CONSTRAINT CK_CambiosMotor_Destino CHECK (DestinoMotorRetirado IN ('DEVUELTO_CLIENTE','RECONSTRUCCION','CHATARRA','ALMACEN')),
    CONSTRAINT CK_CambiosMotor_Fechas  CHECK (FechaInstalacion >= FechaDesmontaje),
    CONSTRAINT CK_CambiosMotor_Serie   CHECK (LEN(LTRIM(SerieMotorAnterior)) > 0),
    CONSTRAINT CK_CambiosMotor_Aut     CHECK (AutorizadoCliente = 1)  -- sin autorización no se registra
);
GO


/* =====================================================================
   10. GARANTÍAS, PAGOS Y FACTURAS (Módulo 8)
   ===================================================================== */
CREATE TABLE dbo.Garantias (
    IdGarantia         INT IDENTITY(1,1) CONSTRAINT PK_Garantias PRIMARY KEY,
    IdOrden            INT           NOT NULL CONSTRAINT FK_Garantias_Orden REFERENCES dbo.OrdenesTrabajo(IdOrden),
    Descripcion        VARCHAR(300)  NOT NULL,
    FechaInicio        DATE          NOT NULL,
    FechaFin           DATE          NOT NULL,
    KilometrajeLimite  INT           NULL,
    Condiciones        VARCHAR(1000) NULL,
    CONSTRAINT UQ_Garantias_Orden UNIQUE (IdOrden),
    CONSTRAINT CK_Garantias_Fechas CHECK (FechaFin > FechaInicio),
    CONSTRAINT CK_Garantias_Km     CHECK (KilometrajeLimite IS NULL OR KilometrajeLimite > 0)
);
GO

CREATE TABLE dbo.Pagos (
    IdPago      INT IDENTITY(1,1) CONSTRAINT PK_Pagos PRIMARY KEY,
    IdOrden     INT           NOT NULL CONSTRAINT FK_Pagos_Orden REFERENCES dbo.OrdenesTrabajo(IdOrden),
    Monto       DECIMAL(12,2) NOT NULL,
    Metodo      VARCHAR(15)   NOT NULL,
    Fecha       DATETIME2(0)  NOT NULL CONSTRAINT DF_Pagos_Fecha DEFAULT SYSDATETIME(),
    Referencia  VARCHAR(60)   NULL,
    CONSTRAINT CK_Pagos_Monto  CHECK (Monto > 0),
    CONSTRAINT CK_Pagos_Metodo CHECK (Metodo IN ('EFECTIVO','TARJETA','TRANSFERENCIA'))
);
GO
CREATE INDEX IX_Pagos_Orden ON dbo.Pagos(IdOrden);
GO

CREATE TABLE dbo.Facturas (
    IdFactura  INT IDENTITY(1,1) CONSTRAINT PK_Facturas PRIMARY KEY,
    Numero     AS ('FAC-' + RIGHT('000000' + CAST(IdFactura AS VARCHAR(10)), 6)) PERSISTED,
    IdOrden    INT           NOT NULL CONSTRAINT FK_Facturas_Orden REFERENCES dbo.OrdenesTrabajo(IdOrden),
    Fecha      DATETIME2(0)  NOT NULL CONSTRAINT DF_Facturas_Fecha DEFAULT SYSDATETIME(),
    Subtotal   DECIMAL(12,2) NOT NULL,
    Descuento  DECIMAL(12,2) NOT NULL,
    Impuesto   DECIMAL(12,2) NOT NULL,
    Total      DECIMAL(12,2) NOT NULL,
    CONSTRAINT UQ_Facturas_Numero UNIQUE (Numero),
    CONSTRAINT UQ_Facturas_Orden  UNIQUE (IdOrden),           -- una factura por orden
    CONSTRAINT CK_Facturas_Montos CHECK (Subtotal >= 0 AND Descuento >= 0 AND Impuesto >= 0 AND Total >= 0)
);
GO


/* =====================================================================
   11. AUDITORÍA
   ===================================================================== */
CREATE TABLE dbo.Auditoria (
    IdAuditoria    BIGINT IDENTITY(1,1) CONSTRAINT PK_Auditoria PRIMARY KEY,
    Tabla          VARCHAR(50)   NOT NULL,
    Operacion      VARCHAR(10)   NOT NULL,
    IdRegistro     INT           NOT NULL,
    Campo          VARCHAR(50)   NOT NULL,
    ValorAnterior  VARCHAR(200)  NULL,
    ValorNuevo     VARCHAR(200)  NULL,
    Usuario        VARCHAR(60)   NOT NULL CONSTRAINT DF_Auditoria_Usuario DEFAULT SUSER_SNAME(),
    Fecha          DATETIME2(0)  NOT NULL CONSTRAINT DF_Auditoria_Fecha   DEFAULT SYSDATETIME()
);
GO


/* =====================================================================
   12. FUNCIONES
   ===================================================================== */

-- Totales de una orden: servicios + repuestos - descuento + impuesto, pagado y saldo.
-- El total no se guarda en la tabla (es un dato derivado); se calcula aquí.
CREATE OR ALTER FUNCTION dbo.fn_TotalesOrden (@IdOrden INT)
RETURNS TABLE
AS
RETURN
WITH Base AS (
    SELECT
        ISNULL((SELECT SUM(d.Cantidad * d.PrecioUnitario) FROM dbo.DetalleOrden d WHERE d.IdOrden = @IdOrden), 0)      AS TotalServicios,
        ISNULL((SELECT SUM(c.Cantidad * c.PrecioUnitario) FROM dbo.ConsumoRepuestos c WHERE c.IdOrden = @IdOrden), 0)  AS TotalRepuestos,
        ISNULL((SELECT TOP 1 p.Descuento FROM dbo.Presupuestos p
                WHERE p.IdOrden = @IdOrden AND p.Estado = 'APROBADO' ORDER BY p.FechaRespuesta DESC), 0)               AS Descuento,
        COALESCE((SELECT TOP 1 i.Porcentaje FROM dbo.Presupuestos p
                  INNER JOIN dbo.ConfiguracionImpuestos i ON i.IdImpuesto = p.IdImpuesto
                  WHERE p.IdOrden = @IdOrden AND p.Estado = 'APROBADO' ORDER BY p.FechaRespuesta DESC),
                 (SELECT TOP 1 i.Porcentaje FROM dbo.ConfiguracionImpuestos i WHERE i.Activo = 1 AND i.EsPredeterminado = 1),
                 0)                                                                                                    AS PorcentajeImpuesto,
        ISNULL((SELECT SUM(pg.Monto) FROM dbo.Pagos pg WHERE pg.IdOrden = @IdOrden), 0)                                AS TotalPagado
),
Calculo AS (
    SELECT *,
           CAST(CASE WHEN TotalServicios + TotalRepuestos - Descuento < 0 THEN 0
                     ELSE TotalServicios + TotalRepuestos - Descuento END AS DECIMAL(12,2)) AS BaseImponible
    FROM Base
)
SELECT
    CAST(TotalServicios AS DECIMAL(12,2))                                               AS TotalServicios,
    CAST(TotalRepuestos AS DECIMAL(12,2))                                               AS TotalRepuestos,
    CAST(Descuento AS DECIMAL(12,2))                                                    AS Descuento,
    BaseImponible,
    CAST(PorcentajeImpuesto AS DECIMAL(5,2))                                            AS PorcentajeImpuesto,
    CAST(ROUND(BaseImponible * PorcentajeImpuesto / 100, 2) AS DECIMAL(12,2))           AS Impuesto,
    CAST(BaseImponible + ROUND(BaseImponible * PorcentajeImpuesto / 100, 2) AS DECIMAL(12,2))               AS Total,
    CAST(TotalPagado AS DECIMAL(12,2))                                                  AS TotalPagado,
    CAST(BaseImponible + ROUND(BaseImponible * PorcentajeImpuesto / 100, 2) - TotalPagado AS DECIMAL(12,2)) AS Saldo
FROM Calculo;
GO

-- Devuelve 1 si el motor es compatible con el vehículo (marca, modelo, año y combustible)
CREATE OR ALTER FUNCTION dbo.fn_MotorCompatible (@IdMotor INT, @IdVehiculo INT)
RETURNS BIT
AS
BEGIN
    DECLARE @Resultado BIT = 0;
    IF EXISTS (
        SELECT 1
        FROM dbo.Motores m
        INNER JOIN dbo.CompatibilidadMotor c ON c.IdMotor = m.IdMotor
        INNER JOIN dbo.Vehiculos v           ON v.IdVehiculo = @IdVehiculo
        WHERE m.IdMotor = @IdMotor
          AND c.Marca = v.Marca
          AND c.Modelo = v.Modelo
          AND v.Anio BETWEEN c.AnioDesde AND c.AnioHasta
          AND m.Combustible = v.Combustible
    )
        SET @Resultado = 1;
    RETURN @Resultado;
END;
GO


/* =====================================================================
   13. TRIGGERS DE AUDITORÍA
   ===================================================================== */

-- Registra cada cambio de stock, costo o precio de un repuesto
CREATE OR ALTER TRIGGER dbo.trg_Repuestos_Auditoria
ON dbo.Repuestos
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO dbo.Auditoria (Tabla, Operacion, IdRegistro, Campo, ValorAnterior, ValorNuevo)
    SELECT 'Repuestos', 'UPDATE', i.IdRepuesto, 'Stock', CAST(d.Stock AS VARCHAR(20)), CAST(i.Stock AS VARCHAR(20))
    FROM inserted i INNER JOIN deleted d ON d.IdRepuesto = i.IdRepuesto
    WHERE i.Stock <> d.Stock;

    INSERT INTO dbo.Auditoria (Tabla, Operacion, IdRegistro, Campo, ValorAnterior, ValorNuevo)
    SELECT 'Repuestos', 'UPDATE', i.IdRepuesto, 'PrecioVenta', CAST(d.PrecioVenta AS VARCHAR(20)), CAST(i.PrecioVenta AS VARCHAR(20))
    FROM inserted i INNER JOIN deleted d ON d.IdRepuesto = i.IdRepuesto
    WHERE i.PrecioVenta <> d.PrecioVenta;

    INSERT INTO dbo.Auditoria (Tabla, Operacion, IdRegistro, Campo, ValorAnterior, ValorNuevo)
    SELECT 'Repuestos', 'UPDATE', i.IdRepuesto, 'CostoUnitario', CAST(d.CostoUnitario AS VARCHAR(20)), CAST(i.CostoUnitario AS VARCHAR(20))
    FROM inserted i INNER JOIN deleted d ON d.IdRepuesto = i.IdRepuesto
    WHERE i.CostoUnitario <> d.CostoUnitario;
END;
GO

-- Registra cada cambio de estado de una orden de trabajo
CREATE OR ALTER TRIGGER dbo.trg_OrdenesTrabajo_AuditoriaEstado
ON dbo.OrdenesTrabajo
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT UPDATE(Estado) RETURN;

    INSERT INTO dbo.Auditoria (Tabla, Operacion, IdRegistro, Campo, ValorAnterior, ValorNuevo)
    SELECT 'OrdenesTrabajo', 'UPDATE', i.IdOrden, 'Estado', d.Estado, i.Estado
    FROM inserted i INNER JOIN deleted d ON d.IdOrden = i.IdOrden
    WHERE i.Estado <> d.Estado;
END;
GO

-- Registra los cambios de precio del catálogo (los importes de órdenes viejas no cambian)
CREATE OR ALTER TRIGGER dbo.trg_Servicios_AuditoriaPrecio
ON dbo.Servicios
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT UPDATE(CostoManoObra) RETURN;

    INSERT INTO dbo.Auditoria (Tabla, Operacion, IdRegistro, Campo, ValorAnterior, ValorNuevo)
    SELECT 'Servicios', 'UPDATE', i.IdServicio, 'CostoManoObra', CAST(d.CostoManoObra AS VARCHAR(20)), CAST(i.CostoManoObra AS VARCHAR(20))
    FROM inserted i INNER JOIN deleted d ON d.IdServicio = i.IdServicio
    WHERE i.CostoManoObra <> d.CostoManoObra;
END;
GO

-- Las órdenes finalizadas o entregadas no se pueden modificar en sus servicios (precios históricos)
CREATE OR ALTER TRIGGER dbo.trg_DetalleOrden_Bloqueo
ON dbo.DetalleOrden
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1 FROM dbo.OrdenesTrabajo o
        WHERE o.IdOrden IN (SELECT IdOrden FROM inserted UNION SELECT IdOrden FROM deleted)
          AND o.Estado IN ('FINALIZADA','ENTREGADA','RECHAZADA','CANCELADA')
    )
    BEGIN
        THROW 50020, 'No se pueden modificar los servicios de una orden cerrada.', 1;
    END
END;
GO


/* =====================================================================
   14. PROCEDIMIENTOS ALMACENADOS
   Todos usan transacciones: si algo falla, se revierte todo (ROLLBACK).
   Los errores de negocio se lanzan con THROW 5xxxx y Java los recibe como
   SQLException con el mensaje en español.
   ===================================================================== */

-- 14.1 Consumir un repuesto en una orden (descuento atómico de inventario)
--      Control de concurrencia:
--        * el UPDATE descuenta solo si hay stock suficiente (WHERE Stock >= @Cantidad),
--          así dos hilos nunca dejan el stock negativo;
--        * @ClaveOperacion evita descontar dos veces la misma operación
--          (doble clic o reintento): la segunda vez no hace nada.
CREATE OR ALTER PROCEDURE dbo.sp_ConsumirRepuesto
    @IdOrden         INT,
    @IdRepuesto      INT,
    @Cantidad        INT,
    @ClaveOperacion  UNIQUEIDENTIFIER = NULL,
    @IdConsumo       INT = NULL OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @Cantidad IS NULL OR @Cantidad <= 0
        THROW 50001, 'La cantidad a consumir debe ser mayor que cero.', 1;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Operación ya procesada: no se repite el descuento
        IF @ClaveOperacion IS NOT NULL AND EXISTS (
            SELECT 1 FROM dbo.MovimientosInventario WITH (UPDLOCK, HOLDLOCK) WHERE ClaveOperacion = @ClaveOperacion)
        BEGIN
            SELECT @IdConsumo = NULL;
            COMMIT TRANSACTION;
            RETURN 1;   -- 1 = ya existía
        END

        DECLARE @Estado VARCHAR(25);
        SELECT @Estado = Estado FROM dbo.OrdenesTrabajo WITH (UPDLOCK) WHERE IdOrden = @IdOrden AND Activo = 1;
        IF @Estado IS NULL
            THROW 50002, 'La orden no existe o está inactiva.', 1;
        IF @Estado NOT IN ('EN_DIAGNOSTICO','EN_REPARACION')
            THROW 50003, 'Solo se pueden consumir repuestos en órdenes en diagnóstico o en reparación.', 1;

        DECLARE @Precio DECIMAL(12,2), @Costo DECIMAL(12,2);

        UPDATE dbo.Repuestos
        SET Stock = Stock - @Cantidad,
            @Precio = PrecioVenta,
            @Costo  = CostoUnitario
        WHERE IdRepuesto = @IdRepuesto
          AND Activo = 1
          AND Stock >= @Cantidad;

        IF @@ROWCOUNT = 0
        BEGIN
            IF NOT EXISTS (SELECT 1 FROM dbo.Repuestos WHERE IdRepuesto = @IdRepuesto AND Activo = 1)
                THROW 50004, 'El repuesto no existe o está inactivo.', 1;
            ELSE
                THROW 50005, 'Stock insuficiente para el repuesto solicitado.', 1;
        END

        INSERT INTO dbo.MovimientosInventario (IdRepuesto, Tipo, Cantidad, CostoUnitario, Referencia, IdOrden, ClaveOperacion)
        VALUES (@IdRepuesto, 'SALIDA', @Cantidad, @Costo,
                CONCAT('Consumo orden OT-', RIGHT('000000' + CAST(@IdOrden AS VARCHAR(10)), 6)), @IdOrden, @ClaveOperacion);

        INSERT INTO dbo.ConsumoRepuestos (IdOrden, IdRepuesto, Cantidad, PrecioUnitario, RegistradoEnInventario)
        VALUES (@IdOrden, @IdRepuesto, @Cantidad, @Precio, 1);

        SET @IdConsumo = SCOPE_IDENTITY();

        COMMIT TRANSACTION;
        RETURN 0;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 14.2 Entrada de inventario (compra a proveedor)
CREATE OR ALTER PROCEDURE dbo.sp_RegistrarEntradaInventario
    @IdRepuesto     INT,
    @Cantidad       INT,
    @CostoUnitario  DECIMAL(12,2),
    @Referencia     VARCHAR(100) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @Cantidad IS NULL OR @Cantidad <= 0
        THROW 50010, 'La cantidad de la entrada debe ser mayor que cero.', 1;
    IF @CostoUnitario IS NULL OR @CostoUnitario < 0
        THROW 50011, 'El costo unitario no puede ser negativo.', 1;

    BEGIN TRY
        BEGIN TRANSACTION;

        UPDATE dbo.Repuestos
        SET Stock = Stock + @Cantidad,
            CostoUnitario = @CostoUnitario
        WHERE IdRepuesto = @IdRepuesto AND Activo = 1;

        IF @@ROWCOUNT = 0
            THROW 50004, 'El repuesto no existe o está inactivo.', 1;

        INSERT INTO dbo.MovimientosInventario (IdRepuesto, Tipo, Cantidad, CostoUnitario, Referencia)
        VALUES (@IdRepuesto, 'ENTRADA', @Cantidad, @CostoUnitario, @Referencia);

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 14.3 Asignar un mecánico a una orden (impide personal inactivo o no disponible)
CREATE OR ALTER PROCEDURE dbo.sp_AsignarMecanico
    @IdOrden        INT,
    @IdMecanico     INT,
    @EsResponsable  BIT = 0
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @Estado VARCHAR(25), @Activo BIT, @Disponible BIT;

        SELECT @Estado = Estado FROM dbo.OrdenesTrabajo WITH (UPDLOCK) WHERE IdOrden = @IdOrden AND Activo = 1;
        IF @Estado IS NULL
            THROW 50002, 'La orden no existe o está inactiva.', 1;
        IF @Estado IN ('FINALIZADA','ENTREGADA','RECHAZADA','CANCELADA')
            THROW 50030, 'No se pueden asignar mecánicos a una orden cerrada.', 1;

        SELECT @Activo = Activo, @Disponible = Disponible FROM dbo.Mecanicos WHERE IdMecanico = @IdMecanico;
        IF @Activo IS NULL
            THROW 50031, 'El mecánico no existe.', 1;
        IF @Activo = 0
            THROW 50032, 'No se puede asignar un mecánico inactivo.', 1;
        IF @Disponible = 0
            THROW 50033, 'El mecánico no está disponible.', 1;

        -- Si es el nuevo responsable, los demás dejan de serlo
        IF @EsResponsable = 1
            UPDATE dbo.AsignacionMecanicos SET EsResponsable = 0 WHERE IdOrden = @IdOrden AND EsResponsable = 1;

        IF EXISTS (SELECT 1 FROM dbo.AsignacionMecanicos WHERE IdOrden = @IdOrden AND IdMecanico = @IdMecanico)
            UPDATE dbo.AsignacionMecanicos SET EsResponsable = @EsResponsable
            WHERE IdOrden = @IdOrden AND IdMecanico = @IdMecanico;
        ELSE
            INSERT INTO dbo.AsignacionMecanicos (IdOrden, IdMecanico, EsResponsable)
            VALUES (@IdOrden, @IdMecanico, @EsResponsable);

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 14.4 Cambiar el estado de una orden validando el flujo y las reglas de negocio
CREATE OR ALTER PROCEDURE dbo.sp_CambiarEstadoOrden
    @IdOrden      INT,
    @NuevoEstado  VARCHAR(25)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @EstadoActual VARCHAR(25), @NivelMaximo TINYINT;

        SELECT @EstadoActual = Estado
        FROM dbo.OrdenesTrabajo WITH (UPDLOCK, HOLDLOCK)
        WHERE IdOrden = @IdOrden AND Activo = 1;

        IF @EstadoActual IS NULL
            THROW 50002, 'La orden no existe o está inactiva.', 1;

        IF NOT EXISTS (SELECT 1 FROM dbo.TransicionesEstadoOrden
                       WHERE EstadoActual = @EstadoActual AND EstadoSiguiente = @NuevoEstado)
        BEGIN
            DECLARE @Msg NVARCHAR(200) = CONCAT('No se permite pasar de ', @EstadoActual, ' a ', @NuevoEstado, '.');
            THROW 50040, @Msg, 1;
        END

        SELECT @NivelMaximo = ISNULL(MAX(s.Nivel), 0)
        FROM dbo.DetalleOrden d INNER JOIN dbo.Servicios s ON s.IdServicio = d.IdServicio
        WHERE d.IdOrden = @IdOrden;

        -- Para presupuestar: debe haber servicios; niveles 3 a 5 exigen diagnóstico registrado
        IF @NuevoEstado = 'PENDIENTE_APROBACION'
        BEGIN
            IF @NivelMaximo = 0
                THROW 50041, 'La orden no tiene servicios registrados.', 1;
            IF @NivelMaximo >= 3 AND NOT EXISTS (SELECT 1 FROM dbo.Diagnosticos WHERE IdOrden = @IdOrden)
                THROW 50042, 'Los servicios de nivel 3 a 5 requieren un diagnóstico registrado.', 1;
        END

        -- Para reparar: presupuesto aprobado por el cliente y mecánico responsable asignado
        IF @NuevoEstado = 'EN_REPARACION' AND @EstadoActual = 'PENDIENTE_APROBACION'
        BEGIN
            IF NOT EXISTS (SELECT 1 FROM dbo.Presupuestos WHERE IdOrden = @IdOrden AND Estado = 'APROBADO')
                THROW 50043, 'El cliente no ha aprobado el presupuesto.', 1;
            IF NOT EXISTS (SELECT 1 FROM dbo.AsignacionMecanicos WHERE IdOrden = @IdOrden AND EsResponsable = 1)
                THROW 50044, 'La orden no tiene un mecánico responsable asignado.', 1;
        END

        IF @NuevoEstado = 'RECHAZADA'
           AND NOT EXISTS (SELECT 1 FROM dbo.Presupuestos WHERE IdOrden = @IdOrden AND Estado = 'RECHAZADO')
            THROW 50045, 'Para rechazar la orden, el presupuesto debe estar rechazado por el cliente.', 1;

        -- Para finalizar: todo debe estar completo
        IF @NuevoEstado = 'FINALIZADA'
        BEGIN
            IF EXISTS (SELECT 1 FROM dbo.ActividadesTrabajo WHERE IdOrden = @IdOrden AND Estado <> 'COMPLETADA')
                THROW 50046, 'No se puede finalizar: hay actividades pendientes.', 1;

            IF EXISTS (SELECT 1 FROM dbo.ConsumoRepuestos WHERE IdOrden = @IdOrden AND RegistradoEnInventario = 0)
                THROW 50047, 'No se puede finalizar: hay consumos de repuestos sin registrar en inventario.', 1;

            -- Control de calidad obligatorio para niveles 3 a 5: el último control debe estar aprobado
            IF @NivelMaximo >= 3 AND ISNULL((SELECT TOP 1 Aprobado FROM dbo.ControlesCalidad
                                             WHERE IdOrden = @IdOrden ORDER BY Fecha DESC, IdControl DESC), 0) = 0
                THROW 50048, 'No se puede finalizar: el control de calidad obligatorio no está aprobado.', 1;

            -- Cambio de motor: pruebas aprobadas
            IF EXISTS (SELECT 1 FROM dbo.CambiosMotor WHERE IdOrden = @IdOrden AND PruebasAprobadas = 0)
                THROW 50049, 'No se puede finalizar: las pruebas del motor nuevo no están aprobadas.', 1;

            -- Reparación mayor (nivel 5): debe existir la garantía
            IF @NivelMaximo = 5 AND NOT EXISTS (SELECT 1 FROM dbo.Garantias WHERE IdOrden = @IdOrden)
                THROW 50050, 'No se puede finalizar: la reparación mayor requiere generar la garantía.', 1;
        END

        UPDATE dbo.OrdenesTrabajo
        SET Estado = @NuevoEstado,
            FechaEntrega = CASE WHEN @NuevoEstado = 'ENTREGADA' THEN SYSDATETIME() ELSE FechaEntrega END
        WHERE IdOrden = @IdOrden;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 14.5 Registrar la sustitución de motor (pasos 2, 3, 4, 9 y 10 del caso Hilux)
--      Bloquea motores incompatibles, no disponibles o sin datos obligatorios, informando la causa.
--      En una sola transacción: registra el cambio, marca el motor como INSTALADO
--      y descuenta del inventario el repuesto que representa al motor.
CREATE OR ALTER PROCEDURE dbo.sp_RegistrarCambioMotor
    @IdOrden               INT,
    @SerieMotorAnterior    VARCHAR(30),
    @IdMotorNuevo          INT,
    @DestinoMotorRetirado  VARCHAR(20),
    @FechaDesmontaje       DATETIME2(0),
    @FechaInstalacion      DATETIME2(0),
    @ResultadoPruebas      VARCHAR(1000),
    @PruebasAprobadas      BIT,
    @Observaciones         VARCHAR(500) = NULL,
    @IdCambioMotor         INT = NULL OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    -- Datos obligatorios
    IF NULLIF(LTRIM(@SerieMotorAnterior), '') IS NULL
        THROW 50060, 'Falta el número de serie del motor anterior.', 1;
    IF @IdMotorNuevo IS NULL
        THROW 50061, 'Debe seleccionar el motor nuevo.', 1;
    IF @DestinoMotorRetirado IS NULL
        THROW 50062, 'Debe indicar el destino del motor retirado.', 1;
    IF @FechaDesmontaje IS NULL OR @FechaInstalacion IS NULL
        THROW 50063, 'Debe registrar las fechas de desmontaje e instalación.', 1;
    IF NULLIF(LTRIM(@ResultadoPruebas), '') IS NULL
        THROW 50064, 'Debe registrar el resultado de las pruebas.', 1;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @IdVehiculo INT, @Estado VARCHAR(25), @EstadoMotor VARCHAR(12), @IdRepuestoMotor INT;

        SELECT @IdVehiculo = IdVehiculo, @Estado = Estado
        FROM dbo.OrdenesTrabajo WITH (UPDLOCK) WHERE IdOrden = @IdOrden AND Activo = 1;

        IF @IdVehiculo IS NULL
            THROW 50002, 'La orden no existe o está inactiva.', 1;
        IF @Estado <> 'EN_REPARACION'
            THROW 50065, 'El cambio de motor solo se registra con la orden en reparación.', 1;
        IF NOT EXISTS (SELECT 1 FROM dbo.Presupuestos WHERE IdOrden = @IdOrden AND Estado = 'APROBADO')
            THROW 50043, 'El cliente no ha autorizado el presupuesto.', 1;

        SELECT @EstadoMotor = Estado, @IdRepuestoMotor = IdRepuesto
        FROM dbo.Motores WITH (UPDLOCK) WHERE IdMotor = @IdMotorNuevo AND Activo = 1;

        IF @EstadoMotor IS NULL
            THROW 50066, 'El motor seleccionado no existe o está inactivo.', 1;
        IF @EstadoMotor <> 'DISPONIBLE'
        BEGIN
            DECLARE @MsgMotor NVARCHAR(200) = CONCAT('El motor seleccionado no está disponible (estado: ', @EstadoMotor, ').');
            THROW 50067, @MsgMotor, 1;
        END
        IF dbo.fn_MotorCompatible(@IdMotorNuevo, @IdVehiculo) = 0
            THROW 50068, 'El motor seleccionado no es compatible con la marca, modelo, año o combustible del vehículo.', 1;
        IF EXISTS (SELECT 1 FROM dbo.Motores WHERE NumeroSerie = @SerieMotorAnterior AND IdMotor = @IdMotorNuevo)
            THROW 50069, 'La serie del motor anterior no puede ser la misma del motor nuevo.', 1;

        INSERT INTO dbo.CambiosMotor (IdOrden, IdVehiculo, SerieMotorAnterior, IdMotorNuevo, DestinoMotorRetirado,
                                      FechaDesmontaje, FechaInstalacion, ResultadoPruebas, PruebasAprobadas,
                                      AutorizadoCliente, Observaciones)
        VALUES (@IdOrden, @IdVehiculo, @SerieMotorAnterior, @IdMotorNuevo, @DestinoMotorRetirado,
                @FechaDesmontaje, @FechaInstalacion, @ResultadoPruebas, @PruebasAprobadas, 1, @Observaciones);

        SET @IdCambioMotor = SCOPE_IDENTITY();

        UPDATE dbo.Motores SET Estado = 'INSTALADO' WHERE IdMotor = @IdMotorNuevo;

        -- El motor también es un artículo del inventario: se descuenta (paso 9)
        IF @IdRepuestoMotor IS NOT NULL
            EXEC dbo.sp_ConsumirRepuesto @IdOrden = @IdOrden, @IdRepuesto = @IdRepuestoMotor, @Cantidad = 1;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 14.6 Registrar un pago parcial o total (impide sobrepagos)
CREATE OR ALTER PROCEDURE dbo.sp_RegistrarPago
    @IdOrden     INT,
    @Monto       DECIMAL(12,2),
    @Metodo      VARCHAR(15),
    @Referencia  VARCHAR(60) = NULL,
    @SaldoNuevo  DECIMAL(12,2) = NULL OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @Monto IS NULL OR @Monto <= 0
        THROW 50070, 'El monto del pago debe ser mayor que cero.', 1;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @Estado VARCHAR(25), @Saldo DECIMAL(12,2);

        -- Bloquea la orden: dos pagos simultáneos no pueden superar el saldo
        SELECT @Estado = Estado FROM dbo.OrdenesTrabajo WITH (UPDLOCK, HOLDLOCK)
        WHERE IdOrden = @IdOrden AND Activo = 1;

        IF @Estado IS NULL
            THROW 50002, 'La orden no existe o está inactiva.', 1;
        IF @Estado IN ('RECHAZADA','CANCELADA')
            THROW 50071, 'No se pueden registrar pagos en una orden rechazada o cancelada.', 1;

        SELECT @Saldo = Saldo FROM dbo.fn_TotalesOrden(@IdOrden);

        IF @Saldo <= 0
            THROW 50072, 'La orden no tiene saldo pendiente.', 1;
        IF @Monto > @Saldo
        BEGIN
            DECLARE @Msg NVARCHAR(200) = CONCAT('Sobrepago: el monto (', @Monto, ') supera el saldo pendiente (', @Saldo, ').');
            THROW 50073, @Msg, 1;
        END

        INSERT INTO dbo.Pagos (IdOrden, Monto, Metodo, Referencia)
        VALUES (@IdOrden, @Monto, @Metodo, @Referencia);

        SET @SaldoNuevo = @Saldo - @Monto;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 14.7 Generar la factura de una orden finalizada
CREATE OR ALTER PROCEDURE dbo.sp_GenerarFactura
    @IdOrden    INT,
    @IdFactura  INT = NULL OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @Estado VARCHAR(25);
        SELECT @Estado = Estado FROM dbo.OrdenesTrabajo WITH (UPDLOCK, HOLDLOCK) WHERE IdOrden = @IdOrden AND Activo = 1;

        IF @Estado IS NULL
            THROW 50002, 'La orden no existe o está inactiva.', 1;
        IF @Estado NOT IN ('FINALIZADA','ENTREGADA')
            THROW 50080, 'Solo se facturan órdenes finalizadas.', 1;
        IF EXISTS (SELECT 1 FROM dbo.Facturas WHERE IdOrden = @IdOrden)
            THROW 50081, 'La orden ya tiene una factura.', 1;

        INSERT INTO dbo.Facturas (IdOrden, Subtotal, Descuento, Impuesto, Total)
        SELECT @IdOrden, t.TotalServicios + t.TotalRepuestos, t.Descuento, t.Impuesto, t.Total
        FROM dbo.fn_TotalesOrden(@IdOrden) t;

        SET @IdFactura = SCOPE_IDENTITY();

        COMMIT TRANSACTION;

        SELECT f.IdFactura, f.Numero, f.Fecha, f.Subtotal, f.Descuento, f.Impuesto, f.Total
        FROM dbo.Facturas f WHERE f.IdFactura = @IdFactura;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 14.8 Historial completo de un vehículo (por placa)
CREATE OR ALTER PROCEDURE dbo.sp_HistorialVehiculo
    @Placa VARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT * FROM dbo.vw_HistorialVehiculo
    WHERE Placa = @Placa
    ORDER BY FechaIngreso DESC;
END;
GO


/* =====================================================================
   15. VISTAS ADMINISTRATIVAS
   ===================================================================== */

-- Resumen de órdenes con cliente, vehículo, responsable y montos
CREATE OR ALTER VIEW dbo.vw_OrdenesResumen
AS
SELECT
    o.IdOrden,
    o.Numero,
    o.FechaIngreso,
    o.FechaEntrega,
    o.Estado,
    e.Descripcion                                   AS EstadoDescripcion,
    c.IdCliente,
    CONCAT(c.Nombre, ' ', c.Apellido)               AS Cliente,
    v.IdVehiculo,
    v.Placa,
    CONCAT(v.Marca, ' ', v.Modelo, ' ', v.Anio)     AS Vehiculo,
    NULLIF(LTRIM(RTRIM(CONCAT(m.Nombre, ' ', m.Apellido))), '') AS Responsable,
    t.TotalServicios,
    t.TotalRepuestos,
    t.Descuento,
    t.Impuesto,
    t.Total,
    t.TotalPagado,
    t.Saldo,
    f.Numero                                        AS Factura
FROM dbo.OrdenesTrabajo o
INNER JOIN dbo.Clientes c      ON c.IdCliente  = o.IdCliente
INNER JOIN dbo.Vehiculos v     ON v.IdVehiculo = o.IdVehiculo
INNER JOIN dbo.EstadosOrden e  ON e.Codigo     = o.Estado
LEFT  JOIN dbo.AsignacionMecanicos a ON a.IdOrden = o.IdOrden AND a.EsResponsable = 1
LEFT  JOIN dbo.Mecanicos m     ON m.IdMecanico = a.IdMecanico
LEFT  JOIN dbo.Facturas f      ON f.IdOrden    = o.IdOrden
CROSS APPLY dbo.fn_TotalesOrden(o.IdOrden) t
WHERE o.Activo = 1;
GO

-- Historial de cada vehículo: fechas, servicios, mecánicos, repuestos y costos
CREATE OR ALTER VIEW dbo.vw_HistorialVehiculo
AS
SELECT
    v.IdVehiculo,
    v.Placa,
    CONCAT(v.Marca, ' ', v.Modelo, ' ', v.Anio)  AS Vehiculo,
    CONCAT(c.Nombre, ' ', c.Apellido)            AS Propietario,
    o.IdOrden,
    o.Numero,
    o.FechaIngreso,
    o.FechaEntrega,
    o.KilometrajeIngreso,
    o.Estado,
    srv.Servicios,
    mec.Mecanicos,
    rep.Repuestos,
    t.TotalServicios,
    t.TotalRepuestos,
    t.Total
FROM dbo.Vehiculos v
INNER JOIN dbo.Clientes c       ON c.IdCliente  = v.IdCliente
INNER JOIN dbo.OrdenesTrabajo o ON o.IdVehiculo = v.IdVehiculo AND o.Activo = 1
OUTER APPLY (SELECT STRING_AGG(s.Nombre, ', ') AS Servicios
             FROM dbo.DetalleOrden d INNER JOIN dbo.Servicios s ON s.IdServicio = d.IdServicio
             WHERE d.IdOrden = o.IdOrden) srv
OUTER APPLY (SELECT STRING_AGG(CONCAT(m.Nombre, ' ', m.Apellido), ', ') AS Mecanicos
             FROM dbo.AsignacionMecanicos a INNER JOIN dbo.Mecanicos m ON m.IdMecanico = a.IdMecanico
             WHERE a.IdOrden = o.IdOrden) mec
OUTER APPLY (SELECT STRING_AGG(CONCAT(r.Nombre, ' x', cr.Cantidad), ', ') AS Repuestos
             FROM dbo.ConsumoRepuestos cr INNER JOIN dbo.Repuestos r ON r.IdRepuesto = cr.IdRepuesto
             WHERE cr.IdOrden = o.IdOrden) rep
CROSS APPLY dbo.fn_TotalesOrden(o.IdOrden) t;
GO

-- Repuestos en o por debajo del stock mínimo (para reabastecer)
CREATE OR ALTER VIEW dbo.vw_InventarioBajoMinimo
AS
SELECT
    r.IdRepuesto,
    r.Codigo,
    r.Nombre,
    r.Categoria,
    r.Stock,
    r.StockMinimo,
    r.StockMinimo - r.Stock + 1      AS CantidadSugerida,
    p.Nombre                         AS Proveedor,
    p.Telefono                       AS TelefonoProveedor
FROM dbo.Repuestos r
LEFT JOIN dbo.Proveedores p ON p.IdProveedor = r.IdProveedor
WHERE r.Activo = 1
  AND r.Stock <= r.StockMinimo;
GO

-- Productividad de mecánicos: órdenes, actividades y horas (GROUP BY)
CREATE OR ALTER VIEW dbo.vw_ProductividadMecanicos
AS
SELECT
    m.IdMecanico,
    m.CodigoEmpleado,
    CONCAT(m.Nombre, ' ', m.Apellido)                                         AS Mecanico,
    COUNT(DISTINCT a.IdOrden)                                                 AS OrdenesAsignadas,
    COUNT(DISTINCT CASE WHEN a.EsResponsable = 1 THEN a.IdOrden END)          AS OrdenesComoResponsable,
    (SELECT COUNT(*) FROM dbo.ActividadesTrabajo x
     WHERE x.IdMecanico = m.IdMecanico AND x.Estado = 'COMPLETADA')           AS ActividadesCompletadas,
    (SELECT ISNULL(SUM(x.Horas), 0) FROM dbo.ActividadesTrabajo x
     WHERE x.IdMecanico = m.IdMecanico)                                       AS HorasTrabajadas
FROM dbo.Mecanicos m
LEFT JOIN dbo.AsignacionMecanicos a ON a.IdMecanico = m.IdMecanico
WHERE m.Activo = 1
GROUP BY m.IdMecanico, m.CodigoEmpleado, m.Nombre, m.Apellido;
GO

-- Servicios más solicitados e ingresos generados (GROUP BY + agregaciones)
CREATE OR ALTER VIEW dbo.vw_ServiciosMasSolicitados
AS
SELECT
    s.IdServicio,
    s.Codigo,
    s.Nombre,
    s.Nivel,
    COUNT(d.IdDetalle)                                  AS VecesSolicitado,
    ISNULL(SUM(d.Cantidad * d.PrecioUnitario), 0)       AS Ingresos,
    ISNULL(AVG(d.PrecioUnitario), 0)                    AS PrecioPromedio
FROM dbo.Servicios s
LEFT JOIN dbo.DetalleOrden d ON d.IdServicio = s.IdServicio
GROUP BY s.IdServicio, s.Codigo, s.Nombre, s.Nivel;
GO


/* =====================================================================
   16. DATOS DE PRUEBA
   ===================================================================== */

INSERT INTO dbo.EstadosOrden (Codigo, Descripcion, Secuencia, EsFinal) VALUES
('RECIBIDA',             'Recibida',                1, 0),
('EN_DIAGNOSTICO',       'En diagnóstico',          2, 0),
('PENDIENTE_APROBACION', 'Pendiente de aprobación', 3, 0),
('EN_REPARACION',        'En reparación',           4, 0),
('EN_CONTROL_CALIDAD',   'En control de calidad',   5, 0),
('FINALIZADA',           'Finalizada',              6, 0),
('ENTREGADA',            'Entregada',               7, 1),
('RECHAZADA',            'Rechazada',               8, 1),
('CANCELADA',            'Cancelada',               9, 1);

INSERT INTO dbo.TransicionesEstadoOrden (EstadoActual, EstadoSiguiente) VALUES
('RECIBIDA',             'EN_DIAGNOSTICO'),
('RECIBIDA',             'CANCELADA'),
('EN_DIAGNOSTICO',       'PENDIENTE_APROBACION'),
('EN_DIAGNOSTICO',       'CANCELADA'),
('PENDIENTE_APROBACION', 'EN_REPARACION'),
('PENDIENTE_APROBACION', 'RECHAZADA'),
('PENDIENTE_APROBACION', 'CANCELADA'),
('EN_REPARACION',        'EN_CONTROL_CALIDAD'),
('EN_REPARACION',        'CANCELADA'),
('EN_CONTROL_CALIDAD',   'FINALIZADA'),
('EN_CONTROL_CALIDAD',   'EN_REPARACION'),   -- si el control falla, vuelve a reparación
('FINALIZADA',           'ENTREGADA');

INSERT INTO dbo.ConfiguracionImpuestos (Nombre, Porcentaje, EsPredeterminado) VALUES
('ISV 15%', 15.00, 1),
('ISV 18%', 18.00, 0),
('Exento',   0.00, 0);

INSERT INTO dbo.Especialidades (Nombre, Descripcion) VALUES
('Mantenimiento general', 'Aceite, filtros, bujías, luces'),
('Frenos y suspensión',   'Sistema de frenos, amortiguadores, dirección'),
('Sistema eléctrico',     'Alternador, batería, cableado, sensores'),
('Transmisiones',         'Caja de cambios, embrague, diferencial'),
('Motores',               'Culata, reconstrucción y sustitución de motor');

-- Clientes (Carlos tiene dos vehículos: historiales separados)
INSERT INTO dbo.Clientes (Identidad, Nombre, Apellido, Telefono, Correo, Direccion) VALUES
('0801-1985-12345', 'Carlos',  'Mejía',     '9988-1122', 'carlos.mejia@correo.hn',  'Col. Kennedy, Tegucigalpa'),
('0501-1990-54321', 'Ana',     'Rodríguez', '9876-5432', 'ana.rodriguez@correo.hn', 'Barrio Río de Piedras, San Pedro Sula'),
('0801-1978-67890', 'Luis',    'Fernández', '3344-5566', NULL,                      'Res. Las Uvas, Tegucigalpa');

INSERT INTO dbo.Vehiculos (IdCliente, Placa, VIN, Marca, Modelo, Anio, Color, Kilometraje, Combustible) VALUES
(1, 'HAB 1234', 'MR0FZ29G5F1234567', 'Toyota', 'Hilux',    2015, 'Blanco', 185000, 'DIESEL'),
(1, 'HCD 5678', '2HGFC2F59JH512345', 'Honda',  'Civic',    2018, 'Gris',    62000, 'GASOLINA'),
(2, 'PBE 4321', '3N6CD33B6KK123456', 'Nissan', 'Frontier', 2019, 'Rojo',    98000, 'DIESEL'),
(3, 'TGU 2468', 'KNADM4A39L6123456', 'Kia',    'Rio',      2020, 'Azul',    45000, 'GASOLINA');

INSERT INTO dbo.Mecanicos (CodigoEmpleado, Identidad, Nombre, Apellido, Telefono, TarifaHora, Disponible, Activo) VALUES
('MEC-001', '0801-1980-11111', 'José',    'Martínez', '9911-2233', 250.00, 1, 1),
('MEC-002', '0801-1988-22222', 'Pedro',   'Álvarez',  '9922-3344', 220.00, 1, 1),
('MEC-003', '0801-1992-33333', 'María',   'López',    '9933-4455', 230.00, 1, 1),
('MEC-004', '0801-1975-44444', 'Ricardo', 'Banegas',  '9944-5566', 200.00, 0, 1),   -- no disponible
('MEC-005', '0801-1970-55555', 'Jorge',   'Zelaya',   '9955-6677', 180.00, 1, 0);   -- inactivo

INSERT INTO dbo.MecanicoEspecialidades (IdMecanico, IdEspecialidad) VALUES
(1, 5), (1, 4),       -- José: motores y transmisiones
(2, 1), (2, 2),       -- Pedro: mantenimiento y frenos
(3, 3), (3, 1),       -- María: eléctrico y mantenimiento
(4, 2), (5, 1);

INSERT INTO dbo.Proveedores (RTN, Nombre, Contacto, Telefono, Correo, Direccion) VALUES
('08019001234567', 'Repuestos Toyota Honduras', 'Mario Cruz',   '2232-1000', 'ventas@reptoyota.hn', 'Blvd. Suyapa, Tegucigalpa'),
('05019007654321', 'Lubricantes del Norte',     'Sofía Pineda', '2550-2000', 'pedidos@lubrinorte.hn', 'Zona Industrial, San Pedro Sula'),
('08019005555555', 'Motores y Partes S.A.',     'Iván Reyes',   '2239-3000', 'info@motoresypartes.hn', 'Anillo Periférico, Tegucigalpa');

INSERT INTO dbo.Repuestos (Codigo, Nombre, Categoria, Marca, Stock, StockMinimo, CostoUnitario, PrecioVenta, IdProveedor) VALUES
('ACE-1540',  'Aceite 15W-40 diésel (galón)',      'ACEITE',      'Mobil',   30, 10,   520.00,   680.00, 2),
('ACE-5W30',  'Aceite sintético 5W-30 (cuarto)',   'ACEITE',      'Castrol', 48, 12,   210.00,   290.00, 2),
('FIL-ACE01', 'Filtro de aceite',                  'FILTRO',      'Toyota',  25,  8,   180.00,   260.00, 1),
('FIL-AIR01', 'Filtro de aire',                    'FILTRO',      'Toyota',  15,  5,   300.00,   420.00, 1),
('BUJ-IRI01', 'Bujía de iridio',                   'BUJIA',       'NGK',     40, 16,   190.00,   280.00, 1),
('BAT-12V70', 'Batería 12V 70Ah',                  'BATERIA',     'LTH',      6,  3,  2600.00,  3400.00, 1),
('FRE-PAS01', 'Juego de pastillas de freno',       'FRENOS',      'Brembo',  12,  4,   950.00,  1350.00, 1),
('RAD-HIL01', 'Radiador Hilux',                    'RADIADOR',    'Denso',    2,  1,  5200.00,  6900.00, 1),
('EMB-KIT01', 'Kit de embrague',                   'TRANSMISION', 'Exedy',    3,  2,  4800.00,  6300.00, 1),
('REF-GAL01', 'Refrigerante (galón)',              'OTRO',        'Prestone', 4, 10,   260.00,   350.00, 2),   -- bajo mínimo
('EMP-CUL01', 'Empaque de culata',                 'MOTOR',       'Toyota',   5,  2,  1100.00,  1500.00, 1),
('MOT-1KD01', 'Motor 1KD-FTV 3.0 diésel',          'MOTOR',       'Toyota',   1,  0, 95000.00,125000.00, 3),
('MOT-2KD01', 'Motor 2KD-FTV 2.5 diésel',          'MOTOR',       'Toyota',   1,  0, 82000.00,108000.00, 3),
('MOT-YD251', 'Motor YD25 2.5 diésel',             'MOTOR',       'Nissan',   1,  0, 78000.00,102000.00, 3);

INSERT INTO dbo.Servicios (Codigo, Nombre, Descripcion, Nivel, CostoManoObra, TiempoEstimadoHoras) VALUES
('SRV-101', 'Cambio de aceite y filtro',       'Drenado, cambio de aceite y filtro',             1,   350.00,  1.00),
('SRV-102', 'Cambio de bujías',                'Sustitución del juego de bujías',                1,   400.00,  1.00),
('SRV-103', 'Revisión de luces',               'Revisión y cambio de bombillos',                 1,   200.00,  0.50),
('SRV-201', 'Afinamiento mayor',               'Bujías, filtros, limpieza de cuerpo de aceleración', 2, 1200.00, 3.00),
('SRV-202', 'Limpieza de inyectores',          'Limpieza por ultrasonido',                       2,  1500.00,  2.50),
('SRV-203', 'Servicio de frenos',              'Cambio de pastillas y rectificado de discos',    2,   900.00,  2.00),
('SRV-301', 'Cambio de radiador',              'Sustitución de radiador y refrigerante',         3,  1800.00,  3.00),
('SRV-302', 'Cambio de embrague',              'Sustitución del kit de embrague',                3,  3500.00,  6.00),
('SRV-303', 'Reparación de alternador',        'Diagnóstico y reparación',                       3,  1600.00,  3.00),
('SRV-401', 'Reparación de transmisión',       'Desarme y reparación de caja de cambios',        4,  8500.00, 16.00),
('SRV-402', 'Reparación de culata',            'Rectificado y cambio de empaque',                4,  7000.00, 12.00),
('SRV-501', 'Sustitución de motor',            'Desmontaje, instalación y pruebas de motor',     5, 18000.00, 24.00),
('SRV-502', 'Reconstrucción de motor',         'Reconstrucción completa del motor',              5, 22000.00, 32.00);

INSERT INTO dbo.ServicioRepuestos (IdServicio, IdRepuesto, CantidadSugerida) VALUES
(1, 2, 5), (1, 3, 1),          -- cambio de aceite: aceite 5W-30 + filtro
(2, 5, 4),                     -- bujías
(4, 5, 4), (4, 4, 1), (4, 3, 1),
(6, 7, 1),                     -- frenos
(7, 8, 1), (7, 10, 2),         -- radiador + refrigerante
(8, 9, 1),                     -- embrague
(11, 11, 1),                   -- culata
(12, 12, 1), (12, 1, 2), (12, 10, 2);  -- sustitución de motor

-- Motores para el caso Hilux 2015
INSERT INTO dbo.Motores (NumeroSerie, Marca, ModeloMotor, Cilindrada, Combustible, Estado, EsNuevo, Costo, IdRepuesto, IdProveedor) VALUES
('1KD-7845123', 'Toyota', '1KD-FTV', '3.0L', 'DIESEL', 'DISPONIBLE', 1,  95000.00, 12, 3),   -- compatible con la Hilux 2015
('2KD-6632145', 'Toyota', '2KD-FTV', '2.5L', 'DIESEL', 'RESERVADO',  1,  82000.00, 13, 3),   -- compatible, pero no disponible
('YD25-998877', 'Nissan', 'YD25DDTi','2.5L', 'DIESEL', 'DISPONIBLE', 1,  78000.00, 14, 3);   -- incompatible con la Hilux

INSERT INTO dbo.CompatibilidadMotor (IdMotor, Marca, Modelo, AnioDesde, AnioHasta) VALUES
(1, 'Toyota', 'Hilux',    2005, 2015),
(1, 'Toyota', 'Fortuner', 2005, 2015),
(2, 'Toyota', 'Hilux',    2005, 2015),
(3, 'Nissan', 'Frontier', 2008, 2020);

-- Orden histórica 1: Honda Civic, cambio de aceite, entregada y pagada
-- (se crea abierta y al final se cierra, porque una orden cerrada no acepta servicios)
INSERT INTO dbo.OrdenesTrabajo (IdCliente, IdVehiculo, FechaIngreso, KilometrajeIngreso, Sintomas, DiagnosticoPreliminar, Estado)
VALUES (1, 2, DATEADD(DAY, -30, SYSDATETIME()), 60000, 'Mantenimiento programado de 60,000 km', 'Cambio de aceite y filtro', 'EN_CONTROL_CALIDAD');

INSERT INTO dbo.DetalleOrden (IdOrden, IdServicio, Cantidad, PrecioUnitario) VALUES (1, 1, 1, 350.00);
INSERT INTO dbo.AsignacionMecanicos (IdOrden, IdMecanico, EsResponsable) VALUES (1, 2, 1);
INSERT INTO dbo.ActividadesTrabajo (IdOrden, IdMecanico, Descripcion, Horas, Estado, Inicio, Fin)
VALUES (1, 2, 'Cambio de aceite y filtro', 1.00, 'COMPLETADA', DATEADD(DAY, -30, SYSDATETIME()), DATEADD(DAY, -30, DATEADD(HOUR, 1, SYSDATETIME())));
INSERT INTO dbo.ConsumoRepuestos (IdOrden, IdRepuesto, Cantidad, PrecioUnitario, Fecha, RegistradoEnInventario) VALUES
(1, 2, 5, 290.00, DATEADD(DAY, -30, SYSDATETIME()), 1),
(1, 3, 1, 260.00, DATEADD(DAY, -30, SYSDATETIME()), 1);
INSERT INTO dbo.MovimientosInventario (IdRepuesto, Tipo, Cantidad, CostoUnitario, Fecha, Referencia, IdOrden) VALUES
(2, 'SALIDA', 5, 210.00, DATEADD(DAY, -30, SYSDATETIME()), 'Consumo orden OT-000001', 1),
(3, 'SALIDA', 1, 180.00, DATEADD(DAY, -30, SYSDATETIME()), 'Consumo orden OT-000001', 1);
INSERT INTO dbo.ControlesCalidad (IdOrden, IdInspector, Fecha, Aprobado, Checklist)
VALUES (1, 3, DATEADD(DAY, -30, DATEADD(HOUR, 1, SYSDATETIME())), 1, 'Nivel de aceite OK; sin fugas; testigo apagado');
-- Total = (350 + 1450 + 260) * 1.15 = 2,369.00
INSERT INTO dbo.Pagos (IdOrden, Monto, Metodo, Fecha, Referencia) VALUES (1, 2369.00, 'TARJETA', DATEADD(DAY, -30, DATEADD(HOUR, 2, SYSDATETIME())), 'VISA-4521');
INSERT INTO dbo.Facturas (IdOrden, Fecha, Subtotal, Descuento, Impuesto, Total)
VALUES (1, DATEADD(DAY, -30, DATEADD(HOUR, 2, SYSDATETIME())), 2060.00, 0, 309.00, 2369.00);
UPDATE dbo.OrdenesTrabajo SET Estado = 'ENTREGADA', FechaEntrega = DATEADD(DAY, -30, DATEADD(HOUR, 2, SYSDATETIME()))
WHERE IdOrden = 1;

-- Orden 2: Nissan Frontier, frenos, en reparación (para la demostración)
INSERT INTO dbo.OrdenesTrabajo (IdCliente, IdVehiculo, KilometrajeIngreso, Sintomas, DiagnosticoPreliminar, Estado)
VALUES (2, 3, 98000, 'Ruido metálico al frenar', 'Pastillas gastadas', 'EN_REPARACION');
INSERT INTO dbo.DetalleOrden (IdOrden, IdServicio, Cantidad, PrecioUnitario) VALUES (2, 6, 1, 900.00);
INSERT INTO dbo.AsignacionMecanicos (IdOrden, IdMecanico, EsResponsable) VALUES (2, 2, 1), (2, 3, 0);
INSERT INTO dbo.Presupuestos (IdOrden, Subtotal, Descuento, IdImpuesto, MontoImpuesto, Total, Estado, FechaRespuesta)
VALUES (2, 2250.00, 0, 1, 337.50, 2587.50, 'APROBADO', SYSDATETIME());
INSERT INTO dbo.DetallePresupuesto (IdPresupuesto, TipoLinea, IdServicio, IdRepuesto, Descripcion, Horas, Cantidad, PrecioUnitario) VALUES
(1, 'MANO_OBRA', 6,    NULL, 'Servicio de frenos',          2.00, 1,  900.00),
(1, 'REPUESTO',  NULL, 7,    'Juego de pastillas de freno', 0.00, 1, 1350.00);
INSERT INTO dbo.ActividadesTrabajo (IdOrden, IdMecanico, Descripcion, Horas, Estado, Inicio)
VALUES (2, 2, 'Cambio de pastillas delanteras', 0, 'EN_PROCESO', SYSDATETIME());

-- Orden 3: Toyota Hilux 2015, caso obligatorio de sustitución de motor (recibida)
INSERT INTO dbo.OrdenesTrabajo (IdCliente, IdVehiculo, KilometrajeIngreso, Sintomas, DiagnosticoPreliminar, Estado)
VALUES (1, 1, 185000, 'Sobrecalentamiento, pérdida de potencia y humo blanco', 'Posible daño interno del motor', 'RECIBIDA');
GO


/* =====================================================================
   17. CONSULTAS DE EJEMPLO
   (sirven para la defensa: INNER JOIN, LEFT JOIN, GROUP BY,
    agregaciones y subconsultas)
   ===================================================================== */

-- INNER JOIN: vehículos con su propietario
-- SELECT v.Placa, v.Marca, v.Modelo, v.Anio, c.Nombre + ' ' + c.Apellido AS Propietario
-- FROM dbo.Vehiculos v INNER JOIN dbo.Clientes c ON c.IdCliente = v.IdCliente;

-- LEFT JOIN: todos los clientes y cuántos vehículos tienen (incluye los que no tienen)
-- SELECT c.Nombre, c.Apellido, COUNT(v.IdVehiculo) AS Vehiculos
-- FROM dbo.Clientes c LEFT JOIN dbo.Vehiculos v ON v.IdCliente = c.IdCliente
-- GROUP BY c.Nombre, c.Apellido;

-- GROUP BY + agregaciones: órdenes e ingresos por estado
-- SELECT Estado, COUNT(*) AS Ordenes, SUM(Total) AS Monto, SUM(Saldo) AS SaldoPendiente
-- FROM dbo.vw_OrdenesResumen GROUP BY Estado;

-- Subconsulta: repuestos más caros que el precio promedio
-- SELECT Codigo, Nombre, PrecioVenta FROM dbo.Repuestos
-- WHERE PrecioVenta > (SELECT AVG(PrecioVenta) FROM dbo.Repuestos);

-- Subconsulta: mecánicos que nunca han sido asignados
-- SELECT CodigoEmpleado, Nombre, Apellido FROM dbo.Mecanicos
-- WHERE IdMecanico NOT IN (SELECT IdMecanico FROM dbo.AsignacionMecanicos);

-- Motores compatibles y disponibles para la Hilux (IdVehiculo = 1)
-- SELECT m.IdMotor, m.NumeroSerie, m.ModeloMotor, m.Estado,
--        dbo.fn_MotorCompatible(m.IdMotor, 1) AS Compatible
-- FROM dbo.Motores m;

-- Vistas
-- SELECT * FROM dbo.vw_OrdenesResumen;
-- SELECT * FROM dbo.vw_InventarioBajoMinimo;
-- SELECT * FROM dbo.vw_ProductividadMecanicos;
-- SELECT * FROM dbo.vw_ServiciosMasSolicitados ORDER BY VecesSolicitado DESC;
-- EXEC dbo.sp_HistorialVehiculo @Placa = 'HCD 5678';

-- Procedimientos
-- DECLARE @Saldo DECIMAL(12,2);
-- EXEC dbo.sp_ConsumirRepuesto @IdOrden = 2, @IdRepuesto = 7, @Cantidad = 1, @ClaveOperacion = 'A1B2C3D4-0000-0000-0000-000000000001';
-- EXEC dbo.sp_RegistrarPago @IdOrden = 2, @Monto = 1000, @Metodo = 'EFECTIVO', @SaldoNuevo = @Saldo OUTPUT; SELECT @Saldo;
-- SELECT * FROM dbo.Auditoria ORDER BY Fecha DESC;
