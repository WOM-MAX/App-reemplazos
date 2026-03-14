package cl.acropolis.reemplazos.controller;

import cl.acropolis.reemplazos.model.Atraso;
import cl.acropolis.reemplazos.service.AtrasoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/atrasos")
@RequiredArgsConstructor
public class AtrasoController {

    private final AtrasoService atrasoService;

    /**
     * Registrar un nuevo atraso.
     */
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Map<String, Object> payload) {
        try {
            Long personalId = Long.parseLong(payload.get("personalId").toString());
            LocalDate fecha = LocalDate.parse(payload.get("fecha").toString());
            int minutosAtraso = Integer.parseInt(payload.get("minutosAtraso").toString());
            String observacion = payload.get("observacion") != null ? payload.get("observacion").toString() : null;

            Atraso atraso = atrasoService.registrar(personalId, fecha, minutosAtraso, observacion);
            return ResponseEntity.ok(atraso);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Actualizar minutos y observación de un atraso.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            int minutosAtraso = Integer.parseInt(payload.get("minutosAtraso").toString());
            String observacion = payload.get("observacion") != null ? payload.get("observacion").toString() : null;

            Atraso atraso = atrasoService.actualizar(id, minutosAtraso, observacion);
            return ResponseEntity.ok(atraso);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Eliminar un atraso.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            atrasoService.eliminar(id);
            return ResponseEntity.ok(Map.of("message", "Atraso eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Listar atrasos del mes (todos los funcionarios).
     */
    @GetMapping
    public ResponseEntity<List<Atraso>> listar(
            @RequestParam(defaultValue = "0") int mes,
            @RequestParam(defaultValue = "0") int anio) {
        if (mes == 0)
            mes = LocalDate.now().getMonthValue();
        if (anio == 0)
            anio = LocalDate.now().getYear();

        return ResponseEntity.ok(atrasoService.listarPorMes(mes, anio));
    }

    /**
     * Resumen mensual individual de un funcionario.
     */
    @GetMapping("/resumen/{personalId}")
    public ResponseEntity<?> resumenMensual(
            @PathVariable Long personalId,
            @RequestParam(defaultValue = "0") int mes,
            @RequestParam(defaultValue = "0") int anio) {
        try {
            if (mes == 0)
                mes = LocalDate.now().getMonthValue();
            if (anio == 0)
                anio = LocalDate.now().getYear();

            return ResponseEntity.ok(atrasoService.obtenerResumenMensual(personalId, mes, anio));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
