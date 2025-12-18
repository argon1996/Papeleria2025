package me.parzibyte.sistemaventasspringboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import me.parzibyte.sistemaventasspringboot.entity.Producto;
import me.parzibyte.sistemaventasspringboot.repository.ProductosRepository;

import java.util.List;

@Controller
@RequestMapping("/faltantes")
public class FaltantesController {

    @Autowired
    private ProductosRepository productosRepository;

    private static final Float UMBRAL_FALTANTES = 10.0f; // Define el umbral para considerar que un producto está faltante

    @GetMapping
    public String mostrarFaltantes(Model model) {
        List<Producto> listaDeFaltantes = productosRepository.findProductosFaltantes(UMBRAL_FALTANTES);
        model.addAttribute("faltantes", listaDeFaltantes);
        return "faltantes/faltantes";
    }
}
