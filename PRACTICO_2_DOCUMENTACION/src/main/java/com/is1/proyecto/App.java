package com.is1.proyecto;

import com.fasterxml.jackson.databind.ObjectMapper;
import static spark.Spark.*;

import org.javalite.activejdbc.Base;
import org.mindrot.jbcrypt.BCrypt;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import com.is1.proyecto.config.DBConfigSingleton;
import com.is1.proyecto.models.User;
import com.is1.proyecto.models.Profesor;
import com.is1.proyecto.models.Alumno;
import com.is1.proyecto.models.Materia;
import com.is1.proyecto.models.PlanDeEstudio;

public class App {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        port(8080);

        DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();

        before((req, res) -> {
            try {
                if (Base.hasConnection()) {
                    Base.close();
                }
                Base.open(dbConfig.getDriver(), dbConfig.getDbUrl(), dbConfig.getUser(), dbConfig.getPass());
                Base.exec("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, password TEXT NOT NULL, rango TEXT NOT NULL);");
                Base.exec("CREATE TABLE IF NOT EXISTS carrera (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, codigo TEXT NOT NULL UNIQUE);");
                Base.exec("CREATE TABLE IF NOT EXISTS plan_de_estudio (id INTEGER PRIMARY KEY AUTOINCREMENT, anio_vigencia INTEGER NOT NULL, activo INTEGER NOT NULL DEFAULT 1, carrera_id INTEGER NOT NULL);");
                Base.exec("CREATE TABLE IF NOT EXISTS materia (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, codigo TEXT NOT NULL UNIQUE, plan_de_estudio_id INTEGER, docente_id INTEGER, base_datos TEXT, horas INTEGER);");
                Base.exec("CREATE TABLE IF NOT EXISTS profesor (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, apellido TEXT NOT NULL, correo TEXT NOT NULL UNIQUE, dni TEXT NOT NULL UNIQUE);");
                Base.exec("CREATE TABLE IF NOT EXISTS alumno (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, apellido TEXT NOT NULL, correo TEXT NOT NULL UNIQUE, dni TEXT NOT NULL UNIQUE, legajo INTEGER NOT NULL UNIQUE, fecha_ingreso TEXT NOT NULL, estado_academico TEXT NOT NULL DEFAULT 'Activo', carrera_id INTEGER);");
            } catch (Exception e) {
                System.err.println("Error crítico en DB: " + e.getMessage());
                halt(500, "Error interno de conexión.");
            }
        });

        after((req, res) -> {
            try {
                if (Base.hasConnection()) {
                    Base.close();
                }
            } catch (Exception e) {
                System.err.println("Error cerrando conexión: " + e.getMessage());
            }
        });

        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            return new ModelAndView(model, "auth/login.mustache");
        }, new MustacheTemplateEngine());

        get("/user/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }
            return new ModelAndView(model, "auth/user_form.mustache");
        }, new MustacheTemplateEngine());

        get("/user/new", (req, res) -> {
            return new ModelAndView(new HashMap<>(), "auth/user_form.mustache");
        }, new MustacheTemplateEngine());

        get("/logout", (req, res) -> {
            req.session().invalidate();
            System.out.println("DEBUG: Sesión cerrada. Redirigiendo a /login.");
            res.redirect("/");
            return null;
        });

        get("/dashboard", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String currentUsername = req.session().attribute("currentUserUsername");
            Boolean loggedIn = req.session().attribute("loggedIn");

            if (currentUsername == null || loggedIn == null || !loggedIn) {
                res.redirect("/login?error=Debes iniciar sesión para acceder a esta página.");
                return null;
            }

            model.put("username", currentUsername);
            java.util.List<com.is1.proyecto.models.Alumno> listaAlumnos = com.is1.proyecto.models.Alumno.findAll();
            model.put("alumnos", listaAlumnos);

            return new ModelAndView(model, "admin/dashboard.mustache");
        }, new MustacheTemplateEngine());

        get("/alumno/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }
            return new ModelAndView(model, "admin/alumno_form.mustache");
        }, new MustacheTemplateEngine());

        get("/profesor/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }
            return new ModelAndView(model, "admin/profesor_form.mustache");
        }, new MustacheTemplateEngine());

        get("/gestion/carreras", (req, res) -> {
            Boolean loggedIn = req.session().attribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                res.redirect("/login?error=Debes iniciar sesión.");
                return null;
            }
            return new ModelAndView(new HashMap<>(), "gestion/gestion_carreras.mustache");
        }, new MustacheTemplateEngine());

        get("/carrera/list", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            java.util.List<com.is1.proyecto.models.Carrera> carreras = com.is1.proyecto.models.Carrera.findAll();
            model.put("carreras", carreras);

            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            return new ModelAndView(model, "gestion/carrera_list.mustache");
        }, new MustacheTemplateEngine());

        get("/plan/list", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            java.util.List<com.is1.proyecto.models.PlanDeEstudio> planes = com.is1.proyecto.models.PlanDeEstudio.findAll();

            for (com.is1.proyecto.models.PlanDeEstudio plan : planes) {
                com.is1.proyecto.models.Carrera carrera = com.is1.proyecto.models.Carrera.findById(plan.getCarreraId());
                if (carrera != null) {
                    plan.set("carrera_nombre", carrera.getNombre());
                }
            }

            model.put("planes", planes);

            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            return new ModelAndView(model, "gestion/plan_list.mustache");
        }, new MustacheTemplateEngine());

        get("/materia/list", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            java.util.List<com.is1.proyecto.models.Materia> materias = com.is1.proyecto.models.Materia.findAll();

            for (com.is1.proyecto.models.Materia materia : materias) {
                int planId = materia.getPlanDeEstudioId();
                if (planId > 0) {
                    com.is1.proyecto.models.PlanDeEstudio plan = com.is1.proyecto.models.PlanDeEstudio.findById(planId);
                    if (plan != null) {
                        materia.set("plan_de_estudio_nombre", "Plan " + plan.getAnioVigencia());
                    }
                }
            }

            model.put("materias", materias);

            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            return new ModelAndView(model, "gestion/materia_list.mustache");
        }, new MustacheTemplateEngine());

        get("/materias/panel-gestion", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            Boolean loggedIn = req.session().attribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                res.redirect("/?error=Debes iniciar sesion.");
                return null;
            }
            model.put("materias", Materia.findAll());
            return new ModelAndView(model, "gestion/gestionar_materia.mustache");
        }, new MustacheTemplateEngine());

        get("/materias/nueva", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            Boolean loggedIn = req.session().attribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                res.redirect("/?error=Debes iniciar sesion.");
                return null;
            }
            model.put("planes", com.is1.proyecto.models.PlanDeEstudio.findAll());
            model.put("docentes", com.is1.proyecto.models.Profesor.findAll());
            return new ModelAndView(model, "gestion/alta_materia.mustache");
        }, new MustacheTemplateEngine());

        post("/user/new", (req, res) -> {
            String name = req.queryParams("name");
            String password = req.queryParams("password");
            String codigoAdmin = req.queryParams("codigoAdmin");

            if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
                res.status(400);
                res.redirect("/user/create?error=Nombre y contraseña son requeridos.");
                return "";
            }

            if (codigoAdmin == null || !codigoAdmin.equals("ADMIN2026")) {
                res.redirect("/user/create?error=Codigo de administrador incorrecto.");
                return "";
            }

            try {
                User ac = new User();
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                ac.set("name", name);
                ac.set("password", hashedPassword);
                ac.set("rango", "Admin");
                ac.saveIt();

                res.status(201);
                res.redirect("/?message=Cuenta creada exitosamente para " + name + ". Ya puede iniciar sesion.");
                return "";

            } catch (Exception e) {
                System.err.println("Error al registrar la cuenta: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                res.redirect("/user/create?error=Error interno al crear la cuenta. Intente de nuevo.");
                return "";
            }
        });

        post("/login", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String username = req.queryParams("username");
            String plainTextPassword = req.queryParams("password");

            if (username == null || username.isEmpty() || plainTextPassword == null || plainTextPassword.isEmpty()) {
                res.status(400);
                model.put("errorMessage", "El nombre de usuario y la contraseña son requeridos.");
                return new ModelAndView(model, "auth/login.mustache");
            }

            User ac = User.findFirst("name = ?", username);

            if (ac == null) {
                res.status(401);
                model.put("errorMessage", "Usuario o contraseña incorrectos.");
                return new ModelAndView(model, "auth/login.mustache");
            }

            String storedHashedPassword = ac.getString("password");

            if (BCrypt.checkpw(plainTextPassword, storedHashedPassword)) {
                res.status(200);
                req.session(true).attribute("currentUserUsername", username);
                req.session().attribute("userId", ac.getId());
                req.session().attribute("loggedIn", true);
                req.session().attribute("rango", ac.getRango());

                String rango = ac.getRango();
                model.put("username", username);

                if ("Admin".equals(rango)) {
                    return new ModelAndView(model, "admin/dashboard.mustache");
                } else if ("Profesor".equals(rango)) {
                    return new ModelAndView(model, "profesor/dashboard_profesor.mustache");
                } else if ("Alumno".equals(rango)) {
                    return new ModelAndView(model, "alumno/dashboard_alumno.mustache");
                } else {
                    model.put("errorMessage", "Rango de usuario no reconocido.");
                    return new ModelAndView(model, "auth/login.mustache");
                }
            } else {
                res.status(401);
                model.put("errorMessage", "Usuario o contraseña incorrectos.");
                return new ModelAndView(model, "auth/login.mustache");
            }
        }, new MustacheTemplateEngine());

        post("/add_users", (req, res) -> {
            res.type("application/json");
            String name = req.queryParams("name");
            String password = req.queryParams("password");

            if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
                res.status(400);
                return objectMapper.writeValueAsString(Map.of("error", "Nombre y contraseña son requeridos."));
            }

            try {
                User newUser = new User();
                newUser.set("name", name);
                newUser.set("password", password);
                newUser.saveIt();

                res.status(201);
                return objectMapper.writeValueAsString(
                        Map.of("message", "Usuario '" + name + "' registrado con éxito.", "id", newUser.getId()));

            } catch (Exception e) {
                System.err.println("Error al registrar usuario: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return objectMapper
                        .writeValueAsString(Map.of("error", "Error interno al registrar usuario: " + e.getMessage()));
            }
        });

        post("/alumno/create", (req, res) -> {
            String nombre = req.queryParams("nombre");
            String apellido = req.queryParams("apellido");
            String correo = req.queryParams("correo");
            String dni = req.queryParams("dni");
            String redirectUrl = "/alumno/create";

            if (nombre == null || nombre.isEmpty() || apellido == null || apellido.isEmpty() ||
                    correo == null || correo.isEmpty() || dni == null || dni.isEmpty()) {
                res.redirect(redirectUrl + "?error=Todos+los+campos+son+obligatorios");
                return "";
            }

            if (!correo.contains("@") || !correo.contains(".")) {
                res.redirect(redirectUrl + "?error=El+formato+del+correo+electronico+no+es+valido");
                return "";
            }

            try {
                boolean correoExiste = Alumno.findFirst("correo = ?", correo) != null;
                if (correoExiste) {
                    res.redirect(redirectUrl + "?error=El+correo+electronico+ya+existe+en+la+base+de+datos");
                    return "";
                }

                boolean dniExiste = Alumno.findFirst("dni = ?", dni) != null;
                if (dniExiste) {
                    res.redirect(redirectUrl + "?error=El+DNI+ya+existe+en+la+base+de+datos");
                    return "";
                }

                int legajo = Alumno.count().intValue() + 1;
                String fechaIngreso = java.time.LocalDate.now().toString();

                Alumno nuevoAlumno = new Alumno();
                nuevoAlumno.setNombre(nombre);
                nuevoAlumno.setApellido(apellido);
                nuevoAlumno.setCorreo(correo);
                nuevoAlumno.setDNI(dni);
                nuevoAlumno.setLegajo(legajo);
                nuevoAlumno.setFechaIngreso(fechaIngreso);
                nuevoAlumno.setEstadoAcademico("Activo");
                nuevoAlumno.saveIt();

                User nuevoUser = new User();
                nuevoUser.setName(correo);
                nuevoUser.setPassword(BCrypt.hashpw(dni, BCrypt.gensalt()));
                nuevoUser.setRango("Alumno");
                nuevoUser.saveIt();

                res.status(201);
                res.redirect(redirectUrl + "?message=Alumno+registrado+exitosamente");
                return "";

            } catch (Exception e) {
                System.err.println("Error al registrar al alumno: " + e.getMessage());
                e.printStackTrace();
                res.redirect(redirectUrl + "?error=Error+interno+al+guardar");
                return "";
            }
        });

        post("/profesor/create", (req, res) -> {
            String nombre = req.queryParams("nombre");
            String apellido = req.queryParams("apellido");
            String correo = req.queryParams("correo");
            String dni = req.queryParams("dni");
            String redirectUrl = "/profesor/create";

            if (nombre == null || nombre.isEmpty() || apellido == null || apellido.isEmpty() ||
                    correo == null || correo.isEmpty() || dni == null || dni.isEmpty()) {
                res.redirect(redirectUrl + "?error=Todos+los+campos+son+obligatorios");
                return "";
            }

            if (!correo.contains("@") || !correo.contains(".")) {
                res.redirect(redirectUrl + "?error=El+formato+del+correo+electronico+no+es+valido");
                return "";
            }

            try {
                boolean correoExiste = Profesor.findFirst("correo = ?", correo) != null;
                if (correoExiste) {
                    res.redirect(redirectUrl + "?error=El+correo+electronico+ya+existe+en+la+base+de+datos");
                    return "";
                }

                boolean dniExiste = Profesor.findFirst("dni = ?", dni) != null;
                if (dniExiste) {
                    res.redirect(redirectUrl + "?error=El+DNI+ya+existe+en+la+base+de+datos");
                    return "";
                }

                Profesor nuevoProfesor = new Profesor();
                nuevoProfesor.setNombre(nombre);
                nuevoProfesor.setApellido(apellido);
                nuevoProfesor.setCorreo(correo);
                nuevoProfesor.setDNI(dni);
                nuevoProfesor.saveIt();

                User nuevoUser = new User();
                nuevoUser.setName(correo);
                nuevoUser.setPassword(BCrypt.hashpw(dni, BCrypt.gensalt()));
                nuevoUser.setRango("Profesor");
                nuevoUser.saveIt();

                res.status(201);
                res.redirect(redirectUrl + "?message=Profesor+registrado+exitosamente");
                return "";

            } catch (Exception e) {
                System.err.println("Error al registrar al profesor: " + e.getMessage());
                e.printStackTrace();
                res.redirect(redirectUrl + "?error=Error+interno+al+guardar");
                return "";
            }
        });

        post("/materias/guardar-completa", (req, res) -> {
            try {
                String nombre = req.queryParams("nombre");
                String codigo = req.queryParams("codigo");
                String planId = req.queryParams("plan_de_estudio_id");
                String horas = req.queryParams("horas");
                String docenteId = req.queryParams("docente_id");

                Materia nueva = new Materia();
                nueva.set("nombre", nombre);
                nueva.set("codigo", codigo);
                nueva.set("horas", Integer.parseInt(horas));

                if (planId != null && !planId.isEmpty()) {
                    nueva.set("plan_de_estudio_id", Integer.parseInt(planId));
                }
                if (docenteId != null && !docenteId.isEmpty()) {
                    nueva.set("docente_id", Integer.parseInt(docenteId));
                }
                nueva.saveIt();

                res.redirect("/materias/panel-gestion");
                return null;
            } catch (Exception e) {
                System.err.println("Error al insertar materia completa: " + e.getMessage());
                res.redirect("/materias/panel-gestion?error=Error al registrar la asignatura.");
                return null;
            }
        });

        post("/materias/eliminar", (req, res) -> {
            try {
                String materiaId = req.queryParams("materia_id");
                if (materiaId != null && !materiaId.isEmpty()) {
                    Materia m = Materia.findById(Integer.parseInt(materiaId));
                    if (m != null) {
                        m.delete();
                    }
                }
                res.redirect("/materias/panel-gestion");
                return null;
            } catch (Exception e) {
                System.err.println("Error al eliminar materia: " + e.getMessage());
                res.redirect("/materias/panel-gestion?error=No se pudo eliminar.");
                return null;
            }
        });
    }
}