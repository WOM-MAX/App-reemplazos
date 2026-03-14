package cl.acropolis.reemplazos.service;

import cl.acropolis.reemplazos.model.Personal;
import cl.acropolis.reemplazos.repository.PersonalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportService {

    private final PersonalRepository personalRepository;

    /**
     * Importa funcionarios desde un archivo Excel (.xlsx).
     * 
     * El Excel debe tener las columnas (en cualquier orden, se detectan por
     * encabezado):
     * RUN | NOMBRE | APELLIDOS | CARGO | EMAIL | MINUTOS CONTRATO
     * 
     * Si la primera fila no tiene encabezados reconocidos, se asume orden fijo:
     * Col 0=RUN, 1=APELLIDOS, 2=NOMBRE, 3=CARGO, 4=EMAIL, 5=MINUTOS
     * 
     * @return Map con "importados", "actualizados", "errores", y "detalles"
     */
    @Transactional
    public Map<String, Object> importarDesdeExcel(MultipartFile file) {
        List<String> errores = new ArrayList<>();
        int importados = 0;
        int actualizados = 0;
        int filaNum = 0;

        try (InputStream is = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return Map.of("error", "El archivo no contiene hojas de cálculo.");
            }

            // Detectar columnas por encabezado
            Map<String, Integer> columnMap = detectarColumnas(sheet.getRow(0));
            int startRow = columnMap.isEmpty() ? 0 : 1; // Si hay encabezados, empezar desde fila 1

            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                filaNum = i + 1;
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row))
                    continue;

                try {
                    Personal persona = parsearFila(row, columnMap);

                    if (persona.getRun() == null || persona.getRun().isBlank()) {
                        errores.add("Fila " + filaNum + ": RUN vacío, se omitió.");
                        continue;
                    }

                    if (persona.getNombre() == null || persona.getNombre().isBlank()) {
                        errores.add("Fila " + filaNum + ": Nombre vacío para RUN " + persona.getRun() + ".");
                        continue;
                    }

                    // Buscar si ya existe por RUN
                    Optional<Personal> existente = personalRepository.findByRun(persona.getRun());

                    if (existente.isPresent()) {
                        // Actualizar datos existentes
                        Personal existing = existente.get();
                        existing.setNombre(persona.getNombre());
                        existing.setApellidos(persona.getApellidos());
                        if (persona.getCargo() != null)
                            existing.setCargo(persona.getCargo());
                        if (persona.getEmailInstitucional() != null)
                            existing.setEmailInstitucional(persona.getEmailInstitucional());
                        if (persona.getMinutosContratoReemplazo() != null
                                && persona.getMinutosContratoReemplazo() > 0) {
                            existing.setMinutosContratoReemplazo(persona.getMinutosContratoReemplazo());
                        }
                        personalRepository.save(existing);
                        actualizados++;
                    } else {
                        // Crear nuevo
                        if (persona.getMinutosContratoReemplazo() == null) {
                            persona.setMinutosContratoReemplazo(0);
                        }
                        personalRepository.save(persona);
                        importados++;
                    }

                } catch (Exception e) {
                    errores.add("Fila " + filaNum + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Error procesando Excel: {}", e.getMessage());
            return Map.of("error", "Error al procesar el archivo: " + e.getMessage());
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("importados", importados);
        resultado.put("actualizados", actualizados);
        resultado.put("errores", errores.size());
        resultado.put("totalProcesados", importados + actualizados);
        if (!errores.isEmpty()) {
            resultado.put("detalleErrores", errores);
        }
        return resultado;
    }

    /**
     * Detecta las columnas del Excel analizando la fila de encabezados.
     */
    private Map<String, Integer> detectarColumnas(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null)
            return map;

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null)
                continue;

            String header = getCellStringValue(cell).toUpperCase().trim();

            if (header.contains("RUN") || header.contains("RUT")) {
                map.put("RUN", i);
            } else if (header.contains("NOMBRE") && !header.contains("APELLIDO")) {
                map.put("NOMBRE", i);
            } else if (header.contains("APELLIDO")) {
                map.put("APELLIDOS", i);
            } else if (header.contains("CARGO") || header.contains("FUNCIÓN") || header.contains("FUNCION")) {
                map.put("CARGO", i);
            } else if (header.contains("EMAIL") || header.contains("CORREO") || header.contains("MAIL")) {
                map.put("EMAIL", i);
            } else if (header.contains("MINUTO") || header.contains("CONTRATO")) {
                map.put("MINUTOS", i);
            }
        }

        // Verificar que al menos RUN y NOMBRE fueron detectados
        if (!map.containsKey("RUN") || !map.containsKey("NOMBRE")) {
            return Collections.emptyMap(); // No se detectaron encabezados válidos
        }

        return map;
    }

    /**
     * Parsea una fila del Excel a un objeto Personal.
     */
    private Personal parsearFila(Row row, Map<String, Integer> columnMap) {
        Personal personal = new Personal();

        if (columnMap.isEmpty()) {
            // Orden fijo: RUN(0), APELLIDOS(1), NOMBRE(2), CARGO(3), EMAIL(4), MINUTOS(5)
            personal.setRun(getCellStringValue(row.getCell(0)).trim());
            personal.setApellidos(getCellStringValue(row.getCell(1)).trim().toUpperCase());
            personal.setNombre(getCellStringValue(row.getCell(2)).trim().toUpperCase());
            personal.setCargo(getCellStringValue(row.getCell(3)).trim());
            personal.setEmailInstitucional(getCellStringValue(row.getCell(4)).trim());
            personal.setMinutosContratoReemplazo(getCellIntValue(row.getCell(5)));
        } else {
            // Usando mapa de columnas detectadas
            personal.setRun(getCellStringValue(row.getCell(columnMap.getOrDefault("RUN", 0))).trim());
            personal.setNombre(
                    getCellStringValue(row.getCell(columnMap.getOrDefault("NOMBRE", 1))).trim().toUpperCase());
            personal.setApellidos(columnMap.containsKey("APELLIDOS")
                    ? getCellStringValue(row.getCell(columnMap.get("APELLIDOS"))).trim().toUpperCase()
                    : "");
            personal.setCargo(columnMap.containsKey("CARGO")
                    ? getCellStringValue(row.getCell(columnMap.get("CARGO"))).trim()
                    : "Docente");
            personal.setEmailInstitucional(columnMap.containsKey("EMAIL")
                    ? getCellStringValue(row.getCell(columnMap.get("EMAIL"))).trim()
                    : null);
            personal.setMinutosContratoReemplazo(columnMap.containsKey("MINUTOS")
                    ? getCellIntValue(row.getCell(columnMap.get("MINUTOS")))
                    : 0);
        }

        return personal;
    }

    /**
     * Obtiene el valor de una celda como String, manejando todos los tipos.
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null)
            return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val)) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> "";
        };
    }

    /**
     * Obtiene el valor de una celda como Integer.
     */
    private Integer getCellIntValue(Cell cell) {
        if (cell == null)
            return 0;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> (int) cell.getNumericCellValue();
                case STRING -> {
                    String val = cell.getStringCellValue().trim();
                    yield val.isEmpty() ? 0 : Integer.parseInt(val);
                }
                default -> 0;
            };
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Verifica si una fila del Excel está completamente vacía.
     */
    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = getCellStringValue(cell).trim();
                if (!val.isEmpty())
                    return false;
            }
        }
        return true;
    }
}
