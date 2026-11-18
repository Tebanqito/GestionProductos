-- 1) Crear la base (ejecutar UNA vez, conectado a la base "postgres"):
--    CREATE DATABASE gestion_productos;
-- 2) Conectarse a gestion_productos y ejecutar el resto de este script.

CREATE TABLE rubros (
    id     INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE proveedores (
    id_prov      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    razon_social VARCHAR(100) NOT NULL UNIQUE,
    domicilio    VARCHAR(150),
    telefono     VARCHAR(30)
);

CREATE TABLE productos (
    id           INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL,
    descripcion  VARCHAR(250),
    precio       NUMERIC(12,2) NOT NULL CHECK (precio > 0),
    stock        INTEGER NOT NULL CHECK (stock >= 0),
    activo       BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_alta   DATE NOT NULL DEFAULT CURRENT_DATE,
    rubro_id     INTEGER NOT NULL,
    proveedor_id INTEGER NOT NULL,
    CONSTRAINT fk_producto_rubro
        FOREIGN KEY (rubro_id) REFERENCES rubros(id),
    CONSTRAINT fk_producto_proveedor
        FOREIGN KEY (proveedor_id) REFERENCES proveedores(id_prov)
);

-- El PIN se guarda como texto para conservar ceros iniciales (ej. 0042).
-- Roles: e = empleado, a = administrador, i = invitado.
CREATE TABLE roles (
    id       INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pin      VARCHAR(4) NOT NULL UNIQUE,
    apellido VARCHAR(80) NOT NULL,
    nombre   VARCHAR(80) NOT NULL,
    rol      CHAR(1) NOT NULL,
    CONSTRAINT ck_roles_pin      CHECK (pin ~ '^[0-9]{4}$'),
    CONSTRAINT ck_roles_tipo     CHECK (rol IN ('e', 'a', 'i')),
    CONSTRAINT ck_roles_apellido CHECK (length(trim(apellido)) > 0),
    CONSTRAINT ck_roles_nombre   CHECK (length(trim(nombre)) > 0)
);

-- ---------- Datos de ejemplo ----------
INSERT INTO rubros (nombre) VALUES ('Informática'), ('Librería'), ('Audio'), ('Accesorios');

INSERT INTO proveedores (razon_social, domicilio, telefono) VALUES
 ('TecnoSur S.A.',  'Av. Corrientes 1234, CABA', '011-4555-0001'),
 ('Papelera Norte', 'San Martín 456, Rosario',   '0341-444-0002');

INSERT INTO productos (nombre, descripcion, precio, stock, rubro_id, proveedor_id) VALUES
 ('Teclado mecánico USB', 'Switches rojos, layout español', 42000.00, 15, 1, 1),
 ('Cuaderno A4 rayado',   '80 hojas',                         3500.50, 100, 2, 2);

INSERT INTO roles (pin, apellido, nombre, rol) VALUES
 ('1234', 'Pérez', 'Ana',   'e'),
 ('5678', 'Gómez', 'Luis',  'a'),
 ('0042', 'Díaz',  'Carla', 'e'),
 ('4321', 'Invitado', 'Demo', 'i');

SELECT id, apellido, nombre, rol FROM roles ORDER BY id;
