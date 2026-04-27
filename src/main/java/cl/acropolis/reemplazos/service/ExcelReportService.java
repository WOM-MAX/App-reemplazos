package cl.acropolis.reemplazos.service;

import cl.acropolis.reemplazos.dto.BalanceMensualDTO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ExcelReportService {

    private final ReemplazoService reemplazoService;

    /**
     * Genera un archivo Excel (.xlsx) con el balance mensual de todo el personal.
     */
    public byte[] generarExcelBalanceMensual(int mes, int anio) throws Exception {
        List<BalanceMensualDTO> balances = reemplazoService.calcularBalanceMensual(mes, anio);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Balance Mensual");

            // Estilos
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle cellStyle = createCellStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle positiveStyle = createColorNumberStyle(workbook, IndexedColors.GREEN);
            CellStyle negativeStyle = createColorNumberStyle(workbook, IndexedColors.RED);
            CellStyle totalStyle = createTotalStyle(workbook);
            CellStyle totalNumberStyle = createTotalNumberStyle(workbook);

            int rowNum = 0;

            // Título
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("COLEGIO ACRÓPOLIS — Balance General de Reemplazos y Ausencias");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            // Período
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "CL"));
            String periodo = LocalDate.of(anio, mes, 1).format(formatter).toUpperCase();
            Row periodoRow = sheet.createRow(rowNum++);
            Cell periodoCell = periodoRow.createCell(0);
            periodoCell.setCellValue("Período: " + periodo + "  |  Generado: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 8));

            rowNum++; // Fila vacía

            // Encabezados
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"N°", "RUN", "Nombre Completo", "Cargo", "Email", "Min. Contrato", "Σ Reemplazo", "Σ Ausencia", "Σ Atraso", "Balance"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int num = 1;
            int totalContrato = 0, totalReemplazo = 0, totalAusencia = 0, totalAtraso = 0, totalBalance = 0;

            for (BalanceMensualDTO balance : balances) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(num++);
                row.getCell(0).setCellStyle(cellStyle);

                Cell runCell = row.createCell(1);
                runCell.setCellValue(balance.getRun());
                runCell.setCellStyle(cellStyle);

                Cell nombreCell = row.createCell(2);
                nombreCell.setCellValue(balance.getNombreCompleto());
                nombreCell.setCellStyle(cellStyle);

                Cell cargoCell = row.createCell(3);
                cargoCell.setCellValue(balance.getCargo() != null ? balance.getCargo() : "—");
                cargoCell.setCellStyle(cellStyle);

                Cell emailCell = row.createCell(4);
                emailCell.setCellValue(balance.getEmailInstitucional() != null ? balance.getEmailInstitucional() : "—");
                emailCell.setCellStyle(cellStyle);

                int contrato = balance.getMinutosContratoReemplazo() != null ? balance.getMinutosContratoReemplazo() : 0;
                int reemplazo = balance.getTotalMinutosReemplazante() != null ? balance.getTotalMinutosReemplazante() : 0;
                int ausencia = balance.getTotalMinutosAusente() != null ? balance.getTotalMinutosAusente() : 0;
                int atraso = balance.getTotalMinutosAtraso() != null ? balance.getTotalMinutosAtraso() : 0;
                int bal = balance.getBalance() != null ? balance.getBalance() : 0;

                Cell contratoCell = row.createCell(5);
                contratoCell.setCellValue(contrato);
                contratoCell.setCellStyle(numberStyle);

                Cell reemplazoCell = row.createCell(6);
                reemplazoCell.setCellValue(reemplazo);
                reemplazoCell.setCellStyle(positiveStyle);

                Cell ausenciaCell = row.createCell(7);
                ausenciaCell.setCellValue(ausencia);
                ausenciaCell.setCellStyle(negativeStyle);

                Cell atrasoCell = row.createCell(8);
                atrasoCell.setCellValue(atraso);
                atrasoCell.setCellStyle(negativeStyle);

                Cell balanceCell = row.createCell(9);
                balanceCell.setCellValue(bal);
                balanceCell.setCellStyle(bal >= 0 ? positiveStyle : negativeStyle);

                totalContrato += contrato;
                totalReemplazo += reemplazo;
                totalAusencia += ausencia;
                totalAtraso += atraso;
                totalBalance += bal;
            }

            // Fila de totales
            rowNum++; // Fila vacía
            Row totalRow = sheet.createRow(rowNum);

            Cell totalLabelCell = totalRow.createCell(4);
            totalLabelCell.setCellValue("TOTALES:");
            totalLabelCell.setCellStyle(totalStyle);

            Cell tc = totalRow.createCell(5);
            tc.setCellValue(totalContrato);
            tc.setCellStyle(totalNumberStyle);

            Cell tr = totalRow.createCell(6);
            tr.setCellValue(totalReemplazo);
            tr.setCellStyle(totalNumberStyle);

            Cell ta = totalRow.createCell(7);
            ta.setCellValue(totalAusencia);
            ta.setCellStyle(totalNumberStyle);

            Cell tat = totalRow.createCell(8);
            tat.setCellValue(totalAtraso);
            tat.setCellStyle(totalNumberStyle);

            Cell tb = totalRow.createCell(9);
            tb.setCellValue(totalBalance);
            tb.setCellStyle(totalNumberStyle);

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    // === Estilos ===

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = createCellStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        return style;
    }

    private CellStyle createColorNumberStyle(Workbook workbook, IndexedColors color) {
        CellStyle style = createCellStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(color.getIndex());
        style.setFont(font);
        return style;
    }

    private CellStyle createTotalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createTotalNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
