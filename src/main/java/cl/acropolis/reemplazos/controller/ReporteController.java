package cl.acropolis.reemplazos.controller;

import cl.acropolis.reemplazos.dto.BalanceMensualDTO;
import cl.acropolis.reemplazos.service.EmailService;
import cl.acropolis.reemplazos.service.PdfReportService;
import cl.acropolis.reemplazos.service.ReemplazoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final PdfReportService pdfReportService;
    private final EmailService emailService;
    private final ReemplazoService reemplazoService;

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
}
