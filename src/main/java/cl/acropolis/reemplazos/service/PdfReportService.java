package cl.acropolis.reemplazos.service;

import cl.acropolis.reemplazos.dto.BalanceMensualDTO;

import cl.acropolis.reemplazos.model.Reemplazo;
import cl.acropolis.reemplazos.repository.ReemplazoRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PdfReportService {

        private final ReemplazoService reemplazoService;
        private final ReemplazoRepository reemplazoRepository;

        // Colores del diseño Stitch
        private static final BaseColor PRIMARY = new BaseColor(15, 23, 41); // #0f1729
        private static final BaseColor ACCENT_ORANGE = new BaseColor(249, 115, 22); // #f97316
        private static final BaseColor LIGHT_GRAY = new BaseColor(241, 245, 249);

        /**
         * Genera PDF de reporte individual para un funcionario en un mes/año.
         */
        public byte[] generarReporteIndividual(Long personalId, int mes, int anio) throws Exception {
                BalanceMensualDTO balance = reemplazoService.calcularBalanceIndividual(personalId, mes, anio);
                List<Reemplazo> detalles = reemplazoRepository.findByMesYAnio(mes, anio).stream()
                                .filter(r -> r.getDocenteAusente().getId().equals(personalId)
                                                || r.getReemplazante().getId().equals(personalId))
                                .toList();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Document document = new Document(PageSize.LETTER, 40, 40, 50, 50);
                PdfWriter.getInstance(document, baos);
                document.open();

                // Fuentes
                Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, PRIMARY);
                Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.GRAY);
                Font headerFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
                Font cellFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.DARK_GRAY);
                Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, PRIMARY);
                Font orangeFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, ACCENT_ORANGE);

                // === ENCABEZADO ===
                Paragraph header = new Paragraph();
                header.add(new Chunk("COLEGIO ACRÓPOLIS\n", titleFont));
                header.add(new Chunk("Reporte de Reemplazos y Ausencias\n", subtitleFont));

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "CL"));
                String periodo = LocalDate.of(anio, mes, 1).format(formatter).toUpperCase();
                header.add(new Chunk("Período: " + periodo, subtitleFont));
                header.setAlignment(Element.ALIGN_CENTER);
                header.setSpacingAfter(20);
                document.add(header);

                // Línea decorativa
                PdfPTable lineTable = new PdfPTable(1);
                lineTable.setWidthPercentage(100);
                PdfPCell lineCell = new PdfPCell();
                lineCell.setBorderWidth(0);
                lineCell.setBorderWidthBottom(3);
                lineCell.setBorderColorBottom(ACCENT_ORANGE);
                lineCell.setFixedHeight(1);
                lineTable.addCell(lineCell);
                document.add(lineTable);
                document.add(new Paragraph(" "));

                // === DATOS DEL FUNCIONARIO ===
                PdfPTable infoTable = new PdfPTable(2);
                infoTable.setWidthPercentage(100);
                infoTable.setWidths(new float[] { 1, 2 });

                addInfoRow(infoTable, "Funcionario:", balance.getNombreCompleto(), boldFont, cellFont);
                addInfoRow(infoTable, "RUN:", balance.getRun(), boldFont, cellFont);
                addInfoRow(infoTable, "Cargo:", balance.getCargo() != null ? balance.getCargo() : "—", boldFont,
                                cellFont);
                addInfoRow(infoTable, "Email:",
                                balance.getEmailInstitucional() != null ? balance.getEmailInstitucional() : "—",
                                boldFont, cellFont);
                document.add(infoTable);
                document.add(new Paragraph(" "));

                // === RESUMEN ===
                PdfPTable balanceTable = new PdfPTable(3);
                balanceTable.setWidthPercentage(100);
                balanceTable.setWidths(new float[] { 1, 1, 1 });

                addBalanceCell(balanceTable, "Min. Contrato", String.valueOf(balance.getMinutosContratoReemplazo()),
                                headerFont,
                                orangeFont);
                addBalanceCell(balanceTable, "Min. Ausente", String.valueOf(balance.getTotalMinutosAusente()),
                                headerFont, orangeFont);
                addBalanceCell(balanceTable, "Min. Reemplazante",
                                String.valueOf(balance.getTotalMinutosReemplazante()),
                                headerFont, orangeFont);

                document.add(balanceTable);
                document.add(new Paragraph(" "));

                // === DETALLE DE REEMPLAZOS ===
                document.add(new Paragraph("Detalle de Movimientos", boldFont));
                document.add(new Paragraph(" "));

                PdfPTable detailTable = new PdfPTable(6);
                detailTable.setWidthPercentage(100);
                detailTable.setWidths(new float[] { 1.2f, 0.8f, 1.5f, 1.2f, 1.2f, 0.8f });

                // Encabezados
                String[] headers = { "Fecha", "Hora", "Rol", "Curso", "Asignatura", "Min." };
                for (String h : headers) {
                        PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                        cell.setBackgroundColor(PRIMARY);
                        cell.setPadding(8);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        detailTable.addCell(cell);
                }

                // Filas de detalle
                DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                boolean alt = false;
                for (Reemplazo r : detalles) {
                        BaseColor rowColor = alt ? LIGHT_GRAY : BaseColor.WHITE;
                        String rol = r.getDocenteAusente().getId().equals(personalId) ? "AUSENTE" : "REEMPLAZANTE";
                        Font rolFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD,
                                        rol.equals("AUSENTE") ? new BaseColor(220, 38, 38)
                                                        : new BaseColor(22, 163, 74));

                        addDetailCell(detailTable, r.getFecha().format(dateFmt), cellFont, rowColor);
                        addDetailCell(detailTable, r.getHoraInicio() != null ? r.getHoraInicio().toString() : "—",
                                        cellFont,
                                        rowColor);
                        addDetailCell(detailTable, rol, rolFont, rowColor);
                        addDetailCell(detailTable, r.getCurso() != null ? r.getCurso() : "—", cellFont, rowColor);
                        addDetailCell(detailTable, r.getAsignatura() != null ? r.getAsignatura() : "—", cellFont,
                                        rowColor);
                        addDetailCell(detailTable, String.valueOf(r.getMinutosReemplazo()), cellFont, rowColor);
                        alt = !alt;
                }

                if (detalles.isEmpty()) {
                        PdfPCell emptyCell = new PdfPCell(new Phrase("Sin movimientos en este período", cellFont));
                        emptyCell.setColspan(6);
                        emptyCell.setPadding(15);
                        emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        detailTable.addCell(emptyCell);
                }

                document.add(detailTable);

                // === ESPACIO PARA FIRMA ===
                document.add(new Paragraph("\n\n\n"));

                PdfPTable firmaTable = new PdfPTable(2);
                firmaTable.setWidthPercentage(80);
                firmaTable.setHorizontalAlignment(Element.ALIGN_CENTER);

                PdfPCell firmaLeft = new PdfPCell();
                firmaLeft.setBorderWidth(0);
                firmaLeft.setBorderWidthTop(1);
                firmaLeft.setBorderColorTop(BaseColor.DARK_GRAY);
                firmaLeft.setPaddingTop(10);
                firmaLeft.setHorizontalAlignment(Element.ALIGN_CENTER);
                firmaLeft.addElement(new Phrase("Recibí Conforme\n" + balance.getNombreCompleto(),
                                new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.DARK_GRAY)));
                firmaTable.addCell(firmaLeft);

                PdfPCell firmaRight = new PdfPCell();
                firmaRight.setBorderWidth(0);
                firmaRight.setBorderWidthTop(1);
                firmaRight.setBorderColorTop(BaseColor.DARK_GRAY);
                firmaRight.setPaddingTop(10);
                firmaRight.setHorizontalAlignment(Element.ALIGN_CENTER);
                firmaRight.addElement(new Phrase("Jefatura / Dirección\nColegio Acrópolis",
                                new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.DARK_GRAY)));
                firmaTable.addCell(firmaRight);

                document.add(firmaTable);

                // === PIE DE PÁGINA ===
                document.add(new Paragraph("\n"));
                Paragraph footer = new Paragraph(
                                "Documento generado automáticamente — " + LocalDate.now().format(dateFmt) +
                                                " — Sistema de Reemplazos y Ausencias, Colegio Acrópolis",
                                new Font(Font.FontFamily.HELVETICA, 7, Font.ITALIC, BaseColor.GRAY));
                footer.setAlignment(Element.ALIGN_CENTER);
                document.add(footer);

                document.close();
                return baos.toByteArray();
        }

        // === Métodos Helper ===

        private void addInfoRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
                PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
                labelCell.setBorderWidth(0);
                labelCell.setPadding(5);
                table.addCell(labelCell);

                PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
                valueCell.setBorderWidth(0);
                valueCell.setPadding(5);
                table.addCell(valueCell);
        }

        private void addBalanceCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
                PdfPCell cell = new PdfPCell();
                cell.setBackgroundColor(LIGHT_GRAY);
                cell.setPadding(12);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorderWidth(0.5f);
                cell.setBorderColor(BaseColor.LIGHT_GRAY);

                Paragraph p = new Paragraph();
                p.setAlignment(Element.ALIGN_CENTER);
                p.add(new Chunk(label + "\n", new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.GRAY)));
                p.add(new Chunk(value, valueFont));
                cell.addElement(p);
                table.addCell(cell);
        }

        private void addDetailCell(PdfPTable table, String text, Font font, BaseColor bgColor) {
                PdfPCell cell = new PdfPCell(new Phrase(text, font));
                cell.setBackgroundColor(bgColor);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorderColor(new BaseColor(226, 232, 240));
                cell.setBorderWidth(0.5f);
                table.addCell(cell);
        }
}
