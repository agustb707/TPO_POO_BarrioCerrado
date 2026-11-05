package Enums;

import java.time.DayOfWeek;

public enum DiaSemana {
    LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO;

    public static DiaSemana desde(DayOfWeek dia) {
        return values()[dia.getValue() - 1];
    }
}
