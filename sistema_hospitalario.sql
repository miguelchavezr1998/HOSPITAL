-- ============================================================================
-- SISTEMA HOSPITALARIO - ESQUEMA AJUSTADO A LA APP JAVA (hospital_zip)
-- ----------------------------------------------------------------------------
-- IMPORTANTE: esta versión reemplaza a sistema_hospitalario_integrado.sql.
-- Ese script usaba uuid nativo y columnas tipo_documento/numero_documento,
-- que NO coinciden con lo que tus DAO (PacienteDAO, DoctorDAO, CitaDAO,
-- HistorialClinicoDAO) esperan. Revisado contra tu código fuente:
--   * IDs: VARCHAR(36) generados como "pac-001", "doc-003", etc. (IdGenerator.java)
--   * pacientes/doctores: campo único "cedula" (no tipo_documento/numero_documento)
--   * citas/historial_clinico: columnas denormalizadas paciente_nombre/doctor_nombre
--   * historial_clinico: presion_arterial, temperatura, pulso, peso, estatura
--     se leen/escriben como String (ps.setString / rs.getString) -> deben ser
--     VARCHAR, no numeric/smallint.
-- Todo lo demás (checks, índices, roles, catálogo CIE-10) se conserva.
-- ============================================================================

BEGIN;

-- ============================================================================
-- SECCION 1 - TABLAS MAESTRAS
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.pacientes (
    id               VARCHAR(36) PRIMARY KEY,
    cedula           VARCHAR(20) NOT NULL UNIQUE,
    nombre           VARCHAR(100) NOT NULL,
    apellido         VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE,
    sexo             VARCHAR(20),
    telefono         VARCHAR(20),
    email            VARCHAR(100),
    direccion        VARCHAR(255),
    tipo_sangre      VARCHAR(10),
    alergias         TEXT,
    fecha_registro   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_pacientes_sexo  CHECK (sexo IS NULL OR sexo IN ('Masculino', 'Femenino', 'Otro')),
    CONSTRAINT chk_pacientes_email CHECK (email IS NULL OR email LIKE '%_@_%._%')
);

CREATE TABLE IF NOT EXISTS public.doctores (
    id           VARCHAR(36) PRIMARY KEY,
    cedula       VARCHAR(20) NOT NULL UNIQUE,
    nombre       VARCHAR(100) NOT NULL,
    apellido     VARCHAR(100) NOT NULL,
    especialidad VARCHAR(100),
    telefono     VARCHAR(20),
    email        VARCHAR(100),
    horario      VARCHAR(100),
    CONSTRAINT chk_doctores_email CHECK (email IS NULL OR email LIKE '%_@_%._%')
);

-- Login: reemplaza las credenciales fijas de LoginController.java
CREATE TABLE IF NOT EXISTS public.usuarios (
    id                VARCHAR(36) PRIMARY KEY,
    usuario           VARCHAR(50) NOT NULL UNIQUE,
    password_hash     VARCHAR(255) NOT NULL,           -- hash BCrypt, nunca texto plano
    rol               VARCHAR(20) NOT NULL DEFAULT 'recepcion',
    persona_id        VARCHAR(36),                      -- opcional: enlaza a doctores.id si rol='medico'
    activo            BOOLEAN NOT NULL DEFAULT TRUE,
    intentos_fallidos SMALLINT NOT NULL DEFAULT 0,
    ultimo_login      TIMESTAMP,
    creado_en         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_usuarios_rol CHECK (rol IN ('admin', 'medico', 'recepcion')),
    CONSTRAINT fk_usuarios_doctor FOREIGN KEY (persona_id) REFERENCES doctores (id) ON DELETE SET NULL
);

-- ============================================================================
-- SECCION 3 - TABLAS TRANSACCIONALES
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.citas (
    id              VARCHAR(36) PRIMARY KEY,
    paciente_id     VARCHAR(36) NOT NULL,
    doctor_id       VARCHAR(36) NOT NULL,
    paciente_nombre VARCHAR(100) NOT NULL,
    doctor_nombre   VARCHAR(100) NOT NULL,
    fecha           DATE NOT NULL,
    hora            TIME NOT NULL,
    motivo          TEXT,
    estado          VARCHAR(20) DEFAULT 'Programada',
    observaciones   TEXT,
    CONSTRAINT fk_citas_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes (id) ON DELETE CASCADE,
    CONSTRAINT fk_citas_doctor   FOREIGN KEY (doctor_id)   REFERENCES doctores (id)  ON DELETE RESTRICT,
    CONSTRAINT chk_citas_estado  CHECK (estado IS NULL OR estado IN ('Programada', 'Completada', 'Cancelada'))
);

CREATE TABLE IF NOT EXISTS public.historial_clinico (
    id                VARCHAR(36) PRIMARY KEY,
    paciente_id       VARCHAR(36) NOT NULL,
    doctor_id         VARCHAR(36) NOT NULL,
    cita_id           VARCHAR(36),
    paciente_nombre   VARCHAR(100) NOT NULL,
    doctor_nombre     VARCHAR(100) NOT NULL,
    fecha             DATE DEFAULT CURRENT_DATE,
    diagnostico       TEXT,
    diagnostico_cie10 VARCHAR(10),         -- codigo CIE-10 opcional (lista comun embebida en la app Java)
    tratamiento       TEXT,
    notas             TEXT,
    presion_arterial  VARCHAR(20),
    temperatura       VARCHAR(20),         -- se guarda como texto: asi lo lee/escribe HistorialClinicoDAO
    pulso             VARCHAR(20),
    peso              VARCHAR(20),
    estatura          VARCHAR(20),
    CONSTRAINT fk_historial_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes (id) ON DELETE CASCADE,
    CONSTRAINT fk_historial_doctor   FOREIGN KEY (doctor_id)   REFERENCES doctores (id)  ON DELETE RESTRICT,
    CONSTRAINT fk_historial_cita     FOREIGN KEY (cita_id)     REFERENCES citas (id)     ON DELETE SET NULL
);

-- ============================================================================
-- SECCION 4 - INDICES
-- ============================================================================

CREATE INDEX IF NOT EXISTS idx_citas_paciente        ON citas (paciente_id);
CREATE INDEX IF NOT EXISTS idx_citas_doctor          ON citas (doctor_id);
CREATE INDEX IF NOT EXISTS idx_citas_fecha           ON citas (fecha);
CREATE INDEX IF NOT EXISTS idx_citas_doctor_fecha    ON citas (doctor_id, fecha);
CREATE INDEX IF NOT EXISTS idx_historial_paciente    ON historial_clinico (paciente_id);
CREATE INDEX IF NOT EXISTS idx_historial_doctor      ON historial_clinico (doctor_id);
CREATE INDEX IF NOT EXISTS idx_historial_cita        ON historial_clinico (cita_id);
CREATE INDEX IF NOT EXISTS idx_historial_cie10       ON historial_clinico (diagnostico_cie10);  -- sin FK: la lista de codigos vive en CodigosCie10Comunes.java, no en la BD
CREATE INDEX IF NOT EXISTS idx_pacientes_nombre      ON pacientes (nombre, apellido);
CREATE INDEX IF NOT EXISTS idx_doctores_especialidad ON doctores (especialidad);

-- ============================================================================
-- SECCION 5 - USUARIO INICIAL
-- ----------------------------------------------------------------------------
-- Hash de ejemplo para la clave "admin123" (SOLO para probar que el login
-- contra la BD funciona). Cambia esta contraseña de inmediato una vez que
-- confirmes que el login funciona -- no la dejes en producción.
-- ============================================================================

INSERT INTO usuarios (id, usuario, password_hash, rol)
VALUES ('usr-001', 'admin', '123456789', 'admin')
ON CONFLICT (usuario) DO NOTHING;

 --para actualizar contraseña:
COMMIT;
INSERT INTO usuarios (id, usuario, password_hash, rol)
VALUES ('usr-001', 'admin', '123456789', 'admin')
ON CONFLICT (usuario) 
DO UPDATE SET password_hash = EXCLUDED.password_hash;

COMMIT;

-- ============================================================================
-- NOTAS
-- ----------------------------------------------------------------------------
-- * diagnostico_cie10 solo guarda el código de texto (ej: "E119"). No hay
--   tabla catálogo ni FK: la lista de códigos comunes vive embebida en
--   CodigosCie10Comunes.java, directamente usada por el ComboBox del
--   formulario de Historial Clínico (no requiere consultar la BD).
-- * Si en el futuro quieres el catálogo completo de 15,037 códigos en una
--   tabla aparte (para reportes, por ejemplo), usa cie10_catalogo_completo.sql
--   como referencia; no es necesario para que el formulario funcione.
-- * Verificado con PostgreSQL 14+.
-- ============================================================================
