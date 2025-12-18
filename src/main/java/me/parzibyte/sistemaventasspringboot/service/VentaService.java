package me.parzibyte.sistemaventasspringboot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.parzibyte.printer.MicroprintClient;
import me.parzibyte.printer.PrintAgentClient;
import me.parzibyte.sistemaventasspringboot.entity.Producto;
import me.parzibyte.sistemaventasspringboot.entity.ProductoParaVender;
import me.parzibyte.sistemaventasspringboot.entity.ProductoVendido;
import me.parzibyte.sistemaventasspringboot.entity.Venta;
import me.parzibyte.sistemaventasspringboot.repository.ProductosRepository;
import me.parzibyte.sistemaventasspringboot.repository.ProductosVendidosRepository;
import me.parzibyte.sistemaventasspringboot.repository.VentasRepository;

@Service
public class VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaService.class);

    private final ProductosRepository productos;
    private final VentasRepository ventas;
    private final ProductosVendidosRepository vendidos;

    private final MicroprintClient printerClient; // impresión
    private final PrintAgentClient drawerClient;  // cajón

    public VentaService(ProductosRepository productos,
                        VentasRepository ventas,
                        ProductosVendidosRepository vendidos,
                        MicroprintClient printerClient,
                        PrintAgentClient drawerClient) {
        this.productos = productos;
        this.ventas = ventas;
        this.vendidos = vendidos;
        this.printerClient = printerClient;
        this.drawerClient = drawerClient;
    }

    public List<Venta> listar() {
        return ventas.findAllByOrderByFechaYHoraDesc();
    }

    @Transactional
    public String terminarVenta(List<ProductoParaVender> carrito, RedirectAttributes ra) {
        // Validación de stock
        for (ProductoParaVender item : carrito) {
            Producto p = productos.findById(item.getId()).orElse(null);
            if (p == null || item.getCantidad() > p.getExistencia()) {
                ra.addFlashAttribute("mensaje",
                        "Stock insuficiente de \"" + (p != null ? p.getNombre() : "?") + "\"")
                        .addFlashAttribute("clase", "danger");
                return "redirect:/vender/";
            }
        }

        // Registrar venta
        Venta v = ventas.save(new Venta());
        List<ProductoVendido> detalle = new ArrayList<>();

        for (ProductoParaVender item : carrito) {
            Producto p = productos.findById(item.getId()).orElseThrow();
            p.restarExistencia(item.getCantidad());
            productos.save(p);

            ProductoVendido pv = new ProductoVendido(
                    item.getCantidad(), item.getPrecio(),
                    item.getNombre(), item.getCodigo(), v);

            vendidos.save(pv);
            detalle.add(pv);
        }

        carrito.clear();

        // Intentar abrir cajón
        try {
            drawerClient.openDrawer();
            log.info("✅ Cajón abierto correctamente.");
            ra.addFlashAttribute("mensaje", "Venta registrada correctamente (cajón abierto)")
              .addFlashAttribute("clase", "success");
        } catch (Exception e) {
            log.error("❌ Error al abrir cajón: {}", e.getMessage());
            ra.addFlashAttribute("mensaje", "Venta registrada, pero no se pudo abrir el cajón.")
              .addFlashAttribute("clase", "warning");
        }

        return "redirect:/vender/";
    }

    /** 🗑️ Eliminar venta junto con su detalle */
    @Transactional
    public boolean eliminarVenta(long id) {
        return ventas.findById(id).map(v -> {
            List<ProductoVendido> detalle = vendidos.findByVenta(v);
            if (!detalle.isEmpty()) {
                vendidos.deleteAll(detalle);
            }
            ventas.delete(v);
            log.info("🗑️ Venta {} eliminada correctamente", id);
            return true;
        }).orElse(false);
    }

    /** 🧾 Reimprimir venta */
    public boolean reimprimirVenta(long idVenta) {
        Optional<Venta> ventaOptional = ventas.findById(idVenta);
        if (ventaOptional.isEmpty()) {
            log.warn("⚠️ Intento de reimprimir venta con ID {} que no existe.", idVenta);
            return false;
        }

        Venta venta = ventaOptional.get();
        List<ProductoVendido> detalle = vendidos.findByVenta(venta);

        try {
            double totalVenta = detalle.stream()
                    .mapToDouble(d -> d.getPrecio() * (d.getCantidad() == null ? 0d : d.getCantidad()))
                    .sum();

            String ticketContent = generarTicket(detalle, totalVenta);
            printerClient.printText(ticketContent);
            log.info("🧾 Reimpresión de la venta ID {} completada.", idVenta);
        } catch (Exception e) {
            log.error("❌ Error en reimpresión de la venta {}: {}", idVenta, e.getMessage());
        }

        return true;
    }

    /** 🧩 Formato de ticket */
    private String generarTicket(List<ProductoVendido> detalle, double totalVenta) {
        StringBuilder ticket = new StringBuilder();
        ticket.append(centrar("INTERESTELAR")).append("\n");
        ticket.append("--------------------------------\n");
        ticket.append(String.format("%-16s %4s %8s\n", "Producto", "Cant", "Precio"));
        ticket.append("--------------------------------\n");
        for (ProductoVendido d : detalle) {
            String nombre = d.getNombre() == null ? "" : d.getNombre();
            int cant = d.getCantidad() == null ? 0 : Math.round(d.getCantidad());
            double precio = d.getPrecio();
            ticket.append(String.format("%-16.16s %4d %8.2f\n", nombre, cant, precio));
        }
        ticket.append("--------------------------------\n");
        ticket.append(String.format("%-16s %4s %8.2f\n", "TOTAL", "", totalVenta));
        ticket.append("\n");
        ticket.append(centrar("Gracias por su compra")).append("\n");
        return ticket.toString();
    }

    private static String centrar(String s) {
        int width = 32;
        if (s == null) return "";
        if (s.length() >= width) return s.substring(0, width);
        int pad = (width - s.length()) / 2;
        return " ".repeat(pad) + s;
    }
}
