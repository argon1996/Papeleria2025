package me.parzibyte.sistemaventasspringboot;

import me.parzibyte.prueba.CashDrawerService;
import me.parzibyte.sistemaventasspringboot.entity.ProductoParaVender;
import me.parzibyte.sistemaventasspringboot.entity.ProductoVendido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(path = "/vender")
public class VenderController {

    @Autowired
    private ProductosRepository productosRepository;
    @Autowired
    private VentasRepository ventasRepository;
    @Autowired
    private ProductosVendidosRepository productosVendidosRepository;
    @Autowired
    private CashDrawerService cashDrawerService;

    // ===========================
    // MÉTODOS EXISTENTES
    // ===========================

    @PostMapping(value = "/quitar/{indice}")
    public String quitarDelCarrito(@PathVariable int indice, HttpServletRequest request) {
        ArrayList<ProductoParaVender> carrito = this.obtenerCarrito(request);
        if (carrito != null && carrito.size() > 0 && carrito.get(indice) != null) {
            carrito.remove(indice);
            this.guardarCarrito(carrito, request);
        }
        return "redirect:/vender/";
    }

    private void limpiarCarrito(HttpServletRequest request) {
        this.guardarCarrito(new ArrayList<>(), request);
    }

    @GetMapping(value = "/limpiar")
    public String cancelarVenta(HttpServletRequest request, RedirectAttributes redirectAttrs) {
        this.limpiarCarrito(request);
        redirectAttrs
                .addFlashAttribute("mensaje", "Venta cancelada")
                .addFlashAttribute("clase", "info");
        return "redirect:/vender/";
    }

    @PostMapping(value = "/terminar")
    public String terminarVenta(HttpServletRequest request, RedirectAttributes redirectAttrs) {
        ArrayList<ProductoParaVender> carrito = this.obtenerCarrito(request);
        if (carrito == null || carrito.isEmpty()) {
            return "redirect:/vender/";
        }

        // Validar stock
        for (ProductoParaVender item : carrito) {
            Producto p = productosRepository.findById(item.getId()).orElse(null);
            if (p == null) {
                redirectAttrs.addFlashAttribute("mensaje", "Producto no encontrado: " + item.getNombre())
                        .addFlashAttribute("clase", "warning");
                return "redirect:/vender/";
            }

            float disponible = p.getExistencia();
            float solicitado = item.getCantidad();
            if (solicitado > disponible) {
                redirectAttrs.addFlashAttribute(
                        "mensaje",
                        "Stock insuficiente de \"" + p.getNombre() +
                                "\". Disponible: " + disponible + ", solicitado: " + solicitado)
                        .addFlashAttribute("clase", "danger");
                return "redirect:/vender/";
            }
        }

        // Registrar venta
        Venta v = ventasRepository.save(new Venta());
        List<ProductoVendido> productosVendidos = new ArrayList<>();

        for (ProductoParaVender item : carrito) {
            Producto p = productosRepository.findById(item.getId()).orElse(null);
            if (p == null)
                continue;

            p.restarExistencia(item.getCantidad());
            productosRepository.save(p);

            ProductoVendido pv = new ProductoVendido(
                    item.getCantidad(),
                    item.getPrecio(),
                    item.getNombre(),
                    item.getCodigo(),
                    v);
            productosVendidos.add(pv);
            productosVendidosRepository.save(pv);
        }

        this.limpiarCarrito(request);
        redirectAttrs.addFlashAttribute("mensaje", "Venta realizada correctamente")
                .addFlashAttribute("clase", "success");

        try {
            cashDrawerService.openDrawer();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/vender/";
    }

    @PostMapping("/ventas/imprimir-factura")
    public String imprimirFactura(@RequestParam("id") Integer ventaId, RedirectAttributes redirectAttributes) {
        try {
            Optional<Venta> optionalVenta = ventasRepository.findByIdAsInteger(ventaId);
            if (optionalVenta.isPresent()) {
                Venta venta = optionalVenta.get();
                List<ProductoVendido> productosVendidos = productosVendidosRepository.findByVenta(venta);
                double totalVenta = productosVendidos.stream().mapToDouble(pv -> pv.getPrecio() * pv.getCantidad())
                        .sum();

                cashDrawerService.printFormattedInvoice(productosVendidos, totalVenta);

                redirectAttributes
                        .addFlashAttribute("mensaje", "Factura impresa correctamente")
                        .addFlashAttribute("clase", "success");
            } else {
                redirectAttributes
                        .addFlashAttribute("mensaje", "No se encontró la venta")
                        .addFlashAttribute("clase", "warning");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes
                    .addFlashAttribute("mensaje", "Error al imprimir la factura")
                    .addFlashAttribute("clase", "danger");
        }
        return "redirect:/vender/";
    }

    @GetMapping(value = "/")
    public String interfazVender(Model model, HttpServletRequest request) {
        model.addAttribute("producto", new Producto());
        float total = 0;
        ArrayList<ProductoParaVender> carrito = this.obtenerCarrito(request);
        for (ProductoParaVender p : carrito)
            total += p.getTotal();
        model.addAttribute("total", total);

        List<Venta> ventas = ventasRepository.findAllByOrderByFechaYHoraDesc();
        model.addAttribute("ventas", ventas);

        return "vender/vender";
    }

    private ArrayList<ProductoParaVender> obtenerCarrito(HttpServletRequest request) {
        ArrayList<ProductoParaVender> carrito = (ArrayList<ProductoParaVender>) request.getSession()
                .getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
        }
        return carrito;
    }

    private void guardarCarrito(ArrayList<ProductoParaVender> carrito, HttpServletRequest request) {
        request.getSession().setAttribute("carrito", carrito);
    }

    @PostMapping(value = "/agregar")
    public String agregarAlCarrito(@ModelAttribute Producto producto, HttpServletRequest request,
            RedirectAttributes redirectAttrs) {
        ArrayList<ProductoParaVender> carrito = this.obtenerCarrito(request);
        Producto productoBuscadoPorCodigo = productosRepository.findFirstByCodigo(producto.getCodigo());
        if (productoBuscadoPorCodigo == null) {
            redirectAttrs
                    .addFlashAttribute("mensaje", "El producto con el código " + producto.getCodigo() + " no existe")
                    .addFlashAttribute("clase", "warning");
            return "redirect:/vender/";
        }
        if (productoBuscadoPorCodigo.sinExistencia()) {
            redirectAttrs.addFlashAttribute("mensaje", "El producto está agotado")
                    .addFlashAttribute("clase", "warning");
            return "redirect:/vender/";
        }
        boolean encontrado = false;
        for (ProductoParaVender productoParaVenderActual : carrito) {
            if (productoParaVenderActual.getCodigo().equals(productoBuscadoPorCodigo.getCodigo())) {
                if (productoParaVenderActual.getCantidad() + 1 > productoBuscadoPorCodigo.getExistencia()) {
                    redirectAttrs.addFlashAttribute("mensaje",
                            "No hay suficiente stock de \"" + productoBuscadoPorCodigo.getNombre() + "\"")
                            .addFlashAttribute("clase", "warning");
                    return "redirect:/vender/";
                }
                productoParaVenderActual.aumentarCantidad();
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            carrito.add(new ProductoParaVender(
                    productoBuscadoPorCodigo.getNombre(),
                    productoBuscadoPorCodigo.getCodigo(),
                    productoBuscadoPorCodigo.getPrecio(),
                    productoBuscadoPorCodigo.getExistencia(),
                    productoBuscadoPorCodigo.getId(),
                    1f));
        }
        this.guardarCarrito(carrito, request);
        return "redirect:/vender/";
    }

    @PostMapping("/agregarAutomatico")
    public String agregarAutomatico(@RequestParam String codigo, HttpServletRequest request) {
        ArrayList<ProductoParaVender> carrito = this.obtenerCarrito(request);

        Producto productoBuscado = productosRepository.findFirstByCodigo(codigo);
        if (productoBuscado == null) {
            return "redirect:/vender/";
        }

        boolean encontrado = false;
        for (ProductoParaVender producto : carrito) {
            if (producto.getCodigo().equals(codigo)) {
                if (producto.getCantidad() + 1 <= producto.getExistencia()) {
                    producto.aumentarCantidad();
                } else {
                    return "redirect:/vender/";
                }
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            carrito.add(new ProductoParaVender(
                    productoBuscado.getNombre(),
                    productoBuscado.getCodigo(),
                    productoBuscado.getPrecio(),
                    productoBuscado.getExistencia(),
                    productoBuscado.getId(),
                    1f));
        }

        this.guardarCarrito(carrito, request);
        return "redirect:/vender/";
    }

    // ===========================
    // NUEVO MÉTODO ✅
    // ===========================
    @PostMapping("/actualizarCantidad/{id}")
    public String actualizarCantidad(@PathVariable int id,
            @RequestParam float cantidad,
            HttpServletRequest request) {
        ArrayList<ProductoParaVender> carrito = this.obtenerCarrito(request);

        for (ProductoParaVender p : carrito) {
            if (p.getId() == id) {
                if (cantidad >= 0) {
                    p.setCantidad(cantidad);
                }
                break;
            }
        }

        this.guardarCarrito(carrito, request);
        return "redirect:/vender/"; // 🔹 Redirige de nuevo a la vista principal
    }

}
