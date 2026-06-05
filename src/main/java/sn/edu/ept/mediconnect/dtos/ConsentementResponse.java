package sn.edu.ept.mediconnect.dtos;

import lombok.Builder;
import lombok.Data;
import sn.edu.ept.mediconnect.consentement.TypeConsentement;

import java.time.LocalDateTime;

@Data
@Builder
public class ConsentementResponse {

    private Long signatureId;
    private Long             consentementId;
    private TypeConsentement type;
    private String           titre;
    private String           contenu;
    private String           version;
    private Boolean          obligatoire;
    private Boolean           accepte;
    private LocalDateTime     dateSignature;
    private LocalDateTime     dateModification;
    private Long   modifiedById;
    private String modifiedByName;
    private String modifiedByRole;
}