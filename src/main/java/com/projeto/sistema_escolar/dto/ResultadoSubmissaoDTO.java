package com.projeto.sistema_escolar.dto;

public class ResultadoSubmissaoDTO {

    private Integer provaId;
    private String tituloProva;
    private long totalQuestoes;
    private long acertos;
    private double nota;
    private String situacao;
    private String mensagem;
    private long totalIncidentes;  // 🔧 NOVO

    public ResultadoSubmissaoDTO(Integer provaId, String tituloProva,
                                  long totalQuestoes, long acertos,
                                  double notaMinimaAprovacao) {
        this.provaId       = provaId;
        this.tituloProva   = tituloProva;
        this.totalQuestoes = totalQuestoes;
        this.acertos       = acertos;
        this.nota = totalQuestoes > 0
            ? Math.round((acertos * 10.0 / totalQuestoes) * 100.0) / 100.0
            : 0.0;
        this.situacao  = this.nota >= notaMinimaAprovacao ? "Aprovado" : "Reprovado";
        this.mensagem  = this.situacao.equals("Aprovado")
            ? "Parabéns! Você foi aprovado nesta prova."
            : "Você não atingiu a nota mínima. Continue estudando!";
        this.totalIncidentes = 0;  // 🔧 INICIALIZA
    }

    public Integer getProvaId()        { return provaId; }
    public String getTituloProva()     { return tituloProva; }
    public long getTotalQuestoes()     { return totalQuestoes; }
    public long getAcertos()           { return acertos; }
    public double getNota()            { return nota; }
    public String getSituacao()        { return situacao; }
    public String getMensagem()        { return mensagem; }
    
    // 🔧 NOVOS GETTER E SETTER
    public long getTotalIncidentes()   { return totalIncidentes; }
    public void setTotalIncidentes(long totalIncidentes) { this.totalIncidentes = totalIncidentes; }
}