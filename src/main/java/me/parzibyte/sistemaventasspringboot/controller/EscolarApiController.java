package me.parzibyte.sistemaventasspringboot.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.parzibyte.sistemaventasspringboot.dto.ProductoEscolarDto;
import me.parzibyte.sistemaventasspringboot.entity.Producto;
import me.parzibyte.sistemaventasspringboot.repository.ProductosRepository;

@RestController
public class EscolarApiController {

    private final ProductosRepository productos;

    public EscolarApiController(ProductosRepository productos) {
        this.productos = productos;
    }

    @GetMapping("/api/escolar/productos")
    public List<ProductoEscolarDto> buscar(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "categoria", required = false) String categoria) {

        String term = q != null ? q.trim() : "";
        String cat = categoria != null ? categoria.trim() : "";

        List<Producto> encontrados = productos.findEscolares(term, cat);

        return encontrados.stream()
                .map(p -> new ProductoEscolarDto(
                        p.getId(),
                        p.getNombre(),
                        p.getCodigo(),
                        p.getPrecio(),
                        p.getExistencia(),
                        p.getArea()
                ))
                .collect(Collectors.toList());
    }
}
