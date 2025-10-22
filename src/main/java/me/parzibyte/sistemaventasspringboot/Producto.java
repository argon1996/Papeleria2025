package me.parzibyte.sistemaventasspringboot;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "producto") // <-- tabla real (singular)
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <-- MySQL autoincrement
    private Integer id;

    @NotNull(message = "Debes especificar el nombre")
    @Size(min = 1, max = 50, message = "El nombre debe medir entre 1 y 50")
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @NotNull(message = "Debes especificar el código")
    @Size(min = 1, max = 50, message = "El código debe medir entre 1 y 50")
    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @NotNull(message = "Debes especificar el precio")
    @Min(value = 0, message = "El precio mínimo es 0")
    @Column(name = "precio", nullable = false)
    private Float precio;

    @NotNull(message = "Debes especificar la existencia")
    @Min(value = 0, message = "La existencia mínima es 0")
    @Column(name = "existencia", nullable = false)
    private Float existencia;

    @NotNull
    @Column(name = "apartado", nullable = false, columnDefinition = "bit(1)")
    private Boolean apartado = false; // también puedes usar 'boolean'

    @Column(name = "fecha_apartado")
    private LocalDateTime fechaApartado;

    @Column(name = "apartado_id")
    private String apartadoId;

    public Producto() {}

    public Producto(String nombre, String codigo, Float precio, Float existencia, Integer id) {
        this.nombre = nombre; this.codigo = codigo; this.precio = precio; this.existencia = existencia; this.id = id;
    }
    public Producto(String nombre, String codigo, Float precio, Float existencia) {
        this.nombre = nombre; this.codigo = codigo; this.precio = precio; this.existencia = existencia;
    }
    public Producto(@NotNull @Size(min = 1, max = 50) String codigo) { this.codigo = codigo; }

    public boolean sinExistencia() { return this.existencia <= 0; }

    // Getters/Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Float getPrecio() { return precio; }
    public void setPrecio(Float precio) { this.precio = precio; }
    public Float getExistencia() { return existencia; }
    public void setExistencia(Float existencia) { this.existencia = existencia; }
    public void restarExistencia(Float existencia) { this.existencia -= existencia; }
    public Boolean getApartado() { return apartado; }
    public void setApartado(Boolean apartado) { this.apartado = apartado; }
    public LocalDateTime getFechaApartado() { return fechaApartado; }
    public void setFechaApartado(LocalDateTime fechaApartado) { this.fechaApartado = fechaApartado; }
    public String getApartadoId() { return apartadoId; }
    public void setApartadoId(String apartadoId) { this.apartadoId = apartadoId; }
}

