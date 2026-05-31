package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("materia")
public class Materia extends Model {

    public String getNombre() {
        return getString("nombre");
    }

    public void setNombre(String nombre) {
        set("nombre", nombre);
    }

    public String getCodigo() {
        return getString("codigo");
    }

    public void setCodigo(String codigo) {
        set("codigo", codigo);
    }

    public int getPlanDeEstudioId() {
        return getInteger("plan_de_estudio_id");
    }

    public void setPlanDeEstudioId(int planId) {
        set("plan_de_estudio_id", planId);
    }
}
