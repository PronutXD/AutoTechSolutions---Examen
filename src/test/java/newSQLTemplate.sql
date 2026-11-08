

SET NOCOUNT ON;
SET QUOTED_IDENTIFIER ON;
GO

/* ---------------------------- REINICIO (opcional) ---------------------
USE master;
ALTER DATABASE AutoTechManager SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
DROP DATABASE AutoTechManager;
------------------------------------------------------------------------ */

IF DB_ID(N'AutoTechManager') IS NULL
    CREATE DATABASE AutoTechManager COLLATE Modern_Spanish_CI_AI;
GO
USE AutoTechManager;
GO
SET QUOTED_IDENTIFIER ON;
GO

/* =====================================================================
   1. TABLAS (en orden de dependencia)
   ===================================================================== */

CREATE TABLE Clientes (
    idCliente     INT IDENTITY(1,1) PRIMARY KEY,
    codigo        VARCHAR(20)   NULL,
    documento     VARCHAR(20)   NOT NULL UNIQUE,
    nombres       NVARCHAR(80)  NOT NULL,
    apellidos     NVARCHAR(80)  NOT NULL,
    telefono      VARCHAR(20)   NULL,
    correo        NVARCHAR(120) NULL,
    direccion     NVARCHAR(200) NULL,
    activo        BIT           NOT NULL DEFAULT 1,
    fechaRegistro DATE          NOT NULL DEFAULT (CONVERT(DATE, SYSDATETIME())),
    version       ROWVERSION
);

CREATE TABLE Mecanicos (
    idMecanico    INT IDENTITY(1,1) PRIMARY KEY,
    codigo        VARCHAR(20)   NULL,
    documento     VARCHAR(20)   NOT NULL UNIQUE,
    nombres       NVARCHAR(80)  NOT NULL,
    apellidos     NVARCHAR(80)  NOT NULL,
    telefono      VARCHAR(20)   NULL,
    especialidad  NVARCHAR(80)  NULL,
    activo        BIT           NOT NULL DEFAULT 1,
    disponible    BIT           NOT NULL DEFAULT 1
);

CREATE TABLE Usuarios (
    idUsuario         INT IDENTITY(1,1) PRIMARY KEY,
    codigo            VARCHAR(20)      NULL,
    idMecanico        INT              NULL REFERENCES Mecanicos(idMecanico),
    usuario           NVARCHAR(50)     NOT NULL UNIQUE,
    hashClave         VARBINARY(64)    NOT NULL,
    salt              UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    rol               NVARCHAR(20)     NOT NULL
        CHECK (rol IN (N'Administrador', N'Mecánico', N'Recepción')),
    activo            BIT              NOT NULL DEFAULT 1,
    ultimoAcceso      DATETIME2(0)     NULL,
    debeCambiarClave  BIT              NOT NULL DEFAULT 1
);
-- Un mecanico solo puede tener una cuenta
CREATE UNIQUE INDEX UX_Usuarios_idMecanico ON Usuarios(idMecanico) WHERE idMecanico IS NOT NULL;

CREATE TABLE Servicios (
    idServicio     INT IDENTITY(1,1) PRIMARY KEY,
    codigo         VARCHAR(20)   NULL,
    nombre         NVARCHAR(100) NOT NULL,
    nivel          TINYINT       NOT NULL CHECK (nivel BETWEEN 1 AND 5),
    manoObra       DECIMAL(12,2) NOT NULL CHECK (manoObra >= 0),
    horasEstimadas DECIMAL(5,2)  NOT NULL DEFAULT 1 CHECK (horasEstimadas >= 0),
    garantiaDias   INT           NOT NULL DEFAULT 0 CHECK (garantiaDias >= 0),
    activo         BIT           NOT NULL DEFAULT 1
);

CREATE TABLE Proveedores (
    idProveedor INT IDENTITY(1,1) PRIMARY KEY,
    codigo      VARCHAR(20)   NULL,
    nombre      NVARCHAR(100) NOT NULL,
    contacto    NVARCHAR(100) NULL,
    telefono    VARCHAR(20)   NULL,
    correo      NVARCHAR(120) NULL,
    activo      BIT           NOT NULL DEFAULT 1
);

CREATE TABLE Repuestos (
    idRepuesto  INT IDENTITY(1,1) PRIMARY KEY,
    codigo      VARCHAR(20)   NULL,
    idProveedor INT           NULL REFERENCES Proveedores(idProveedor),
    nombre      NVARCHAR(100) NOT NULL,
    categoria   NVARCHAR(20)  NOT NULL
        CHECK (categoria IN (N'Aceite', N'Filtro', N'Bujía', N'Batería', N'Freno',
                             N'Radiador', N'Transmisión', N'Motor', N'Otro')),
    stock       INT           NOT NULL DEFAULT 0 CHECK (stock >= 0),
    stockMinimo INT           NOT NULL DEFAULT 0 CHECK (stockMinimo >= 0),
    costo       DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (costo >= 0),
    precioVenta DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (precioVenta >= 0),
    activo      BIT           NOT NULL DEFAULT 1,
    version     ROWVERSION
);

CREATE TABLE Motores (
    idMotor          INT IDENTITY(1,1) PRIMARY KEY,
    codigo           VARCHAR(20)   NULL,
    idRepuesto       INT           NOT NULL UNIQUE REFERENCES Repuestos(idRepuesto),
    numeroSerie      VARCHAR(40)   NOT NULL UNIQUE,
    marca            NVARCHAR(50)  NOT NULL,
    modeloCompatible NVARCHAR(100) NULL,
    anioDesde        SMALLINT      NOT NULL,
    anioHasta        SMALLINT      NOT NULL,
    cilindraje       DECIMAL(4,1)  NOT NULL,   -- en litros, ej. 1.8
    combustible      NVARCHAR(20)  NOT NULL
        CHECK (combustible IN (N'Gasolina', N'Diésel', N'Híbrido', N'Eléctrico', N'Gas')),
    estado           NVARCHAR(20)  NOT NULL DEFAULT N'Disponible'
        CHECK (estado IN (N'Disponible', N'Reservado', N'Instalado', N'Retirado', N'Descartado')),
    CHECK (anioHasta >= anioDesde)
);

CREATE TABLE ServicioRepuestos (
    idServicio INT NOT NULL REFERENCES Servicios(idServicio),
    idRepuesto INT NOT NULL REFERENCES Repuestos(idRepuesto),
    cantidad   INT NOT NULL DEFAULT 1 CHECK (cantidad > 0),
    PRIMARY KEY (idServicio, idRepuesto)
);

CREATE TABLE Vehiculos (
    idVehiculo  INT IDENTITY(1,1) PRIMARY KEY,
    codigo      VARCHAR(20)  NULL,
    idCliente   INT          NOT NULL REFERENCES Clientes(idCliente),
    placa       VARCHAR(15)  NOT NULL UNIQUE,
    vin         VARCHAR(25)  NULL,
    marca       NVARCHAR(50) NOT NULL,
    modelo      NVARCHAR(50) NOT NULL,
    anio        SMALLINT     NOT NULL CHECK (anio BETWEEN 1950 AND 2100),
    color       NVARCHAR(30) NULL,
    kilometraje INT          NOT NULL DEFAULT 0 CHECK (kilometraje >= 0),
    combustible NVARCHAR(20) NOT NULL DEFAULT N'Gasolina'
        CHECK (combustible IN (N'Gasolina', N'Diésel', N'Híbrido', N'Eléctrico', N'Gas')),
    activo      BIT          NOT NULL DEFAULT 1,
    version     ROWVERSION
);
CREATE UNIQUE INDEX UX_Vehiculos_vin ON Vehiculos(vin) WHERE vin IS NOT NULL;

CREATE TABLE OrdenesTrabajo (
    idOrden            INT IDENTITY(1,1) PRIMARY KEY,
    codigo             VARCHAR(20)   NULL,
    idVehiculo         INT           NOT NULL REFERENCES Vehiculos(idVehiculo),
    idUsuario          INT           NOT NULL REFERENCES Usuarios(idUsuario),
    fechaIngreso       DATETIME2(0)  NOT NULL DEFAULT SYSDATETIME(),
    kilometrajeIngreso INT           NOT NULL DEFAULT 0 CHECK (kilometrajeIngreso >= 0),
    motivo             NVARCHAR(300) NULL,
    estado             NVARCHAR(30)  NOT NULL DEFAULT N'Recibida'
        CHECK (estado IN (N'Recibida', N'En diagnóstico', N'Pendiente de aprobación',
                          N'En reparación', N'En control de calidad',
                          N'Finalizada y entregada', N'Rechazada', N'Cancelada')),
    descuento          DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (descuento >= 0),
    porcentajeImpuesto DECIMAL(5,2)  NOT NULL DEFAULT 15 CHECK (porcentajeImpuesto BETWEEN 0 AND 100),
    fechaEntrega       DATETIME2(0)  NULL,
    version            ROWVERSION
);
CREATE INDEX IX_Ordenes_Vehiculo ON OrdenesTrabajo(idVehiculo);
CREATE INDEX IX_Ordenes_Estado   ON OrdenesTrabajo(estado);

CREATE TABLE OrdenMecanicos (
    idOrden      INT NOT NULL REFERENCES OrdenesTrabajo(idOrden),
    idMecanico   INT NOT NULL REFERENCES Mecanicos(idMecanico),
    esResponsable BIT NOT NULL DEFAULT 0,
    PRIMARY KEY (idOrden, idMecanico)
);

CREATE TABLE DetalleOrden (
    idDetalle      INT IDENTITY(1,1) PRIMARY KEY,
    idOrden        INT           NOT NULL REFERENCES OrdenesTrabajo(idOrden),
    tipo           NVARCHAR(10)  NOT NULL CHECK (tipo IN (N'Servicio', N'Repuesto')),
    idServicio     INT           NULL REFERENCES Servicios(idServicio),
    idRepuesto     INT           NULL REFERENCES Repuestos(idRepuesto),
    cantidad       INT           NOT NULL CHECK (cantidad > 0),
    precioUnitario DECIMAL(12,2) NOT NULL CHECK (precioUnitario >= 0),   -- snapshot
    CHECK ( (tipo = N'Servicio' AND idServicio IS NOT NULL AND idRepuesto IS NULL)
         OR (tipo = N'Repuesto' AND idRepuesto IS NOT NULL AND idServicio IS NULL) )
);
CREATE INDEX IX_DetalleOrden_Orden ON DetalleOrden(idOrden);

CREATE TABLE Diagnosticos (
    idDiagnostico  INT IDENTITY(1,1) PRIMARY KEY,
    codigo         VARCHAR(20)   NULL,
    idOrden        INT           NOT NULL REFERENCES OrdenesTrabajo(idOrden),
    idMecanico     INT           NOT NULL REFERENCES Mecanicos(idMecanico),
    fecha          DATETIME2(0)  NOT NULL DEFAULT SYSDATETIME(),
    descripcion    NVARCHAR(500) NOT NULL,
    recomendacion  NVARCHAR(500) NULL
);

CREATE TABLE Presupuestos (
    idPresupuesto INT IDENTITY(1,1) PRIMARY KEY,
    codigo        VARCHAR(20)   NULL,
    idOrden       INT           NOT NULL REFERENCES OrdenesTrabajo(idOrden),
    idDiagnostico INT           NULL REFERENCES Diagnosticos(idDiagnostico),
    fecha         DATETIME2(0)  NOT NULL DEFAULT SYSDATETIME(),
    vigenciaDias  INT           NOT NULL DEFAULT 15 CHECK (vigenciaDias > 0),
    estado        NVARCHAR(15)  NOT NULL DEFAULT N'Pendiente'
        CHECK (estado IN (N'Pendiente', N'Aprobado', N'Rechazado')),
    subtotal      DECIMAL(12,2) NOT NULL DEFAULT 0,
    descuento     DECIMAL(12,2) NOT NULL DEFAULT 0,
    impuesto      DECIMAL(12,2) NOT NULL DEFAULT 0,
    total         DECIMAL(12,2) NOT NULL DEFAULT 0
);

CREATE TABLE DetallePresupuesto (
    idDetallePresupuesto INT IDENTITY(1,1) PRIMARY KEY,
    idPresupuesto        INT           NOT NULL REFERENCES Presupuestos(idPresupuesto),
    tipo                 NVARCHAR(10)  NOT NULL CHECK (tipo IN (N'Servicio', N'Repuesto')),
    idServicio           INT           NULL REFERENCES Servicios(idServicio),
    idRepuesto           INT           NULL REFERENCES Repuestos(idRepuesto),
    cantidad             INT           NOT NULL CHECK (cantidad > 0),
    precioUnitario       DECIMAL(12,2) NOT NULL CHECK (precioUnitario >= 0),   -- snapshot
    CHECK ( (tipo = N'Servicio' AND idServicio IS NOT NULL AND idRepuesto IS NULL)
         OR (tipo = N'Repuesto' AND idRepuesto IS NOT NULL AND idServicio IS NULL) )
);

CREATE TABLE Pagos (
    idPago     INT IDENTITY(1,1) PRIMARY KEY,
    codigo     VARCHAR(20)   NULL,
    idOrden    INT           NOT NULL REFERENCES OrdenesTrabajo(idOrden),
    idUsuario  INT           NOT NULL REFERENCES Usuarios(idUsuario),
    fecha      DATETIME2(0)  NOT NULL DEFAULT SYSDATETIME(),
    monto      DECIMAL(12,2) NOT NULL CHECK (monto > 0),
    metodo     NVARCHAR(15)  NOT NULL CHECK (metodo IN (N'Efectivo', N'Tarjeta', N'Transferencia')),
    referencia NVARCHAR(60)  NULL
);
CREATE INDEX IX_Pagos_Orden ON Pagos(idOrden);

CREATE TABLE CambiosMotor (
    idCambio         INT IDENTITY(1,1) PRIMARY KEY,
    codigo           VARCHAR(20)   NULL,
    idOrden          INT           NOT NULL REFERENCES OrdenesTrabajo(idOrden),
    idMotorRetirado  INT           NULL REFERENCES Motores(idMotor),
    idMotorInstalado INT           NOT NULL REFERENCES Motores(idMotor),
    fecha            DATETIME2(0)  NOT NULL DEFAULT SYSDATETIME(),
    motivo           NVARCHAR(200) NULL,
    kilometraje      INT           NULL CHECK (kilometraje >= 0),
    garantiaDias     INT           NOT NULL DEFAULT 0 CHECK (garantiaDias >= 0),
    observaciones    NVARCHAR(300) NULL
);

CREATE TABLE ActividadesTrabajo (
    idActividad INT IDENTITY(1,1) PRIMARY KEY,
    idOrden     INT           NOT NULL REFERENCES OrdenesTrabajo(idOrden),
    idMecanico  INT           NOT NULL REFERENCES Mecanicos(idMecanico),
    idServicio  INT           NULL REFERENCES Servicios(idServicio),
    descripcion NVARCHAR(300) NOT NULL,
    horaInicio  DATETIME2(0)  NOT NULL DEFAULT SYSDATETIME(),
    horaFin     DATETIME2(0)  NULL,
    estado      NVARCHAR(15)  NOT NULL DEFAULT N'Pendiente'
        CHECK (estado IN (N'Pendiente', N'En curso', N'Terminada')),
    CHECK (horaFin IS NULL OR horaFin >= horaInicio)
);

CREATE TABLE ControlesCalidad (
    idControl     INT IDENTITY(1,1) PRIMARY KEY,
    codigo        VARCHAR(20)   NULL,
    idOrden       INT           NOT NULL REFERENCES OrdenesTrabajo(idOrden),
    idMecanico    INT           NOT NULL REFERENCES Mecanicos(idMecanico),   -- inspector
    fecha         DATETIME2(0)  NOT NULL DEFAULT SYSDATETIME(),
    resultado     NVARCHAR(15)  NOT NULL CHECK (resultado IN (N'Aprobado', N'Rechazado')),
    pruebaRuta    BIT           NOT NULL DEFAULT 0,
    observaciones NVARCHAR(300) NULL
);

CREATE TABLE MovimientosInventario (
    idMovimiento  INT IDENTITY(1,1) PRIMARY KEY,
    codigo        VARCHAR(20)   NULL,
    idRepuesto    INT           NOT NULL REFERENCES Repuestos(idRepuesto),
    tipo          NVARCHAR(10)  NOT NULL CHECK (tipo IN (N'Entrada', N'Salida')),
    cantidad      INT           NOT NULL CHECK (cantidad > 0),
    costoUnitario DECIMAL(12,2) NOT NULL DEFAULT 0,
    fecha         DATETIME2(0)  NOT NULL DEFAULT SYSDATETIME(),
    idProveedor   INT           NULL REFERENCES Proveedores(idProveedor),   -- entradas
    idOrden       INT           NULL REFERENCES OrdenesTrabajo(idOrden),    -- salidas
    idUsuario     INT           NOT NULL REFERENCES Usuarios(idUsuario)
);
CREATE INDEX IX_Mov_Repuesto ON MovimientosInventario(idRepuesto);

CREATE TABLE Auditoria (
    idAuditoria   INT IDENTITY(1,1) PRIMARY KEY,
    fecha         DATETIME2(0)  NOT NULL DEFAULT SYSDATETIME(),
    idUsuario     INT           NULL REFERENCES Usuarios(idUsuario),
    tabla         NVARCHAR(60)  NOT NULL,
    idRegistro    INT           NOT NULL,
    accion        NVARCHAR(15)  NOT NULL CHECK (accion IN (N'Alta', N'Modificación', N'Eliminación')),
    valorAnterior NVARCHAR(MAX) NULL,   -- JSON
    valorNuevo    NVARCHAR(MAX) NULL    -- JSON
);
GO

/* =====================================================================
   2. VISTAS
   ===================================================================== */

-- Total de cada orden = (detalle - descuento) + impuesto; saldo = total - pagado
CREATE VIEW vw_SaldoOrdenes AS
SELECT o.idOrden, o.codigo,
       s.subtotal, o.descuento, im.impuesto, t.total,
       ISNULL(p.pagado, 0)           AS pagado,
       t.total - ISNULL(p.pagado, 0) AS saldo
FROM OrdenesTrabajo o
OUTER APPLY (SELECT SUM(d.cantidad * d.precioUnitario) AS suma
             FROM DetalleOrden d WHERE d.idOrden = o.idOrden) dt
OUTER APPLY (SELECT SUM(pg.monto) AS pagado
             FROM Pagos pg WHERE pg.idOrden = o.idOrden) p
CROSS APPLY (SELECT CAST(ISNULL(dt.suma, 0) AS DECIMAL(12,2)) AS subtotal) s
CROSS APPLY (SELECT CAST((s.subtotal - o.descuento) * o.porcentajeImpuesto / 100.0
                         AS DECIMAL(12,2)) AS impuesto) im
CROSS APPLY (SELECT s.subtotal - o.descuento + im.impuesto AS total) t;
GO

CREATE VIEW vw_RepuestosBajoMinimo AS
SELECT r.idRepuesto, r.codigo, r.nombre, r.categoria, r.stock, r.stockMinimo,
       r.stockMinimo - r.stock AS faltante,
       pv.nombre AS proveedor, pv.telefono AS telefonoProveedor
FROM Repuestos r
LEFT JOIN Proveedores pv ON pv.idProveedor = r.idProveedor
WHERE r.activo = 1 AND r.stock < r.stockMinimo;
GO

CREATE VIEW vw_HistorialVehiculo AS
SELECT v.idVehiculo, v.codigo AS codigoVehiculo, v.placa, v.marca, v.modelo,
       o.idOrden, o.codigo AS codigoOrden, o.fechaIngreso, o.estado,
       (SELECT STRING_AGG(sv.nombre, N', ')
          FROM DetalleOrden d JOIN Servicios sv ON sv.idServicio = d.idServicio
         WHERE d.idOrden = o.idOrden AND d.tipo = N'Servicio')                 AS servicios,
       (SELECT STRING_AGG(m.nombres + N' ' + m.apellidos, N', ')
          FROM OrdenMecanicos om JOIN Mecanicos m ON m.idMecanico = om.idMecanico
         WHERE om.idOrden = o.idOrden)                                          AS mecanicos,
       (SELECT STRING_AGG(CONCAT(r.nombre, N' x', d.cantidad), N', ')
          FROM DetalleOrden d JOIN Repuestos r ON r.idRepuesto = d.idRepuesto
         WHERE d.idOrden = o.idOrden AND d.tipo = N'Repuesto')                  AS repuestos,
       sa.total AS costo
FROM Vehiculos v
JOIN OrdenesTrabajo o ON o.idVehiculo = v.idVehiculo
JOIN vw_SaldoOrdenes sa ON sa.idOrden = o.idOrden;
GO

CREATE VIEW vw_ListadoOrdenes AS
SELECT o.idOrden, o.codigo, o.fechaIngreso,
       c.nombres + N' ' + c.apellidos          AS cliente,
       v.marca + N' ' + v.modelo + N' (' + v.placa + N')' AS vehiculo,
       resp.responsable,
       o.estado, sa.total
FROM OrdenesTrabajo o
JOIN Vehiculos v ON v.idVehiculo = o.idVehiculo
JOIN Clientes  c ON c.idCliente  = v.idCliente
JOIN vw_SaldoOrdenes sa ON sa.idOrden = o.idOrden
OUTER APPLY (SELECT TOP 1 m.nombres + N' ' + m.apellidos AS responsable
               FROM OrdenMecanicos om JOIN Mecanicos m ON m.idMecanico = om.idMecanico
              WHERE om.idOrden = o.idOrden AND om.esResponsable = 1) resp;
GO

/* =====================================================================
   3. TRIGGERS
   3.1 Codigo legible PREFIJO-NNNN-AAAA (uno por tabla con codigo)
   3.2 Auditoria en OrdenesTrabajo, Repuestos, Pagos y Usuarios
   Se generan con SQL dinamico para no repetir 19 veces el mismo bloque.
   ===================================================================== */

DECLARE @lista TABLE (n INT IDENTITY(1,1), tabla SYSNAME, llave SYSNAME, pref VARCHAR(3));
INSERT INTO @lista (tabla, llave, pref) VALUES
 ('Clientes','idCliente','CLI'),           ('Vehiculos','idVehiculo','VEH'),
 ('Mecanicos','idMecanico','MEC'),         ('Usuarios','idUsuario','USU'),
 ('Servicios','idServicio','SRV'),         ('Proveedores','idProveedor','PRV'),
 ('Repuestos','idRepuesto','REP'),         ('Motores','idMotor','MOT'),
 ('MovimientosInventario','idMovimiento','MOV'), ('OrdenesTrabajo','idOrden','ORD'),
 ('Diagnosticos','idDiagnostico','DIA'),   ('Presupuestos','idPresupuesto','PRE'),
 ('Pagos','idPago','PAG'),                 ('CambiosMotor','idCambio','CMB'),
 ('ControlesCalidad','idControl','CAL');

DECLARE @tplCodigo NVARCHAR(MAX) = N'
CREATE TRIGGER trg_Codigo_{T} ON {T}
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE x
       SET codigo = ''{P}-'' + FORMAT(x.{K}, ''0000'') + ''-'' + CAST(YEAR(SYSDATETIME()) AS VARCHAR(4))
      FROM {T} x
      JOIN inserted i ON i.{K} = x.{K};
END';

DECLARE @i INT = 1, @max INT = (SELECT COUNT(*) FROM @lista),
        @nom SYSNAME, @llave SYSNAME, @pref VARCHAR(3), @sql NVARCHAR(MAX);

WHILE @i <= @max
BEGIN
    SELECT @nom = tabla, @llave = llave, @pref = pref FROM @lista WHERE n = @i;

    SET @sql = REPLACE(REPLACE(REPLACE(@tplCodigo, '{T}', @nom), '{K}', @llave), '{P}', @pref);
    EXEC (@sql);

    SET @sql = N'CREATE UNIQUE INDEX UX_' + @nom + N'_codigo ON ' + @nom
             + N'(codigo) WHERE codigo IS NOT NULL';
    EXEC (@sql);

    SET @i += 1;
END

-- Auditoria: el usuario de la aplicacion llega por SESSION_CONTEXT('idUsuario')
DECLARE @aud TABLE (n INT IDENTITY(1,1), tabla SYSNAME, llave SYSNAME, columnas NVARCHAR(500));
INSERT INTO @aud (tabla, llave, columnas) VALUES
 ('OrdenesTrabajo','idOrden',  N'dd.*'),
 ('Repuestos',     'idRepuesto', N'dd.*'),
 ('Pagos',         'idPago',   N'dd.*'),
 -- En Usuarios no se guarda hashClave ni salt dentro de la auditoria
 ('Usuarios',      'idUsuario',
  N'dd.idUsuario, dd.codigo, dd.idMecanico, dd.usuario, dd.rol, dd.activo, dd.debeCambiarClave');

DECLARE @tplAud NVARCHAR(MAX) = N'
CREATE TRIGGER trg_Auditoria_{T} ON {T}
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @u INT = TRY_CAST(SESSION_CONTEXT(N''idUsuario'') AS INT);

    -- Modificacion (se ignora la asignacion inicial del codigo legible)
    INSERT INTO Auditoria (idUsuario, tabla, idRegistro, accion, valorAnterior, valorNuevo)
    SELECT @u, N''{T}'', i.{K}, N''Modificación'',
           (SELECT {D} FROM deleted dd  WHERE dd.{K} = d.{K} FOR JSON PATH, WITHOUT_ARRAY_WRAPPER),
           (SELECT {I} FROM inserted ii WHERE ii.{K} = i.{K} FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
      FROM inserted i
      JOIN deleted d ON d.{K} = i.{K}
     WHERE d.codigo IS NOT NULL;

    -- Alta
    INSERT INTO Auditoria (idUsuario, tabla, idRegistro, accion, valorAnterior, valorNuevo)
    SELECT @u, N''{T}'', i.{K}, N''Alta'', NULL,
           (SELECT {I} FROM inserted ii WHERE ii.{K} = i.{K} FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
      FROM inserted i
     WHERE NOT EXISTS (SELECT 1 FROM deleted);

    -- Eliminacion
    INSERT INTO Auditoria (idUsuario, tabla, idRegistro, accion, valorAnterior, valorNuevo)
    SELECT @u, N''{T}'', d.{K}, N''Eliminación'',
           (SELECT {D} FROM deleted dd WHERE dd.{K} = d.{K} FOR JSON PATH, WITHOUT_ARRAY_WRAPPER), NULL
      FROM deleted d
     WHERE NOT EXISTS (SELECT 1 FROM inserted);
END';

DECLARE @cols NVARCHAR(500);
SET @i = 1;
SET @max = (SELECT COUNT(*) FROM @aud);
WHILE @i <= @max
BEGIN
    SELECT @nom = tabla, @llave = llave, @cols = columnas FROM @aud WHERE n = @i;
    SET @sql = REPLACE(REPLACE(REPLACE(REPLACE(@tplAud, '{T}', @nom), '{K}', @llave),
                       '{D}', @cols), '{I}', REPLACE(@cols, N'dd.', N'ii.'));
    EXEC (@sql);
    SET @i += 1;
END
GO

/* =====================================================================
   4. PROCEDIMIENTOS ALMACENADOS
   Todos reciben @idUsuario (el usuario logueado en la aplicacion) y lo
   guardan en SESSION_CONTEXT para que la auditoria sepa quien hizo el cambio.
   Los errores de negocio se lanzan con THROW (numeros 50001 en adelante).
   ===================================================================== */

CREATE PROCEDURE sp_RegistrarEntrada
    @idRepuesto    INT,
    @cantidad      INT,
    @costoUnitario DECIMAL(12,2),
    @idProveedor   INT = NULL,
    @idUsuario     INT
AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    BEGIN TRY
        IF @cantidad <= 0
            THROW 50001, N'La cantidad debe ser mayor que cero.', 1;
        IF NOT EXISTS (SELECT 1 FROM Repuestos WHERE idRepuesto = @idRepuesto AND activo = 1)
            THROW 50002, N'El repuesto no existe o está inactivo.', 1;

        EXEC sys.sp_set_session_context @key = N'idUsuario', @value = @idUsuario;

        BEGIN TRAN;
            INSERT INTO MovimientosInventario (idRepuesto, tipo, cantidad, costoUnitario, idProveedor, idUsuario)
            VALUES (@idRepuesto, N'Entrada', @cantidad, @costoUnitario, @idProveedor, @idUsuario);
            DECLARE @idMov INT = SCOPE_IDENTITY();

            UPDATE Repuestos SET stock = stock + @cantidad WHERE idRepuesto = @idRepuesto;
        COMMIT;

        SELECT @idMov AS idMovimiento;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK;
        THROW;
    END CATCH
END
GO

CREATE PROCEDURE sp_RegistrarConsumo
    @idOrden    INT,
    @idRepuesto INT,
    @cantidad   INT,
    @idUsuario  INT
AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    BEGIN TRY
        IF @cantidad <= 0
            THROW 50001, N'La cantidad debe ser mayor que cero.', 1;

        EXEC sys.sp_set_session_context @key = N'idUsuario', @value = @idUsuario;

        DECLARE @estado NVARCHAR(30), @stock INT, @costo DECIMAL(12,2), @precio DECIMAL(12,2);

        BEGIN TRAN;
            SELECT @estado = estado FROM OrdenesTrabajo WITH (UPDLOCK) WHERE idOrden = @idOrden;
            IF @estado IS NULL
                THROW 50003, N'La orden no existe.', 1;
            IF @estado IN (N'Finalizada y entregada', N'Rechazada', N'Cancelada')
                THROW 50004, N'La orden está cerrada; no admite más repuestos.', 1;

            SELECT @stock = stock, @costo = costo, @precio = precioVenta
              FROM Repuestos WITH (UPDLOCK, ROWLOCK)
             WHERE idRepuesto = @idRepuesto AND activo = 1;
            IF @stock IS NULL
                THROW 50002, N'El repuesto no existe o está inactivo.', 1;
            IF @stock < @cantidad
                THROW 50005, N'Stock insuficiente para el repuesto solicitado.', 1;

            INSERT INTO MovimientosInventario (idRepuesto, tipo, cantidad, costoUnitario, idOrden, idUsuario)
            VALUES (@idRepuesto, N'Salida', @cantidad, @costo, @idOrden, @idUsuario);

            UPDATE Repuestos SET stock = stock - @cantidad WHERE idRepuesto = @idRepuesto;

            INSERT INTO DetalleOrden (idOrden, tipo, idRepuesto, cantidad, precioUnitario)
            VALUES (@idOrden, N'Repuesto', @idRepuesto, @cantidad, @precio);   -- precio snapshot
        COMMIT;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK;
        THROW;
    END CATCH
END
GO

CREATE PROCEDURE sp_RegistrarPago
    @idOrden    INT,
    @monto      DECIMAL(12,2),
    @metodo     NVARCHAR(15),
    @referencia NVARCHAR(60) = NULL,
    @idUsuario  INT
AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    BEGIN TRY
        IF @monto <= 0
            THROW 50006, N'El monto debe ser mayor que cero.', 1;

        EXEC sys.sp_set_session_context @key = N'idUsuario', @value = @idUsuario;

        DECLARE @estado NVARCHAR(30), @saldo DECIMAL(12,2);

        BEGIN TRAN;
            SELECT @estado = estado FROM OrdenesTrabajo WITH (UPDLOCK, HOLDLOCK) WHERE idOrden = @idOrden;
            IF @estado IS NULL
                THROW 50003, N'La orden no existe.', 1;
            IF @estado IN (N'Rechazada', N'Cancelada')
                THROW 50007, N'No se pueden registrar pagos en una orden rechazada o cancelada.', 1;

            SELECT @saldo = saldo FROM vw_SaldoOrdenes WHERE idOrden = @idOrden;
            IF @monto > @saldo
                THROW 50008, N'El pago excede el saldo pendiente de la orden.', 1;

            INSERT INTO Pagos (idOrden, idUsuario, monto, metodo, referencia)
            VALUES (@idOrden, @idUsuario, @monto, @metodo, @referencia);
            DECLARE @idPago INT = SCOPE_IDENTITY();
        COMMIT;

        SELECT @idPago AS idPago, @saldo - @monto AS saldoRestante;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK;
        THROW;
    END CATCH
END
GO

CREATE PROCEDURE sp_FinalizarOrden
    @idOrden   INT,
    @idUsuario INT
AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    BEGIN TRY
        EXEC sys.sp_set_session_context @key = N'idUsuario', @value = @idUsuario;

        DECLARE @estado NVARCHAR(30), @saldo DECIMAL(12,2), @resultado NVARCHAR(15);

        BEGIN TRAN;
            SELECT @estado = estado FROM OrdenesTrabajo WITH (UPDLOCK, HOLDLOCK) WHERE idOrden = @idOrden;
            IF @estado IS NULL
                THROW 50003, N'La orden no existe.', 1;
            IF @estado IN (N'Finalizada y entregada', N'Rechazada', N'Cancelada')
                THROW 50009, N'La orden ya está cerrada.', 1;

            SELECT TOP 1 @resultado = resultado
              FROM ControlesCalidad WHERE idOrden = @idOrden
             ORDER BY fecha DESC, idControl DESC;
            IF @resultado IS NULL OR @resultado <> N'Aprobado'
                THROW 50010, N'La orden necesita un control de calidad aprobado.', 1;

            SELECT @saldo = saldo FROM vw_SaldoOrdenes WHERE idOrden = @idOrden;
            IF @saldo > 0
                THROW 50011, N'La orden tiene saldo pendiente de pago.', 1;

            UPDATE OrdenesTrabajo
               SET estado = N'Finalizada y entregada', fechaEntrega = SYSDATETIME()
             WHERE idOrden = @idOrden;
        COMMIT;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK;
        THROW;
    END CATCH
END
GO

CREATE PROCEDURE sp_CambiarMotor
    @idOrden          INT,
    @idMotorInstalado INT,
    @idMotorRetirado  INT = NULL,
    @motivo           NVARCHAR(200) = NULL,
    @kilometraje      INT = NULL,
    @garantiaDias     INT = 0,
    @observaciones    NVARCHAR(300) = NULL,
    @idUsuario        INT
AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    BEGIN TRY
        EXEC sys.sp_set_session_context @key = N'idUsuario', @value = @idUsuario;

        DECLARE @estOrden NVARCHAR(30), @estIns NVARCHAR(20), @estRet NVARCHAR(20),
                @idRepIns INT, @stock INT, @costo DECIMAL(12,2), @precio DECIMAL(12,2);

        BEGIN TRAN;
            SELECT @estOrden = estado FROM OrdenesTrabajo WITH (UPDLOCK) WHERE idOrden = @idOrden;
            IF @estOrden IS NULL
                THROW 50003, N'La orden no existe.', 1;
            IF @estOrden IN (N'Finalizada y entregada', N'Rechazada', N'Cancelada')
                THROW 50004, N'La orden está cerrada.', 1;

            SELECT @estIns = estado, @idRepIns = idRepuesto
              FROM Motores WITH (UPDLOCK) WHERE idMotor = @idMotorInstalado;
            IF @estIns IS NULL
                THROW 50012, N'El motor a instalar no existe.', 1;
            IF @estIns NOT IN (N'Disponible', N'Reservado')
                THROW 50013, N'El motor a instalar no está disponible.', 1;

            IF @idMotorRetirado IS NOT NULL
            BEGIN
                SELECT @estRet = estado FROM Motores WITH (UPDLOCK) WHERE idMotor = @idMotorRetirado;
                IF @estRet IS NULL OR @estRet <> N'Instalado'
                    THROW 50014, N'El motor a retirar debe estar en estado Instalado.', 1;
                UPDATE Motores SET estado = N'Retirado' WHERE idMotor = @idMotorRetirado;
            END

            UPDATE Motores SET estado = N'Instalado' WHERE idMotor = @idMotorInstalado;

            SELECT @stock = stock, @costo = costo, @precio = precioVenta
              FROM Repuestos WITH (UPDLOCK) WHERE idRepuesto = @idRepIns;
            IF @stock < 1
                THROW 50005, N'El motor no tiene existencia en inventario.', 1;

            -- Movimiento de inventario (sale el motor) y cobro en la orden
            INSERT INTO MovimientosInventario (idRepuesto, tipo, cantidad, costoUnitario, idOrden, idUsuario)
            VALUES (@idRepIns, N'Salida', 1, @costo, @idOrden, @idUsuario);
            UPDATE Repuestos SET stock = stock - 1 WHERE idRepuesto = @idRepIns;
            INSERT INTO DetalleOrden (idOrden, tipo, idRepuesto, cantidad, precioUnitario)
            VALUES (@idOrden, N'Repuesto', @idRepIns, 1, @precio);

            INSERT INTO CambiosMotor (idOrden, idMotorRetirado, idMotorInstalado, motivo,
                                      kilometraje, garantiaDias, observaciones)
            VALUES (@idOrden, @idMotorRetirado, @idMotorInstalado, @motivo,
                    @kilometraje, @garantiaDias, @observaciones);
            DECLARE @idCambio INT = SCOPE_IDENTITY();
        COMMIT;

        SELECT @idCambio AS idCambio;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK;
        THROW;
    END CATCH
END
GO

-- Alimenta el boton "Buscar compatibles". Cualquier parametro en NULL se ignora.
CREATE PROCEDURE sp_BuscarMotoresCompatibles
    @marca       NVARCHAR(50)  = NULL,
    @anio        SMALLINT      = NULL,
    @cilindraje  DECIMAL(4,1)  = NULL,
    @combustible NVARCHAR(20)  = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT m.idMotor, m.codigo, m.numeroSerie, m.marca, m.modeloCompatible,
           m.anioDesde, m.anioHasta, m.cilindraje, m.combustible,
           r.nombre AS repuesto, r.precioVenta
      FROM Motores m
      JOIN Repuestos r ON r.idRepuesto = m.idRepuesto
     WHERE m.estado = N'Disponible'
       AND (@marca       IS NULL OR m.marca       = @marca)
       AND (@anio        IS NULL OR @anio BETWEEN m.anioDesde AND m.anioHasta)
       AND (@cilindraje  IS NULL OR m.cilindraje  = @cilindraje)
       AND (@combustible IS NULL OR m.combustible = @combustible)
     ORDER BY m.marca, m.cilindraje;
END
GO

/* ----- Procedimientos de apoyo para el login de la aplicacion ----- */

CREATE PROCEDURE sp_CrearUsuario
    @usuario    NVARCHAR(50),
    @clave      NVARCHAR(100),
    @rol        NVARCHAR(20),
    @idMecanico INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM Usuarios WHERE usuario = @usuario)
        THROW 50020, N'Ya existe un usuario con ese nombre.', 1;

    DECLARE @salt UNIQUEIDENTIFIER = NEWID();
    INSERT INTO Usuarios (idMecanico, usuario, hashClave, salt, rol)
    VALUES (@idMecanico, @usuario,
            HASHBYTES('SHA2_512', CONCAT(@clave, CAST(@salt AS NVARCHAR(36)))),
            @salt, @rol);
    SELECT SCOPE_IDENTITY() AS idUsuario;
END
GO

-- Devuelve 1 fila si usuario y clave son correctos, 0 filas si no.
CREATE PROCEDURE sp_ValidarLogin
    @usuario NVARCHAR(50),
    @clave   NVARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @id INT;

    SELECT @id = idUsuario
      FROM Usuarios
     WHERE usuario = @usuario AND activo = 1
       AND hashClave = HASHBYTES('SHA2_512', CONCAT(@clave, CAST(salt AS NVARCHAR(36))));

    IF @id IS NOT NULL
    BEGIN
        UPDATE Usuarios SET ultimoAcceso = SYSDATETIME() WHERE idUsuario = @id;
        SELECT idUsuario, usuario, rol, idMecanico, debeCambiarClave
          FROM Usuarios WHERE idUsuario = @id;
    END
END
GO

CREATE PROCEDURE sp_CambiarClave
    @idUsuario  INT,
    @claveNueva NVARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @salt UNIQUEIDENTIFIER = NEWID();
    UPDATE Usuarios
       SET salt = @salt,
           hashClave = HASHBYTES('SHA2_512', CONCAT(@claveNueva, CAST(@salt AS NVARCHAR(36)))),
           debeCambiarClave = 0
     WHERE idUsuario = @idUsuario;
END
GO

/* =====================================================================
   5. DATOS DE PRUEBA (se pueden borrar esta seccion si no los quiere)
   ===================================================================== */

-- Mecanicos y usuarios
INSERT INTO Mecanicos (documento, nombres, apellidos, telefono, especialidad) VALUES
 ('0000-0000-00001', N'Carlos', N'Mejía',  '9999-0001', N'Motor y transmisión'),
 ('0000-0000-00002', N'Luis',   N'Pineda', '9999-0002', N'Frenos y suspensión'),
 ('0000-0000-00003', N'Ana',    N'Reyes',  '9999-0003', N'Electricidad automotriz');

DECLARE @mec1 INT = (SELECT idMecanico FROM Mecanicos WHERE documento = '0000-0000-00001');
EXEC sp_CrearUsuario N'admin',     N'1234', N'Administrador';
EXEC sp_CrearUsuario N'recepcion', N'1234', N'Recepción';
EXEC sp_CrearUsuario N'cmejia',    N'1234', N'Mecánico', @mec1;

-- Clientes y vehiculos
INSERT INTO Clientes (documento, nombres, apellidos, telefono, correo, direccion) VALUES
 ('0000-0000-10001', N'María',  N'Fernández', '9888-1001', N'maria@correo.com', N'Col. Centro, Villanueva'),
 ('0000-0000-10002', N'José',   N'Martínez',  '9888-1002', N'jose@correo.com',  N'Col. Los Pinos, San Pedro Sula'),
 ('0000-0000-10003', N'Daniela',N'Flores',    '9888-1003', NULL,                N'Barrio Medina, Choloma');

INSERT INTO Vehiculos (idCliente, placa, vin, marca, modelo, anio, color, kilometraje, combustible)
SELECT idCliente, 'HAA1234', 'JTDBR32E720000001', N'Toyota', N'Corolla', 2012, N'Gris', 85000, N'Gasolina'
  FROM Clientes WHERE documento = '0000-0000-10001';
INSERT INTO Vehiculos (idCliente, placa, vin, marca, modelo, anio, color, kilometraje, combustible)
SELECT idCliente, 'HBB5678', NULL, N'Honda', N'Civic', 2016, N'Negro', 120000, N'Gasolina'
  FROM Clientes WHERE documento = '0000-0000-10002';
INSERT INTO Vehiculos (idCliente, placa, vin, marca, modelo, anio, color, kilometraje, combustible)
SELECT idCliente, 'HCC9012', NULL, N'Nissan', N'Frontier', 2018, N'Blanco', 60000, N'Diésel'
  FROM Clientes WHERE documento = '0000-0000-10003';

-- Catalogos
INSERT INTO Proveedores (nombre, contacto, telefono, correo) VALUES
 (N'Repuestos del Norte', N'Pedro Avilés',  '2550-0001', N'ventas@repnorte.com'),
 (N'Auto Partes Honduras', N'Sandra Lagos', '2550-0002', N'contacto@autopartes.hn');

INSERT INTO Servicios (nombre, nivel, manoObra, horasEstimadas, garantiaDias) VALUES
 (N'Cambio de aceite y filtro',      1,  350.00, 0.5,  0),
 (N'Cambio de pastillas de freno',   2,  600.00, 1.5, 30),
 (N'Alineación y balanceo',          2,  500.00, 1.0,  0),
 (N'Diagnóstico eléctrico',          3,  700.00, 2.0,  0),
 (N'Reparación de transmisión',      4, 4500.00, 10.0, 90),
 (N'Cambio de motor',                5, 8000.00, 24.0, 180);

DECLARE @p1 INT = (SELECT idProveedor FROM Proveedores WHERE nombre = N'Repuestos del Norte');
DECLARE @p2 INT = (SELECT idProveedor FROM Proveedores WHERE nombre = N'Auto Partes Honduras');
INSERT INTO Repuestos (idProveedor, nombre, categoria, stockMinimo, costo, precioVenta) VALUES
 (@p1, N'Aceite 5W-30 (litro)',          N'Aceite',   20,    70.00,    95.00),
 (@p1, N'Filtro de aceite',              N'Filtro',   10,    80.00,   120.00),
 (@p1, N'Bujía NGK',                     N'Bujía',    16,    45.00,    70.00),
 (@p2, N'Batería 12V 60Ah',              N'Batería',   3,  1800.00,  2400.00),
 (@p2, N'Pastillas de freno delanteras', N'Freno',     6,   650.00,   950.00),
 (@p2, N'Radiador universal',            N'Radiador',  2,  1500.00,  2100.00),
 (@p2, N'Motor Toyota 1.8L 2ZR',         N'Motor',     0, 38000.00, 52000.00),
 (@p2, N'Motor Honda 2.0L R20',          N'Motor',     0, 42000.00, 58000.00);

INSERT INTO Motores (idRepuesto, numeroSerie, marca, modeloCompatible, anioDesde, anioHasta, cilindraje, combustible)
SELECT idRepuesto, 'TOY2ZR000001', N'Toyota', N'Corolla / Matrix', 2009, 2014, 1.8, N'Gasolina'
  FROM Repuestos WHERE nombre = N'Motor Toyota 1.8L 2ZR';
INSERT INTO Motores (idRepuesto, numeroSerie, marca, modeloCompatible, anioDesde, anioHasta, cilindraje, combustible)
SELECT idRepuesto, 'HON-R20-000001', N'Honda', N'Civic / CR-V', 2012, 2017, 2.0, N'Gasolina'
  FROM Repuestos WHERE nombre = N'Motor Honda 2.0L R20';

INSERT INTO ServicioRepuestos (idServicio, idRepuesto, cantidad)
SELECT s.idServicio, r.idRepuesto, x.cantidad
  FROM (VALUES (N'Cambio de aceite y filtro', N'Aceite 5W-30 (litro)', 4),
               (N'Cambio de aceite y filtro', N'Filtro de aceite', 1),
               (N'Cambio de pastillas de freno', N'Pastillas de freno delanteras', 1))
       AS x(servicio, repuesto, cantidad)
  JOIN Servicios s ON s.nombre = x.servicio
  JOIN Repuestos r ON r.nombre = x.repuesto;
GO

-- Existencias iniciales (siempre por el procedimiento, para que quede el movimiento)
DECLARE @admin INT = (SELECT idUsuario FROM Usuarios WHERE usuario = N'admin');
DECLARE @ent TABLE (n INT IDENTITY(1,1), nombre NVARCHAR(100), cantidad INT);
INSERT INTO @ent (nombre, cantidad) VALUES
 (N'Aceite 5W-30 (litro)', 24),          (N'Filtro de aceite', 3),
 (N'Bujía NGK', 32),                     (N'Batería 12V 60Ah', 5),
 (N'Pastillas de freno delanteras', 8),  (N'Radiador universal', 2),
 (N'Motor Toyota 1.8L 2ZR', 1),          (N'Motor Honda 2.0L R20', 1);

DECLARE @i INT = 1, @max INT = (SELECT COUNT(*) FROM @ent),
        @idRep INT, @cant INT, @costo DECIMAL(12,2), @prov INT;
WHILE @i <= @max
BEGIN
    SELECT @idRep = r.idRepuesto, @cant = e.cantidad, @costo = r.costo, @prov = r.idProveedor
      FROM @ent e JOIN Repuestos r ON r.nombre = e.nombre
     WHERE e.n = @i;
    EXEC sp_RegistrarEntrada @idRep, @cant, @costo, @prov, @admin;
    SET @i += 1;
END
GO

-- Orden de ejemplo con servicio, repuestos y un pago parcial
DECLARE @admin INT = (SELECT idUsuario FROM Usuarios WHERE usuario = N'admin');
EXEC sys.sp_set_session_context @key = N'idUsuario', @value = @admin;

INSERT INTO OrdenesTrabajo (idVehiculo, idUsuario, kilometrajeIngreso, motivo)
SELECT idVehiculo, @admin, 85000, N'Cambio de aceite y revisión general'
  FROM Vehiculos WHERE placa = 'HAA1234';
DECLARE @ord INT = SCOPE_IDENTITY();

INSERT INTO OrdenMecanicos (idOrden, idMecanico, esResponsable)
SELECT @ord, idMecanico, 1 FROM Mecanicos WHERE documento = '0000-0000-00001';

INSERT INTO DetalleOrden (idOrden, tipo, idServicio, cantidad, precioUnitario)
SELECT @ord, N'Servicio', idServicio, 1, manoObra
  FROM Servicios WHERE nombre = N'Cambio de aceite y filtro';

DECLARE @rAceite INT = (SELECT idRepuesto FROM Repuestos WHERE nombre = N'Aceite 5W-30 (litro)');
DECLARE @rFiltro INT = (SELECT idRepuesto FROM Repuestos WHERE nombre = N'Filtro de aceite');
EXEC sp_RegistrarConsumo @ord, @rAceite, 4, @admin;
EXEC sp_RegistrarConsumo @ord, @rFiltro, 1, @admin;

UPDATE OrdenesTrabajo SET estado = N'En reparación' WHERE idOrden = @ord;
EXEC sp_RegistrarPago @ord, 500.00, N'Efectivo', NULL, @admin;
GO

/* =====================================================================
   6. USUARIO DE LA APLICACION (login SQL para Java / JDBC)
   Requiere que la instancia permita autenticacion mixta
   (Propiedades del servidor > Seguridad > "SQL Server y Windows").
   CAMBIE LA CLAVE antes de ejecutar.
   ===================================================================== */
USE master;
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = N'autotech_app')
    CREATE LOGIN autotech_app WITH PASSWORD = N'AutoTech#2026', CHECK_POLICY = OFF;
GO
USE AutoTechManager;
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = N'autotech_app')
    CREATE USER autotech_app FOR LOGIN autotech_app;
ALTER ROLE db_datareader ADD MEMBER autotech_app;
ALTER ROLE db_datawriter ADD MEMBER autotech_app;
GRANT EXECUTE TO autotech_app;
GO

/* =====================================================================
   7. VERIFICACION
   ===================================================================== */
SELECT COUNT(*) AS tablas FROM sys.tables;   -- debe dar 21
SELECT * FROM vw_ListadoOrdenes;
SELECT * FROM vw_SaldoOrdenes;
SELECT * FROM vw_RepuestosBajoMinimo;        -- Filtro de aceite queda bajo el minimo
SELECT * FROM vw_HistorialVehiculo;
EXEC sp_BuscarMotoresCompatibles @marca = N'Toyota', @anio = 2012;
SELECT TOP 10 idAuditoria, tabla, idRegistro, accion, idUsuario FROM Auditoria ORDER BY idAuditoria DESC;
SELECT idUsuario, codigo, usuario, rol FROM Usuarios;
GO