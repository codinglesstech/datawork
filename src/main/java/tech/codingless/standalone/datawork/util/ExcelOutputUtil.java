package tech.codingless.standalone.datawork.util;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.util.CollectionUtils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.handler.context.RowWriteHandlerContext;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.v1.data.ExcelData;
import tech.codingless.standalone.datawork.v1.data.ExcelData.Style;

@Slf4j
public class ExcelOutputUtil {

	public static class CustomCellWriteHandler implements RowWriteHandler {
		private List<ExcelData.Style> styles = null;
		private Map<Integer, ExcelData.Style> rowStyles = null;
		private Map<Integer, ExcelData.Style> columnStyles = null;
		private Map<String, ExcelData.Style> cellStyles = null;
		private boolean invaild = true;

		public CustomCellWriteHandler(List<ExcelData.Style> styles) {

			this.styles = styles;
			rowStyles = new HashMap<>();
			columnStyles = new HashMap<>();
			cellStyles = new HashMap<>();
			if (CollectionUtils.isEmpty(styles)) {
				return;
			}
			styles.forEach(style -> {
				if ("row".equalsIgnoreCase(style.getFormat()) && style.getRow() != null) {
					rowStyles.put(style.getRow(), style);
				} else if ("column".equalsIgnoreCase(style.getFormat()) && style.getColumn() != null && style.getColumn() > -1) {
					columnStyles.put(style.getColumn(), style);
				} else if ("cell".equalsIgnoreCase(style.getFormat()) && style.getColumn() != null && style.getRow() != null && style.getRow() > -1 && style.getColor() > -1) {
					cellStyles.put(style.getRow() + ":" + style.getColumn(), style);
				}

			});

			if (!rowStyles.isEmpty() || !columnStyles.isEmpty() || !cellStyles.isEmpty()) {
				invaild = false;
			}

		}

		@Override
		public void afterRowDispose(RowWriteHandlerContext context) {
			if (invaild || styles == null || styles.isEmpty()) {
				return;
			}

			try {
				Workbook workbook = context.getRow().getSheet().getWorkbook();
				Row row = context.getRow();

				if (!rowStyles.isEmpty() && rowStyles.containsKey(context.getRowIndex())) {
					// 这一行有样式定义
					Style style = rowStyles.get(context.getRowIndex());
					CellStyle cellStyle = fetch(style, workbook);
					if (style.getHeight() != null) {
						row.setHeight((short) style.getHeight().intValue());
					}
					// cellStyle.setBorderBottom(BorderStyle.DASH_DOT_DOT);

					for (short i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
						row.getCell(i).setCellStyle(cellStyle);
					}

					// row.setRowStyle(cellStyle);
				}

				if (!columnStyles.isEmpty()) {
					columnStyles.entrySet().forEach(entry -> {
						Style style = entry.getValue();
						if (row.getRowNum() < style.getBeginRow()) {
							return;
						}
						Cell cell = row.getCell(style.getColumn());
						if (cell == null) {
							return;
						}
						CellStyle cellStyle = fetch(style, workbook);
						cell.setCellStyle(cellStyle);
						try {
							if ("number".equalsIgnoreCase(style.getType())) {
								String value = cell.getStringCellValue();
								if (StringUtil.isFloat(value)) {
									cell.setCellValue(Double.parseDouble(value));
								}
							}
						} catch (Exception e) {
							log.error("Set Cell Value Fail", e);
						}
					});
				}
			} catch (Exception e) {
				log.error("Update Style Fail", e);
			}

			RowWriteHandler.super.afterRowDispose(context);
		}

		private Map<Style, CellStyle> styleCache = new HashMap<>();

		/**
		 * 根据样式定义，获取CellStyle对象，第二次缓存起来
		 * 
		 * @param value
		 * @param workbook
		 * @return
		 */
		private CellStyle fetch(Style style, Workbook workbook) {
			if (styleCache.containsKey(style)) {
				return styleCache.get(style);
			}
			CellStyle cellStyle = workbook.createCellStyle();
			Font font = workbook.createFont();
			if (BooleanUtil.isTrue(style.getBold())) {
				font.setBold(true);
			}
			if (BooleanUtil.isTrue(style.getItalic())) {
				font.setItalic(true);
			}
			if (style.getColor() != null) {
				font.setColor((short) style.getColor().intValue());
			}
			cellStyle.setFont(font);
			styleCache.put(style, cellStyle);
			// cellStyle.setBorderBottom(BorderStyle.DASH_DOT_DOT);
			return cellStyle;
		}
	}

	public static void write(ExcelData data, OutputStream outputstream) {

		try (ExcelWriter writer = EasyExcel.write(outputstream).build()) {
			for (ExcelData.ExcelSheet sheetData : data.getSheets()) {

				ExcelWriterSheetBuilder builder = EasyExcel.writerSheet(sheetData.getName());
				builder.registerWriteHandler(new LongestMatchColumnWidthStyleStrategy());
				if (!CollectionUtils.isEmpty(sheetData.getStyles())) {
					CustomCellWriteHandler hander = new CustomCellWriteHandler(sheetData.getStyles());
					builder.registerWriteHandler(hander);
				}
				if (!CollectionUtils.isEmpty(sheetData.getHead())) {
					builder.head(sheetData.getHead().stream().map(item -> List.of(item)).collect(Collectors.toList()));
				}
				WriteSheet sheet = builder.build();
				writer.write(sheetData.getContent(), sheet);
			}
		}
	}

}
