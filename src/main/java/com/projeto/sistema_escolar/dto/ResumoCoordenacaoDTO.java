package com.projeto.sistema_escolar.dto;

public class ResumoCoordenacaoDTO {

    private long totalEscolas;
    private long totalProfessores;
    private long totalTurmas;
    private long totalProvasAplicadas;
    private double mediaGeral;

    public ResumoCoordenacaoDTO(long totalEscolas, long totalProfessores,
                                 long totalTurmas, long totalProvasAplicadas,
                                 double mediaGeral) {
        this.totalEscolas          = totalEscolas;
        this.totalProfessores      = totalProfessores;
        this.totalTurmas           = totalTurmas;
        this.totalProvasAplicadas  = totalProvasAplicadas;
        this.mediaGeral            = mediaGeral;
    }

    public long getTotalEscolas()           { return totalEscolas; }
    public long getTotalProfessores()       { return totalProfessores; }
    public long getTotalTurmas()            { return totalTurmas; }
    public long getTotalProvasAplicadas()   { return totalProvasAplicadas; }
    public double getMediaGeral()           { return mediaGeral; }
}