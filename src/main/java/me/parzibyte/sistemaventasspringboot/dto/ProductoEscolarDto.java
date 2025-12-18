package me.parzibyte.sistemaventasspringboot.dto;

public class ProductoEscolarDto {
    private Integer id;
    private String nombre;
    private String codigo;
    private Float precio;
    private Float existencia;
    private String categoria;

    public ProductoEscolarDto(Integer id, String nombre, String codigo, Float precio, Float existencia, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
        this.precio = precio;
        this.existencia = existencia;
        this.categoria = categoria;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCodigo() { return codigo; }
    public Float getPrecio() { return precio; }
    public Float getExistencia() { return existencia; }
    public String getCategoria() { return categoria; }
}
