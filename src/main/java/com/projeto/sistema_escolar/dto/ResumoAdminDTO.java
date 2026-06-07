package com.projeto.sistema_escolar.dto;

public class ResumoAdminDTO {

    private long totalAlunos;
    private long totalProfessores;
    private long totalEscolas;
    private long totalProvas;
    private double mediaGeral;

    public ResumoAdminDTO(long totalAlunos, long totalProfessores,
                           long totalEscolas, long totalProvas,
                           double mediaGeral) {
        this.totalAlunos      = totalAlunos;
        this.totalProfessores = totalProfessores;
        this.totalEscolas     = totalEscolas;
        this.totalProvas      = totalProvas;
        this.mediaGeral       = mediaGeral;
    }

    public long getTotalAlunos()        { return totalAlunos; }
    public long getTotalProfessores()   { return totalProfessores; }
    public long getTotalEscolas()       { return totalEscolas; }
    public long getTotalProvas()        { return totalProvas; }
    public double getMediaGeral()       { return mediaGeral; }
}