package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("plan_de_estudio")
public class PlanDeEstudio extends Model {

    public int getAnioVigencia() {
        return getInteger("anio_vigencia");
    }

    public void setAnioVigencia(int anio) {
        set("anio_vigencia", anio);
    }

    public boolean isActivo() {
        return getInteger("activo") == 1;
    }

    public void setActivo(boolean activo) {
        set("activo", activo ? 1 : 0);
    }

    public int getCarreraId() {
        return getInteger("carrera_id");
    }

    public void setCarreraId(int carreraId) {
        set("carrera_id", carreraId);
    }
}
