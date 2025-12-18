package me.parzibyte.sistemaventasspringboot.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.parzibyte.sistemaventasspringboot.entity.Producto;
import me.parzibyte.sistemaventasspringboot.entity.ProductoParaVender;
import me.parzibyte.sistemaventasspringboot.repository.ProductosRepository;

@Service
public class CartService {

    private final ProductosRepository productos;

    public CartService(ProductosRepository productos) {
        this.productos = productos;
    }

    public void addByCodigo(List<ProductoParaVender> carrito, String codigo, float qty, RedirectAttributes ra) {
        String cod = (codigo == null) ? "" : codigo.trim();
        if (cod.isEmpty()) {
            flash(ra, "Código vacío", "warning");
            return;
        }

        Producto p = productos.findFirstByCodigo(cod);
        if (p == null) {
            flash(ra, "El producto con código " + cod + " no existe", "warning");
            return;
        }

        ProductoParaVender existente = carrito.stream()
                .filter(i -> i.getCodigo().equals(cod))
                .findFirst()
                .orElse(null);

        float nueva = (existente == null ? qty : existente.getCantidad() + qty);

        if (nueva > p.getExistencia()) {
            flash(ra, "Stock insuficiente de \"" + p.getNombre() + "\"", "warning");
            return;
        }

        if (existente == null) {
            // Usa el factory para copiar también la imagenUrl
            carrito.add(ProductoParaVender.from(p, qty));
        } else {
            existente.setCantidad(nueva);
        }
    }

    public void remove(List<ProductoParaVender> carrito, int index) {
        if (index >= 0 && index < carrito.size()) carrito.remove(index);
    }

    public void updateCantidad(List<ProductoParaVender> carrito, int id, float cantidad) {
        carrito.stream()
               .filter(x -> Objects.equals(x.getId(), id))
               .findFirst()
               .ifPresent(x -> x.setCantidad(Math.max(cantidad, 0)));
    }

    public float total(List<ProductoParaVender> carrito) {
        return (float) carrito.stream().mapToDouble(ProductoParaVender::getTotal).sum();
    }

    private void flash(RedirectAttributes ra, String msg, String clase) {
        ra.addFlashAttribute("mensaje", msg).addFlashAttribute("clase", clase);
    }
}
