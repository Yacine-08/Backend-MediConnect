package sn.edu.ept.mediconnect.users.patient;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class PatientNumeroService {

    public String generer() {
        String date = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "PAT-" + date;
    }
}