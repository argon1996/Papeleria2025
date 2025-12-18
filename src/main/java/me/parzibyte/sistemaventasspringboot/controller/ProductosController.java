package me.parzibyte.sistemaventasspringboot.controller;

import javax.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.parzibyte.sistemaventasspringboot.entity.Producto;
import me.parzibyte.sistemaventasspringboot.service.ProductoService;
import me.parzibyte.sistemaventasspringboot.service.ApartadoService;
import me.parzibyte.sistemaventasspringboot.service.ImageService;

@Controller
@RequestMapping("/productos")
public class ProductosController {

    private final ProductoService productos;
    private final ApartadoService apartados;
    private final ImageService imageService;

    public ProductosController(ProductoService productos,
                               ApartadoService apartados,
                               ImageService imageService) {
        this.productos = productos;
        this.apartados = apartados;
        this.imageService = imageService;
    }

    // -------- Productos --------
    @GetMapping({"/agregar", "/nuevo"})
    public String agregar(Model model) {
        model.addAttribute("producto", new Producto());
        return "productos/agregar_producto";
    }

    @PostMapping({"/agregar", "/guardar"})
    public String guardar(@Valid @ModelAttribute Producto producto,
                          BindingResult result,
                          @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                          RedirectAttributes ra) {
        try {
            if (imagen != null && !imagen.isEmpty()) {
                String url = imageService.saveWithWhiteBg(imagen);
                producto.setImagenUrl(url);
            }
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "❌ No se pudo procesar la imagen")
              .addFlashAttribute("clase", "danger");
        }
        return productos.guardar(producto, result, ra);
    }

    @GetMapping("/mostrar")
    public String mostrar(Model model) {
        model.addAttribute("productos", productos.listarTodos());
        return "productos/ver_productos";
    }

    // ✅ Solo el ADMIN puede eliminar productos
    @PostMapping("/eliminar")
    public String eliminar(@RequestParam("id") Integer id, RedirectAttributes ra) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));

        if (!esAdmin) {
            ra.addFlashAttribute("mensaje", "🚫 No tienes permisos de administrador para eliminar productos.")
              .addFlashAttribute("clase", "danger");
            return "redirect:/productos/mostrar";
        }

        return productos.eliminar(id, ra);
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        return productos.editarVista(id, model, ra);
    }

    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable Integer id,
                             @Valid @ModelAttribute Producto p,
                             BindingResult result,
                             @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                             RedirectAttributes ra) {
        try {
            if (imagen != null && !imagen.isEmpty()) {
                String url = imageService.saveWithWhiteBg(imagen);
                p.setImagenUrl(url);
            }
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "❌ No se pudo procesar la imagen")
              .addFlashAttribute("clase", "danger");
        }
        return productos.actualizar(id, p, result, ra);
    }

    // -------- Apartados --------
    @GetMapping("/apartados")
    public String listarApartados(@RequestParam(name = "search", required = false) String search, Model model) {
        return apartados.listarApartados(search, model);
    }

    @PostMapping("/apartar")
    public String apartar(@RequestParam Integer id,
                          @RequestParam Float cantidad,
                          @RequestParam String nombrePersona,
                          @RequestParam Float abonoInicial,
                          RedirectAttributes ra) {
        return apartados.apartar(id, cantidad, nombrePersona, abonoInicial, ra);
    }

    @PostMapping("/apartado/retirar")
    public String retirar(@RequestParam("id") Integer id, RedirectAttributes ra) {
        return apartados.retirar(id, ra);
    }

    @PostMapping("/abonar")
    public String abonar(@RequestParam("id") Integer id,
                         @RequestParam("cantidad") Float cantidad,
                         RedirectAttributes ra) {
        return apartados.abonar(id, cantidad, ra);
    }
}
