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
    plan_de_estudio_id INTEGER,
    docente_id INTEGER,  
    base_datos TEXT, 
    horas INTEGER,              
    FOREIGN KEY (plan_de_estudio_id) REFERENCES plan_de_estudio(id),
    FOREIGN KEY (docente_id) REFERENCES profesor(id)
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