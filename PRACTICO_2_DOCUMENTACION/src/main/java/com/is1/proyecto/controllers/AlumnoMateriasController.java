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

        // ==================== VISTA: INSCRIPCIÓN A NUEVA MATERIA ====================
        get("/alumno/inscripcion-materia", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
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

            List<Map> planes = Base.findAll("SELECT id FROM plan_de_estudio WHERE carrera_id = ? AND activo = 1", carreraId);
            if (planes.isEmpty()) {
                model.put("errorMessage", "No hay un plan de estudio activo configurado para tu carrera.");
                return new ModelAndView(model, "alumno/inscripcion_materia.mustache");
            }
            int planId = Integer.parseInt(planes.get(0).get("id").toString());

            // ACÁ ESTÁ LA MAGIA 1: Excluimos solo las materias donde ya estés cursando, regular o aprobado.
            // Si estás 'Libre', la materia NO se excluye, por lo tanto te va a aparecer para anotarte de nuevo.
            String sqlMateriasPlan = 
                "SELECT m.id, m.nombre, m.codigo FROM materia m " +
                "INNER JOIN materias_planes mp ON m.id = mp.materia_id " +
                "WHERE mp.plan_de_estudio_id = ? " +
                "AND m.id NOT IN (SELECT materia_id FROM inscripcion WHERE alumno_id = ? AND estado IN ('Cursando', 'Pendiente', 'Regular', 'Regularizada', 'Aprobada'))";
            
            List<Map> materiasCandidatas = Base.findAll(sqlMateriasPlan, planId, alumnoId);
            List<Map<String, Object>> materiasDisponibles = new ArrayList<>();

            for (Map mat : materiasCandidatas) {
                int materiaId = Integer.parseInt(mat.get("id").toString());
                List<Map> requeridas = Base.findAll("SELECT correlativa_id FROM correlatividades WHERE plan_de_estudio_id = ? AND materia_id = ?", planId, materiaId);

                boolean puedeInscribirse = true;
                for (Map reqCorr : requeridas) {
                    int correlativaId = Integer.parseInt(reqCorr.get("correlativa_id").toString());
                    long aprobada = Base.count("inscripcion", "alumno_id = ? AND materia_id = ? AND estado = 'Aprobada'", alumnoId, correlativaId);
                    if (aprobada == 0) {
                        puedeInscribirse = false; 
                        break;
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
            if (error != null) model.put("errorMessage", error);
            String msg = req.queryParams("message");
            if (msg != null) model.put("successMessage", msg);

            return new ModelAndView(model, "alumno/inscripcion_materia.mustache");
        }, new MustacheTemplateEngine());


        // ==================== LÓGICA: PROCESAR INSCRIPCIÓN ====================
        post("/alumno/procesar-inscripcion", (req, res) -> {
            String materiaIdStr = req.queryParams("materia_id");
            String correoAlumno = req.session().attribute("currentUserUsername");
            Alumno alumno = Alumno.findFirst("correo = ?", correoAlumno);

            if (alumno == null || materiaIdStr == null || materiaIdStr.isEmpty()) {
                res.redirect("/alumno/inscripcion-materia?error=Datos+invalidos");
                return "";
            }

            try {
                // ACÁ ESTÁ LA MAGIA 2: Verificamos si ya había un registro previo (ej: quedaste Libre)
                Inscripcion inscripcionExistente = Inscripcion.findFirst("alumno_id = ? AND materia_id = ?", alumno.getId(), Integer.parseInt(materiaIdStr));

                if (inscripcionExistente != null) {
                    if (inscripcionExistente.getString("estado").equalsIgnoreCase("Libre")) {
                        // Si estabas libre, te renovamos la cursada sin duplicar el registro en la BD
                        inscripcionExistente.set("estado", "Cursando");
                        inscripcionExistente.set("fecha", java.time.LocalDate.now().toString());
                        inscripcionExistente.saveIt();
                        res.redirect("/alumno/cursando?message=Re-inscripcion+exitosa.+Ya+estas+cursando+nuevamente.");
                    } else {
                        res.redirect("/alumno/cursando?error=Ya+te+encuentras+inscripto+en+esta+materia.");
                    }
                } else {
                    // Inscripción totalmente nueva y auto-aceptada
                    Inscripcion nuevaInscripcion = new Inscripcion();
                    nuevaInscripcion.set("alumno_id", alumno.getId());
                    nuevaInscripcion.set("materia_id", Integer.parseInt(materiaIdStr));
                    nuevaInscripcion.set("fecha", java.time.LocalDate.now().toString());
                    nuevaInscripcion.set("estado", "Cursando"); // Se acepta de una
                    nuevaInscripcion.saveIt();
                    res.redirect("/alumno/cursando?message=Inscripcion+realizada+con+exito.+Ya+estas+cursando.");
                }
            } catch (Exception e) {
                System.err.println("Error procesando inscripción: " + e.getMessage());
                res.redirect("/alumno/inscripcion-materia?error=Error+interno+al+procesar+la+inscripcion");
            }
            return "";
        });


        // ==================== VISTA: MATERIAS CURSANDO ====================
        get("/alumno/cursando", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            
            String correoAlumno = req.session().attribute("currentUserUsername");
            if (correoAlumno == null) {
                res.redirect("/?error=Debes+iniciar+sesion");
                return null;
            }

            Alumno alumno = Alumno.findFirst("correo = ?", correoAlumno);
            if (alumno == null) {
                res.redirect("/?error=Alumno+no+encontrado");
                return null;
            }

            int alumnoId = Integer.parseInt(alumno.getId().toString());

            // Agregamos m.id as materia_id para poder buscar las notas después
            String sql = "SELECT m.id as materia_id, m.nombre as nombre_materia, m.codigo, i.estado " +
                         "FROM inscripcion i " +
                         "INNER JOIN materia m ON i.materia_id = m.id " +
                         "WHERE i.alumno_id = ? AND i.estado != 'Aprobada'";
            
            List<Map> cursando = Base.findAll(sql, alumnoId);
            List<Map<String, Object>> materiasVista = new ArrayList<>();

            for (Map row : cursando) {
                Map<String, Object> mat = new HashMap<>(row);
                int materiaId = Integer.parseInt(row.get("materia_id").toString());
                String estado = (String) row.get("estado");
                
                // 1. Lógica de colores y estados
                if (estado != null && estado.equalsIgnoreCase("Libre")) {
                    mat.put("es_libre", true);
                    mat.put("clase_estado", "badge-rojo");
                } else if (estado != null && (estado.equalsIgnoreCase("Regular") || estado.equalsIgnoreCase("Regularizada"))) {
                    mat.put("es_regular", true);
                    mat.put("clase_estado", "badge-verde");
                } else {
                    mat.put("es_cursando", true);
                    mat.put("clase_estado", "badge-amarillo");
                }

                // 2. Lógica para buscar las notas de esta materia
                List<Map> notas = Base.findAll("SELECT instancia, nota FROM calificaciones WHERE alumno_id = ? AND materia_id = ?", alumnoId, materiaId);
                
                String notaP1 = "-", notaP2 = "-", notaRec = "-";
                Double valP1 = null, valP2 = null, valRec = null;

                for (Map n : notas) {
                    String inst = (String) n.get("instancia");
                    Double val = Double.parseDouble(n.get("nota").toString());
                    
                    if (inst.equalsIgnoreCase("Primer Parcial")) { notaP1 = String.valueOf(val); valP1 = val; }
                    else if (inst.equalsIgnoreCase("Segundo Parcial")) { notaP2 = String.valueOf(val); valP2 = val; }
                    else if (inst.equalsIgnoreCase("Recuperatorio")) { notaRec = String.valueOf(val); valRec = val; }
                }

                // 3. Calcular el promedio mostrado al alumno
                String promedioFinal = "-";
                if (valP1 != null || valP2 != null) {
                    Double defP1 = valP1;
                    Double defP2 = valP2;
                    
                    if (valRec != null) {
                        if (valP1 != null && valP1 < 5) defP1 = valRec;
                        else if (valP2 != null && valP2 < 5) defP2 = valRec;
                        else if (valP1 != null && valP2 != null) {
                            if (valP1 < valP2) defP1 = Math.max(valP1, valRec);
                            else defP2 = Math.max(valP2, valRec);
                        }
                    }
                    if (defP1 != null && defP2 != null) {
                        double prom = (defP1 + defP2) / 2.0;
                        promedioFinal = String.format("%.2f", prom);
                    }
                }

                mat.put("nota_p1", notaP1);
                mat.put("nota_p2", notaP2);
                mat.put("nota_rec", notaRec);
                mat.put("promedio_final", promedioFinal);

                materiasVista.add(mat);
            }
            
            model.put("materias_cursando", materiasVista);

            String error = req.queryParams("error");
            if (error != null) model.put("errorMessage", error);
            
            String msg = req.queryParams("message");
            if (msg != null) model.put("successMessage", msg);

            return new ModelAndView(model, "alumno/materias_cursando.mustache");
        }, new MustacheTemplateEngine());


        // ==================== VISTA: MATERIAS APROBADAS (ANALÍTICO) ====================
        get("/alumno/aprobadas", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            
            String correoAlumno = req.session().attribute("currentUserUsername");
            if (correoAlumno == null) {
                res.redirect("/?error=Debes+iniciar+sesion");
                return null;
            }

            Alumno alumno = Alumno.findFirst("correo = ?", correoAlumno);
            if (alumno == null) {
                res.redirect("/?error=Alumno+no+encontrado");
                return null;
            }
            int alumnoId = Integer.parseInt(alumno.getId().toString());

            String sqlAprobadas = "SELECT m.nombre as nombre_materia, m.codigo, i.nota_final_cursada " +
                                  "FROM inscripcion i " +
                                  "INNER JOIN materia m ON i.materia_id = m.id " +
                                  "WHERE i.alumno_id = ? AND i.estado = 'Aprobada'";
            
            List<Map> aprobadas = Base.findAll(sqlAprobadas, alumnoId);
            model.put("materias_aprobadas", aprobadas);
            
            int totalAprobadas = aprobadas.size();
            int porcentaje = 0;
            Object carreraIdObj = alumno.get("carrera_id");
            
            if (carreraIdObj != null) {
                int carreraId = Integer.parseInt(carreraIdObj.toString());
                List<Map> planes = Base.findAll("SELECT id FROM plan_de_estudio WHERE carrera_id = ? AND activo = 1", carreraId);
                if (!planes.isEmpty()) {
                    int planId = Integer.parseInt(planes.get(0).get("id").toString());
                    long totalMateriasPlan = Base.count("materias_planes", "plan_de_estudio_id = ?", planId);
                    if (totalMateriasPlan > 0) {
                        porcentaje = (int) Math.round(((double) totalAprobadas / totalMateriasPlan) * 100);
                    }
                }
            }

            model.put("total_aprobadas", totalAprobadas);
            model.put("porcentaje", porcentaje);

            return new ModelAndView(model, "alumno/materias_aprobadas.mustache");
        }, new MustacheTemplateEngine());

    }
}