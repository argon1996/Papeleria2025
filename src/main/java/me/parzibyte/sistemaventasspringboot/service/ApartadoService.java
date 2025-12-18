package me.parzibyte.sistemaventasspringboot.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.parzibyte.sistemaventasspringboot.entity.Abono;
import me.parzibyte.sistemaventasspringboot.entity.Producto;
import me.parzibyte.sistemaventasspringboot.entity.ProductoApartado;
import me.parzibyte.sistemaventasspringboot.entity.ProductoVendido;
import me.parzibyte.sistemaventasspringboot.entity.Venta;
import me.parzibyte.sistemaventasspringboot.repository.AbonoRepository;
import me.parzibyte.sistemaventasspringboot.repository.ProductoApartadoRepository;
import me.parzibyte.sistemaventasspringboot.repository.ProductosRepository;
import me.parzibyte.sistemaventasspringboot.repository.ProductosVendidosRepository;
import me.parzibyte.sistemaventasspringboot.repository.VentasRepository;

@Service
public class ApartadoService {

    private final ProductoApartadoRepository apartados;
    private final ProductosRepository productos;
    private final VentasRepository ventas;
    private final ProductosVendidosRepository vendidos;
    private final AbonoRepository abonos;

    public ApartadoService(ProductoApartadoRepository apartados,
                           ProductosRepository productos,
                           VentasRepository ventas,
                           ProductosVendidosRepository vendidos,
                           AbonoRepository abonos) {
        this.apartados = apartados;
        this.productos = productos;
        this.ventas = ventas;
        this.vendidos = vendidos;
        this.abonos = abonos;
    }

    /** listado para la vista */
    public List<ProductoApartado> listarTodos() {
        return apartados.findAllWithProductos();
    }

    /** carga historial para la vista */
    public String historial(Integer id, Model model) {
        Optional<ProductoApartado> opt = apartados.findById(id);
        if (opt.isEmpty()) {
            model.addAttribute("mensaje", "Producto apartado no encontrado");
            model.addAttribute("clase", "danger");
            return "apartados/historial";
        }
        model.addAttribute("productoApartado", opt.get());
        model.addAttribute("abonos", abonos.findByProductoApartadoIdOrderByFechaAbonoDesc(id));
        return "apartados/historial";
    }

    /** apartar producto */
    @Transactional
    public String apartar(Integer id, Float cantidad, String nombrePersona, Float abonoInicial, RedirectAttributes ra) {
        Optional<Producto> optP = productos.findById(id);
        if (optP.isEmpty()) {
            ra.addFlashAttribute("mensaje", "Producto no encontrado").addFlashAttribute("clase", "danger");
            return "redirect:/productos/apartados";
        }
        Producto p = optP.get();
        if (p.getExistencia() < cantidad) {
            ra.addFlashAttribute("mensaje", "No hay suficiente existencia para apartar")
              .addFlashAttribute("clase", "danger");
            return "redirect:/productos/apartados";
        }

        Float existenciaActual = p.getExistencia();
        p.restarExistencia(cantidad); // descuenta al apartar
        productos.save(p);

        ProductoApartado pa = new ProductoApartado(
                p, cantidad, LocalDateTime.now(), existenciaActual, nombrePersona, abonoInicial);
        apartados.save(pa);

        ra.addFlashAttribute("mensaje", "Producto apartado correctamente").addFlashAttribute("clase", "success");
        return "redirect:/productos/apartados";
    }

    /** retirar (convertir el apartado en venta) */
    @Transactional
    public String retirar(Integer id, RedirectAttributes ra) {
        Optional<ProductoApartado> opt = apartados.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("mensaje", "Producto apartado no encontrado").addFlashAttribute("clase", "danger");
            return "redirect:/apartados";
        }

        ProductoApartado pa = opt.get();
        Producto producto = pa.getProducto();
        if (producto == null) {
            ra.addFlashAttribute("mensaje", "Producto relacionado no encontrado").addFlashAttribute("clase", "danger");
            return "redirect:/apartados";
        }

        // Crear venta con lo apartado (NO devolver stock)
        Venta v = ventas.save(new Venta());

        ProductoVendido pv = new ProductoVendido(
                pa.getCantidad(), producto.getPrecio(),
                producto.getNombre(), producto.getCodigo(), v);
        vendidos.save(pv);

        Set<ProductoVendido> set = v.getProductos();
        if (set == null) set = new HashSet<>();
        set.add(pv);
        v.setProductos(set);
        ventas.save(v);

        // 1) borrar abonos (evita violación de FK)
        abonos.deleteByProductoApartadoId(id);
        // 2) borrar el apartado
        apartados.deleteById(id);

        ra.addFlashAttribute("mensaje", "Producto retirado (vendido) correctamente")
          .addFlashAttribute("clase", "success");
        return "redirect:/apartados";
    }

    /** listar apartados + buscador (para vista productos/apartados) */
    public String listarApartados(String search, Model model) {
        List<ProductoApartado> list = apartados.findAllWithProductos();
        model.addAttribute("apartados", list);
        model.addAttribute("producto", new Producto());

        if (search != null && !search.isBlank()) {
            model.addAttribute("productos",
                    productos.findByNombreContainingOrCodigoContaining(search, search));
        } else {
            model.addAttribute("productos", productos.findAll());
        }
        return "productos/apartados";
    }

    /** registrar abono */
    @Transactional
    public String abonar(Integer id, Float cantidad, RedirectAttributes ra) {
        Optional<ProductoApartado> opt = apartados.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("mensaje", "Producto apartado no encontrado").addFlashAttribute("clase", "danger");
            return "redirect:/apartados";
        }
        ProductoApartado pa = opt.get();
        if (cantidad == null || cantidad <= 0f) {
            ra.addFlashAttribute("mensaje", "Cantidad de abono no válida").addFlashAttribute("clase", "danger");
            return "redirect:/apartados";
        }
        Float pendiente = pa.getAbonoPendiente();
        if (cantidad > pendiente) {
            ra.addFlashAttribute("mensaje", "El abono no puede ser mayor al pendiente")
              .addFlashAttribute("clase", "danger");
            return "redirect:/apartados";
        }

        LocalDateTime fecha = LocalDateTime.now();
        Abono ab = new Abono(pa, cantidad, fecha);
        abonos.save(ab);

        if (pa.getTotalAbono() == null) pa.setTotalAbono(0f);
        pa.setFechaUltimoAbono(fecha);
        pa.setTotalAbono(pa.getTotalAbono() + cantidad);
        apartados.save(pa);

        ra.addFlashAttribute("mensaje", "Abono registrado correctamente").addFlashAttribute("clase", "success");
        return "redirect:/apartados";
    }
}
