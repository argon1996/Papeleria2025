package me.parzibyte.sistemaventasspringboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import me.parzibyte.sistemaventasspringboot.entity.Abono;
import me.parzibyte.sistemaventasspringboot.entity.ProductoApartado;
import me.parzibyte.sistemaventasspringboot.repository.AbonoRepository;
import me.parzibyte.sistemaventasspringboot.repository.ProductoApartadoRepository;

import java.util.List;

@Controller
public class HistorialController {

    @Autowired
    private AbonoRepository abonoRepository;

    @Autowired
    private ProductoApartadoRepository productoApartadoRepository;

    @GetMapping("/historial-abonos")
    public String verHistorialAbonos(@RequestParam("id") Integer productoApartadoId, Model model) {
        ProductoApartado productoApartado =
                productoApartadoRepository.findById(productoApartadoId).orElse(null);

        if (productoApartado == null) {
            model.addAttribute("mensaje", "No se encontró el producto apartado con id " + productoApartadoId);
            model.addAttribute("clase", "warning");
            return "productos/historial_abonos"; // muestra aviso en la misma vista
        }

        List<Abono> abonos =
                abonoRepository.findByProductoApartadoIdOrderByFechaAbonoDesc(productoApartadoId);

        model.addAttribute("productoApartado", productoApartado);
        model.addAttribute("abonos", abonos);
        return "productos/historial_abonos";
    }
}
