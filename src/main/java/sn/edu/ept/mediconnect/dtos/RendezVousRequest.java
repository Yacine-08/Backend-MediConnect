package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sn.edu.ept.mediconnect.medical.rendezvous.TypeRendezVous;
import java.time.LocalDateTime;

@Data
public class RendezVousRequest {

    // Obligatoire uniquement si c'est le médecin qui crée
    // Ignoré si c'est le patient (on prend son id depuis le token)
    private Long patientId;

    @NotNull(message = "Le médecin est obligatoire")
    private Long medecinId;

    private String nomHopital;

    @NotNull(message = "La date et l'heure sont obligatoires")
    @Future(message = "La date doit être dans le futur")
    private LocalDateTime dateHeure;

    @NotNull(message = "Le type est obligatoire")
    private TypeRendezVous type;

    private String motif;
}