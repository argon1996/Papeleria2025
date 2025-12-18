package me.parzibyte.sistemaventasspringboot.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Abono {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    private ProductoApartado productoApartado;

    private Float cantidad;
    private LocalDateTime fechaAbono;

    // ====== Constructores ======
    public Abono() {} // <— importante: constructor por defecto

    public Abono(ProductoApartado productoApartado, Float cantidad, LocalDateTime fechaAbono) {
        this.productoApartado = productoApartado;
        this.cantidad = cantidad;
        this.fechaAbono = fechaAbono;
    }

    // ====== Getters / Setters ======
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public ProductoApartado getProductoApartado() { return productoApartado; }
    public void setProductoApartado(ProductoApartado productoApartado) { this.productoApartado = productoApartado; }

    public Float getCantidad() { return cantidad; }
    public void setCantidad(Float cantidad) { this.cantidad = cantidad; }

    public LocalDateTime getFechaAbono() { return fechaAbono; }
    public void setFechaAbono(LocalDateTime fechaAbono) { this.fechaAbono = fechaAbono; }
}
