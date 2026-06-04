package com.is1.proyecto;

// Importaciones necesarias para la aplicación Spark
import java.util.HashMap; // Utilidad para serializar/deserializar objetos Java a/desde JSON.
import java.util.Map; // Importa los métodos estáticos principales de Spark (get, post, before, after, etc.).

import org.javalite.activejdbc.Base; // Clase central de ActiveJDBC para gestionar la conexión a la base de datos.
import org.mindrot.jbcrypt.BCrypt; // Utilidad para hashear y verificar contraseñas de forma segura.

import com.fasterxml.jackson.databind.ObjectMapper; // Representa un modelo de datos y el nombre de la vista a renderizar.
import com.is1.proyecto.config.DBConfigSingleton; // Motor de plantillas Mustache para Spark.
import com.is1.proyecto.models.Alumno; // Para crear mapas de datos (modelos para las plantillas).
import com.is1.proyecto.models.Materia; // Interfaz Map, utilizada para Map.of() o HashMap.
import com.is1.proyecto.models.Profesor; // Clase Singleton para la configuración de la base de datos.
import com.is1.proyecto.models.User; // Modelo de ActiveJDBC que representa la tabla 'users'.

import spark.ModelAndView; // <--- AGREGAR ESTO
import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.port;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

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

        get("/plan/list", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            java.util.List<com.is1.proyecto.models.PlanDeEstudio> planes = com.is1.proyecto.models.PlanDeEstudio.findAll();

            // Creamos una lista de Mapas flexibles para no enojar a ActiveJDBC
            java.util.List<Map<String, Object>> planesVista = new java.util.ArrayList<>();

            for (com.is1.proyecto.models.PlanDeEstudio plan : planes) {
                // Convertimos el modelo estricto a un mapa flexible
                Map<String, Object> planMap = new HashMap<>(plan.toMap());
                
                com.is1.proyecto.models.Carrera carrera = com.is1.proyecto.models.Carrera.findById(plan.getCarreraId());
                if (carrera != null) {
                    // Ahora podemos inyectar un dato inventado sin problemas
                    planMap.put("carrera_nombre", carrera.getNombre());
                } else {
                    planMap.put("carrera_nombre", "Sin carrera");
                }
                
                // Agregamos el mapa a nuestra nueva lista
                planesVista.add(planMap);
            }

            // Pasamos nuestra nueva lista flexible a la vista
            model.put("planes", planesVista);

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

        // ==================== CARRERA - LISTAR ====================
        get("/carrera/list", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            
            // Busca todas las carreras en la base de datos
            java.util.List<com.is1.proyecto.models.Carrera> carreras = com.is1.proyecto.models.Carrera.findAll();
            model.put("carreras", carreras);

            // Atrapa el mensaje de éxito (cuando creas o editas una carrera)
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            
            // Atrapa mensajes de error (cuando intentas borrar una carrera con planes, por ejemplo)
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            // Renderiza la vista (Nota: sin la carpeta "gestion/" para que la encuentre bien)
            return new ModelAndView(model, "gestion/carrera_list.mustache");
        }, new MustacheTemplateEngine());
        
        // ==================== CARRERA - CREAR ====================
        // Muestra el formulario para crear una nueva carrera
        get("/carrera/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            // Manejo de mensajes de error o éxito pasados por URL
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            return new ModelAndView(model, "gestion/carrera_form.mustache");
        }, new MustacheTemplateEngine());

        // Procesa los datos del formulario y los guarda en SQLite usando ActiveJDBC
        post("/carrera/create", (req, res) -> {
            String nombre = req.queryParams("nombre");
            String codigo = req.queryParams("codigo");
            String redirectUrl = "/carrera/create";

            // Validaciones básicas de campos vacíos
            if (nombre == null || nombre.trim().isEmpty() || codigo == null || codigo.trim().isEmpty()) {
                res.redirect(redirectUrl + "?error=Todos+los+campos+son+obligatorios");
                return "";
            }

            try {
                // Validación para evitar códigos duplicados
                boolean codigoExiste = com.is1.proyecto.models.Carrera.findFirst("codigo = ?", codigo) != null;
                if (codigoExiste) {
                    res.redirect(redirectUrl + "?error=El+codigo+de+carrera+ya+existe");
                    return "";
                }

                // Guardado en la base de datos
                com.is1.proyecto.models.Carrera nuevaCarrera = new com.is1.proyecto.models.Carrera();
                nuevaCarrera.setNombre(nombre);
                nuevaCarrera.setCodigo(codigo);
                nuevaCarrera.saveIt(); // ActiveJDBC hace el INSERT de manera automática

                // Redirección exitosa al listado de carreras
                res.status(201);
                res.redirect("/carrera/list?message=Carrera+creada+exitosamente");
                return "";

            } catch (Exception e) {
                System.err.println("Error al registrar la carrera: " + e.getMessage());
                e.printStackTrace();
                res.redirect(redirectUrl + "?error=Error+interno+al+guardar+la+carrera");
                return "";
            }
        });

        // ==================== CARRERA - MODIFICAR ====================
        // Muestra el formulario con los datos cargados de la carrera a editar
        get("/carrera/edit", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String idStr = req.queryParams("id");

            if (idStr == null || idStr.isEmpty()) {
                res.redirect("/carrera/list?error=ID+de+carrera+no+proporcionado");
                return null;
            }

            com.is1.proyecto.models.Carrera carrera = com.is1.proyecto.models.Carrera.findById(idStr);
            if (carrera == null) {
                res.redirect("/carrera/list?error=Carrera+no+encontrada");
                return null;
            }

            model.put("carrera", carrera);
            
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            return new ModelAndView(model, "gestion/carrera_edit.mustache");
        }, new MustacheTemplateEngine());

        // Procesa los cambios y los guarda en la base de datos
        post("/carrera/edit", (req, res) -> {
            String idStr = req.queryParams("id");
            String nombre = req.queryParams("nombre");
            String codigo = req.queryParams("codigo");

            if (idStr == null || nombre == null || codigo == null || nombre.trim().isEmpty() || codigo.trim().isEmpty()) {
                res.redirect("/carrera/edit?id=" + idStr + "&error=Todos+los+campos+son+obligatorios");
                return "";
            }

            try {
                com.is1.proyecto.models.Carrera carrera = com.is1.proyecto.models.Carrera.findById(idStr);
                if (carrera != null) {
                    // Validar que el nuevo código no le pertenezca ya a otra carrera distinta
                    com.is1.proyecto.models.Carrera carreraExistente = com.is1.proyecto.models.Carrera.findFirst("codigo = ?", codigo);
                    if (carreraExistente != null && !carreraExistente.getId().toString().equals(idStr)) {
                         res.redirect("/carrera/edit?id=" + idStr + "&error=El+codigo+ya+esta+en+uso+por+otra+carrera");
                         return "";
                    }

                    carrera.setNombre(nombre);
                    carrera.setCodigo(codigo);
                    carrera.saveIt();
                    res.redirect("/carrera/list?message=Carrera+actualizada+exitosamente");
                } else {
                    res.redirect("/carrera/list?error=Carrera+no+encontrada");
                }
            } catch (Exception e) {
                System.err.println("Error al actualizar la carrera: " + e.getMessage());
                res.redirect("/carrera/edit?id=" + idStr + "&error=Error+interno+al+actualizar");
            }
            return "";
        });

        // ==================== CARRERA - ELIMINAR ====================
        // Elimina la carrera seleccionada (siempre y cuando no tenga planes asociados)
        get("/carrera/delete", (req, res) -> {
            String idStr = req.queryParams("id");
            if (idStr != null && !idStr.isEmpty()) {
                try {
                    com.is1.proyecto.models.Carrera carrera = com.is1.proyecto.models.Carrera.findById(idStr);
                    if (carrera != null) {
                        // Regla de negocio: No permitir eliminar si hay Planes de Estudio asociados
                        long planesAsociados = com.is1.proyecto.models.PlanDeEstudio.count("carrera_id = ?", idStr);
                        if (planesAsociados > 0) {
                             res.redirect("/carrera/list?error=No+se+puede+eliminar+la+carrera+porque+tiene+planes+de+estudio+activos");
                             return null;
                        }

                        carrera.delete();
                        res.redirect("/carrera/list?message=Carrera+eliminada+exitosamente");
                    } else {
                        res.redirect("/carrera/list?error=Carrera+no+encontrada");
                    }
                } catch (Exception e) {
                    System.err.println("Error al eliminar la carrera: " + e.getMessage());
                    res.redirect("/carrera/list?error=Error+al+eliminar+la+carrera");
                }
            }
            return null;
        });

        // ==================== PLAN DE ESTUDIO - CREAR ====================
        
        // Mostrar el formulario
        get("/plan/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            // Necesitamos enviar todas las carreras a la vista para armar el menú desplegable (Select)
            java.util.List<com.is1.proyecto.models.Carrera> carreras = com.is1.proyecto.models.Carrera.findAll();
            model.put("carreras", carreras);

            // Manejo de mensajes de error
            String errorKey = req.queryParams("error");
            if (errorKey != null && !errorKey.isEmpty()) {
                String message = "Ocurrió un error inesperado.";
                
                switch (errorKey) {
                    case "empty_fields":
                        message = "El año de vigencia y la carrera son obligatorios.";
                        break;
                    case "invalid_range":
                        message = "El año de vigencia no es válido (debe ser entre 1900 y 2100).";
                        break;
                    case "duplicate":
                        message = "Ya existe un plan de estudio para esa carrera en ese año.";
                        break;
                    case "not_a_number":
                        message = "El año de vigencia debe ser un número válido.";
                        break;
                    case "internal_error":
                        message = "Error interno del sistema al intentar guardar el plan.";
                        break;
                }
                model.put("errorMessage", message);
            }

            return new ModelAndView(model, "gestion/plan_form.mustache"); 
        }, new MustacheTemplateEngine());

        // Procesar los datos y guardar
        post("/plan/create", (req, res) -> {
            String anioVigencia = req.queryParams("anio_vigencia");
            String carreraId = req.queryParams("carrera_id");
            String activoStr = req.queryParams("activo"); 
            
            // Si el checkbox está marcado, Spark recibe "on". Si no, recibe null.
            int activo = (activoStr != null && activoStr.equals("on")) ? 1 : 0;
            
            String redirectUrl = "/plan/create";

            // Validaciones básicas
            if (anioVigencia == null || anioVigencia.trim().isEmpty() || 
                carreraId == null || carreraId.trim().isEmpty()) {
                res.redirect(redirectUrl + "?error=El+anio+de+vigencia+y+la+carrera+son+obligatorios");
                return "";
            }

            try {
                int anio = Integer.parseInt(anioVigencia);
                int idCarrera = Integer.parseInt(carreraId);

                if (anio < 1900 || anio > 2100) {
                    res.redirect(redirectUrl + "?error=invalid_range");
                    return "";
                }

                // Buscamos si ya existe un plan con esa carrera Y ese mismo año
                com.is1.proyecto.models.PlanDeEstudio planExistente = com.is1.proyecto.models.PlanDeEstudio.findFirst(
                    "carrera_id = ? AND anio_vigencia = ?", 
                    idCarrera, 
                    anio
                );

                if (planExistente != null) {
                    // Si encontramos uno, frenamos todo y devolvemos un error a la vista
                    res.redirect(redirectUrl + "?error=Ya+existe+un+plan+de+estudio+para+esa+carrera+en+ese+anio");
                    return "";
                }
                // Instanciamos y guardamos el nuevo plan usando ActiveJDBC
                com.is1.proyecto.models.PlanDeEstudio nuevoPlan = new com.is1.proyecto.models.PlanDeEstudio();
                nuevoPlan.set("anio_vigencia", Integer.parseInt(anioVigencia));
                nuevoPlan.set("carrera_id", Integer.parseInt(carreraId));
                nuevoPlan.set("activo", activo);
                nuevoPlan.saveIt();

                res.status(201);
                res.redirect("/plan/list?message=Plan+de+estudio+creado+exitosamente");
                return "";

            } catch (NumberFormatException e) {
                res.redirect(redirectUrl + "?error=El+anio+de+vigencia+debe+ser+un+numero+valido");
                return "";
            } catch (Exception e) {
                System.err.println("Error al registrar el plan: " + e.getMessage());
                res.redirect(redirectUrl + "?error=Error+interno+al+guardar+el+plan");
                return "";
            }
        });
    } // Fin del método main
} // Fin de la clase App 