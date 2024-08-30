package tech.codingless.standalone.datawork.v1.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson2.JSON;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.util.StringUtil;

@Slf4j
public class ProcessResponse {
	Consumer<? super Map<Object, Object>> jsonAction;
	Consumer<? super ExcelData> excelAction;
	Consumer<Long> mqAction;
	Consumer<? super Map<Object, Object>> finishedAction;
	Consumer<Tuple2<String, String>> pdfAction;
	Consumer<String> htmlAction;
	Consumer<String> beginZipAction;
	Consumer<? super Map<Object, Object>> csvAction;
	Consumer<Tuple2<String, String>> textAction;

	/**
	 * 延迟消费
	 * 
	 * @param mqAction
	 */
	public void onConsumerDelay(Consumer<Long> mqAction) {
		this.mqAction = mqAction;
	}

	public void reconsumerLater(Long delay) {
		mqAction.accept(delay);
	}

	/**
	 * 当输出JSON数据的时候
	 * 
	 * @param jsonAction
	 */
	public void onOutputJson(Consumer<? super Map<Object, Object>> jsonAction) {
		this.jsonAction = jsonAction;
	}

	/**
	 * 输出Excel内容
	 * 
	 * @param excelAction
	 */
	public void onOutputExcel(Consumer<? super ExcelData> excelAction) {
		this.excelAction = excelAction;
	}

	/**
	 * 结束
	 * 
	 * @param finishedAction
	 */
	public void onFinished(Consumer<? super Map<Object, Object>> finishedAction) {
		this.finishedAction = finishedAction;
	}

	public void json(Map<Object, Object> map) {
		if (jsonAction != null) {
			jsonAction.accept(map);
		} else {
			log.warn("Has JSON Response But Not Found Consumer");
		}
	}

	public void finished(Map<Object, Object> map) {
		if (finishedAction != null) {
			finishedAction.accept(map);
		}
	}

	@SuppressWarnings("unchecked")
	public void excel(Map<Object, Object> map) {
		if (excelAction != null) {
			try {
				ExcelData excelData = new ExcelData();
				Object zip = map.get("zip");
				if (zip != null && zip instanceof Boolean) {
					excelData.setZip((Boolean) zip);
				}
				Object zipName = map.get("zipName");
				if (zipName != null && zipName instanceof String) {
					excelData.setZipName((String) zipName);
				}
				excelData.setName(StringUtil.defaultIfEmpty(map.get("name"), "未命名Excel"));
				Object sheets = map.get("sheets");
				if (sheets == null) {
					return;
				}
				List<Map<String, Object>> sheetList = (List<Map<String, Object>>) sheets;
				if (sheetList.isEmpty()) {
					return;
				}
				excelData.setSheets(new ArrayList<>(sheetList.size() * 3 / 2 + 1));
				int i = 0;
				for (Map<String, Object> data : sheetList) {
					i++;
					ExcelData.ExcelSheet sheet = new ExcelData.ExcelSheet();
					excelData.getSheets().add(sheet);
					sheet.setName(StringUtil.defaultIfEmpty(data.get("name"), "Sheet" + i));
					if (data.containsKey("head")) {
						List<String> headList = (List<String>) data.get("head");
						sheet.setHead(headList);
					}
					if (data.containsKey("content")) {
						sheet.setContent((List<List<String>>) data.get("content"));
					}

					if (data.containsKey("table") && data.get("table") instanceof List && data.containsKey("columns")) {

						List<Map<String, Object>> table = (List<Map<String, Object>>) data.get("table");
						List<String> columns = (List<String>) data.get("columns");
						if (!CollectionUtils.isEmpty(columns)) {
							List<List<String>> tableDataList = table.stream().map(row -> {
								List<String> excelDataRow = new ArrayList<>(columns.size() * 3 / 2 + 1);

								columns.forEach(columnName -> {
									excelDataRow.add(StringUtil.defaultIfEmpty(row.get(columnName), ""));
								});
								return excelDataRow;

							}).collect(Collectors.toList());
							sheet.setContent(tableDataList);
						}

					}

					if (data.containsKey("styles")) {
						List<Map<String, Object>> styles = (List<Map<String, Object>>) data.get("styles");
						List<ExcelData.Style> styleList = styles.stream().map(style -> {
							try {
								return JSON.parseObject(JSON.toJSONString(style), ExcelData.Style.class);
							} catch (Exception e) {
								log.error("Parse Style Fail", e);
							}
							return null;
						}).filter(item -> item != null).collect(Collectors.toList());
						sheet.setStyles(styleList);
					}

					// 使用完数据就删除，以节省内存
					// data.clear();
				}
				// 使用完数据就删除，以节省内存
				// map.clear();
				excelAction.accept(excelData);
			} catch (Exception e) {
				log.error("format excel data fail", e);
			}
		} else {
			log.warn("Has Excel Response But Not Found Consumer");
		}
	}

}
