package sn.edu.ept.mediconnect.users.medecin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.mediconnect.common.entities.Adresse;
import sn.edu.ept.mediconnect.common.entities.Hopital;
import sn.edu.ept.mediconnect.common.repositories.AdresseRepository;
import sn.edu.ept.mediconnect.common.repositories.HopitalRepository;
import sn.edu.ept.mediconnect.dtos.*;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.medical.dossier.*;
import sn.edu.ept.mediconnect.users.UserRepository;
import sn.edu.ept.mediconnect.users.patient.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedecinService {

    private final MedecinRepository        medecinRepository;
    private final UserRepository           userRepository;
    private final HopitalRepository        hopitalRepository;
    private final AdresseRepository        adresseRepository;
    private final PatientRepository        patientRepository;
    private final DossierMedicalRepository dossierMedicalRepository;
    private final DossierMedicalService    dossierMedicalService;


    @Transactional(readOnly = true)
    public List<MedecinResponse> getAll() {
        return medecinRepository.findAll()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedecinResponse> getActifs() {
        return medecinRepository.findByActif(true)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedecinResponse> search(String terme) {
        return medecinRepository.search(terme)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MedecinResponse getById(Long id) {
        return toResponse(find(id));
    }

    // champs NON modifiables : numOrdre, section, specialite (données venant de l'Ordre des Médecins)
    @Transactional
    public MedecinResponse update(Long id, MedecinUpdateRequest req) {

        Medecin medecin = find(id);

        // Email
        if (!estVide(req.getEmail())) {
            String newEmail = req.getEmail().trim().toLowerCase();
            if (!newEmail.equals(medecin.getEmail())) {
                userRepository.findByEmail(newEmail).ifPresent(u -> {
                    if (!u.getId().equals(id))
                        throw BusinessException.conflict("Cet email est déjà utilisé.");
                });
                medecin.setEmail(newEmail);
            }
        }

        // Téléphone
        if (!estVide(req.getTelephone())) {
            if (!req.getTelephone().equals(medecin.getTelephone())) {
                userRepository.findByTelephone(req.getTelephone()).ifPresent(u -> {
                    if (!u.getId().equals(id))
                        throw BusinessException.conflict(
                            "Ce numéro est déjà utilisé.");
                });
                medecin.setTelephone(req.getTelephone());
            }
        }

        if (!estVide(req.getNom()))    medecin.setNom(req.getNom().trim().toUpperCase());
        if (!estVide(req.getPrenom())) medecin.setPrenom(req.getPrenom().trim());

        // Disponibilité
        if (req.getDisponible() != null)
            medecin.setDisponible(req.getDisponible());

        // Changement d'établissement → vérification dans la table hopitaux
        if (!estVide(req.getNomEtablissement())) {
            Hopital hopital = hopitalRepository
                .findByNom(req.getNomEtablissement())
                .orElseThrow(() -> BusinessException.badRequest(
                    "Établissement introuvable : " + req.getNomEtablissement()
                    + ". Vérifiez le nom exact dans la liste des hôpitaux."));
            medecin.setEtablissement(hopital);
        }

        // Adresse
        if (req.getAdresse() != null) {
            medecin.setAdresse(resolveAddress(req.getAdresse()));
        }

        medecinRepository.save(medecin);
        log.info("Médecin mis à jour : id={}", id);
        return toResponse(medecin);
    }

    // SUPPRESSION LOGIQUE (ADMIN uniquement)
    @Transactional
    public void supprimer(Long id) {
        Medecin medecin = find(id);
        medecin.setActif(false);
        medecinRepository.save(medecin);
        log.info("Médecin supprimé (soft delete) : id={}", id);
    }

    // ADMIN uniquement
    @Transactional
    public MedecinResponse activate(Long id) {
        Medecin medecin = find(id);
        medecin.setActif(true);
        medecinRepository.save(medecin);
        log.info("Médecin activé : id={}", id);
        return toResponse(medecin);
    }

    @Transactional
    public MedecinResponse desactivate(Long id) {
        Medecin medecin = find(id);
        medecin.setActif(false);
        medecinRepository.save(medecin);
        log.info("Médecin désactivé : id={}", id);
        return toResponse(medecin);
    }

    private Medecin find(Long id) {
        return medecinRepository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(
                "Médecin introuvable (id=" + id + ")"));
    }

    private Adresse resolveAdresse(AdresseRequest req) {
        return adresseRepository
            .findByRegionAndDepartementAndCommune(
                req.getRegion(), req.getDepartement(), req.getCommune())
            .orElseThrow(() -> BusinessException.badRequest(
                "Adresse introuvable : " + req.getRegion()
                + " / " + req.getDepartement()
                + " / " + req.getCommune()));
    }

    private boolean estVide(String s) {
        return s == null || s.isBlank();
    }

    private Adresse resolveAddress(AdresseRequest req) {
        return adresseRepository.findByRegionAndDepartementAndCommune(
                        req.getRegion(), req.getDepartement(), req.getCommune())
                .orElseThrow(() -> BusinessException.badRequest(
                        "Adresse introuvable : " + req.getRegion()
                                + " / " + req.getDepartement()
                                + " / " + req.getCommune()));
    }

    public MedecinResponse toResponse(Medecin m) {
        MedecinResponse.MedecinResponseBuilder b = MedecinResponse.builder()
            .id(m.getId())
            .nom(m.getNom())
            .prenom(m.getPrenom())
            .email(m.getEmail())
            .telephone(m.getTelephone())
            .numOrdre(m.getNumOrdre())
            .section(m.getSection())
            .specialite(m.getSpecialite())
            .disponible(m.getDisponible())
            .verified(m.getVerified())
            .actif(m.getActif())
            .createdAt(m.getCreatedAt())
            .updatedAt(m.getUpdatedAt());

        if (m.getEtablissement() != null)
            b.etablissement(m.getEtablissement().getNom());

        return b.build();
    }

    private PatientResponse toPatientResponse(Patient p) {
        PatientResponse.PatientResponseBuilder b = PatientResponse.builder()
            .id(p.getId())
            .numPatient(p.getNumPatient())
            .nom(p.getNom())
            .prenom(p.getPrenom())
            .email(p.getEmail())
            .telephone(p.getTelephone())
            .dateNaissance(p.getDateNaissance())
            .sexe(p.getSexe())
            .groupeSanguin(p.getGroupeSanguin())
            .assurance(p.getAssurance())
            .actif(p.getActif());

        if (p.getAdresse() != null)
            b.region(p.getAdresse().getRegion())
             .departement(p.getAdresse().getDepartement())
             .commune(p.getAdresse().getCommune());

        return b.build();
    }
}