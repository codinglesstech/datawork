package tech.codingless.standalone.datawork.v1.data;

import java.util.List;

import lombok.Data;

@Data
public class ExcelData {
	private String name;
	private List<ExcelSheet> sheets;
	private Boolean zip;
	private String zipName;

	@Data
	public static class ExcelSheet {
		private String name;
		private List<String> head;
		private List<List<String>> content;
		private List<Style> styles;

	}

	@Data
	public static class Style {
		// [{format:"row|column|cell",row:-1,column:1,type:"number",color:1,height:100,width:300}]
		private String format;//
		private Integer row;
		private Integer beginRow = 1;
		private Integer column;
		private String type; // number, link
		private Integer color;
		private Integer height;
		private Integer width;
		private Boolean bold;
		private Boolean italic;
	}
}
