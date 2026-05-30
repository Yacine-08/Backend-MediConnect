package sn.edu.ept.mediconnect.dtos;

import lombok.Data;

@Data
public class ConstantesRequest {

    private String tensionArterielle;
    private Integer frequenceCardiaque;
    private Float temperature;
    private Float poids;
    private Float taille;
    private Float spo2;
}