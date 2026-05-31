package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sn.edu.ept.mediconnect.medical.alerte.NiveauAlerte;

@Data
public class AlerteRequest {

    @NotNull(message = "Le patient est obligatoire")
    private Long patientId;

    private Long consultationId;

    @NotNull(message = "Le niveau est obligatoire")
    private NiveauAlerte niveau;

    @NotBlank(message = "Le message est obligatoire")
    private String message;

    private String source;
}
