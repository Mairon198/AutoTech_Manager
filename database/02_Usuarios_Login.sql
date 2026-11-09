/* =====================================================================
   AutoTech Manager - Usuarios para el inicio de sesión
   Ejecutar UNA VEZ sobre AutoTechDB (ya creada con AutoTechDB.sql).
   No borra ni modifica ninguna otra tabla. Se puede volver a ejecutar:
   recrea la tabla Usuarios con los tres usuarios del equipo.
   (AutoTechDB.sql ya incluye esta misma sección para instalaciones nuevas.)
   ===================================================================== */

DROP TABLE IF EXISTS dbo.Usuarios;
GO

CREATE TABLE dbo.Usuarios (
    usuario_id         INT IDENTITY(1,1) CONSTRAINT PK_Usuarios PRIMARY KEY,
    nombre_usuario     VARCHAR(30)   NOT NULL CONSTRAINT UQ_Usuarios_Nombre UNIQUE,
    nombre_completo    VARCHAR(100)  NOT NULL,
    rol                VARCHAR(20)   NOT NULL CONSTRAINT DF_Usuarios_Rol DEFAULT 'OPERADOR',
    sal                CHAR(32)      NOT NULL,
    hash_contrasena    CHAR(64)      NOT NULL,
    iteraciones        INT           NOT NULL,
    intentos_fallidos  INT           NOT NULL CONSTRAINT DF_Usuarios_Intentos DEFAULT 0,
    ultimo_acceso      DATETIME2(0)  NULL,
    activo             BIT           NOT NULL CONSTRAINT DF_Usuarios_Activo DEFAULT 1,
    fecha_registro     DATETIME2(0)  NOT NULL CONSTRAINT DF_Usuarios_Fecha DEFAULT SYSDATETIME(),
    CONSTRAINT CK_Usuarios_Rol      CHECK (rol IN ('ADMIN','OPERADOR','CAJA')),
    CONSTRAINT CK_Usuarios_Intentos CHECK (intentos_fallidos >= 0),
    CONSTRAINT CK_Usuarios_Iter     CHECK (iteraciones >= 10000)
);
GO

INSERT INTO dbo.Usuarios (nombre_usuario, nombre_completo, rol, sal, hash_contrasena, iteraciones) VALUES
    ('mairon', 'Mairon', 'ADMIN', 'CB7F56B26F204AB62CDD15513F6B63E5', 'F78513C84CCD9CCA5E135BF01A5FD3FD973DCB2C40C5A77CEB9B4C8BD3CF4C27', 120000),
    ('cristhian', 'Cristhian', 'ADMIN', '4F219CD00485915D8488EBC47E614C0C', '34E7B4E8532EA66A895B14185579AF63AA254DEB7539E00230364F5661581491', 120000),
    ('cristopher', 'Cristopher', 'ADMIN', 'FB5C65184048B9A7F2C2108845A51395', '1ABA41D9C55FCE2AAB59DA0E0F1E625BE9E5460B295BC4845D4C23D8EECBA83C', 120000);
GO

-- Consulta de ejemplo: usuarios y su último acceso
-- SELECT nombre_usuario, nombre_completo, rol, intentos_fallidos, ultimo_acceso, activo FROM dbo.Usuarios;
-- Desbloquear un usuario:
-- UPDATE dbo.Usuarios SET intentos_fallidos = 0 WHERE nombre_usuario = 'mairon';
