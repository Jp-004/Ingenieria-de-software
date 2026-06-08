-- ==============================================================================
-- SEED SQL PARA PRESENTACIÓN - SISTEMA DE GESTIÓN ACADÉMICA
-- ==============================================================================

-- 1. CARRERA Y PLAN DE ESTUDIO
INSERT INTO carrera (id, nombre, codigo) VALUES (1, 'Analista en computacion', 'AC-2026');
INSERT INTO plan_de_estudio (id, anio_vigencia, activo, carrera_id) VALUES (1, 2026, 1, 1);

-- ==============================================================================
-- 2. USUARIOS ADMINISTRADORES (Contraseña: 1234)
-- ==============================================================================
-- El hash $2a$12$... corresponde a la contraseña "1234" encriptada con BCrypt
INSERT INTO users (id, name, password, rango, ultimo_acceso) VALUES
(1, 'Esteban', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Admin', '08/06/2026 10:00'),
(2, 'Juani', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Admin', '08/06/2026 10:00'),
(3, 'Juanchi', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Admin', '08/06/2026 10:00'),
(4, 'Facundo', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Admin', '08/06/2026 10:00');

-- ==============================================================================
-- 3. PROFESORES (Pilotos F1) Y SUS USUARIOS (Contraseña: 1234)
-- El usuario para ingresar es su correo (ej: colapinto@uni.edu)
-- ==============================================================================
INSERT INTO profesor (id, nombre, apellido, correo, dni) VALUES
(1, 'Max', 'Verstappen', 'verstappen@uni.edu', '11111111'),
(2, 'Charles', 'Leclerc', 'leclerc@uni.edu', '22222222'),
(3, 'Lewis', 'Hamilton', 'hamilton@uni.edu', '33333333'),
(4, 'Lando', 'Norris', 'norris@uni.edu', '44444444'),
(5, 'Fernando', 'Alonso', 'alonso@uni.edu', '55555555'),
(6, 'Franco', 'Colapinto', 'colapinto@uni.edu', '66666666'),
(7, 'Carlos', 'Sainz', 'sainz@uni.edu', '77777777'),
(8, 'George', 'Russell', 'russell@uni.edu', '88888888'),
(9, 'Oscar', 'Piastri', 'piastri@uni.edu', '99999999'),
(10, 'Sergio', 'Perez', 'perez@uni.edu', '10101010');

INSERT INTO users (id, name, password, rango) VALUES
(5, 'verstappen@uni.edu', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Profesor'),
(6, 'leclerc@uni.edu', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Profesor'),
(7, 'hamilton@uni.edu', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Profesor'),
(8, 'norris@uni.edu', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Profesor'),
(9, 'alonso@uni.edu', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Profesor'),
(10, 'colapinto@uni.edu', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Profesor'),
(11, 'sainz@uni.edu', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Profesor'),
(12, 'russell@uni.edu', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Profesor');

-- ==============================================================================
-- 4. MATERIAS (Desde tu imagen) Y SUS HORAS
-- ==============================================================================
INSERT INTO materia (id, nombre, codigo, docente_id) VALUES
(1, 'Introducción a la Computación y Programación I', '3410', 1),
(2, 'Introducción a la Matemática', '3376', 2),
(3, 'Lógica y Resolución de Problemas', '3377', 3),
(4, 'Introducción a la Computación y Programación II', '3411', 1),
(5, 'Matemática Discreta', '3379', 2),
(6, 'Estructura de Datos y Algoritmos', '3412', 4),
(7, 'Organización de Computadoras', '3381', 5),
(8, 'Computación y Sociedad', '3382', 6),
(9, 'Inglés I', '3402', 3),
(10, 'Análisis y Diseño de Algoritmos I', '3383', 4),
(11, 'Bases de Datos', '3384', 5),
(12, 'Ingeniería de Software I', '3385', 6),
(13, 'Inglés II', '3403', 3);

-- Asociación Materias-Planes
INSERT INTO materias_planes (plan_de_estudio_id, materia_id, horas) VALUES
(1, 1, 112), (1, 2, 112), (1, 3, 112), (1, 4, 112),
(1, 5, 140), (1, 6, 112), (1, 7, 112), (1, 8, 56),
(1, 9, 56), (1, 10, 112), (1, 11, 112), (1, 12, 112), (1, 13, 56);

-- ==============================================================================
-- 5. PERIODO DE EXAMEN ÚNICO (Parciales y Finales)
-- ==============================================================================
-- Doble registro para burlar la validación interna de "Parcial" o "Final" con un solo periodo visual
INSERT INTO periodo_examen (id, tipo, descripcion, fecha_inicio, fecha_fin) VALUES
(1, 'Parcial', 'Periodo Global de Evaluaciones 2026', '2026-01-01', '2026-12-31'),
(2, 'Final', 'Periodo Global de Evaluaciones 2026', '2026-01-01', '2026-12-31');

-- ==============================================================================
-- 6. FECHAS DE EXÁMENES (Programadas por los profesores)
-- ==============================================================================
INSERT INTO fecha_examen (materia_id, instancia, fecha) VALUES
(1, 'Primer Parcial', '2026-06-15'),
(1, 'Segundo Parcial', '2026-06-25'),
(12, 'Examen Final', '2026-07-10'),
(6, 'Primer Parcial', '2026-06-20');

-- ==============================================================================
-- 7. ALUMNOS (Selección Argentina) Y SUS USUARIOS (Contraseña: 1234)
-- ==============================================================================
INSERT INTO alumno (id, nombre, apellido, correo, dni, legajo, fecha_ingreso, estado_academico, carrera_id) VALUES
(1, 'Lionel', 'Messi', 'messi@alu.edu', '10000010', 10001, '2022-03-01', 'Activo', 1),
(2, 'Emiliano', 'Martinez', 'martinez@alu.edu', '23000023', 10002, '2023-03-01', 'Activo', 1),
(3, 'Rodrigo', 'De Paul', 'depaul@alu.edu', '70000007', 10003, '2025-03-01', 'Activo', 1),
(4, 'Alexis', 'Mac Allister', 'macallister@alu.edu', '20000020', 10004, '2025-03-01', 'Activo', 1),
(5, 'Angel', 'Di Maria', 'dimaria@alu.edu', '11000011', 10005, '2022-03-01', 'Activo', 1),
(6, 'Julian', 'Alvarez', 'alvarez@alu.edu', '90000009', 10006, '2026-03-01', 'Activo', 1),
(7, 'Cristian', 'Romero', 'romero@alu.edu', '13000013', 10007, '2024-03-01', 'Activo', 1),
(8, 'Nicolas', 'Otamendi', 'otamendi@alu.edu', '19000019', 10008, '2022-03-01', 'Activo', 1);

INSERT INTO users (id, name, password, rango) VALUES
(13, 'messi@alu.edu', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Alumno'),
(14, 'martinez@alu.edu', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Alumno'),
(15, 'depaul@alu.edu', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Alumno'),
(16, 'macallister@alu.edu', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Alumno'),
(17, 'dimaria@alu.edu', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Alumno'),
(18, 'alvarez@alu.edu', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Alumno'),
(19, 'romero@alu.edu', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Alumno'),
(20, 'otamendi@alu.edu', '$2a$12$Jn6GUP9IxhsMNNSN5gLiPuOD4YeXP4jGjThLFw532PPqKSzXMl9UC', 'Alumno');

-- ==============================================================================
-- 8. INSCRIPCIONES Y CALIFICACIONES (Para mostrar actividad en la demo)
-- ==============================================================================
-- Messi y Di Maria (Están avanzados, casi por recibirse)
INSERT INTO inscripcion (fecha, estado, nota_final_cursada, alumno_id, materia_id) VALUES
('2022-03-01', 'Aprobada', 9.5, 1, 1),
('2022-03-01', 'Aprobada', 8.0, 1, 2),
('2023-03-01', 'Aprobada', 10.0, 1, 6),
('2026-03-01', 'Cursando', NULL, 1, 12),
('2022-03-01', 'Aprobada', 8.5, 5, 1),
('2023-03-01', 'Aprobada', 7.5, 5, 4),
('2026-03-01', 'Cursando', NULL, 5, 12);

-- De Paul y Mac Allister (En segundo año)
INSERT INTO inscripcion (fecha, estado, nota_final_cursada, alumno_id, materia_id) VALUES
('2025-03-01', 'Aprobada', 7.0, 3, 1),
('2026-03-01', 'Cursando', NULL, 3, 6),
('2025-03-01', 'Aprobada', 8.0, 4, 1),
('2026-03-01', 'Cursando', NULL, 4, 6);

-- Julian Alvarez (Recién ingresante en 1er año)
INSERT INTO inscripcion (fecha, estado, nota_final_cursada, alumno_id, materia_id) VALUES
('2026-03-10', 'Cursando', NULL, 6, 1),
('2026-03-10', 'Cursando', NULL, 6, 2);

-- Calificaciones registradas para que el profesor (Ej: Franco Colapinto, que dicta Ing. de Software I)
-- vea que sus alumnos (Messi y Di Maria) ya tienen notas en el sistema.
INSERT INTO calificaciones (alumno_id, materia_id, instancia, nota, fecha) VALUES
(1, 12, 'Primer Parcial', 9.0, '2026-05-15'),
(5, 12, 'Primer Parcial', 8.5, '2026-05-15');