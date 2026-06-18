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

            List<Map> planes = Base.findAll("SELECT id FROM plan_de_estudio WHERE carrera_id = ? AND activo = 1",
                    carreraId);
            if (planes.isEmpty()) {
                model.put("errorMessage", "No hay un plan de estudio activo configurado para tu carrera.");
                return new ModelAndView(model, "alumno/inscripcion_materia.mustache");
            }
            int planId = Integer.parseInt(planes.get(0).get("id").toString());

            // ACÁ ESTÁ LA MAGIA 1: Excluimos solo las materias donde ya estés cursando,
            // regular o aprobado.
            // Si estás 'Libre', la materia NO se excluye, por lo tanto te va a aparecer
            // para anotarte de nuevo.
            String sqlMateriasPlan = "SELECT m.id, m.nombre, m.codigo FROM materia m " +
                    "INNER JOIN materias_planes mp ON m.id = mp.materia_id " +
                    "WHERE mp.plan_de_estudio_id = ? " +
                    "AND m.id NOT IN (SELECT materia_id FROM inscripcion WHERE alumno_id = ? AND estado IN ('Cursando', 'Pendiente', 'Regular', 'Regularizada', 'Aprobada'))";

            List<Map> materiasCandidatas = Base.findAll(sqlMateriasPlan, planId, alumnoId);
            List<Map<String, Object>> materiasDisponibles = new ArrayList<>();

            for (Map mat : materiasCandidatas) {
                int materiaId = Integer.parseInt(mat.get("id").toString());
                List<Map> requeridas = Base.findAll(
                        "SELECT correlativa_id FROM correlatividades WHERE plan_de_estudio_id = ? AND materia_id = ?",
                        planId, materiaId);

                boolean puedeInscribirse = true;
                for (Map reqCorr : requeridas) {
                    int correlativaId = Integer.parseInt(reqCorr.get("correlativa_id").toString());
                    long aprobada = Base.count("inscripcion",
                            "alumno_id = ? AND materia_id = ? AND estado = 'Aprobada'", alumnoId, correlativaId);
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
            if (error != null)
                model.put("errorMessage", error);
            String msg = req.queryParams("message");
            if (msg != null)
                model.put("successMessage", msg);

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
                // ACÁ ESTÁ LA MAGIA 2: Verificamos si ya había un registro previo (ej: quedaste
                // Libre)
                Inscripcion inscripcionExistente = Inscripcion.findFirst("alumno_id = ? AND materia_id = ?",
                        alumno.getId(), Integer.parseInt(materiaIdStr));

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

                // ACÁ BUSCA TODAS LAS FECHAS DE LOS PARCIALES
                List<Map> fechas = Base.findAll("SELECT instancia, fecha FROM fecha_examen WHERE materia_id = ?", materiaId);
                String fechaP1 = "A definir", fechaP2 = "A definir", fechaRec = "A definir";
                
                for (Map f : fechas) {
                    String inst = (String) f.get("instancia");
                    String date = (String) f.get("fecha");
                    if (inst.equalsIgnoreCase("Primer Parcial")) { fechaP1 = date; }
                    else if (inst.equalsIgnoreCase("Segundo Parcial")) { fechaP2 = date; }
                    else if (inst.equalsIgnoreCase("Recuperatorio")) { fechaRec = date; }
                }

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
                mat.put("fecha_p1", fechaP1);
                mat.put("fecha_p2", fechaP2);
                mat.put("fecha_rec", fechaRec);
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

        // ==================== VISTA: MATERIAS APROBADAS (ANALÍTICO)
        // ====================
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
                List<Map> planes = Base.findAll("SELECT id FROM plan_de_estudio WHERE carrera_id = ? AND activo = 1",
                        carreraId);
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

        // ==================== VISTA: INSCRIPCIÓN A FINALES ====================
        get("/alumno/finales", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String correo = req.session().attribute("currentUserUsername");
            Alumno alumno = Alumno.findFirst("correo = ?", correo);
            
            if(alumno == null) { 
                res.redirect("/?error=Alumno+no+encontrado"); 
                return null; 
            }
            int alumnoId = Integer.parseInt(alumno.getId().toString());

            List<Map<String, Object>> finalesDisponibles = new ArrayList<>();
            List<Map<String, Object>> misInscripciones = new ArrayList<>();

            // --- PARTE 1: MIS INSCRIPCIONES (Se muestran siempre, incluso si ya aprobó el final) ---
            String sqlInscriptos = "SELECT ie.fecha_examen_id, fe.materia_id, fe.fecha, fe.instancia, m.nombre as nombre_materia " +
                                   "FROM inscripcion_examen ie " +
                                   "INNER JOIN fecha_examen fe ON ie.fecha_examen_id = fe.id " +
                                   "INNER JOIN materia m ON fe.materia_id = m.id " +
                                   "WHERE ie.alumno_id = ?";
            
            List<Map> inscriptos = Base.findAll(sqlInscriptos, alumnoId);

            for (Map f : inscriptos) {
                int fechaId = Integer.parseInt(f.get("fecha_examen_id").toString());
                int materiaId = Integer.parseInt(f.get("materia_id").toString());
                String fechaStr = (String) f.get("fecha");
                
                // --- ACÁ ESTÁ LA CORRECCIÓN: Usamos findAll en lugar de findFirst ---
                List<Map> insc = Base.findAll("SELECT estado FROM inscripcion WHERE alumno_id = ? AND materia_id = ?", alumnoId, materiaId);
                String estadoCursada = !insc.isEmpty() ? (String) insc.get(0).get("estado") : "Desconocido";

                Map<String, Object> item = new HashMap<>();
                item.put("materia", f.get("nombre_materia"));
                item.put("fecha", fechaStr);
                item.put("instancia", f.get("instancia"));
                item.put("estado_cursada", estadoCursada);
                item.put("fecha_id", fechaId);

                // Buscamos si el profesor ya le cargó la nota de este final
                List<Map> califs = Base.findAll("SELECT nota FROM calificaciones WHERE alumno_id = ? AND materia_id = ? AND instancia = 'Examen Final'", alumnoId, materiaId);
                
                if (!califs.isEmpty()) {
                    item.put("nota", califs.get(0).get("nota"));
                    item.put("tiene_nota", true);
                    item.put("puede_cancelar", false); // Si ya tiene nota, es imposible cancelar
                } else {
                    item.put("nota", "Pendiente de corrección");
                    item.put("tiene_nota", false);

                    // Lógica para saber si puede cancelar
                    java.time.LocalDate fechaExamenLD = java.time.LocalDate.parse(fechaStr);
                    java.time.LocalDate hoy = java.time.LocalDate.now();
                    long diasFaltantes = java.time.temporal.ChronoUnit.DAYS.between(hoy, fechaExamenLD);
                    
                    if (diasFaltantes >= 2) {
                        item.put("puede_cancelar", true);
                    } else {
                        item.put("puede_cancelar", false);
                    }
                }
                misInscripciones.add(item);
            }


            // --- PARTE 2: FINALES DISPONIBLES (Solo materias Regulares/Libres donde NO esté inscripto) ---
            String sqlMateriasHabilitadas = "SELECT materia_id, estado FROM inscripcion WHERE alumno_id = ? AND estado IN ('Regular', 'Regularizada', 'Libre')";
            List<Map> materiasHabilitadas = Base.findAll(sqlMateriasHabilitadas, alumnoId);
            
            for (Map mat : materiasHabilitadas) {
                int materiaId = Integer.parseInt(mat.get("materia_id").toString());
                String estadoCursada = (String) mat.get("estado");

                com.is1.proyecto.models.Materia materia = com.is1.proyecto.models.Materia.findById(materiaId);
                String nombreMateria = materia != null ? materia.getString("nombre") : "Materia Desconocida";

                List<Map> fechas = Base.findAll("SELECT id, fecha, instancia FROM fecha_examen WHERE materia_id = ? AND instancia = 'Examen Final'", materiaId);

                for (Map f : fechas) {
                    int fechaId = Integer.parseInt(f.get("id").toString());
                    long estaInscripto = Base.count("inscripcion_examen", "alumno_id = ? AND fecha_examen_id = ?", alumnoId, fechaId);

                    // Si no está inscripto a esta fecha, se la mostramos para que pueda anotarse
                    if (estaInscripto == 0) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("materia", nombreMateria);
                        item.put("fecha", f.get("fecha"));
                        item.put("instancia", f.get("instancia"));
                        item.put("estado_cursada", estadoCursada);
                        item.put("fecha_id", fechaId);
                        
                        finalesDisponibles.add(item);
                    }
                }
            }

            model.put("disponibles", finalesDisponibles);
            model.put("inscriptos", misInscripciones);
            
            String error = req.queryParams("error");
            if (error != null) model.put("errorMessage", error);
            String msg = req.queryParams("message");
            if (msg != null) model.put("successMessage", msg);

            return new ModelAndView(model, "alumno/inscripcion_finales.mustache");
        }, new MustacheTemplateEngine());

        // ==================== LÓGICA: PROCESAR INSCRIPCIÓN A FINAL
        // ====================
        post("/alumno/inscribirse-final", (req, res) -> {
            String fechaIdStr = req.queryParams("fecha_id");
            String correo = req.session().attribute("currentUserUsername");
            Alumno alumno = Alumno.findFirst("correo = ?", correo);

            if (alumno != null && fechaIdStr != null && !fechaIdStr.isEmpty()) {
                try {
                    // Usamos Base.exec para hacer un INSERT directo a la tabla puente
                    Base.exec(
                            "INSERT INTO inscripcion_examen (alumno_id, fecha_examen_id, fecha_inscripcion) VALUES (?, ?, ?)",
                            alumno.getId(), Integer.parseInt(fechaIdStr), java.time.LocalDate.now().toString());
                    res.redirect("/alumno/finales?message=Inscripcion+al+examen+exitosa.+Exitos!");
                } catch (Exception e) {
                    System.err.println("Error al inscribirse al final: " + e.getMessage());
                    res.redirect("/alumno/finales?error=Error+interno+al+procesar+la+inscripcion");
                }
            } else {
                res.redirect("/alumno/finales?error=Datos+invalidos");
            }
            return "";
        });// ==================== LÓGICA: CANCELAR INSCRIPCIÓN A FINAL ====================
        post("/alumno/cancelar-final", (req, res) -> {
            String fechaIdStr = req.queryParams("fecha_id");
            String correo = req.session().attribute("currentUserUsername");
            Alumno alumno = Alumno.findFirst("correo = ?", correo);

            if (alumno != null && fechaIdStr != null && !fechaIdStr.isEmpty()) {
                try {
                    // Primero verificamos la fecha por seguridad (por si alguien intenta hackear el botón)
                    List<Map> fechas = Base.findAll("SELECT fecha FROM fecha_examen WHERE id = ?", fechaIdStr);
                    if (!fechas.isEmpty()) {
                        String fechaStr = (String) fechas.get(0).get("fecha");
                        java.time.LocalDate fechaExamenLD = java.time.LocalDate.parse(fechaStr);
                        java.time.LocalDate hoy = java.time.LocalDate.now();
                        long diasFaltantes = java.time.temporal.ChronoUnit.DAYS.between(hoy, fechaExamenLD);

                        if (diasFaltantes >= 2) {
                            // Si todo está bien, lo borramos de la lista de asistencia
                            Base.exec("DELETE FROM inscripcion_examen WHERE alumno_id = ? AND fecha_examen_id = ?", alumno.getId(), Integer.parseInt(fechaIdStr));
                            res.redirect("/alumno/finales?message=Inscripcion+cancelada+exitosamente");
                        } else {
                            res.redirect("/alumno/finales?error=No+se+puede+cancelar+con+menos+de+48+horas+de+anticipacion");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error al cancelar el final: " + e.getMessage());
                    res.redirect("/alumno/finales?error=Error+interno+al+procesar+la+cancelacion");
                }
            } else {
                res.redirect("/alumno/finales?error=Datos+invalidos");
            }
            return "";
        });
        
        // ==================== LÓGICA: CANCELAR INSCRIPCIÓN A FINAL ====================
        post("/alumno/cancelar-final", (req, res) -> {
            String fechaIdStr = req.queryParams("fecha_id");
            String correo = req.session().attribute("currentUserUsername");
            Alumno alumno = Alumno.findFirst("correo = ?", correo);

            if (alumno != null && fechaIdStr != null && !fechaIdStr.isEmpty()) {
                try {
                    // Primero verificamos la fecha por seguridad (por si alguien intenta hackear el botón)
                    List<Map> fechas = Base.findAll("SELECT fecha FROM fecha_examen WHERE id = ?", fechaIdStr);
                    if (!fechas.isEmpty()) {
                        String fechaStr = (String) fechas.get(0).get("fecha");
                        java.time.LocalDate fechaExamenLD = java.time.LocalDate.parse(fechaStr);
                        java.time.LocalDate hoy = java.time.LocalDate.now();
                        long diasFaltantes = java.time.temporal.ChronoUnit.DAYS.between(hoy, fechaExamenLD);

                        if (diasFaltantes >= 2) {
                            // Si todo está bien, lo borramos de la lista de asistencia
                            Base.exec("DELETE FROM inscripcion_examen WHERE alumno_id = ? AND fecha_examen_id = ?", alumno.getId(), Integer.parseInt(fechaIdStr));
                            res.redirect("/alumno/finales?message=Inscripcion+cancelada+exitosamente");
                        } else {
                            res.redirect("/alumno/finales?error=No+se+puede+cancelar+con+menos+de+48+horas+de+anticipacion");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error al cancelar el final: " + e.getMessage());
                    res.redirect("/alumno/finales?error=Error+interno+al+procesar+la+cancelacion");
                }
            } else {
                res.redirect("/alumno/finales?error=Datos+invalidos");
            }
            return "";
        });
    }
}