package sn.edu.ept.mediconnect.users.patient;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Sexe {

    MASCULIN("M"),
    FEMININ("F");

    private final String label;

    Sexe(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static Sexe fromValue(String value) {
        if (value == null) return null;
        String normalized = value.trim()
                .toUpperCase()
                .replace("É", "E")
                .replace("È", "E")
                .replace("Ê", "E");
        for (Sexe s : values()) {
            if (s.name().equals(normalized)) return s;
        }
        throw new IllegalArgumentException("Valeur de sexe invalide: " + value + ". Valeurs acceptées: MASCULIN, FEMININ");
    }
}
