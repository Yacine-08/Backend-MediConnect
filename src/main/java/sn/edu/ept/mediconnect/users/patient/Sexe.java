package sn.edu.ept.mediconnect.users.patient;

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
}
