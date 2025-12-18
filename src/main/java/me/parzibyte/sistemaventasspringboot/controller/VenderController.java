package me.parzibyte.sistemaventasspringboot.controller;

import java.util.ArrayList;
import java.util.List;

import me.parzibyte.sistemaventasspringboot.entity.Producto;
import me.parzibyte.sistemaventasspringboot.entity.ProductoParaVender;
import me.parzibyte.sistemaventasspringboot.entity.Venta;
import me.parzibyte.sistemaventasspringboot.service.CartService;
import me.parzibyte.sistemaventasspringboot.service.FacturaService;
import me.parzibyte.sistemaventasspringboot.service.VentaService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vender")
@SessionAttributes("carrito")
public class VenderController {

    private final CartService cart;
    private final VentaService ventas;
    private final FacturaService factura;

    public VenderController(CartService cart, VentaService ventas, FacturaService factura) {
        this.cart = cart;
        this.ventas = ventas;
        this.factura = factura;
    }

    @ModelAttribute("carrito")
    public List<ProductoParaVender> initCarrito() {
        return new ArrayList<>();
    }

    @GetMapping("/")
    public String ui(Model model, @ModelAttribute("carrito") List<ProductoParaVender> carrito) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("total", cart.total(carrito));
        List<Venta> lista = ventas.listar();
        model.addAttribute("ventas", lista);
        return "vender/vender";
    }

    @PostMapping("/agregar")
    public String agregar(@ModelAttribute Producto producto,
            @ModelAttribute("carrito") List<ProductoParaVender> carrito,
            RedirectAttributes ra) {
        cart.addByCodigo(carrito, producto.getCodigo(), 1f, ra);
        return "redirect:/vender/";
    }

    @PostMapping("/agregarAutomatico")
    public String agregarAuto(@RequestParam String codigo,
            @ModelAttribute("carrito") List<ProductoParaVender> carrito,
            RedirectAttributes ra) {
        cart.addByCodigo(carrito, codigo, 1f, ra);
        return "redirect:/vender/";
    }

    @PostMapping("/quitar/{i}")
    public String quitar(@PathVariable int i,
            @ModelAttribute("carrito") List<ProductoParaVender> carrito) {
        cart.remove(carrito, i);
        return "redirect:/vender/";
    }

    @GetMapping("/limpiar")
    public String limpiar(@ModelAttribute("carrito") List<ProductoParaVender> carrito,
            RedirectAttributes ra) {
        carrito.clear();
        ra.addFlashAttribute("mensaje", "Venta cancelada").addFlashAttribute("clase", "info");
        return "redirect:/vender/";
    }

    @PostMapping("/actualizarCantidad/{id}")
    public String actualizar(@PathVariable int id, @RequestParam float cantidad,
            @ModelAttribute("carrito") List<ProductoParaVender> carrito) {
        cart.updateCantidad(carrito, id, cantidad);
        return "redirect:/vender/";
    }

    @PostMapping("/terminar")
    public String terminar(@ModelAttribute("carrito") List<ProductoParaVender> carrito,
            RedirectAttributes ra) {
        if (carrito.isEmpty())
            return "redirect:/vender/";
        return ventas.terminarVenta(carrito, ra);
    }

    // VenderController.java
    @PostMapping("/ventas/imprimir-factura")
    public String imprimir(@RequestParam Long id, RedirectAttributes ra) {
        factura.imprimir(id, ra);
        return "redirect:/ventas";
    }

}
