package sn.edu.ept.mediconnect.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LignePrescriptionResponse {

    private Long id;
    private String medicament;
    private String dosage;
    private String posologie;
    private String dureeJours;
    private String instructions;
}