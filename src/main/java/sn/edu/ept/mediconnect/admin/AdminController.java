package sn.edu.ept.mediconnect.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.mediconnect.common.repositories.HopitalRepository;
import sn.edu.ept.mediconnect.medical.alerte.AlerteRepository;
import sn.edu.ept.mediconnect.medical.consultation.ConsultationRepository;
import sn.edu.ept.mediconnect.medical.examen.ExamenRepository;
import sn.edu.ept.mediconnect.medical.ordonnance.OrdonnanceRepository;
import sn.edu.ept.mediconnect.medical.rendezvous.RendezVousRepository;
import sn.edu.ept.mediconnect.medical.transfert.TransfertRepository;
import sn.edu.ept.mediconnect.users.assistant.Assistant;
import sn.edu.ept.mediconnect.users.assistant.AssistantRepository;
import sn.edu.ept.mediconnect.users.infirmier.Infirmier;
import sn.edu.ept.mediconnect.users.infirmier.InfirmierRepository;
import sn.edu.ept.mediconnect.users.medecin.Medecin;
import sn.edu.ept.mediconnect.users.medecin.MedecinRepository;
import sn.edu.ept.mediconnect.users.patient.Patient;
import sn.edu.ept.mediconnect.users.patient.PatientRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final PatientRepository      patientRepository;
    private final MedecinRepository      medecinRepository;
    private final InfirmierRepository    infirmierRepository;
    private final AssistantRepository    assistantRepository;
    private final HopitalRepository      hopitalRepository;
    private final ConsultationRepository consultationRepository;
    private final OrdonnanceRepository   ordonnanceRepository;
    private final RendezVousRepository   rendezVousRepository;
    private final TransfertRepository    transfertRepository;
    private final ExamenRepository       examenRepository;
    private final AlerteRepository       alerteRepository;

    /** GET /api/admin/stats — statistiques globales de la plateforme */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {

        List<Patient>   patients   = patientRepository.findAll();
        List<Medecin>   medecins   = medecinRepository.findAll();
        List<Infirmier> infirmiers = infirmierRepository.findAll();
        List<Assistant> assistants = assistantRepository.findAll();

        long totalPatients     = patients.size();
        long patientsActifs    = patients.stream().filter(p -> Boolean.TRUE.equals(p.getActif())).count();

        long totalMedecins     = medecins.size();
        long medecinsActifs    = medecins.stream().filter(m -> Boolean.TRUE.equals(m.getActif())).count();
        long medecinsValides   = medecins.stream().filter(m -> Boolean.TRUE.equals(m.getVerified())).count();
        long medecinsEnAttente = medecins.stream().filter(m -> !Boolean.TRUE.equals(m.getVerified())).count();

        long totalInfirmiers   = infirmiers.size();
        long infirmiersActifs  = infirmiers.stream().filter(i -> Boolean.TRUE.equals(i.getActif())).count();

        long totalAssistants   = assistants.size();
        long assistantsActifs  = assistants.stream().filter(a -> Boolean.TRUE.equals(a.getActif())).count();

        long totalConsultations    = consultationRepository.count();
        long totalOrdonnances      = ordonnanceRepository.count();
        long totalRendezVous       = rendezVousRepository.count();
        long totalTransferts       = transfertRepository.count();
        long totalExamens          = examenRepository.count();
        long totalAlertes          = alerteRepository.count();
        long alertesNonAcquittees  = alerteRepository.findByAcquittee(false).size();
        long totalHopitaux         = hopitalRepository.count();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("patients",   map("total", totalPatients,   "actifs", patientsActifs,   "inactifs", totalPatients - patientsActifs));
        data.put("medecins",   map("total", totalMedecins,   "actifs", medecinsActifs,   "valides", medecinsValides, "enAttente", medecinsEnAttente));
        data.put("infirmiers", map("total", totalInfirmiers, "actifs", infirmiersActifs, "inactifs", totalInfirmiers - infirmiersActifs));
        data.put("assistants", map("total", totalAssistants, "actifs", assistantsActifs, "inactifs", totalAssistants - assistantsActifs));
        data.put("hopitaux",   map("total", totalHopitaux));
        data.put("medical",    map(
            "consultations", totalConsultations,
            "ordonnances",   totalOrdonnances,
            "rendezVous",    totalRendezVous,
            "transferts",    totalTransferts,
            "examens",       totalExamens,
            "alertes",       totalAlertes,
            "alertesNonAcquittees", alertesNonAcquittees
        ));

        return ResponseEntity.ok(Map.of("success", true, "data", data, "timestamp", LocalDateTime.now()));
    }

    /** GET /api/admin/activity — journal d'activité synthétique */
    @GetMapping("/activity")
    public ResponseEntity<?> getActivity() {
        List<Map<String, Object>> activities = new ArrayList<>();

        patientRepository.findAll().stream()
            .filter(p -> p.getCreatedAt() != null)
            .sorted(Comparator.comparing(Patient::getCreatedAt).reversed())
            .limit(10)
            .forEach(p -> activities.add(activityEntry(
                "Nouveau patient enregistré",
                (p.getPrenom() != null ? p.getPrenom() : "") + " " + (p.getNom() != null ? p.getNom() : ""),
                "PATIENT", "success", p.getCreatedAt().toString()
            )));

        medecinRepository.findAll().stream()
            .filter(m -> m.getCreatedAt() != null)
            .sorted(Comparator.comparing(Medecin::getCreatedAt).reversed())
            .limit(10)
            .forEach(m -> {
                String action = Boolean.TRUE.equals(m.getVerified())
                    ? "Médecin validé"
                    : "Médecin inscrit — en attente de validation";
                activities.add(activityEntry(
                    action,
                    "Dr. " + (m.getPrenom() != null ? m.getPrenom() : "") + " " + (m.getNom() != null ? m.getNom() : ""),
                    "MEDECIN",
                    Boolean.TRUE.equals(m.getVerified()) ? "success" : "warning",
                    m.getCreatedAt().toString()
                ));
            });

        infirmierRepository.findAll().stream()
            .filter(i -> i.getCreatedAt() != null)
            .sorted(Comparator.comparing(Infirmier::getCreatedAt).reversed())
            .limit(10)
            .forEach(i -> activities.add(activityEntry(
                Boolean.TRUE.equals(i.getActif()) ? "Infirmier inscrit" : "Infirmier inscrit — compte inactif",
                (i.getPrenom() != null ? i.getPrenom() : "") + " " + (i.getNom() != null ? i.getNom() : ""),
                "INFIRMIER",
                Boolean.TRUE.equals(i.getActif()) ? "info" : "warning",
                i.getCreatedAt().toString()
            )));

        assistantRepository.findAll().stream()
            .filter(a -> a.getCreatedAt() != null)
            .sorted(Comparator.comparing(Assistant::getCreatedAt).reversed())
            .limit(10)
            .forEach(a -> activities.add(activityEntry(
                Boolean.TRUE.equals(a.getActif()) ? "Assistant médical inscrit" : "Assistant médical inscrit — compte inactif",
                (a.getPrenom() != null ? a.getPrenom() : "") + " " + (a.getNom() != null ? a.getNom() : ""),
                "ASSISTANT",
                Boolean.TRUE.equals(a.getActif()) ? "info" : "warning",
                a.getCreatedAt().toString()
            )));

        activities.sort((a, b) -> b.get("timestamp").toString().compareTo(a.get("timestamp").toString()));

        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", activities.stream().limit(40).collect(Collectors.toList()),
            "timestamp", LocalDateTime.now()
        ));
    }

    // ── Utilitaires privés ────────────────────────────────────────────────────

    private static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    private static Map<String, Object> activityEntry(
            String action, String userName, String userRole, String type, String timestamp) {
        return map("action", action, "userName", userName,
                   "userRole", userRole, "type", type, "timestamp", timestamp);
    }
}
