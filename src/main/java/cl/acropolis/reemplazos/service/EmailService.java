package cl.acropolis.reemplazos.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final PdfReportService pdfReportService;

    /**
     * Envía el reporte PDF mensual al email institucional del funcionario.
     */
    public void enviarReportePorEmail(Long personalId, int mes, int anio, String emailDestino,
            String nombreFuncionario) {
        try {
            byte[] pdfBytes = pdfReportService.generarReporteIndividual(personalId, mes, anio);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(emailDestino);
            helper.setSubject("Reporte de Reemplazos - " + getNombreMes(mes) + " " + anio + " - Colegio Acrópolis");
            helper.setText(buildEmailBody(nombreFuncionario, mes, anio), true);

            String filename = "Reporte_Reemplazos_" + nombreFuncionario.replace(" ", "_") + "_" + mes + "_" + anio
                    + ".pdf";
            helper.addAttachment(filename, new ByteArrayResource(pdfBytes), "application/pdf");

            mailSender.send(message);
            log.info("Reporte enviado exitosamente a: {}", emailDestino);

        } catch (Exception e) {
            log.error("Error al enviar reporte a {}: {}", emailDestino, e.getMessage());
            throw new RuntimeException("Error al enviar el correo: " + e.getMessage(), e);
        }
    }

    private String buildEmailBody(String nombre, int mes, int anio) {
        return """
                <div style="font-family: 'Inter', Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #0f1729; padding: 30px; text-align: center; border-radius: 12px 12px 0 0;">
                        <h1 style="color: #f97316; margin: 0; font-size: 24px;">Colegio Acrópolis</h1>
                        <p style="color: #94a3b8; margin: 5px 0 0 0; font-size: 12px; text-transform: uppercase; letter-spacing: 2px;">
                            Sistema de Reemplazos y Ausencias
                        </p>
                    </div>
                    <div style="background-color: #f8fafc; padding: 30px; border: 1px solid #e2e8f0;">
                        <p style="color: #334155; font-size: 16px;">Estimado/a <strong>%s</strong>,</p>
                        <p style="color: #64748b; font-size: 14px;">
                            Adjunto encontrará su reporte de reemplazos y ausencias correspondiente al período de
                            <strong>%s %d</strong>.
                        </p>
                        <p style="color: #64748b; font-size: 14px;">
                            Por favor revise el documento adjunto y firme el espacio "Recibí Conforme" para entregarlo
                            a la Jefatura correspondiente.
                        </p>
                        <div style="background-color: #fff7ed; border-left: 4px solid #f97316; padding: 15px; margin: 20px 0; border-radius: 0 8px 8px 0;">
                            <p style="color: #9a3412; margin: 0; font-size: 13px;">
                                <strong>⚠ Importante:</strong> Este es un documento oficial generado automáticamente.
                                Cualquier consulta, diríjase a la oficina administrativa.
                            </p>
                        </div>
                    </div>
                    <div style="background-color: #0f1729; padding: 15px; text-align: center; border-radius: 0 0 12px 12px;">
                        <p style="color: #64748b; margin: 0; font-size: 11px;">
                            Colegio Acrópolis — Sistema de Gestión de Reemplazos
                        </p>
                    </div>
                </div>
                """
                .formatted(nombre, getNombreMes(mes), anio);
    }

    private String getNombreMes(int mes) {
        String[] meses = { "", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre" };
        return meses[mes];
    }
}
