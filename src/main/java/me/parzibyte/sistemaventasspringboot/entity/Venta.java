package me.parzibyte.sistemaventasspringboot.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "ventas")
public class Venta {
    @Id
    // 🟢 CORRECCIÓN CLAVE: El ID debe ser Long para evitar TypeMismatchException
    // Generación de tipo IDENTITY es la mejor opción para MySQL
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🟢 CORRECCIÓN: Usar LocalDateTime y anotación para manejar la fecha/hora
    @CreationTimestamp
    @Column(name = "fecha_y_hora")
    private LocalDateTime fechaYHora;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)
    // Mantenemos el tipo Set que tenías
    private Set<ProductoVendido> productos = new HashSet<>();

    public Venta() {
        // La fecha y hora se inicializan automáticamente con @CreationTimestamp
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaYHora() {
        return fechaYHora;
    }

    public void setFechaYHora(LocalDateTime fechaYHora) {
        this.fechaYHora = fechaYHora;
    }

    public double getTotal() {
        double total = 0;
        for (ProductoVendido producto : productos) {
            // Aseguramos que la cantidad no sea null, aunque en teoría no debería pasar
            double cantidad = producto.getCantidad() != null ? producto.getCantidad() : 0;
            total += producto.getPrecio() * cantidad;
        }
        return total;
    }

    public Set<ProductoVendido> getProductos() {
        return productos;
    }

    public void setProductos(Set<ProductoVendido> productos) {
        this.productos = productos;
    }
}
