package sn.edu.ept.mediconnect.users.patient;

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

    public String getLabel() {
        return label;
    }
}
