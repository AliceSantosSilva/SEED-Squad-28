package com.projeto.sistema_escolar.dto;

public class ResumoAlunoDTO {

    private int provasPendentes;
    private int provasRealizadas;
    private int totalProvas;
    private double mediaGeral;

    public ResumoAlunoDTO(int provasPendentes, int provasRealizadas,
                          int totalProvas, double mediaGeral) {
        this.provasPendentes = provasPendentes;
        this.provasRealizadas = provasRealizadas;
        this.totalProvas = totalProvas;
        this.mediaGeral = mediaGeral;
    }

    public int getProvasPendentes()  { return provasPendentes; }
    public int getProvasRealizadas() { return provasRealizadas; }
    public int getTotalProvas()      { return totalProvas; }
    public double getMediaGeral()    { return mediaGeral; }
}