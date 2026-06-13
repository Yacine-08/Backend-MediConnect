package sn.edu.ept.mediconnect.users.assistant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.mediconnect.common.entities.Adresse;
import sn.edu.ept.mediconnect.common.entities.Hopital;
import sn.edu.ept.mediconnect.common.repositories.AdresseRepository;
import sn.edu.ept.mediconnect.common.repositories.HopitalRepository;
import sn.edu.ept.mediconnect.dtos.AdresseRequest;
import sn.edu.ept.mediconnect.dtos.AssistantRequest;
import sn.edu.ept.mediconnect.dtos.AssistantResponse;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.users.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssistantService {

    private final AssistantRepository assistantRepository;
    private final UserRepository      userRepository;
    private final HopitalRepository   hopitalRepository;
    private final AdresseRepository   adresseRepository;

    @Transactional(readOnly = true)
    public List<AssistantResponse> getAll() {
        return assistantRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssistantResponse> getActifs() {
        return assistantRepository.findByActif(true)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssistantResponse> search(String terme) {
        return assistantRepository.search(terme)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AssistantResponse getById(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public AssistantResponse update(Long id, AssistantRequest req) {
        Assistant assistant = find(id);

        String newEmail      = normalizeEmail(req.getEmail());
        String newPhone      = req.getTelephone();

        if (newEmail != null && !newEmail.equals(assistant.getEmail())) {
            userRepository.findByEmail(newEmail).ifPresent(u -> {
                if (!u.getId().equals(id)) {
                    throw BusinessException.conflict("Cet email est déjà utilisé.");
                }
            });
            assistant.setEmail(newEmail);
        }

        if (newPhone != null && !newPhone.equals(assistant.getTelephone())) {
            userRepository.findByTelephone(newPhone).ifPresent(u -> {
                if (!u.getId().equals(id)) {
                    throw BusinessException.conflict("Ce numéro de téléphone est déjà utilisé.");
                }
            });
            assistant.setTelephone(newPhone);
        }

        if (req.getNom() != null)            assistant.setNom(req.getNom().trim().toUpperCase());
        if (req.getPrenom() != null)         assistant.setPrenom(req.getPrenom().trim());
        if (req.getServiceAffecte() != null) assistant.setServiceAffecte(req.getServiceAffecte());

        if (req.getNomHopital() != null) {
            Hopital hopital = hopitalRepository.findByNom(req.getNomHopital())
                    .orElseThrow(() -> BusinessException.badRequest(
                            "Hôpital introuvable : " + req.getNomHopital()));
            assistant.setHopital(hopital);
        }

        if (req.getAdresse() != null) {
            assistant.setAdresse(resolveAddress(req.getAdresse()));
        }

        assistantRepository.save(assistant);
        log.info("Assistant mis à jour : id={}", id);
        return toResponse(assistant);
    }

    @Transactional
    public AssistantResponse activate(Long id) {
        Assistant assistant = find(id);
        assistant.setActif(true);
        assistantRepository.save(assistant);
        log.info("Assistant activé : id={}", id);
        return toResponse(assistant);
    }

    @Transactional
    public AssistantResponse deactivate(Long id) {
        Assistant assistant = find(id);
        assistant.setActif(false);
        assistantRepository.save(assistant);
        log.info("Assistant désactivé : id={}", id);
        return toResponse(assistant);
    }

    @Transactional
    public void supprimer(Long id) {
        Assistant assistant = find(id);
        assistant.setActif(false);
        assistantRepository.save(assistant);
        log.info("Assistant supprimé (soft delete) : id={}", id);
    }

    private Assistant find(Long id) {
        return assistantRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(
                        "Assistant introuvable (id=" + id + ")"));
    }

    private Adresse resolveAddress(AdresseRequest req) {
        return adresseRepository.findByRegionAndDepartementAndCommune(
                        req.getRegion(), req.getDepartement(), req.getCommune())
                .orElseThrow(() -> BusinessException.badRequest(
                        "Adresse introuvable : " + req.getRegion()
                                + " / " + req.getDepartement()
                                + " / " + req.getCommune()));
    }

    private String normalizeEmail(String email) {
        return (email != null && !email.isBlank())
                ? email.trim().toLowerCase()
                : null;
    }

    public AssistantResponse toResponse(Assistant a) {
        AssistantResponse.AssistantResponseBuilder b = AssistantResponse.builder()
                .id(a.getId())
                .nom(a.getNom())
                .prenom(a.getPrenom())
                .email(a.getEmail())
                .telephone(a.getTelephone())
                .serviceAffecte(a.getServiceAffecte())
                .actif(a.getActif())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt());

        if (a.getHopital() != null) {
            b.hopital(a.getHopital().getNom());
        }
        if (a.getAdresse() != null) {
            b.region(a.getAdresse().getRegion())
                    .departement(a.getAdresse().getDepartement())
                    .commune(a.getAdresse().getCommune());
        }

        return b.build();
    }
}
