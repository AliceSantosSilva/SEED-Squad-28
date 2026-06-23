package com.projeto.sistema_escolar.dto;

import java.time.LocalDateTime;

public class ResultadoProvaDTO {

    private Integer provaId;
    private String tituloprova;
    private LocalDateTime dataRealizado;
    private long totalQuestoes;
    private long acertos;
    private double nota;
    private String situacao; // "Aprovado" | "Reprovado"
    private long totalIncidentes;  // 🔧 NOVO

    public ResultadoProvaDTO(Integer provaId, String tituloProva,
                             LocalDateTime dataRealizado, long totalQuestoes,
                             long acertos, double notaMinimaAprovacao) {
        this.provaId = provaId;
        this.tituloprova = tituloProva;
        this.dataRealizado = dataRealizado;
        this.totalQuestoes = totalQuestoes;
        this.acertos = acertos;
        this.nota = totalQuestoes > 0
            ? Math.round((acertos * 10.0 / totalQuestoes) * 100.0) / 100.0
            : 0.0;
        this.situacao = this.nota >= notaMinimaAprovacao ? "Aprovado" : "Reprovado";
        this.totalIncidentes = 0;  // 🔧 INICIALIZA
    }

    public Integer getProvaId()          { return provaId; }
    public String getTituloProva()       { return tituloprova; }
    public LocalDateTime getDataRealizado() { return dataRealizado; }
    public long getTotalQuestoes()       { return totalQuestoes; }
    public long getAcertos()             { return acertos; }
    public double getNota()              { return nota; }
    public String getSituacao()          { return situacao; }
    
    // 🔧 NOVOS GETTER E SETTER
    public long getTotalIncidentes()     { return totalIncidentes; }
    public void setTotalIncidentes(long totalIncidentes) { this.totalIncidentes = totalIncidentes; }
}