package com.web.book.version.controller;

import com.web.book.version.model.CerraduraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CerraduraController {

    @Autowired
    private CerraduraService cerraduraService;

    @GetMapping("/kleene")
    public Map<String, String> getKleeneCerradura(@RequestParam int n) {
        return cerraduraService.kleeneCerradura(n);
    }

    @GetMapping("/positive")
    public Map<String, String> getKleeneClausuraPositiva(@RequestParam int n) {
        return cerraduraService.kleeneClausuraPositiva(n);
    }
}