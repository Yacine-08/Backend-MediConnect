package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdresseRequest {

    @NotBlank(message = "La région est obligatoire")
    private String region;

    @NotBlank(message = "Le département est obligatoire")
    private String departement;

    @NotBlank(message = "La commune est obligatoire")
    private String commune;
}