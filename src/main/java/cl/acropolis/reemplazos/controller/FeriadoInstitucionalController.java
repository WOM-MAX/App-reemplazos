package cl.acropolis.reemplazos.controller;

import cl.acropolis.reemplazos.model.FeriadoInstitucional;
import cl.acropolis.reemplazos.repository.FeriadoInstitucionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/feriados-institucionales")
@RequiredArgsConstructor
public class FeriadoInstitucionalController {

    private final FeriadoInstitucionalRepository repository;

    @GetMapping
    public List<FeriadoInstitucional> getFeriados(@RequestParam(required = false) Integer anio) {
        if (anio != null) {
            LocalDate start = LocalDate.of(anio, 1, 1);
            LocalDate end = LocalDate.of(anio, 12, 31);
            return repository.findByFechaBetweenOrderByFechaAsc(start, end);
        }
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody FeriadoInstitucional feriado) {
        try {
            FeriadoInstitucional saved = repository.save(feriado);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\":\"La fecha ya está registrada u ocurrió un error\"}");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
