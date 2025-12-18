/*
 * ProductoVendidosRepository.java
 */
package me.parzibyte.sistemaventasspringboot.repository;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import me.parzibyte.sistemaventasspringboot.entity.ProductoVendido;
import me.parzibyte.sistemaventasspringboot.entity.Venta;

public interface ProductosVendidosRepository extends CrudRepository<ProductoVendido, Integer> {
    List<ProductoVendido> findByVenta(Venta venta);

}
