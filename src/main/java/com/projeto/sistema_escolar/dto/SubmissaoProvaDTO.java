package com.projeto.sistema_escolar.dto;

import java.util.List;

public class SubmissaoProvaDTO {

    private Integer provaId;
    private List<RespostaItemDTO> respostas;

    public Integer getProvaId()                    { return provaId; }
    public void setProvaId(Integer provaId)        { this.provaId = provaId; }

    public List<RespostaItemDTO> getRespostas()    { return respostas; }
    public void setRespostas(List<RespostaItemDTO> respostas) { this.respostas = respostas; }

    // Item individual: qual alternativa o aluno escolheu para cada questão
    public static class RespostaItemDTO {
        private Integer questaoId;
        private Integer alternativaId;

        public Integer getQuestaoId()                      { return questaoId; }
        public void setQuestaoId(Integer questaoId)        { this.questaoId = questaoId; }

        public Integer getAlternativaId()                  { return alternativaId; }
        public void setAlternativaId(Integer alternativaId){ this.alternativaId = alternativaId; }
    }
}