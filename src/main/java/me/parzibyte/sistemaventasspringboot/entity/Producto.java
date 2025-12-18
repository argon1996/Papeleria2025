package me.parzibyte.sistemaventasspringboot.entity;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "producto",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_producto_codigo", columnNames = "codigo"),
        @UniqueConstraint(name = "uk_producto_nombre", columnNames = "nombre")
    }
)
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @NotNull
    @Min(0)
    @Column(name = "precio", nullable = false)
    private Float precio;

    @NotNull
    @Min(0)
    @Column(name = "existencia", nullable = false)
    private Float existencia;

    @NotNull
    @Column(name = "apartado", nullable = false, columnDefinition = "bit(1)")
    private Boolean apartado = false;

    @Column(name = "fecha_apartado")
    private LocalDateTime fechaApartado;

    @Column(name = "apartado_id")
    private String apartadoId;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    // --- Nuevo campo para el área/categoría ---
    @Column(name = "area", length = 50)
    private String area; // Este es el campo que añadimos

    // --- Constructores ---
    public Producto() {}

    public Producto(String nombre, String codigo, Float precio, Float existencia, Integer id, String area) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.precio = precio;
        this.existencia = existencia;
        this.id = id;
        this.area = area; // Inicializamos el área
    }

    public Producto(String nombre, String codigo, Float precio, Float existencia, String area) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.precio = precio;
        this.existencia = existencia;
        this.area = area; // Inicializamos el área
    }

    // Métodos auxiliares
    public boolean sinExistencia() {
        return this.existencia <= 0;
    }

    public void restarExistencia(Float cantidad) {
        this.existencia -= cantidad;
    }

    // --- Getters y Setters ---
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Float getPrecio() {
        return precio;
    }

    public void setPrecio(Float precio) {
        this.precio = precio;
    }

    public Float getExistencia() {
        return existencia;
    }

    public void setExistencia(Float existencia) {
        this.existencia = existencia;
    }

    public Boolean getApartado() {
        return apartado;
    }

    public void setApartado(Boolean apartado) {
        this.apartado = apartado;
    }

    public LocalDateTime getFechaApartado() {
        return fechaApartado;
    }

    public void setFechaApartado(LocalDateTime fechaApartado) {
        this.fechaApartado = fechaApartado;
    }

    public String getApartadoId() {
        return apartadoId;
    }

    public void setApartadoId(String apartadoId) {
        this.apartadoId = apartadoId;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    // Getter y Setter para el campo `area`
    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}
