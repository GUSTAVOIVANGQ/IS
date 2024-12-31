package com.version.nutrition.service;

import com.version.nutrition.model.Alimento;
import com.version.nutrition.repository.AlimentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AlimentoService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlimentoService.class);
    
    @Autowired
    private AlimentoRepository alimentoRepository;

    public List<Alimento> listarTodos() {
        List<Alimento> alimentos = alimentoRepository.findAll();
        logger.info("Total de alimentos encontrados: {}", alimentos.size());
        alimentos.forEach(a -> logger.debug("Alimento encontrado: {}", a.getNombre()));
        return alimentos;
    }

    public Optional<Alimento> buscarPorId(Long id) {
        return alimentoRepository.findById(id);
    }
}
