package com.projeto.sistema_escolar.dto;

public class ResumoProfessorDTO {

    private int totalProvas;
    private int totalTurmas;
    private int totalAlunos;
    private double mediaGeral;

    public ResumoProfessorDTO(int totalProvas, int totalTurmas,
                               int totalAlunos, double mediaGeral) {
        this.totalProvas   = totalProvas;
        this.totalTurmas   = totalTurmas;
        this.totalAlunos   = totalAlunos;
        this.mediaGeral    = mediaGeral;
    }

    public int getTotalProvas()    { return totalProvas; }
    public int getTotalTurmas()    { return totalTurmas; }
    public int getTotalAlunos()    { return totalAlunos; }
    public double getMediaGeral()  { return mediaGeral; }
}