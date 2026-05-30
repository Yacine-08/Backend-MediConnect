package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sn.edu.ept.mediconnect.medical.examen.ExamenType;

@Data
public class ExamenRequest {

    @NotNull(message = "La consultation est obligatoire")
    private Long consultationId;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotNull(message = "Le type est obligatoire")
    private ExamenType type;

    private String fichierUrl;
    private String format;
    private Long tailleFichier;
}