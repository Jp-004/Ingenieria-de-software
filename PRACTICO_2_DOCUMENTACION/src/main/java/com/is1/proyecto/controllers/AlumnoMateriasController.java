package com.is1.proyecto.controllers;

import static spark.Spark.*;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;
import org.javalite.activejdbc.Base;
import com.is1.proyecto.models.Alumno;
import com.is1.proyecto.models.Inscripcion;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class AlumnoMateriasController {

    public static void init() {

        // ==================== LÓGICA DE INSCRIPCIÓN DEL ALUMNO ====================

        get("/alumno/inscripcion-materia", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            // 1. Identificar al Alumno logueado a través de su cuenta/correo en la sesión
            String correoAlumno = req.session().attribute("currentUserUsername");
            Alumno alumno = Alumno.findFirst("correo = ?", correoAlumno);

            if (alumno == null) {
                res.redirect("/?error=Error+al+identificar+perfil+estudiantil");
                return null;
            }

            int alumnoId = Integer.parseInt(alumno.getId().toString());
            Object carreraIdObj = alumno.get("carrera_id");

            if (carreraIdObj == null) {
                model.put("errorMessage", "No tienes una carrera asignada. Contacta al administrador.");
                return new ModelAndView(model, "alumno/inscripcion_materia.mustache");
            }
            int carreraId = Integer.parseInt(carreraIdObj.toString());

            // 2. Buscar el plan de estudio activo para la carrera del alumno
            List<Map> planes = Base.findAll("SELECT id FROM plan_de_estudio WHERE carrera_id = ? AND activo = 1",
                    carreraId);
            if (planes.isEmpty()) {
                model.put("errorMessage", "No hay un plan de estudio activo configurado para tu carrera.");
                return new ModelAndView(model, "alumno/inscripcion_materia.mustache");
            }
            int planId = Integer.parseInt(planes.get(0).get("id").toString());

            // 3. Traer materias del plan en las que el alumno NO esté ya inscripto (ni
            // cursando ni aprobadas)
            String sqlMateriasPlan = "SELECT m.id, m.nombre, m.codigo FROM materia m " +
                    "INNER JOIN materias_planes mp ON m.id = mp.materia_id " +
                    "WHERE mp.plan_de_estudio_id = ? " +
                    "AND m.id NOT IN (SELECT materia_id FROM inscripcion WHERE alumno_id = ?)";

            List<Map> materiasCandidatas = Base.findAll(sqlMateriasPlan, planId, alumnoId);
            List<Map<String, Object>> materiasDisponibles = new ArrayList<>();

            // 4. Evaluar el árbol de correlatividades para cada materia candidata
            for (Map mat : materiasCandidatas) {
                int materiaId = Integer.parseInt(mat.get("id").toString());

                // Buscamos si esta materia exige materias previas en este plan
                List<Map> requeridas = Base.findAll(
                        "SELECT correlativa_id FROM correlatividades WHERE plan_de_estudio_id = ? AND materia_id = ?",
                        planId, materiaId);

                boolean puedeInscribirse = true;
                for (Map reqCorr : requeridas) {
                    int correlativaId = Integer.parseInt(reqCorr.get("correlativa_id").toString());

                    // Verificamos si la correlativa está aprobada (en inscripcion con estado
                    // 'Aprobada')
                    long aprobada = Base.count("inscripcion",
                            "alumno_id = ? AND materia_id = ? AND estado = 'Aprobada'",
                            alumnoId, correlativaId);

                    if (aprobada == 0) {
                        puedeInscribirse = false;
                        break; // No cumple esta correlativa, se descarta la materia
                    }
                }

                if (puedeInscribirse) {
                    Map<String, Object> disponible = new HashMap<>();
                    disponible.put("id", materiaId);
                    disponible.put("nombre", mat.get("nombre"));
                    disponible.put("codigo", mat.get("codigo"));
                    materiasDisponibles.add(disponible);
                }
            }

            model.put("materias_disponibles", materiasDisponibles);

            String error = req.queryParams("error");
            if (error != null)
                model.put("errorMessage", error);
            String msg = req.queryParams("message");
            if (msg != null)
                model.put("successMessage", msg);

            return new ModelAndView(model, "alumno/inscripcion_materia.mustache");
        }, new MustacheTemplateEngine());

        post("/alumno/procesar-inscripcion", (req, res) -> {
            String materiaIdStr = req.queryParams("materia_id");
            String correoAlumno = req.session().attribute("currentUserUsername");
            Alumno alumno = Alumno.findFirst("correo = ?", correoAlumno);

            if (alumno == null || materiaIdStr == null || materiaIdStr.isEmpty()) {
                res.redirect("/alumno/inscripcion-materia?error=Datos+invalidos");
                return "";
            }

            try {
                Inscripcion nuevaInscripcion = new Inscripcion();
                nuevaInscripcion.set("alumno_id", alumno.getId());
                nuevaInscripcion.set("materia_id", Integer.parseInt(materiaIdStr));
                nuevaInscripcion.set("fecha", java.time.LocalDate.now().toString());
                nuevaInscripcion.set("estado", "Pendiente"); // Queda pendiente hasta que se apruebe
                nuevaInscripcion.saveIt();

                res.redirect("/alumno/cursando?message=Inscripcion+realizada+con+exito.+Espera+confirmacion.");
            } catch (Exception e) {
                System.err.println("Error procesando inscripción: " + e.getMessage());
                res.redirect("/alumno/inscripcion-materia?error=Error+interno+al+procesar+la+inscripcion");
            }
            return "";
        });

        // ==================== VER MATERIAS CURSANDO ====================
        get("/alumno/cursando", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            // Validar sesión
            String correoAlumno = req.session().attribute("currentUserUsername");
            if (correoAlumno == null) {
                res.redirect("/?error=Debes+iniciar+sesion");
                return null;
            }

            // Buscar alumno
            Alumno alumno = Alumno.findFirst("correo = ?", correoAlumno);
            if (alumno == null) {
                res.redirect("/?error=Alumno+no+encontrado");
                return null;
            }

            // Traer las inscripciones que NO sean "Aprobada" o "Regularizada" (es decir,
            // que esté cursando o pendiente)
            // Hacemos un JOIN directo con la tabla materia para traer el nombre y código
            String sql = "SELECT m.nombre as nombre_materia, m.codigo, i.estado " +
                    "FROM inscripcion i " +
                    "INNER JOIN materia m ON i.materia_id = m.id " +
                    "WHERE i.alumno_id = ? AND i.estado NOT IN ('Aprobada', 'Regularizada')";

            List<Map> cursando = Base.findAll(sql, alumno.getId());

            model.put("materias_cursando", cursando);
            String error = req.queryParams("error");
            if (error != null)
                model.put("errorMessage", error);

            String msg = req.queryParams("message");
            if (msg != null)
                model.put("successMessage", msg);
            // -----------------------------------------------

            return new ModelAndView(model, "alumno/materias_cursando.mustache");
        }, new MustacheTemplateEngine());
    }

}