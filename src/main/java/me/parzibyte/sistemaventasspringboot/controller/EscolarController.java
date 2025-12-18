package me.parzibyte.sistemaventasspringboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import me.parzibyte.sistemaventasspringboot.repository.ProductosRepository;

@Controller
public class EscolarController {

    private final ProductosRepository productos;

    public EscolarController(ProductosRepository productos) {
        this.productos = productos;
    }

    @GetMapping("/escolar")
    public String publico(Model model) {
        model.addAttribute("productos", productos.findAll());
        return "escolar/publico";
    }

    @GetMapping("/escolar/vendedor")
    public String vendedor(Model model) {
        model.addAttribute("productos", productos.findAll());
        return "escolar/vendedor";
    }
}
