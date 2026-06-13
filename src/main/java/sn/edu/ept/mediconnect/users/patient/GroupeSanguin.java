package sn.edu.ept.mediconnect.users.patient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GroupeSanguin {
    A_PLUS("A+"),
    A_MOINS("A-"),
    B_PLUS("B+"),
    B_MOINS("B-"),
    AB_PLUS("AB+"),
    AB_MOINS("AB-"),
    O_PLUS("O+"),
    O_MOINS("O-");

    private final String label;

    GroupeSanguin(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static GroupeSanguin fromValue(String value) {
        if (value == null || value.isBlank()) return null;
        String trimmed = value.trim();
        for (GroupeSanguin g : values()) {
            if (g.label.equalsIgnoreCase(trimmed) || g.name().equalsIgnoreCase(trimmed)) {
                return g;
            }
        }
        throw new IllegalArgumentException("Groupe sanguin invalide: " + value);
    }
}
