package me.parzibyte.sistemaventasspringboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.parzibyte.sistemaventasspringboot.entity.Abono;
import me.parzibyte.sistemaventasspringboot.entity.ProductoApartado;
import me.parzibyte.sistemaventasspringboot.repository.AbonoRepository;
import me.parzibyte.sistemaventasspringboot.repository.ProductoApartadoRepository;
import me.parzibyte.sistemaventasspringboot.service.ApartadoService;

import java.util.List;

@Controller
@RequestMapping("/apartados")
public class ApartadosController {

    @Autowired private ProductoApartadoRepository productoApartadoRepository;
    @Autowired private AbonoRepository abonoRepository;
    @Autowired private ApartadoService apartadoService;

    @GetMapping
    public String mostrarApartados(Model model) {
        List<ProductoApartado> apartados = productoApartadoRepository.findAll();
        if (apartados.isEmpty()) {
            model.addAttribute("mensaje", "No hay productos apartados disponibles");
            model.addAttribute("clase", "info");
        } else {
            model.addAttribute("apartados", apartados);
        }
        return "productos/apartados"; // <-- ruta correcta
    }

    @GetMapping("/{id}/historial")
    public String historialAbonos(@PathVariable Integer id, Model model) {
        ProductoApartado productoApartado = productoApartadoRepository.findById(id).orElse(null);
        if (productoApartado != null) {
            List<Abono> abonos = abonoRepository.findByProductoApartadoIdOrderByFechaAbonoDesc(id);
            model.addAttribute("productoApartado", productoApartado);
            model.addAttribute("abonos", abonos);
        } else {
            model.addAttribute("mensaje", "Producto apartado no encontrado");
            model.addAttribute("clase", "danger");
        }
        return "productos/historial_abonos"; // <-- ruta correcta
    }

    @PostMapping("/{id}/retirar")
    public String retirar(@PathVariable Integer id, RedirectAttributes ra) {
        return apartadoService.retirar(id, ra);
    }
}
