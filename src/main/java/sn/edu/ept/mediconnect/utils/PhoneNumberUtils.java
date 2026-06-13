package sn.edu.ept.mediconnect.utils;

public class PhoneNumberUtils {
    
    public static String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }

        // Supprimer tous les caractères non numériques
        String digits = phoneNumber.replaceAll("\\D+", "");

        // Retirer le préfixe international sénégalais (221 ou 00221)
        if (digits.startsWith("00221")) {
            digits = digits.substring(5);
        } else if (digits.startsWith("221")) {
            digits = digits.substring(3);
        }

        // Retirer un éventuel 0 de tête (format local 077...)
        if (digits.length() == 10 && digits.startsWith("0")) {
            digits = digits.substring(1);
        }

        // Si on n'arrive pas à 9 chiffres, stocker le numéro brut (pas de blocage)
        if (digits.length() != 9) {
            return phoneNumber.trim();
        }

        // Formater le numéro : +221 77 123 45 67
        return "+221 " + digits.substring(0, 2) + " " +
               digits.substring(2, 5) + " " +
               digits.substring(5, 7) + " " +
               digits.substring(7);
    }
    
    public static boolean isValidSenegalPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        String digits = phoneNumber.replaceAll("\\D+", "");
        
        if (digits.startsWith("221")) {
            digits = digits.substring(3);
        }
        
        return digits.matches("^77\\d{7}$|^76\\d{7}$|^75\\d{7}$|^70\\d{7}$");
    }
}
