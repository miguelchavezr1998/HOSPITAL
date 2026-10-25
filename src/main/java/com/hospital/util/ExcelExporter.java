package com.hospital.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class ExcelExporter {

    private ExcelExporter() {
    }

    public static void exportar(String nombreHoja, List<String> encabezados,
                                List<List<String>> registros, Path destino) throws IOException {
        Objects.requireNonNull(nombreHoja, "El nombre de la hoja es obligatorio");
        Objects.requireNonNull(encabezados, "Los encabezados son obligatorios");
        Objects.requireNonNull(registros, "Los registros son obligatorios");
        Objects.requireNonNull(destino, "La ruta de destino es obligatoria");
        if (encabezados.isEmpty()) {
            throw new IllegalArgumentException("Debe especificar al menos un encabezado");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(nombreHoja);
            CellStyle estiloEncabezado = workbook.createCellStyle();
            estiloEncabezado.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            estiloEncabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font fuente = workbook.createFont();
            fuente.setBold(true);
            estiloEncabezado.setFont(fuente);

            Row filaEncabezado = sheet.createRow(0);
            for (int columna = 0; columna < encabezados.size(); columna++) {
                Cell celda = filaEncabezado.createCell(columna);
                celda.setCellValue(encabezados.get(columna));
                celda.setCellStyle(estiloEncabezado);
            }

            for (int fila = 0; fila < registros.size(); fila++) {
                List<String> registro = registros.get(fila);
                if (registro.size() != encabezados.size()) {
                    throw new IllegalArgumentException(
                            "El registro " + (fila + 1) + " no coincide con el numero de encabezados");
                }
                Row filaDatos = sheet.createRow(fila + 1);
                for (int columna = 0; columna < registro.size(); columna++) {
                    String valor = registro.get(columna);
                    filaDatos.createCell(columna).setCellValue(valor != null ? valor : "");
                }
            }

            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(
                    0, registros.size(), 0, encabezados.size() - 1));
            for (int columna = 0; columna < encabezados.size(); columna++) {
                sheet.autoSizeColumn(columna);
                sheet.setColumnWidth(columna, Math.min(sheet.getColumnWidth(columna), 60 * 256));
            }

            try (OutputStream output = Files.newOutputStream(destino)) {
                workbook.write(output);
            }
        }
    }
}
