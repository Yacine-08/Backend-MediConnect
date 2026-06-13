package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sn.edu.ept.mediconnect.medical.transfert.TypeTransfert;

@Data
public class TransfertRequest {

    // Identifiant numérique du patient (utilisé quand numPatient n'est pas passé en URL)
    private Long patientId;

    // Médecin destinataire du transfert (qui devra accepter ou refuser)
    private Long medecinDestinationId;

    // Optionnel : hôpital source peut être omis (déduit du dossier patient)
    private String nomHopitalSource;

    @NotBlank(message = "L'hôpital destination est obligatoire")
    private String nomHopitalDestination;

    @NotNull(message = "Le type est obligatoire")
    private TypeTransfert type;

    private String motif;
    private String compteRendu;
}