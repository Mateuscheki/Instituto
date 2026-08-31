package edu.unialfa.institutoMario.model;

import jakarta.persistence.*;

@Entity
public class Presenca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chamada_id")
    private Chamada chamada;

    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    private Boolean presente;

    // Construtor padrão obrigatório pelo JPA
    public Presenca() {
    }

    // Construtor auxiliar para facilitar a criação de novos registros
    public Presenca(Chamada chamada, Aluno aluno, Boolean presente) {
        this.chamada = chamada;
        this.aluno = aluno;
        this.presente = presente;
    }

    // --- MÉTODOS GETTERS E SETTERS ---

    // Método exigido para resolver o erro "cannot find symbol: method getId()"
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Chamada getChamada() {
        return chamada;
    }

    public void setChamada(Chamada chamada) {
        this.chamada = chamada;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Boolean getPresente() {
        return presente;
    }

    // Método exigido para atualizar o status de presença (presente/falta)
    public void setPresente(Boolean presente) {
        this.presente = presente;
    }
}