package cl.acropolis.reemplazos.controller;

import cl.acropolis.reemplazos.model.Personal;
import cl.acropolis.reemplazos.repository.PersonalRepository;
import cl.acropolis.reemplazos.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/personal")
@RequiredArgsConstructor
public class PersonalController {

    private final PersonalRepository personalRepository;
    private final ExcelImportService excelImportService;

    @GetMapping
    public List<Personal> listarTodos() {
        return personalRepository.findAllByOrderByApellidosAscNombreAsc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Personal> obtenerPorId(@PathVariable Long id) {
        return personalRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public List<Personal> buscar(@RequestParam String q) {
        return personalRepository.findByApellidosContainingIgnoreCaseOrNombreContainingIgnoreCase(q, q);
    }

    @PostMapping
    public Personal crear(@RequestBody Personal personal) {
        return personalRepository.save(personal);
    }

    /**
     * Importar funcionarios desde archivo Excel (.xlsx).
     * Detecta columnas automáticamente por encabezados.
     */
    @PostMapping("/importar-excel")
    public ResponseEntity<Map<String, Object>> importarExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo está vacío."));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Solo se aceptan archivos Excel (.xlsx, .xls)"));
        }

        Map<String, Object> resultado = excelImportService.importarDesdeExcel(file);

        if (resultado.containsKey("error")) {
            return ResponseEntity.badRequest().body(resultado);
        }

        return ResponseEntity.ok(resultado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Personal> actualizar(@PathVariable Long id, @RequestBody Personal personalData) {
        return personalRepository.findById(id)
                .map(existing -> {
                    existing.setRun(personalData.getRun());
                    existing.setNombre(personalData.getNombre());
                    existing.setApellidos(personalData.getApellidos());
                    existing.setCargo(personalData.getCargo());
                    existing.setEmailInstitucional(personalData.getEmailInstitucional());
                    existing.setMinutosContratoReemplazo(personalData.getMinutosContratoReemplazo());
                    return ResponseEntity.ok(personalRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (personalRepository.existsById(id)) {
            personalRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
