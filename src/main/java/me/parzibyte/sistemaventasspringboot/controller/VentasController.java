package me.parzibyte.sistemaventasspringboot.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.parzibyte.sistemaventasspringboot.entity.Venta;
import me.parzibyte.sistemaventasspringboot.service.VentaService;

@Controller
@RequestMapping(path = "/ventas")
public class VentasController {

    private final VentaService ventaService;

    public VentasController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    // ✅ Mostrar lista de ventas
    @GetMapping("")
    public String mostrarVentas(Model model) {
        List<Venta> ventas = ventaService.listar();
        model.addAttribute("ventas", ventas);
        return "ventas/ver_ventas";
    }

    // ✅ Eliminar venta (solo ADMIN puede hacerlo)
    @GetMapping("/eliminar/{id}")
    public String eliminarVenta(@PathVariable long id, RedirectAttributes ra) {
        // Verificar si el usuario actual es ADMIN
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));

        if (!esAdmin) {
            ra.addFlashAttribute("mensaje", "🚫 No tienes permisos de administrador para eliminar ventas.")
              .addFlashAttribute("clase", "danger");
            return "redirect:/ventas";
        }

        if (ventaService.eliminarVenta(id)) {
            ra.addFlashAttribute("mensaje", "✅ Venta eliminada correctamente.")
              .addFlashAttribute("clase", "success");
        } else {
            ra.addFlashAttribute("mensaje", "⚠️ Venta no encontrada.")
              .addFlashAttribute("clase", "warning");
        }
        return "redirect:/ventas";
    }

    // ✅ Reimprimir venta (disponible para todos)
    @GetMapping("/reimprimir/{id}")
    public String reimprimir(@PathVariable Long id, RedirectAttributes ra) {
        if (ventaService.reimprimirVenta(id)) {
            ra.addFlashAttribute("mensaje", "🧾 Reimpresión procesada correctamente.")
              .addFlashAttribute("clase", "success");
        } else {
            ra.addFlashAttribute("mensaje", "❌ No se encontró la venta para reimprimir.")
              .addFlashAttribute("clase", "danger");
        }
        return "redirect:/ventas";
    }
}
