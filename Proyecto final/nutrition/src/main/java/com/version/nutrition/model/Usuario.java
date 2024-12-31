package com.version.nutrition.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
public abstract class Usuario {
    @Id
    @GeneratedValue
    private Long id;

    // Change from private to protected for inheritance access
    protected String email;
    protected String password;
    protected String rol;
    protected boolean activo;
    protected String resetPasswordToken;
    protected String fotoPerfil = "default-avatar.png"; // Valor predeterminado

    // Getters y Setters
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
        
    public void setRol(String rol) {
        this.rol = rol;
    }
    public String getRol() {
        return rol;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getResetPasswordToken() {
        return resetPasswordToken;
    }
    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    // Agregar getter para id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}