package com.cerraduras.cerraduras.service;

import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class CerraduraService {

    public Set<String> calcularCerraduraEstrella(int n) {
        Set<String> resultado = new HashSet<>();
        resultado.add("");  // λ (cadena vacía)
        
        for (int i = 1; i <= n; i++) {
            generarCadenasBinarias("", i, resultado);
        }
        
        return resultado;
    }

    public Set<String> calcularCerraduraPositiva(int n) {
        Set<String> resultado = new HashSet<>();
        
        for (int i = 1; i <= n; i++) {
            generarCadenasBinarias("", i, resultado);
        }
        
        return resultado;
    }

    private void generarCadenasBinarias(String prefijo, int longitud, Set<String> resultado) {
        if (longitud == 0) {
            resultado.add(prefijo);
            return;
        }
        
        generarCadenasBinarias(prefijo + "0", longitud - 1, resultado);
        generarCadenasBinarias(prefijo + "1", longitud - 1, resultado);
    }
}