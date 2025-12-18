package me.parzibyte.sistemaventasspringboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import me.parzibyte.sistemaventasspringboot.entity.Abono;
import java.util.List;

public interface AbonoRepository extends JpaRepository<Abono, Integer> {
    List<Abono> findByProductoApartadoIdOrderByFechaAbonoDesc(Integer productoApartadoId);

    void deleteByProductoApartadoId(Integer productoApartadoId); // ← añade esto
}
