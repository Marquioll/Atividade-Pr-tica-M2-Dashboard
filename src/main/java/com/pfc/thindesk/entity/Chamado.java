package com.pfc.thindesk.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "chamados")
public class Chamado{
    
    @Id
    private String id;
    private String descricao;
    private String status;
    private String tipo;
    private String tecnico;
    private String usuario;
    private java.time.Instant criadoEm;
    private java.time.Instant concluidoEm;

    public java.time.Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(java.time.Instant data) { criadoEm = data; }
    public java.time.Instant getConcluidoEm() { return concluidoEm; }
    public void setConcluidoEm(java.time.Instant data) { concluidoEm = data; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTecnico() {
        return tecnico;
    }

    public void setTecnico(String tecnico) {
        this.tecnico = tecnico;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
    
    
    
}
