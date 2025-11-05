package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("profesor") // Esta anotación conecta la clase con la tabla 'profesor' de la BD
public class Profesor extends Model {

    // ActiveJDBC maneja automáticamente el mapeo de columnas.
    // Agregamos setters para seguir el estilo de User.java y 
    // facilitar la creación en el controlador.

    public void setNombre(String nombre) {
        set("nombre", nombre);
    }

    public void setApellido(String apellido) {
        set("apellido", apellido);
    }

    public void setCorreo(String correo) {
        set("correo", correo);
    }

    public void setDNI(String dni) {
        set("dni", dni);
    }

    // También puedes agregar Getters si los necesitas
    public String getNombre() {
        return getString("nombre");
    }

    public String getApellido() {
        return getString("apellido");
    }
    
    public String getCorreo() {
        return getString("correo");
    }

    public String getDNI() {
        return getString("dni");
    }
}