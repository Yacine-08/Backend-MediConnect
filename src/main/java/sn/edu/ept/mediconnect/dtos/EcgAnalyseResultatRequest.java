package sn.edu.ept.mediconnect.dtos;

import lombok.Data;

@Data
public class EcgAnalyseResultatRequest {
    private String analyseIaJson;
    private Boolean analyseIaAnomalie;
    private Float analyseIaConfiance;
}
