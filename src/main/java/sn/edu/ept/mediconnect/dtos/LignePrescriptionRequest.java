package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LignePrescriptionRequest {

    @NotBlank(message = "Le médicament est obligatoire")
    private String medicament;

    private String dosage;
    private String posologie;
    private String dureeJours;
    private String instructions;
}