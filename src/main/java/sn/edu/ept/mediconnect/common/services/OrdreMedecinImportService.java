package sn.edu.ept.mediconnect.common.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import sn.edu.ept.mediconnect.common.entities.OrdreMedecin;
import sn.edu.ept.mediconnect.common.repositories.OrdreMedecinRepository;

import java.io.IOException;
import java.text.Normalizer;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdreMedecinImportService {

    private final OrdreMedecinRepository repository;

    /**
     * Au démarrage : synchronise mail + localisation depuis le site de l'Ordre.
     * Insert les nouveaux, met à jour mail/localisation des existants si manquants.
     */
    @PostConstruct
    public void initAuDemarrage() {
        try {
            synchroniser();
        } catch (Exception e) {
            log.warn("Synchronisation ordre médecins impossible (site indisponible ?): {}", e.getMessage());
            // Fallback local : regénérer les mails sans réseau
            mettreAJourMailsLocal();
        }
    }

    public void synchroniser() throws IOException {
        Document doc = Jsoup.connect("https://www.ordremedecins.sn/tableau/")
                .timeout(30_000)
                .get();

        Elements rows = doc.select("table tbody tr");
        int inseres = 0, miseAJour = 0;

        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() < 5) continue;

            String numOrdre     = cols.get(0).text().trim();
            String section      = cols.get(1).text().trim();
            String nom          = cols.get(2).text().trim();
            String prenom       = cols.get(3).text().trim();
            String specialite   = cols.get(4).text().trim();
            String localisation = cols.size() > 5 ? cols.get(5).text().trim() : null;
            if (localisation != null && localisation.isBlank()) localisation = null;

            if (numOrdre.isBlank()) continue;

            String mailGenere = genererMail(prenom, nom);

            var opt = repository.findByNumOrdre(numOrdre);
            if (opt.isEmpty()) {
                repository.save(OrdreMedecin.builder()
                        .numOrdre(numOrdre)
                        .section(section)
                        .nom(nom)
                        .prenom(prenom)
                        .specialite(specialite)
                        .mail(mailGenere)
                        .localisation(localisation)
                        .actif(true)
                        .build());
                inseres++;
            } else {
                OrdreMedecin o = opt.get();
                boolean changed = false;
                if (o.getMail() == null || o.getMail().isBlank()) {
                    o.setMail(mailGenere);
                    changed = true;
                }
                if ((o.getLocalisation() == null || o.getLocalisation().isBlank())
                        && localisation != null) {
                    o.setLocalisation(localisation);
                    changed = true;
                }
                if (changed) { repository.save(o); miseAJour++; }
            }
        }
        log.info("Ordre médecins : {} insérés, {} mis à jour (mail/localisation)", inseres, miseAJour);
    }

    /** Fallback sans réseau : génère le mail localement pour les enregistrements sans mail. */
    public void mettreAJourMailsLocal() {
        int nb = 0;
        for (OrdreMedecin o : repository.findAll()) {
            if (o.getMail() == null || o.getMail().isBlank()) {
                o.setMail(genererMail(o.getPrenom(), o.getNom()));
                repository.save(o);
                nb++;
            }
        }
        if (nb > 0) log.info("Mails institutionnels générés localement pour {} médecins", nb);
    }

    /** Méthode publique pour forcer une re-synchronisation (ex : endpoint admin). */
    public void importer() throws IOException {
        synchroniser();
    }

    /** Génère prenom.nom@medisen.sn sans accents ni espaces. */
    public static String genererMail(String prenom, String nom) {
        return normaliser(prenom) + "." + normaliser(nom) + "@medisen.sn";
    }

    private static String normaliser(String s) {
        if (s == null || s.isBlank()) return "x";
        String n = Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD)
                             .replaceAll("\\p{InCombiningDiacriticalMarks}", "")
                             .replaceAll("[^a-z0-9]", ".");
        // collapse multiple dots and strip leading/trailing
        return n.replaceAll("\\.{2,}", ".").replaceAll("^\\.|\\.$", "");
    }
}