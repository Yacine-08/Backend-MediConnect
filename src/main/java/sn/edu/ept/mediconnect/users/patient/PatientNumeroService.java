package sn.edu.ept.mediconnect.users.patient;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PatientNumeroService {

    private final PatientRepository patientRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    public String generer() {

        String numero;

        do {
            String date = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            int aleatoire = 1000 + RANDOM.nextInt(9000);

            numero = "PAT-" + date + "-" + aleatoire;

        } while (patientRepository.existsByNumPatient(numero));

        return numero;
    }
}