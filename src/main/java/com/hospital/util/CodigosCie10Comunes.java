package com.hospital.util;

import java.util.Collections;
import java.util.List;

/** Codigos CIE-10 frecuentes; los diagnosticos fuera del catalogo son texto libre. */
public final class CodigosCie10Comunes {

    private CodigosCie10Comunes() {
    }

    public static final List<String> LISTA = Collections.unmodifiableList(java.util.Arrays.asList(
        "A09 - OTRAS GASTROENTERITIS Y COLITIS DE ORIGEN INFECCIOSO Y NO ESPECIFICADO",
        "A90 - FIEBRE DE DENGUE [DENGUE CLASICO]",
        "A97 - DENGUE",
        "B829 - PARASITOSIS INTESTINAL, SIN OTRA ESPECIFICACION",
        "D649 - ANEMIA DE TIPO NO ESPECIFICADO",
        "E039 - HIPOTIROIDISMO, NO ESPECIFICADO",
        "E11 - DIABETES MELLITUS NO INSULINODEPENDIENTE",
        "E119 - DIABETES MELLITUS TIPO 2, SIN MENCION DE COMPLICACION",
        "E66 - OBESIDAD",
        "F419 - TRASTORNO DE ANSIEDAD, NO ESPECIFICADO",
        "G439 - MIGRAÑA, NO ESPECIFICADA",
        "H108 - OTRAS CONJUNTIVITIS",
        "I10 - HIPERTENSION ESENCIAL (PRIMARIA)",
        "J00 - RINOFARINGITIS AGUDA [RESFRIADO COMUN]",
        "J02 - FARINGITIS AGUDA",
        "J03 - AMIGDALITIS AGUDA",
        "J06 - INFECCIONES AGUDAS DE LAS VIAS RESPIRATORIAS SUPERIORES, DE SITIOS MULTIPLES O NO ESPECIFICADOS",
        "J11 - INFLUENZA DEBIDA A VIRUS NO IDENTIFICADO",
        "J18 - NEUMONIA, ORGANISMO NO ESPECIFICADO",
        "J20 - BRONQUITIS AGUDA",
        "J45 - ASMA",
        "K021 - CARIES DE LA DENTINA",
        "K297 - GASTRITIS, NO ESPECIFICADA",
        "K358 - OTRAS APENDICITIS AGUDAS, Y LAS NO ESPECIFICADAS",
        "K590 - CONSTIPACION",
        "L309 - DERMATITIS, NO ESPECIFICADA",
        "M545 - LUMBAGO NO ESPECIFICADO",
        "N30 - CISTITIS",
        "N390 - INFECCION DE VIAS URINARIAS, SITIO NO ESPECIFICADO",
        "O269 - COMPLICACION RELACIONADA CON EL EMBARAZO, NO ESPECIFICADA",
        "R51 - CEFALEA",
        "R509 - FIEBRE, NO ESPECIFICADA",
        "S019 - HERIDA DE LA CABEZA, PARTE NO ESPECIFICADA",
        "T140 - TRAUMATISMO SUPERFICIAL DE REGION NO ESPECIFICADA DEL CUERPO",
        "Z000 - EXAMEN MEDICO GENERAL",
        "Z348 - SUPERVISION DE OTROS EMBARAZOS NORMALES"
    ));

    public static String extraerCodigo(String item) {
        if (item == null) {
            return null;
        }
        String[] partes = item.split(" - ", 2);
        if (partes.length == 2 && partes[0].trim().matches("^[A-Z][0-9A-Z]{1,5}$")) {
            return partes[0].trim();
        }
        return null;
    }

    public static String extraerDescripcion(String item) {
        if (item == null) {
            return null;
        }
        String codigo = extraerCodigo(item);
        if (codigo != null) {
            return item.substring(item.indexOf(" - ") + 3).trim();
        }
        return item.trim();
    }
}