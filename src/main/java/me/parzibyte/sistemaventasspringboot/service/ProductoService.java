package me.parzibyte.sistemaventasspringboot.service;

import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.util.StringUtils;

import me.parzibyte.sistemaventasspringboot.entity.Producto;
import me.parzibyte.sistemaventasspringboot.entity.ProductoApartado;
import me.parzibyte.sistemaventasspringboot.repository.ProductoApartadoRepository;
import me.parzibyte.sistemaventasspringboot.repository.ProductosRepository;

@Service
public class ProductoService {

    private final ProductosRepository productos;
    private final ProductoApartadoRepository apartados;

    public ProductoService(ProductosRepository productos, ProductoApartadoRepository apartados) {
        this.productos = productos;
        this.apartados = apartados;
    }

    public List<Producto> listarTodos() {
        return productos.findAll();
    }

    public String guardar(Producto p, BindingResult result, RedirectAttributes ra) {
        // Normaliza
        if (p.getCodigo() != null) p.setCodigo(p.getCodigo().trim());
        if (p.getNombre() != null) p.setNombre(p.getNombre().trim());

        // Duplicados
        if (p.getCodigo() != null && productos.existsByCodigoIgnoreCase(p.getCodigo())) {
            result.rejectValue("codigo", "duplicado", "Ya existe un producto con ese código");
        }
        if (p.getNombre() != null && productos.existsByNombreIgnoreCase(p.getNombre())) {
            result.rejectValue("nombre", "duplicado", "Ya existe un producto con ese nombre");
        }
        if (result.hasErrors()) return "productos/agregar_producto";

        productos.save(p); // aquí es nuevo; si no hay imagenUrl, queda null
        ra.addFlashAttribute("mensaje", "Producto guardado correctamente")
          .addFlashAttribute("clase", "success");
        return "redirect:/productos/mostrar";
    }

    @Transactional
    public String eliminar(Integer id, RedirectAttributes ra) {
        Optional<Producto> opt = productos.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("mensaje", "Producto no encontrado")
              .addFlashAttribute("clase", "danger");
            return "redirect:/productos/mostrar";
        }
        List<ProductoApartado> ties = apartados.findByProducto(opt.get());
        if (!ties.isEmpty()) {
            ra.addFlashAttribute("mensaje", "No se puede eliminar: tiene apartados activos")
              .addFlashAttribute("clase", "danger");
            return "redirect:/productos/mostrar";
        }
        productos.deleteById(id);
        ra.addFlashAttribute("mensaje", "Producto eliminado correctamente")
          .addFlashAttribute("clase", "success");
        return "redirect:/productos/mostrar";
    }

    public String editarVista(Integer id, Model model, RedirectAttributes ra) {
        return productos.findById(id).map(p -> {
            model.addAttribute("producto", p);
            return "productos/editar_producto";
        }).orElseGet(() -> {
            ra.addFlashAttribute("mensaje", "Producto no encontrado")
              .addFlashAttribute("clase", "danger");
            return "redirect:/productos/mostrar";
        });
    }

    public String actualizar(Integer id, Producto p, BindingResult result, RedirectAttributes ra) {
        // Normaliza
        if (p.getCodigo() != null) p.setCodigo(p.getCodigo().trim());
        if (p.getNombre() != null) p.setNombre(p.getNombre().trim());

        // Duplicados (excluyendo el propio id)
        if (p.getCodigo() != null && productos.existsByCodigoIgnoreCaseAndIdNot(p.getCodigo(), id)) {
            result.rejectValue("codigo", "duplicado", "Código ya usado por otro producto");
        }
        if (p.getNombre() != null && productos.existsByNombreIgnoreCaseAndIdNot(p.getNombre(), id)) {
            result.rejectValue("nombre", "duplicado", "Nombre ya usado por otro producto");
        }
        if (result.hasErrors()) return "productos/editar_producto";

        // --- Preservar imagenUrl y actualizar el área ---
        Producto db = productos.findById(id).orElse(null);
        if (db == null) {
            ra.addFlashAttribute("mensaje", "Producto no encontrado")
              .addFlashAttribute("clase", "danger");
            return "redirect:/productos/mostrar";
        }

        db.setNombre(p.getNombre());
        db.setCodigo(p.getCodigo());
        db.setPrecio(p.getPrecio());
        db.setExistencia(p.getExistencia());
        db.setApartado(p.getApartado());
        db.setFechaApartado(p.getFechaApartado());
        db.setApartadoId(p.getApartadoId());
        db.setArea(p.getArea());  // Aquí se actualiza el campo 'area'

        // Solo reemplaza si llegó una nueva URL desde el Controller (cuando subiste archivo)
        if (StringUtils.hasText(p.getImagenUrl())) {
            db.setImagenUrl(p.getImagenUrl());
        }
        // si p.getImagenUrl() es null o vacío, conserva la existente en db

        productos.save(db);

        ra.addFlashAttribute("mensaje", "Producto actualizado correctamente")
          .addFlashAttribute("clase", "success");
        return "redirect:/productos/mostrar";
    }
}
