package me.parzibyte.sistemaventasspringboot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import me.parzibyte.sistemaventasspringboot.entity.ProductoApartado;
import me.parzibyte.sistemaventasspringboot.entity.Producto;

public interface ProductoApartadoRepository extends JpaRepository<ProductoApartado, Integer> {

    List<ProductoApartado> findByProducto(Producto producto);

    @Query("SELECT pa FROM ProductoApartado pa LEFT JOIN FETCH pa.producto")
    List<ProductoApartado> findAllWithProductos();
}
