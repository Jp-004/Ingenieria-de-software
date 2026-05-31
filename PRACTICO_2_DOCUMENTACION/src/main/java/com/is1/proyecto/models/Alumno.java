package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("alumno")
public class Alumno extends Model {

    public String getNombre() {
        return getString("nombre");
    }

    public void setNombre(String nombre) {
        set("nombre", nombre);
    }

    public String getApellido() {
        return getString("apellido");
    }

    public void setApellido(String apellido) {
        set("apellido", apellido);
    }

    public String getCorreo() {
        return getString("correo");
    }

    public void setCorreo(String correo) {
        set("correo", correo);
    }

    public String getDNI() {
        return getString("dni");
    }

    public void setDNI(String dni) {
        set("dni", dni);
    }

    public int getLegajo() {
        return getInteger("legajo");
    }

    public void setLegajo(int legajo) {
        set("legajo", legajo);
    }

    public String getFechaIngreso() {
        return getString("fecha_ingreso");
    }

    public void setFechaIngreso(String fechaIngreso) {
        set("fecha_ingreso", fechaIngreso);
    }

    public String getEstadoAcademico() {
        return getString("estado_academico");
    }

    public void setEstadoAcademico(String estado) {
        set("estado_academico", estado);
    }

    public int getCarreraId() {
        return getInteger("carrera_id");
    }

    public void setCarreraId(int carreraId) {
        set("carrera_id", carreraId);
    }
}