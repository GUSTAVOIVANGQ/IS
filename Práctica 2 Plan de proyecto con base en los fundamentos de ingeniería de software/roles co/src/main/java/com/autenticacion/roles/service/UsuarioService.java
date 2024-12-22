package com.autenticacion.roles.service;

import com.autenticacion.roles.model.Usuario;
import com.autenticacion.roles.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Usuario registrar(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    public Usuario obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    // Guardar nuevo usuario
    public void guardarUsuario(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));  // Encriptar contraseña
        usuarioRepository.save(usuario);
    }    
}
