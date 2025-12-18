package me.parzibyte.sistemaventasspringboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import me.parzibyte.sistemaventasspringboot.repository.ProductosRepository;

@Controller
public class ShopController {

    private final ProductosRepository productos;

    // Constructor para inyectar el repositorio de productos
    public ShopController(ProductosRepository productos) {
        this.productos = productos;
    }

    // Mapea la ruta "/" y "/shop" para mostrar la tienda
    @GetMapping({ "/", "/shop" })
    public String index(@RequestParam(name = "search", required = false) String search,
                        @RequestParam(name = "categoria", required = false) String categoria, // Agregado parámetro para filtrar por categoría
                        Model model) {

        final String term = search != null ? search.trim() : null;
        final String area = categoria != null ? categoria.trim() : null;

        // Si hay un término de búsqueda, consulta productos que coincidan con el nombre o código (insensible a mayúsculas)
        if (term != null && !term.isEmpty()) {
            model.addAttribute("productos", productos.searchByTermAndArea(term, area));
        } else {
            // Si no hay término de búsqueda, muestra los productos con existencia mayor a cero
            if (area != null && !area.isEmpty()) {
                // Si hay una categoría seleccionada, muestra productos con existencia mayor a cero para esa categoría
                model.addAttribute("productos", productos.findByExistenciaGreaterThanAndArea(0f, area));
            } else {
                // Si no hay categoría, muestra todos los productos con existencia mayor a cero
                model.addAttribute("productos", productos.findByExistenciaGreaterThan(0f));
            }
        }
        return "shop/index"; // Retorna la vista de la tienda
    }
}
