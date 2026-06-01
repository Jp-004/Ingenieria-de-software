# INGENIERIA EN SOFTWARE II [3387]

### Problema que se quiere resolver: 
El objetivo es desarrollar una herramienta que centralice y organice la información académica mediante una plataforma integrada. Esta permitirá registrar, modificar y consultar datos de alumnos, profesores, carreras y de la institución, con el fin de facilitar el seguimiento académico y ofrecer una interfaz intuitiva para las actividades diarias del personal.

### Funcionalidades principales:
Módulo de Gestión Académica: Administración de carreras, planes de estudio, materias y sus correlatividades.
Módulo de Usuarios: Gestión integral de perfiles (Alumnos, Docentes y Usuarios.
Seguimiento Estudiantil: Consulta de notas, listado de alumnos por materia y gestión de la situación académica general del estudiante.
Módulo de Comunicación: Sistema de notificaciones/alertas.

## Cómo ejecutar el proyecto

El proyecto cuenta con scripts automatizados para compilar el código fuente y levantar el servidor local sin necesidad de escribir los comandos de Maven y Java a mano cada vez.

### Requisitos previos
Para poder ejecutar el proyecto, asegúrate de tener instalado:
* **Java** (JDK)
* **Maven** (agregado al PATH)
* **SQLite** ---

### Para usuarios de Windows

Puedes iniciar el proyecto haciendo **doble clic** en los archivos `.bat` desde el explorador de archivos, o ejecutándolos desde tu consola.

*  **`iniciar.bat` (Uso diario):** Compila los últimos cambios del código y levanta el servidor en el puerto 8080. **Mantiene intacta la base de datos** con todos los registros actuales.
*  **`reiniciar.bat` (Limpieza total):** Borra la base de datos actual, recrea las tablas desde cero usando `scheme.sql`, compila el proyecto y levanta el servidor. Útil si se rompen los datos de prueba o si se modificó el esquema de la base de datos.

---

### Para usuarios de Linux / Ubuntu

Abre una terminal en la carpeta raíz del proyecto y ejecuta los scripts `.sh`. 

* **`Iniciar el servidor` (Mantiene los datos): ./iniciar.sh
* *`*Reiniciar el servidor` (Borra los datos): ./reiniciar.sh
