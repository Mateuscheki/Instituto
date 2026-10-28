package br.com.imgazin.beneficios.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/beneficios")
public class BeneficiosHomeController {

    @GetMapping
    public String home(Model model) {
        model.addAttribute("tituloPagina", "Início");
        return "beneficios/home";
    }
}
