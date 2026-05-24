package sn.edu.ept.mediconnect.users.infirmier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.mediconnect.common.entities.Adresse;
import sn.edu.ept.mediconnect.common.entities.Hopital;
import sn.edu.ept.mediconnect.common.repositories.AdresseRepository;
import sn.edu.ept.mediconnect.common.repositories.HopitalRepository;
import sn.edu.ept.mediconnect.dtos.AdresseRequest;
import sn.edu.ept.mediconnect.dtos.InfirmierRequest;
import sn.edu.ept.mediconnect.dtos.InfirmierResponse;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.users.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InfirmierService {

    private final InfirmierRepository infirmierRepository;
    private final UserRepository      userRepository;
    private final HopitalRepository   hopitalRepository;
    private final AdresseRepository   adresseRepository;
    

    @Transactional(readOnly = true)
    public List<InfirmierResponse> getAll() {
        return infirmierRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InfirmierResponse> getActifs() {
        return infirmierRepository.findByActif(true)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InfirmierResponse> search(String terme) {
        return infirmierRepository.search(terme)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }



    @Transactional(readOnly = true)
    public InfirmierResponse getById(Long id) {
        return toResponse(find(id));
    }


    //  L'infirmier peut modifier son propre profil.
    @Transactional
    public InfirmierResponse update(Long id, InfirmierRequest req) {

        Infirmier infirmier = find(id);

        // Vérifier unicité email/téléphone uniquement si la valeur change
        String newEmail      = normalizeEmail(req.getEmail());
        String newPhoneNumber = req.getTelephone();

        if (newEmail != null && !newEmail.equals(infirmier.getEmail())) {
            userRepository.findByEmail(newEmail).ifPresent(u -> {
                if (!u.getId().equals(id)) {
                    throw BusinessException.conflict("Cet email est déjà utilisé.");
                }
            });
            infirmier.setEmail(newEmail);
        }

        if (newPhoneNumber != null && !newPhoneNumber.equals(infirmier.getTelephone())) {
            userRepository.findByTelephone(newPhoneNumber).ifPresent(u -> {
                if (!u.getId().equals(id)) {
                    throw BusinessException.conflict("Ce numéro de téléphone est déjà utilisé.");
                }
            });
            infirmier.setTelephone(newPhoneNumber);
        }

        if (req.getNom() != null)            infirmier.setNom(req.getNom().trim().toUpperCase());
        if (req.getPrenom() != null)         infirmier.setPrenom(req.getPrenom().trim());
        if (req.getServiceAffecte() != null) infirmier.setServiceAffecte(req.getServiceAffecte());

        if (req.getNomHopital() != null) {
            Hopital hopital = hopitalRepository.findByNom(req.getNomHopital())
                    .orElseThrow(() -> BusinessException.badRequest(
                            "Hôpital introuvable : " + req.getNomHopital()));
            infirmier.setHopital(hopital);
        }

        if (req.getAdresse() != null) {
            infirmier.setAdresse(resolveAddress(req.getAdresse()));
        }

        infirmierRepository.save(infirmier);
        log.info("Infirmier mis à jour : id={}", id);
        return toResponse(infirmier);
    }
    
    
    //  SUPPRESSION LOGIQUE (ADMIN uniquement)
    @Transactional
    public void supprimer(Long id) {
        Infirmier infirmier = find(id);
        infirmier.setActif(false);
        infirmierRepository.save(infirmier);
        log.info("Infirmier supprimé (soft delete) : id={}", id);
    }



    private Infirmier find(Long id) {
        return infirmierRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(
                        "Infirmier introuvable (id=" + id + ")"));
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

    // ADMIN uniquement
    @Transactional
    public InfirmierResponse activate(Long id) {
        Infirmier infirmier = find(id);
        infirmier.setActif(true);
        infirmierRepository.save(infirmier);
        log.info("Infirmier activé : id={}", id);
        return toResponse(infirmier);
    }

    @Transactional
    public InfirmierResponse deactivate(Long id) {
        Infirmier infirmier = find(id);
        infirmier.setActif(false);
        infirmierRepository.save(infirmier);
        log.info("Infirmier désactivé : id={}", id);
        return toResponse(infirmier);
    }

    public InfirmierResponse toResponse(Infirmier i) {
        InfirmierResponse.InfirmierResponseBuilder b = InfirmierResponse.builder()
                .id(i.getId())
                .nom(i.getNom())
                .prenom(i.getPrenom())
                .email(i.getEmail())
                .telephone(i.getTelephone())
                .serviceAffecte(i.getServiceAffecte())
                .actif(i.getActif())
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt());

        if (i.getHopital() != null) {
            b.hopital(i.getHopital().getNom());
        }
        if (i.getAdresse() != null) {
            b.region(i.getAdresse().getRegion())
                    .departement(i.getAdresse().getDepartement())
                    .commune(i.getAdresse().getCommune());
        }

        return b.build();
    }
}