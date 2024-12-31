package com.version.nutrition.service;

import com.version.nutrition.model.Usuario;
import com.version.nutrition.model.Paciente;
import com.version.nutrition.model.Nutricionista;
import com.version.nutrition.repository.PacienteRepository;
import com.version.nutrition.repository.NutricionistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private NutricionistaRepository nutricionistaRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Primero buscar en pacientes
        Optional<Paciente> paciente = pacienteRepository.findByEmail(email);
        if (paciente.isPresent()) {
            return createUserDetails(paciente.get());
        }

        // Si no es paciente, buscar en nutricionistas
        Optional<Nutricionista> nutricionista = nutricionistaRepository.findByEmail(email);
        if (nutricionista.isPresent()) {
            return createUserDetails(nutricionista.get());
        }

        throw new UsernameNotFoundException("Usuario no encontrado: " + email);
    }

    private UserDetails createUserDetails(Usuario usuario) {
        return new User(
            usuario.getEmail(),
            usuario.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority(usuario.getRol()))
        );
    }

    public String iniciarRecuperacionPassword(String email) {
        String token = UUID.randomUUID().toString();
        
        // Intentar actualizar token en paciente
        Optional<Paciente> paciente = pacienteRepository.findByEmail(email);
        if (paciente.isPresent()) {
            Paciente p = paciente.get();
            p.setResetPasswordToken(token);
            pacienteRepository.save(p);
            return token;
        }

        // Intentar actualizar token en nutricionista
        Optional<Nutricionista> nutricionista = nutricionistaRepository.findByEmail(email);
        if (nutricionista.isPresent()) {
            Nutricionista n = nutricionista.get();
            n.setResetPasswordToken(token);
            nutricionistaRepository.save(n);
            return token;
        }

        throw new UsernameNotFoundException("Usuario no encontrado: " + email);
    }

    public void resetearPassword(String token, String newPassword) {
        // Buscar usuario por token y actualizar contraseña
        Optional<Paciente> paciente = findPacienteByResetToken(token);
        if (paciente.isPresent()) {
            Paciente p = paciente.get();
            p.setPassword(passwordEncoder.encode(newPassword));
            p.setResetPasswordToken(null);
            pacienteRepository.save(p);
            return;
        }

        Optional<Nutricionista> nutricionista = findNutricionistaByResetToken(token);
        if (nutricionista.isPresent()) {
            Nutricionista n = nutricionista.get();
            n.setPassword(passwordEncoder.encode(newPassword));
            n.setResetPasswordToken(null);
            nutricionistaRepository.save(n);
            return;
        }

        throw new IllegalArgumentException("Token inválido");
    }

    public boolean validarToken(String token) {
        return findPacienteByResetToken(token).isPresent() || 
               findNutricionistaByResetToken(token).isPresent();
    }

    private Optional<Paciente> findPacienteByResetToken(String token) {
        return pacienteRepository.findAll().stream()
            .filter(p -> token.equals(p.getResetPasswordToken()))
            .findFirst();
    }

    private Optional<Nutricionista> findNutricionistaByResetToken(String token) {
        return nutricionistaRepository.findAll().stream()
            .filter(n -> token.equals(n.getResetPasswordToken()))
            .findFirst();
    }

    public boolean verificarCredenciales(String email, String password) {
        UserDetails userDetails = loadUserByUsername(email);
        return passwordEncoder.matches(password, userDetails.getPassword());
    }
    

    public void updateProfilePhoto(String username, String fileName) {
        // Primero buscar en pacientes
        Optional<Paciente> paciente = pacienteRepository.findByEmail(username);
        if (paciente.isPresent()) {
            Paciente p = paciente.get();
            String oldPhoto = p.getFotoPerfil();
            p.setFotoPerfil(fileName);
            pacienteRepository.save(p);
            deleteOldPhotoIfExists(oldPhoto);
            return;
        }

        // Si no es paciente, buscar en nutricionistas
        Optional<Nutricionista> nutricionista = nutricionistaRepository.findByEmail(username);
        if (nutricionista.isPresent()) {
            Nutricionista n = nutricionista.get();
            String oldPhoto = n.getFotoPerfil();
            n.setFotoPerfil(fileName);
            nutricionistaRepository.save(n);
            deleteOldPhotoIfExists(oldPhoto);
            return;
        }

        throw new UsernameNotFoundException("Usuario no encontrado: " + username);
    }

    private void deleteOldPhotoIfExists(String oldPhotoFileName) {
        if (oldPhotoFileName == null || oldPhotoFileName.startsWith("default-")) {
            return; // No eliminar fotos predeterminadas
        }

        try {
            Path oldPhotoPath = Paths.get("uploads/profile-photos", oldPhotoFileName);
            Files.deleteIfExists(oldPhotoPath);
        } catch (IOException e) {
            // Log error pero no interrumpir la operación
            System.err.println("Error al eliminar foto antigua: " + e.getMessage());
        }
    }

    public String getDefaultProfilePhoto(String role, String gender) {
        if ("ROLE_NUTRICIONISTA".equals(role)) {
            return "MASCULINO".equals(gender) ? "nutricionista-male-default.png" : 
                   "FEMENINO".equals(gender) ? "nutricionista-female-default.png" : 
                   "nutricionista-default.png";
        } else if ("ROLE_PACIENTE".equals(role)) {
            return "MASCULINO".equals(gender) ? "paciente-male-default.png" : 
                   "FEMENINO".equals(gender) ? "paciente-female-default.png" : 
                   "paciente-default.png";
        }
        return "default-avatar.png";
    }
    
    public void updateUserProfile(String username, Usuario updatedUser) {
        Optional<Paciente> paciente = pacienteRepository.findByEmail(username);
        if (paciente.isPresent()) {
            Paciente p = paciente.get();
            p.setNombre(p.getNombre());
            p.setApellido(p.getApellido());
            p.setTelefono(p.getTelefono());
            p.setDireccion(p.getDireccion());
            pacienteRepository.save(p);
            return;
        }
        // Similar para nutricionista si es necesario
    }
}
