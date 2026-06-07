-- Cargar Carreras
INSERT INTO carrera (nombre, codigo) VALUES ('Analista en Computación', 'ANC');
INSERT INTO carrera (nombre, codigo) VALUES ('Licenciatura en Computación', 'LCC');
INSERT INTO carrera (nombre, codigo) VALUES ('Licenciatura en Biología', 'LCB');
INSERT INTO carrera (nombre, codigo) VALUES ('Licenciatura en Física', 'LCF');


-- Cargar Plan de Estudio
INSERT INTO plan_de_estudio (anio_vigencia, activo, carrera_id) VALUES (2018, 1, 1);
INSERT INTO plan_de_estudio (anio_vigencia, activo, carrera_id) VALUES (2015, 1, 2);
INSERT INTO plan_de_estudio (anio_vigencia, activo, carrera_id) VALUES (2021, 1, 3);
INSERT INTO plan_de_estudio (anio_vigencia, activo, carrera_id) VALUES (2025, 1, 4);
INSERT INTO plan_de_estudio (anio_vigencia, activo, carrera_id) VALUES (2026, 0, 1);
INSERT INTO plan_de_estudio (anio_vigencia, activo, carrera_id) VALUES (2026, 0, 1);
INSERT INTO plan_de_estudio (anio_vigencia, activo, carrera_id) VALUES (2026, 0, 3);
INSERT INTO plan_de_estudio (anio_vigencia, activo, carrera_id) VALUES (2026, 0, 4);
INSERT INTO plan_de_estudio (anio_vigencia, activo, carrera_id) VALUES (2026, 0, 4);

-- Cargar Materias
INSERT INTO materia (nombre, codigo) VALUES ('Estructuras de Datos', '3381');
INSERT INTO materia (nombre, codigo) VALUES ('Lógica', '3382');
INSERT INTO materia (nombre, codigo) VALUES ('Matemática', '1978');
INSERT INTO materia (nombre, codigo) VALUES ('Álgebra Lineal', '1979');
INSERT INTO materia (nombre, codigo) VALUES ('Física Molecular', '1980');
INSERT INTO materia (nombre, codigo) VALUES ('Matemática Discreta', '3383');
INSERT INTO materia (nombre, codigo) VALUES ('Electromagnetismo', '1981');
INSERT INTO materia (nombre, codigo) VALUES ('Matemática II', '1982');
INSERT INTO materia (nombre, codigo) VALUES ('Sistemas Operativos', '1983');
INSERT INTO materia (nombre, codigo) VALUES ('Ingenieria de Software I', '3384');
INSERT INTO materia (nombre, codigo) VALUES ('Ingenieria de Software II', '3385');
INSERT INTO materia (nombre, codigo) VALUES ('Geometría', '3386');
INSERT INTO materia (nombre, codigo) VALUES ('Seminario de Redacción Informativa', '3387');

-- -- Cargar un Profesor
-- INSERT INTO profesor (nombre, apellido, correo, dni) VALUES ('Carlos', 'Gomez', 'cgomez@uni.edu', '12345678');
-- INSERT INTO profesor (nombre, apellido, correo, dni) VALUES ('Julián', 'Álvarez', -- 'julianalv@uni.edu', '36748389');
--INSERT INTO profesor (nombre, apellido, correo, dni) VALUES ('Enzo', 'Fernandez', 'enzofer@uni.edu', '38273940');
--INSERT INTO profesor (nombre, apellido, correo, dni) VALUES ('Lionel', 'Messi', 'lionelmes@uni.edu', '29384730');
--INSERT INTO profesor (nombre, apellido, correo, dni) VALUES ('Lautaro', 'Martinez', 'latuaromar@uni.edu', '30283749');

-- Cargar Alumnos
--INSERT INTO alumno (nombre, apellido, correo, dni, legajo, fecha_ingreso, estado_academico, carrera_id) 
--VALUES ('Cristian', 'Romero', 'cromero@gmail.com', '30123456', 10001, '2026-03-01', 'Activo', 1);
--INSERT INTO alumno (nombre, apellido, correo, dni, legajo, fecha_ingreso, estado_academico, carrera_id) 
--VALUES ('Emiliano', 'Martinez', 'emartinez@gmail.com', '35123456', 10002, '2026-03-01', 'Activo', 1);

-- Cargar Administrador
--INSERT INTO users (name, password, rango) VALUES ('Esteban', '1234', 'Admin');
--INSERT INTO users (name, password, rango) VALUES ('Juanchi', '1234', 'Admin');
--INSERT INTO users (name, password, rango) VALUES ('Facundo', '1234', 'Admin');
--INSERT INTO users (name, password, rango) VALUES ('Juani', '1234', 'Admin');
