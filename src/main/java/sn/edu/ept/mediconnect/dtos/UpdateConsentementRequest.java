package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateConsentementRequest {

    @NotNull(message = "La décision est obligatoire")
    private Boolean accepte;
}