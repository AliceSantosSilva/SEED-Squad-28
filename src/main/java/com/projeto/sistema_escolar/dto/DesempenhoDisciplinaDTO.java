package com.projeto.sistema_escolar.dto;

public class DesempenhoDisciplinaDTO {
    private Integer disciplinaId;
    private String disciplinaNome;
    private Long totalRespostas;
    private Long acertos;

    public DesempenhoDisciplinaDTO(Integer disciplinaId, String disciplinaNome,
                                    Long totalRespostas, Long acertos) {
        this.disciplinaId = disciplinaId;
        this.disciplinaNome = disciplinaNome;
        this.totalRespostas = totalRespostas;
        this.acertos = acertos;
    }

    public Integer getDisciplinaId() { return disciplinaId; }
    public String getDisciplinaNome() { return disciplinaNome; }
    public Long getTotalRespostas() { return totalRespostas; }
    public Long getAcertos() { return acertos; }

    public double getPercentualAcerto() {
        if (totalRespostas == null || totalRespostas == 0) return 0.0;
        long ac = acertos != null ? acertos : 0L;
        return Math.round((ac * 100.0 / totalRespostas) * 100.0) / 100.0;
    }
}