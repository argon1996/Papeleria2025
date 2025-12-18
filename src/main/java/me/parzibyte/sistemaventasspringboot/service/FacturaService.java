package me.parzibyte.sistemaventasspringboot.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.parzibyte.prueba.CashDrawerService;
import me.parzibyte.sistemaventasspringboot.entity.ProductoVendido;
import me.parzibyte.sistemaventasspringboot.repository.ProductosVendidosRepository;
import me.parzibyte.sistemaventasspringboot.repository.VentasRepository;

@Service
public class FacturaService {

    private final VentasRepository ventas;
    private final ProductosVendidosRepository vendidos;
    private final CashDrawerService cash;

    public FacturaService(VentasRepository ventas,
                          ProductosVendidosRepository vendidos,
                          CashDrawerService cash) {
        this.ventas = ventas;
        this.vendidos = vendidos;
        this.cash = cash;
    }

    public void imprimir(Long ventaId, RedirectAttributes ra) {
        ventas.findById(ventaId).ifPresentOrElse(v -> {
            List<ProductoVendido> lista = vendidos.findByVenta(v);

            double total = lista.stream()
                    .mapToDouble(p -> {
                        double precio = p.getPrecio() == null ? 0d : p.getPrecio();
                        double cant   = p.getCantidad() == null ? 0d : p.getCantidad();
                        return precio * cant;
                    })
                    .sum();

            try {
                cash.printFormattedInvoice(lista, total);
                ra.addFlashAttribute("mensaje", "Factura impresa correctamente")
                  .addFlashAttribute("clase", "success");
            } catch (Exception e) {
                ra.addFlashAttribute("mensaje", "Error al imprimir la factura: " + e.getMessage())
                  .addFlashAttribute("clase", "danger");
            }
        }, () -> ra.addFlashAttribute("mensaje", "No se encontró la venta")
                    .addFlashAttribute("clase", "warning"));
    }
}
