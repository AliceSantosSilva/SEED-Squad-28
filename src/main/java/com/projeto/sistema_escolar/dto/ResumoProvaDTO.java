package com.projeto.sistema_escolar.dto;

import java.time.LocalDateTime;

public class ResumoProvaDTO {

    private Integer provaId;
    private String titulo;
    private String turma;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private String status;
    private long participacao;
    private int totalQuestoes;
    private double media;

    public ResumoProvaDTO(Integer provaId, String titulo, String turma,
                          LocalDateTime dataInicio, LocalDateTime dataFim,
                          String status, long participacao,
                          int totalQuestoes, double media) {
        this.provaId       = provaId;
        this.titulo        = titulo;
        this.turma         = turma;
        this.dataInicio    = dataInicio;
        this.dataFim       = dataFim;
        this.status        = status;
        this.participacao  = participacao;
        this.totalQuestoes = totalQuestoes;
        this.media         = media;
    }

    public Integer getProvaId()        { return provaId; }
    public String getTitulo()          { return titulo; }
    public String getTurma()           { return turma; }
    public LocalDateTime getDataInicio() { return dataInicio; }
    public LocalDateTime getDataFim()  { return dataFim; }
    public String getStatus()          { return status; }
    public long getParticipacao()      { return participacao; }
    public int getTotalQuestoes()      { return totalQuestoes; }
    public double getMedia()           { return media; }
}