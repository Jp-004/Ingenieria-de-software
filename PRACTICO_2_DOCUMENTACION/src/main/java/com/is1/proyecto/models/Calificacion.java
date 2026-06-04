package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("calificaciones")
public class Calificacion extends Model {

    static {
        validatePresenceOf("alumno_id", "materia_id", "instancia", "nota");
        validateNumericalityOf("nota");
    }
}
