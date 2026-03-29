# Ingenieria-de-software 
# Proyecto integrador: Especificación, Gestión y planificación

## 1. (Requirements) Describir su proyecto

* **Problema que se quiere resolver:** El problema que se quiere resolver es disponer de una herramiento que centralice y organice la información académica. Brindar una plataforma integrada que permita registrar, consultar y registrar datos de alumnos y profesores, gestionar carreras y facilitar el seguimiento académico.
* **Usuarios del sistema:** Oficina de alumnos: gestionar los datos de la institución, carreras, materias, inscripciones y correlatividades
Estudiantes: pueden consultar su información académica, visualizar notas e inscribirse a materias
Profesores: acceden para registrar las calificaciones y consultar los listados de los alumnos inscriptos en las materias que dictan
* **Funcionalidades principales:** 
 **Módulo de Gestión Académica:** Administración de carreras, planes de estudio, materias y sus correlatividades.
	* **Módulo de Usuarios:** Gestión integral de perfiles (Alumnos, Docentes y Usuarios.
	* **Seguimiento Estudiantil:** Consulta de notas, listado de alumnos por materia y gestión de la situación académica general del estudiante.
	* **Módulo de Comunicación:** Sistema de notificaciones/alertas, foros de noticias y chat integrado para el intercambio de mensajes entre docentes y estudiantes.
* **Restricciones técnicas:**
  	- Disponibilidad y concurrencia: el sistema tiene que estar disponible en todo momento (24/7) y soportar múltiples accesos simultáneos sin deteriorar el rendimiento.
	- Seguridad: Autenticación mediante usuarios y contraseña, con un flujo verídico para la recuperación de credenciales.
	- Usabilidad: Debe contar con una interfaz gráfica amigable e interactiva.

* **Tamaño del equipo:** El equipo cuenta con 4 integrantes. 
Irusta, Juan Ignacio.
Maccio, Facundo Damian.
Pereno, Juan Cruz.
Trabucco, Esteban Daniel.

* **Tecnologías elegidas y justificación:** Se hace uso de Java, lenguaje conocido por su robustez en sistemas de gestión, junto con una base de datos relacional para garantizar la integridad de las correlatividades y notas. 
* **Plazo estimado:** El desarrollo está estructurado para evolucionar a lo largo de 7 prácticas de la asignatura, cubriendo de requerimientos hasta pruebas.
* **Cambios de alcance ocurridos:** * 
* **Problemas encontrados:** * [
* **Forma de organización del equipo:** * Trabajo colaborativo mediante repositorios de GitHub, con una estructura de carpetas definidas para la documentación y el código, asegurando la trazabilidad de los cambios. 

---

## 2. (Auditoría) Análisis de riesgos con IA

### a) Riesgos identificados por el LLM
Prompt: Actúa como un auditor de proyectos de software. A partir de la siguiente descripción Problema que se quiere resolver: El problema que se quiere resolver es disponer de una herramiento que centralice y organice la información académica. Brindar una plataforma integrada que permita registrar, consultar y registrar datos de alumnos y profesores, gestionar carreras y facilitar el seguimiento académico. Clasifícalos por probabilidad e impacto.

Pasamos el prompt predispuesto por la consigna a la asistente de inteligencia artificial generativa junto con el readme.md establecido hasta el momento, para poder comprender el contexto en el cual estamos trabajando. El LLM utilizado por nosotros para realizar la pregunta fue Claude.

| Tipo de Riesgo | Descripción | Probabilidad | Impacto |
|---|---|---|---|
| **Técnico** | Curva de aprendizaje en nuevas tecnologías del stack (ej: frameworks de backend/frontend no utilizados antes por el equipo). | Media | Alto |
| **Técnico** | Complejidad en la lógica de correlatividades: validar cadenas de prereqs puede derivar en bugs difíciles de testear. | Alta | Alto |
| **Técnico** | Integración entre módulos (alumnos, materias, inscripciones) con posibles inconsistencias de datos si no se definen contratos de API claros. | Media | Alto |
| **Técnico** | Falta de cobertura de tests en funcionalidades críticas (inscripciones, notas), generando regresiones silenciosas. | Alta | Crítico |
| **Técnico** | Deuda técnica acumulada por presión de entrega, dificultando el mantenimiento y extensión del sistema. | Alta | Medio |
| **Técnico** | Seguridad insuficiente en la gestión de sesiones o validación de roles (ej: alumno accediendo a datos de otro alumno). | Media | Crítico |
| **Organizacional** | Desalineación entre miembros sobre prioridad de tareas del backlog, generando trabajo duplicado o conflictos de merge. | Baja | Medio |
| **Organizacional** | Ausencia de un proceso claro de revisión de código (code review), reduciendo la calidad y la transferencia de conocimiento. | Media | Medio |
| **Organizacional** | Dependencia de un único integrante con conocimiento crítico de un módulo (bus factor = 1). | Media | Alto |
| **Organizacional** | Cambios de alcance no gestionados formalmente que alteran el backlog sin actualizar estimaciones ni roadmap. | Alta | Alto |
| **Planificación** | Desviación en la estimación de historias complejas (ej: gestión de correlatividades, situación académica), subestimando el esfuerzo real. | Alta | Crítico |
| **Planificación** | Falta de criterios de aceptación claros al inicio puede generar retrabajo al momento de validar las historias. | Media | Alto |
| **Planificación** | No actualización del burndown chart ni del roadmap, impidiendo detectar desvíos a tiempo. | Alta | Medio |
| **Planificación** | Dependencias no identificadas entre historias (ej: Gestionar Inscripción depende de Gestionar Materia y Gestionar Alumno), bloqueando sprints. | Media | Alto |
| **Humano** | Falta de disponibilidad de un integrante clave en período de parciales o exámenes, afectando la velocidad del equipo. | Alta | Crítico |
| **Humano** | Diferencias en el nivel técnico entre integrantes que generan cuellos de botella en tareas avanzadas. | Media | Medio |
| **Humano** | Baja motivación o fatiga al avanzar el cuatrimestre, especialmente en etapas de integración y testing. | Media | Medio |
| **Humano** | Conflictos interpersonales no resueltos que impactan en la comunicación y la toma de decisiones del equipo. | Baja | Alto |


### b) Riesgos identificados manualmente por el equipo
| Tipo de Riesgo | Descripción | Probabilidad | Impacto | Identificado por |
|---|---|---|---|---|
| **Técnico** | Conocimiento del uso de herramientas necesarias. | Media | Alto | Facundo Maccio |
| **Planificación** | Disponibilidad horaria de los integrantes. | Alta | Crítico | Juan Cruz Pereno |
| **Planificación** | Falta de integrantes a futuro. | Baja | Alto | Esteban Trabucco |
| **Planificación** | Orden de prioridad ante las materias | Alta | Alto | Juan Irusta |
| **Organizacional** | Incumplimiento de tiempo estimado de trabajo | Media | Crítico | Juan Cruz Pereno |
| **Técnico** | Carencia de hardware necesario | Baja | Bajo | Juan Irusta |
| **Humano** | Capacidades técnicas de cada integrante | Media | Alta | Facundo Maccio |
| **Humano** | Falta de motivación, o carencia de estado de ánimo por problema interpersonal | Medio | Medio | Facundo Maccio | 
| **Tecnico** | Falta de test | Medio | Alto | Esteban Trabucco |


### c) Comparación de ambos análisis

* **Riesgos que encontró la IA y el equipo no:**
Riesgos técnicos específicos de arquitectura y lógica.
Fallas de buenas prácticas.	
Gestión del proyecto.

* **Riesgos que encontró el equipo y la IA no:**
	Falta de disponibilidad horaria.
	Priorizar otras materias sobre el proyecto de ingeniería en software II. 
	Posibilidad de no contar con el hardware necesario.
	Riesgos más enfocados al contexto universitario.

* **Calidad del análisis:**
	Fue útil para dar una claridad con respecto a los tipos de riesgo técnicos mientras que nosotros identificamos más riesgos con respecto a lo humano o contexto universitario.

