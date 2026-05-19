package sn.edu.ept.mediconnect.common.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import sn.edu.ept.mediconnect.common.entities.OrdreMedecin;
import sn.edu.ept.mediconnect.common.repositories.OrdreMedecinRepository;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class OrdreMedecinImportService {

    private final OrdreMedecinRepository repository;

    public void importer() throws IOException {

        // url du site
        Document doc = Jsoup.connect(
            "https://www.ordremedecins.sn/tableau/"
        ).get();

        Elements rows = doc.select("table tbody tr");

        for (Element row : rows) {

            Elements cols = row.select("td");

            if (cols.size() < 4) continue;

            String numOrdre = cols.get(0).text();
            String nom = cols.get(1).text();
            String prenom = cols.get(2).text();
            String specialite = cols.get(3).text();

            if (!repository.existsByNumOrdre(numOrdre)) {

                OrdreMedecin medecin = OrdreMedecin.builder()
                        .numOrdre(numOrdre)
                        .nom(nom)
                        .prenom(prenom)
                        .specialite(specialite)
                        .actif(true)
                        .build();

                repository.save(medecin);
            }
        }
    }
}