package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConsultationRequest {

    @NotNull(message = "Le patient est obligatoire")
    private Long patientId;

    @NotNull(message = "Le médecin est obligatoire")
    private Long medecinId;

    private Long rendezVousId;
}