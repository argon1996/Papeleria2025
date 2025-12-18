package me.parzibyte.sistemaventasspringboot.entity;

public class ProductoParaVender extends Producto {
    private Float cantidad;

    public ProductoParaVender() {
        // Requerido por frameworks/serialización
    }

    // Llamamos al constructor correcto de Producto con los parámetros necesarios, incluyendo area
    public ProductoParaVender(String nombre, String codigo, Float precio, Float existencia, Integer id, Float cantidad, String area) {
        super(nombre, codigo, precio, existencia, id, area); // Aquí ahora llamamos al constructor correcto de Producto
        this.setImagenUrl(getImagenUrl()); // No hace nada si no se ha seteado antes
        this.cantidad = cantidad;
    }

    // NUEVO: Factory recomendado para asegurar imagen
    public static ProductoParaVender from(Producto p, Float cantidad) {
        ProductoParaVender x = new ProductoParaVender(
                p.getNombre(),
                p.getCodigo(),
                p.getPrecio(),
                p.getExistencia(),
                p.getId(),
                cantidad,
                p.getArea() // Ahora también pasamos el área de Producto
        );
        x.setImagenUrl(p.getImagenUrl()); // Copiamos la imagen del producto
        return x;
    }

    public void aumentarCantidad() {
        this.cantidad++;
    }

    public Float getCantidad() {
        return cantidad;
    }

    public Float getTotal() {
        return this.getPrecio() * this.cantidad;
    }

    public void setCantidad(Float cantidad) {
        this.cantidad = cantidad;
    }
}
