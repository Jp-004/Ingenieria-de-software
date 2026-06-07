package com.is1.proyecto.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Base;

import com.is1.proyecto.models.Materia;
import com.is1.proyecto.models.PlanDeEstudio;

import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class PlanDeEstudioController {

    public static void init() {

        // 1. RENDERIZAR EL FORMULARIO DE EDICIÓN
        get("/plan/edit", (req, res) -> {
            String planId = req.queryParams("id");

            if (planId == null || planId.isEmpty()) {
                halt(400, "Falta el ID del plan de estudio.");
                return null;
            }

            PlanDeEstudio plan = PlanDeEstudio.findById(planId);
            if (plan == null) {
                halt(404, "Plan no encontrado.");
                return null;
            }

            Map<String, Object> model = new HashMap<>();

            // 1. Convertimos el Plan a Map y le inyectamos el nombre de la Carrera
            Map<String, Object> planMap = new HashMap<>(plan.toMap());

            int estadoActivo = plan.getInteger("activo") != null ? plan.getInteger("activo") : 0;
            planMap.put("is_activo", estadoActivo == 1);
            
            com.is1.proyecto.models.Carrera carrera = com.is1.proyecto.models.Carrera.findById(plan.get("carrera_id"));
            if (carrera != null) {
                planMap.put("carrera_nombre", carrera.getString("nombre"));
            }
            model.put("plan", planMap);

            String from = req.queryParams("from");
            if ("carrera".equals(from)) {
                // Si vino de carrera, vuelve filtrado por esa carrera
                model.put("backUrl", "/plan/list?carrera_id=" + plan.get("carrera_id") + "&from=carrera");
            } else {
                // Si vino de "Ver planes de estudio", vuelve a la lista completa
                model.put("backUrl", "/plan/list");
            }
            
            // 2. Traemos las materias del plan y las pasamos a Map
            List<Materia> materiasEnPlan = Materia.findBySQL(
                    "SELECT m.* FROM materia m " +
                            "INNER JOIN materias_planes mp ON m.id = mp.materia_id " +
                            "WHERE mp.plan_de_estudio_id = ?",
                    planId);

            List<Map<String, Object>> materiasEnPlanMap = new ArrayList<>();
            for (Materia m : materiasEnPlan) {
                materiasEnPlanMap.add(m.toMap());
            }
            model.put("materiasEnPlan", materiasEnPlanMap);

            // 3. Traemos las materias disponibles (que no están en este plan)
            List<Materia> todasLasMaterias = Materia.findAll();
            List<Map<String, Object>> materiasDisponiblesMap = new ArrayList<>();

            for (Materia m : todasLasMaterias) {
                boolean yaEsta = false;
                for (Materia mp : materiasEnPlan) {
                    if (m.getId().equals(mp.getId())) {
                        yaEsta = true;
                        break;
                    }
                }
                if (!yaEsta) {
                    materiasDisponiblesMap.add(m.toMap());
                }
            }
            model.put("materiasDisponibles", materiasDisponiblesMap);

            model.put("error", req.queryParams("error"));
            model.put("message", req.queryParams("message"));

            return new ModelAndView(model, "gestion/editar_planDeEstudio.mustache");
        }, new MustacheTemplateEngine());

        // 2. PROCESAR AGREGAR MATERIA E INCORPORAR SUS CORRELATIVAS
        post("/plan/edit/agregar-materia", (req, res) -> {
            String planId = req.queryParams("id"); 
            String materiaId = req.queryParams("materia_id");
            String horas = req.queryParams("horas");
            
            // LA SOLUCIÓN: Usar queryParamsValues en lugar de queryMap().values()
            // Esto devuelve un array de Strings, o "null" de forma segura si no se seleccionó nada.
            String[] correlativasIds = req.queryParamsValues("correlativas_ids");

            if (planId == null || materiaId == null || horas == null || horas.trim().isEmpty()) {
                res.redirect("/plan/edit?id=" + planId + "&error=Datos+incompletos");
                return "";
            }

            try {
                // Insertamos la relación de la materia con el plan junto a sus horas cátedra
                org.javalite.activejdbc.Base.exec(
                    "INSERT INTO materias_planes (plan_de_estudio_id, materia_id, horas) VALUES (?, ?, ?)",
                    planId, materiaId, horas
                );

                // Si seleccionó correlativas (es decir, el array no es null), las guardamos
                if (correlativasIds != null) {
                    for (String corrId : correlativasIds) {
                        if (corrId != null && !corrId.isEmpty()) {
                            org.javalite.activejdbc.Base.exec(
                                "INSERT INTO correlatividades (plan_de_estudio_id, materia_id, correlativa_id) VALUES (?, ?, ?)",
                                planId, materiaId, corrId
                            );
                        }
                    }
                }
        
                res.redirect("/plan/edit?id=" + planId + "&message=Materia+agregada+al+plan+exitosamente");
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/plan/edit?id=" + planId + "&error=Error+al+agregar+materia");
            }
            return "";
        });

        // 3. PROCESAR QUITAR MATERIA DEL PLAN
        post("/plan/edit/:id/quitar-materia", (req, res) -> {
            String planId = req.params(":id");
            String materiaId = req.queryParams("materia_id");

            try {
                // Primero limpiamos las correlatividades de este plan donde esta materia esté
                // involucrada
                // (Ya sea como materia principal o como requisito de otra)
                Base.exec(
                        "DELETE FROM correlatividades WHERE plan_de_estudio_id = ? AND (materia_id = ? OR correlativa_id = ?)",
                        planId, materiaId, materiaId);

                // Luego la desvinculamos del plan de estudios
                Base.exec(
                        "DELETE FROM materias_planes WHERE plan_de_estudio_id = ? AND materia_id = ?",
                        planId, materiaId);

                res.redirect("/plan/edit?id=" + planId + "&message=Materia+quitada+del+plan+exitosamente");
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/plan/edit?id=" + planId + "&error=Error+al+quitar+la+materia");
            }
            return "";
        });
    }
}