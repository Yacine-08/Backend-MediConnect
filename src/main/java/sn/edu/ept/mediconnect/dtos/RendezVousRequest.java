package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sn.edu.ept.mediconnect.medical.rendezvous.TypeRendezVous;
import java.time.LocalDateTime;

@Data
public class RendezVousRequest {

    private String nomHopital;

    @NotNull(message = "La date et l'heure sont obligatoires")
    @Future(message = "La date doit être dans le futur")
    private LocalDateTime dateHeure;

    @NotNull(message = "Le type est obligatoire")
    private TypeRendezVous type;

    private String motif;
}