package cl.acropolis.reemplazos.controller;

import cl.acropolis.reemplazos.dto.BalanceMensualDTO;
import cl.acropolis.reemplazos.service.EmailService;
import cl.acropolis.reemplazos.service.ExcelReportService;
import cl.acropolis.reemplazos.service.PdfReportService;
import cl.acropolis.reemplazos.service.ReemplazoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Slf4j
public class ReporteController {

    private final PdfReportService pdfReportService;
    private final EmailService emailService;
    private final ReemplazoService reemplazoService;
    private final ExcelReportService excelReportService;

    /**
     * Descarga PDF del reporte individual.
     */
    @GetMapping("/pdf/{personalId}")
    public ResponseEntity<byte[]> descargarPdf(
            @PathVariable Long personalId,
            @RequestParam(defaultValue = "0") int mes,
            @RequestParam(defaultValue = "0") int anio) {
        try {
            if (mes == 0)
                mes = LocalDate.now().getMonthValue();
            if (anio == 0)
                anio = LocalDate.now().getYear();

            byte[] pdf = pdfReportService.generarReporteIndividual(personalId, mes, anio);

            BalanceMensualDTO balance = reemplazoService.calcularBalanceIndividual(personalId, mes, anio);
            String filename = "Reporte_" + balance.getNombreCompleto().replace(" ", "_") + "_" + mes + "_" + anio
                    + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Descarga Excel del balance mensual general.
     */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> descargarExcel(
            @RequestParam(defaultValue = "0") int mes,
            @RequestParam(defaultValue = "0") int anio) {
        try {
            if (mes == 0)
                mes = LocalDate.now().getMonthValue();
            if (anio == 0)
                anio = LocalDate.now().getYear();

            byte[] excel = excelReportService.generarExcelBalanceMensual(mes, anio);

            String filename = "Balance_General_" + mes + "_" + anio + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excel);
        } catch (Exception e) {
            log.error("Error generando Excel: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Envía el reporte PDF por correo electrónico al funcionario.
     */
    @PostMapping("/enviar-email/{personalId}")
    public ResponseEntity<Map<String, String>> enviarEmail(
            @PathVariable Long personalId,
            @RequestParam(defaultValue = "0") int mes,
            @RequestParam(defaultValue = "0") int anio) {
        try {
            if (mes == 0)
                mes = LocalDate.now().getMonthValue();
            if (anio == 0)
                anio = LocalDate.now().getYear();

            BalanceMensualDTO balance = reemplazoService.calcularBalanceIndividual(personalId, mes, anio);

            if (balance.getEmailInstitucional() == null || balance.getEmailInstitucional().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El funcionario no tiene email institucional registrado."));
            }

            emailService.enviarReportePorEmail(personalId, mes, anio,
                    balance.getEmailInstitucional(), balance.getNombreCompleto());

            return ResponseEntity.ok(Map.of(
                    "message", "Reporte enviado exitosamente a " + balance.getEmailInstitucional(),
                    "destinatario", balance.getNombreCompleto()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al enviar correo: " + e.getMessage()));
        }
    }

    /**
     * Envío masivo: envía el reporte PDF a TODOS los funcionarios con email registrado.
     */
    @PostMapping("/enviar-masivo")
    public ResponseEntity<Map<String, Object>> enviarMasivo(
            @RequestParam(defaultValue = "0") int mes,
            @RequestParam(defaultValue = "0") int anio) {
        try {
            if (mes == 0)
                mes = LocalDate.now().getMonthValue();
            if (anio == 0)
                anio = LocalDate.now().getYear();

            List<BalanceMensualDTO> balances = reemplazoService.calcularBalanceMensual(mes, anio);

            int enviados = 0;
            int sinEmail = 0;
            int fallidos = 0;
            List<String> errores = new ArrayList<>();

            for (BalanceMensualDTO balance : balances) {
                if (balance.getEmailInstitucional() == null || balance.getEmailInstitucional().isBlank()) {
                    sinEmail++;
                    continue;
                }

                try {
                    emailService.enviarReportePorEmail(
                            balance.getPersonalId(), mes, anio,
                            balance.getEmailInstitucional(), balance.getNombreCompleto());
                    enviados++;
                    log.info("Reporte masivo enviado a: {} ({})", balance.getNombreCompleto(),
                            balance.getEmailInstitucional());
                } catch (Exception e) {
                    fallidos++;
                    errores.add(balance.getNombreCompleto() + ": " + e.getMessage());
                    log.error("Error enviando reporte a {}: {}", balance.getNombreCompleto(), e.getMessage());
                }
            }

            Map<String, Object> resultado = new LinkedHashMap<>();
            resultado.put("enviados", enviados);
            resultado.put("sinEmail", sinEmail);
            resultado.put("fallidos", fallidos);
            resultado.put("total", balances.size());
            resultado.put("errores", errores);

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error en envío masivo: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error en envío masivo: " + e.getMessage()));
        }
    }
}
