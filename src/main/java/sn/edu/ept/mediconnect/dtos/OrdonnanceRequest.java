package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrdonnanceRequest {

    private LocalDateTime dateExpiration;

    @NotNull(message = "Les lignes de prescription sont obligatoires")
    private List<LignePrescriptionRequest> lignes;
}