package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sn.edu.ept.mediconnect.medical.transfert.TypeTransfert;

@Data
public class TransfertRequest {

    @NotNull(message = "Le patient est obligatoire")
    private Long patientId;

    @NotNull(message = "Le médecin est obligatoire")
    private Long medecinId;

    @NotBlank(message = "L'hôpital source est obligatoire")
    private String nomHopitalSource;

    @NotBlank(message = "L'hôpital destination est obligatoire")
    private String nomHopitalDestination;

    @NotNull(message = "Le type est obligatoire")
    private TypeTransfert type;

    private String motif;
    private String compteRendu;
}