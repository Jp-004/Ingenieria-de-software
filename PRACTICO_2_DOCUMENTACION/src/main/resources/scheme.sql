DROP TABLE IF EXISTS inscripcion;
DROP TABLE IF EXISTS alumno;
DROP TABLE IF EXISTS profesor;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS plan_materia;
DROP TABLE IF EXISTS materia;
DROP TABLE IF EXISTS plan_de_estudio;
DROP TABLE IF EXISTS carrera;

CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    rango TEXT NOT NULL,
    CONSTRAINT CHK_rango CHECK (rango IN ('Admin', 'Profesor', 'Alumno'))
);

CREATE TABLE carrera (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    codigo TEXT NOT NULL UNIQUE
);

CREATE TABLE plan_de_estudio (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    anio_vigencia INTEGER NOT NULL,
    activo INTEGER NOT NULL DEFAULT 1,
    carrera_id INTEGER NOT NULL,
    FOREIGN KEY (carrera_id) REFERENCES carrera(id)
);

CREATE TABLE materia (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    codigo TEXT NOT NULL UNIQUE,
    docente_id INTEGER
);

CREATE TABLE profesor (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    correo TEXT NOT NULL UNIQUE,
    dni TEXT NOT NULL UNIQUE
);

CREATE TABLE alumno (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    correo TEXT NOT NULL UNIQUE,
    dni TEXT NOT NULL UNIQUE,
    legajo INTEGER NOT NULL UNIQUE,
    fecha_ingreso TEXT NOT NULL,
    estado_academico TEXT NOT NULL DEFAULT 'Activo',
    carrera_id INTEGER,
    FOREIGN KEY (carrera_id) REFERENCES carrera(id)
);

CREATE TABLE inscripcion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TEXT NOT NULL,
    estado TEXT NOT NULL DEFAULT 'Pendiente',
    nota_final_cursada REAL,
    alumno_id INTEGER NOT NULL,
    materia_id INTEGER NOT NULL,
    FOREIGN KEY (alumno_id) REFERENCES alumno(id),
    FOREIGN KEY (materia_id) REFERENCES materia(id)
);

CREATE TABLE IF NOT EXISTS calificaciones (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    alumno_id INTEGER NOT NULL,
    materia_id INTEGER NOT NULL,
    instancia VARCHAR(50) NOT NULL, -- Ej: 'Primer Parcial', 'Segundo Parcial', 'Final'
    nota REAL NOT NULL,
    fecha TEXT,
    FOREIGN KEY (alumno_id) REFERENCES alumnos(id),
    FOREIGN KEY (materia_id) REFERENCES materias(id)
);

CREATE TABLE IF NOT EXISTS periodo_examen (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    carrera_id INTEGER NOT NULL,
    tipo TEXT NOT NULL,         -- NUEVO: Acá guardamos 'Parcial' o 'Final'
    descripcion TEXT NOT NULL,  -- Ej: "Primeros Parciales 2026"
    fecha_inicio TEXT NOT NULL, -- Formato 'YYYY-MM-DD'
    fecha_fin TEXT NOT NULL,    -- Formato 'YYYY-MM-DD'
    FOREIGN KEY (carrera_id) REFERENCES carrera(id)
);

CREATE TABLE IF NOT EXISTS fecha_examen (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    materia_id INTEGER NOT NULL,
    instancia VARCHAR(50) NOT NULL,
    fecha TEXT NOT NULL,
    FOREIGN KEY (materia_id) REFERENCES materia(id)
);

CREATE TABLE materias_planes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    plan_de_estudio_id INTEGER NOT NULL,
    materia_id INTEGER NOT NULL,
    horas INTEGER NOT NULL,
    FOREIGN KEY (plan_de_estudio_id) REFERENCES plan_de_estudio(id) ON DELETE CASCADE,
    FOREIGN KEY (materia_id) REFERENCES materia(id) ON DELETE CASCADE
);

CREATE TABLE correlatividades (
    plan_de_estudio_id INTEGER NOT NULL,
    materia_id INTEGER NOT NULL,
    correlativa_id INTEGER NOT NULL,
    PRIMARY KEY (plan_de_estudio_id, materia_id, correlativa_id),
    FOREIGN KEY (plan_de_estudio_id) REFERENCES plan_de_estudio(id) ON DELETE CASCADE,
    FOREIGN KEY (materia_id) REFERENCES materia(id) ON DELETE CASCADE,
    FOREIGN KEY (correlativa_id) REFERENCES materia(id) ON DELETE CASCADE
);

CREATE TABLE materias_colaboradores (
    materia_id INTEGER NOT NULL,
    profesor_id INTEGER NOT NULL,
    PRIMARY KEY (materia_id, profesor_id),
    FOREIGN KEY (materia_id) REFERENCES materias(id) ON DELETE CASCADE,
    FOREIGN KEY (profesor_id) REFERENCES profesores(id) ON DELETE CASCADE
);

CREATE TABLE inscripcion_examen (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    alumno_id INTEGER NOT NULL,
    fecha_examen_id INTEGER NOT NULL,
    fecha_inscripcion TEXT NOT NULL,
    FOREIGN KEY(alumno_id) REFERENCES alumno(id) ON DELETE CASCADE,
    FOREIGN KEY(fecha_examen_id) REFERENCES fecha_examen(id) ON DELETE CASCADE
);
