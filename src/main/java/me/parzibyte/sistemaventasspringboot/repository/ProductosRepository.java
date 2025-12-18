package me.parzibyte.sistemaventasspringboot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import me.parzibyte.sistemaventasspringboot.entity.Producto;

public interface ProductosRepository extends JpaRepository<Producto, Integer> {

    // Buscar productos por nombre o código
    List<Producto> findByNombreContainingOrCodigoContaining(String nombre, String codigo);

    // Versión insensible a mayúsculas/minúsculas para el buscador
    List<Producto> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(String nombre, String codigo);

    // Obtener un producto por código
    Producto findFirstByCodigo(String codigo);

    // Obtener productos con existencia mayor que un valor
    List<Producto> findByExistenciaGreaterThan(Float existencia);

    // Obtener producto por código sin considerar mayúsculas/minúsculas
    Optional<Producto> findByCodigoIgnoreCase(String codigo);

    // Buscar productos con existencia menor que un umbral
    @Query("SELECT p FROM Producto p WHERE p.existencia < :umbral")
    List<Producto> findProductosFaltantes(@Param("umbral") Float umbral);

    // Buscar productos apartados
    @Query("SELECT p FROM Producto p WHERE p.apartado = true")
    List<Producto> findProductosApartados();

    // Obtener todos los productos apartados
    List<Producto> findByApartadoTrue();

    // Validaciones de unicidad para el nombre y código
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Integer id);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);

    // Obtener productos con existencia mayor a un valor y área especificada
    List<Producto> findByExistenciaGreaterThanAndArea(Float existencia, String area);

    // Método que busca productos por nombre, código y área
    List<Producto> findByNombreContainingOrCodigoContainingAndArea(String nombre, String codigo, String area);  // <-- Este método debe estar aquí

    // Búsqueda insensible a mayúsculas por término (nombre o código) con filtro opcional de área
    @Query("SELECT p FROM Producto p "
         + "WHERE (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :term, '%')) "
         + "   OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :term, '%'))) "
         + "  AND (:area IS NULL OR :area = '' OR p.area = :area)")
    List<Producto> searchByTermAndArea(@Param("term") String term, @Param("area") String area);

    // Para lista escolar: productos por término y categoría (opcional), filtrando solo los activos con precio y existencia >= 0
    @Query("SELECT p FROM Producto p "
         + "WHERE (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :term, '%')) "
         + "   OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :term, '%'))) "
         + "  AND (:categoria IS NULL OR :categoria = '' OR p.area = :categoria)")
    List<Producto> findEscolares(@Param("term") String term, @Param("categoria") String categoria);
}
