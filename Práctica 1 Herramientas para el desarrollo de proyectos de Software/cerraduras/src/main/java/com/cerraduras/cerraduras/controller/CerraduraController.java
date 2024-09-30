package com.cerraduras.cerraduras.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.cerraduras.cerraduras.service.CerraduraService;
import java.util.Set;

@RestController
@RequestMapping("/api/cerradura")
@CrossOrigin(origins = "*")  // Permite solicitudes de cualquier origen
public class CerraduraController {

    @Autowired
    private CerraduraService cerraduraService;

    @PutMapping("/estrella")
    public Set<String> cerraduraEstrella(@RequestParam int n) {
        return cerraduraService.calcularCerraduraEstrella(n);
    }

    @PutMapping("/positiva")
    public Set<String> cerraduraPositiva(@RequestParam int n) {
        return cerraduraService.calcularCerraduraPositiva(n);
    }
}

