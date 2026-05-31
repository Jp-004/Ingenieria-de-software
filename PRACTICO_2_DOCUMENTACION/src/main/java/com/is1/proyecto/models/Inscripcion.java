package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("inscripcion")
public class Inscripcion extends Model {

    public String getFecha() {
        return getString("fecha");
    }

    public void setFecha(String fecha) {
        set("fecha", fecha);
    }

    public String getEstado() {
        return getString("estado");
    }

    public void setEstado(String estado) {
        set("estado", estado);
    }

    public double getNotaFinalCursada() {
        return getDouble("nota_final_cursada");
    }

    public void setNotaFinalCursada(double nota) {
        set("nota_final_cursada", nota);
    }

    public int getAlumnoId() {
        return getInteger("alumno_id");
    }

    public void setAlumnoId(int alumnoId) {
        set("alumno_id", alumnoId);
    }

    public int getMateriaId() {
        return getInteger("materia_id");
    }

    public void setMateriaId(int materiaId) {
        set("materia_id", materiaId);
    }
}
