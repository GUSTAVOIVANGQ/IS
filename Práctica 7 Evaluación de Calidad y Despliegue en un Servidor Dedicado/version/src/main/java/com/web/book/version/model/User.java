package com.web.book.version.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    // In your User class:
    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    private RoleType rol;

    @NotBlank
    @Size(max = 20)
    @Column(unique = true)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    @Size(max = 120)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @Column(name = "nombre")
    private String nombre;
    
    @Column(name = "apellido") 
    private String apellido;

    @Column(name = "fecha_registro")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;

    @Column(name = "activo")
    private Boolean activo = true; // Valor por defecto

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<SearchHistory> searchHistories = new HashSet<>();

    // Default constructor
    public User() {
        }
    // Method to check if the user is empty
    public boolean isEmpty() {
        return username == null || username.isEmpty();
    }
    // Add getters/setters
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getApellido() {
        return apellido;
    }
    
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Set<Role> getRoles() {
        return roles;
    }

    public RoleType getRol() {
        return rol;
    }

    public void setRol(RoleType rol) {
        this.rol = rol;
    }    

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
    @Column
    private String profilePhotoUrl;
    
    // Add getter and setter
    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }
    
    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    // Métodos helper para la relación bidireccional
    public void addSearchHistory(SearchHistory searchHistory) {
        searchHistories.add(searchHistory);
        searchHistory.setUser(this);
    }
    
    public void removeSearchHistory(SearchHistory searchHistory) {
        searchHistories.remove(searchHistory);
        searchHistory.setUser(null);
    }
    
    public Set<SearchHistory> getSearchHistories() {
        return searchHistories;
    }

    public void setSearchHistories(Set<SearchHistory> searchHistories) {
        this.searchHistories = searchHistories;
    }

}