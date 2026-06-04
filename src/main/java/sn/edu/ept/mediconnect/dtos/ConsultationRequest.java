package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConsultationRequest {

    private String motif;
}