<%@page import="ru.runa.common.web.MessagesOther"%>
<%@page import="ru.runa.common.web.MessagesTimeScale"%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="java.util.Locale"%>
<%@ page import="org.apache.struts.util.RequestUtils"%>
<%@ page import="ru.runa.common.web.Commons"%>
<%@ page import="ru.runa.common.web.form.IdForm" %>

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
	<script language="javascript" src="<html:rewrite page="/js/dhtmlxgantt/dhtmlxgantt.js"/>" type="text/javascript" charset="utf-8"></script>
	<script language="javascript" src="http://export.dhtmlx.com/gantt/api.js" type="text/javascript" charset="utf-8"></script>
	<link rel="stylesheet" href="<html:rewrite page="/css/dhtmlxgantt.css" />" type="text/css" media="screen" title="no title" charset="utf-8" />
	<script language="javascript" src="<html:rewrite page="/js/dhtmlxgantt/locale/locale.js" />" type="text/javascript" charset="utf-8"></script>
	<script language="javascript" src="<html:rewrite page="/js/dhtmlxgantt/locale/locale_" /><%= RequestUtils.getUserLocale(request, null).getLanguage() %>.js" type="text/javascript" charset="utf-8"></script>

<style type="text/css">
.gantt-chart-gray {
	border: 1px solid #808080;
	color: #808080;
	background: #6ba8e3;
}

.gantt-chart-gray .gantt_task_progress {
	background: #707070;
}

.gantt-chart-green {
	border: 1px solid #34c461;
	color: #34c461;
	background: #34c461;
}

.gantt-chart-green .gantt_task_progress {
	background: #23964d;
}

.gantt-chart-blue {
	border: 1px solid #6ba8e3;
	color: #6ba8e3;
	background: #6ba8e3;
}

.gantt-chart-blue .gantt_task_progress {
	background: #547dab;
}

.gantt-chart-bright-green {
	border: 1px solid #34c461;
	color: #34e461;
	background: #34c461;
}

.gantt-chart-bright-green .gantt_task_progress {
	background: #23b64d;
}

.weekend {
	background: #f4f7f4;
}

.gantt_selected .weekend {
	background: #f7eb91;
}
</style>
</tiles:put>

<tiles:put name="body" type="string">
<%
	String parameterName = IdForm.ID_INPUT_NAME;
	Long processId = Long.parseLong(request.getParameter(parameterName));
%>

<wf:showGanttDiagram identifiableId="<%= processId %>" >
<table width="100%">
	<tr>
		<td align="right">
			<wf:updateProcessLink identifiableId='<%=processId %>' href='<%= "/manage_process.do?" + parameterName+ "=" + processId %>'  />
		</td>
	</tr>
</table>
<br/>
<div id="GanttChartDIV" style='position:relative; width:100%; height:600px'></div>
</wf:showGanttDiagram>

<table width="100%">
	<tr>
		<td align="left">
			<%= MessagesTimeScale.LABEL_TIME_SCALE.message(pageContext) %>:
			<input type="radio" name="gantt-chart-scale" id="scale-minute" value="minute"><label for="minute"><%= MessagesTimeScale.LABEL_MINUTE.message(pageContext) %></label>
			<input type="radio" name="gantt-chart-scale" id="scale-hour" value="hour"><label for="hour"><%= MessagesTimeScale.LABEL_HOUR.message(pageContext) %></label>
			<input type="radio" name="gantt-chart-scale" id="scale-day" value="day" checked><label for="day"><%= MessagesTimeScale.LABEL_DAY.message(pageContext) %></label>
			<input type="radio" name="gantt-chart-scale" id="scale-week" value="week"><label for="week"><%= MessagesTimeScale.LABEL_WEEK.message(pageContext) %></label>
			<input type="radio" name="gantt-chart-scale" id="scale-month" value="month"><label for="month"><%= MessagesTimeScale.LABEL_MONTH.message(pageContext) %></label>
			<input type="radio" name="gantt-chart-scale" id="scale-quarter" value="quarter"><label for="quarter"><%= MessagesTimeScale.LABEL_QUARTER.message(pageContext) %></label>
			<input type="radio" name="gantt-chart-scale" id="scale-year" value="year"><label for="year"><%= MessagesTimeScale.LABEL_YEAR.message(pageContext) %></label>
		</td>
		<td align="right">
			<%= MessagesOther.LABEL_EXPORT_TO.message(pageContext) %>:
			<input value="PDF" type="button" onclick='gantt.exportToPDF()'/>
			<input value="PNG" type="button" onclick='gantt.exportToPNG()'/>
			<input value="Excel" type="button" onclick='gantt.exportToExcel()'/>
		</td>
	</tr>
</table>

<script language="javascript">
	function setGanttChartScale(scale) {
		switch (scale) {
		case "minute":
			gantt.config.scale_unit = "minute";
			gantt.config.step = 1;
			gantt.config.date_scale = "%i";
			gantt.config.duration_unit = "minute";
			gantt.config.duration_step = 1;
			gantt.config.min_column_width = 20;
			gantt.config.scale_height = 50;
			gantt.config.subscales = [
				{unit: "day", step: 1, date: "%j %F, %Y, %l"},
				{unit: "hour", step: 1, date: "%G"}
			];
			gantt.templates.date_scale = null;
			break;
		case "hour":
			gantt.config.scale_unit = "hour";
			gantt.config.step = 1;
			gantt.config.date_scale = "%G:%i";
			gantt.config.duration_unit = "hour";
			gantt.config.duration_step = 1;
			gantt.config.min_column_width = 40;
			gantt.config.scale_height = 50;
			gantt.config.subscales = [
				{unit: "day", step: 1, date: "%j %F, %Y, %l"}
			];
			gantt.templates.date_scale = null;
			break;
		case "day":
			gantt.config.scale_unit = "day";
			gantt.config.step = 1;
			gantt.config.date_scale = "%j, %D";
			gantt.config.duration_step = 1;
			gantt.config.duration_unit = "day";
			gantt.config.min_column_width = 50;
			gantt.config.scale_height = 50;
			var weekScaleTemplate = function(date) {
				var dateToStr = gantt.date.date_to_str("%d %M");
				var dateToStrY = gantt.date.date_to_str("%Ww, %Y");
				var endDate = gantt.date.add(gantt.date.add(date, 1, "week"), -1, "day");
				return dateToStr(date) + " - " + dateToStr(endDate) + ", " + dateToStrY(date);
			};
			gantt.config.subscales = [
				{unit: "week", step: 1, template: weekScaleTemplate}
			];
			gantt.templates.date_scale = null;
			break;
		case "week":
			gantt.config.scale_unit = "week";
			gantt.config.step = 1;
			gantt.config.duration_unit = "week";
			gantt.config.duration_step = 1;
			gantt.config.min_column_width = 75;
			gantt.config.scale_height = 50;
			gantt.config.subscales = [
				{unit: "month", step: 1, date: "%F, %Y"}
			];
			var weekScaleTemplate = function(date) {
				var dateToStr = gantt.date.date_to_str("%d");
				var dateToStrY = gantt.date.date_to_str("%Ww");
				var endDate = gantt.date.add(gantt.date.add(date, 1, "week"), -1, "day");
				return dateToStr(date) + " - " + dateToStr(endDate) + ", " + dateToStrY(date);
			};
			gantt.templates.date_scale = weekScaleTemplate;
			break;
		case "month":
			gantt.config.scale_unit = "month";
			gantt.config.step = 1;
			gantt.config.duration_unit = "month";
			gantt.config.duration_step = 1;
			gantt.config.date_scale = "%M";
			gantt.config.scale_height = 50;
			gantt.config.min_column_width = 40;
			gantt.config.subscales = [
				{unit: "year", step: 1, date: "%Y"}
			];
			gantt.templates.date_scale = null;
			break;
		case "quarter":
			gantt.config.scale_unit = "quarter";
			gantt.config.step = 1;
			gantt.config.date_scale = null; //"%M";
			gantt.config.duration_unit = "month";
			gantt.config.duration_step = 1;
			gantt.config.min_column_width = 40;
			gantt.config.scale_height = 50;
			var quarterLabel = function(date) {
				var month = date.getMonth();
				var q_num;
				if (month >= 9) {
					q_num = 4;
				} else if (month >= 6) {
					q_num = 3;
				} else if (month >= 3) {
					q_num = 2;
				} else {
					q_num = 1;
				}
				return "Q" + q_num;
			}
			gantt.config.subscales = [
				{unit: "year", step: 1, date: "%Y"},
				{unit: "month", step: 1, date: "%M"}
			];
			gantt.templates.date_scale = quarterLabel;
			break;
		case "year":
			gantt.config.scale_unit = "year";
			gantt.config.step = 1;
			gantt.config.date_scale = "%Y";
			gantt.config.duration_unit = "year";
			gantt.config.duration_step = 1;
			gantt.config.min_column_width = 40;
			gantt.config.scale_height = 50;
			var monthScaleTemplate = function(date){
				var dateToStr = gantt.date.date_to_str("%M");
				var endDate = gantt.date.add(date, 2, "month");
				return dateToStr(date) + " - " + dateToStr(endDate);
			};
			gantt.config.subscales = [
				{unit: "month", step: 3, template: monthScaleTemplate},
				{unit: "month", step: 1, date: "%M" }
			];
			gantt.templates.date_scale = null;
			break;
		}
	}

	gantt.config.columns = [
		{name: "text", label: " ", tree: true, align: "left", width: "*", resize: true},
		{name: "resource", align: "left", resize: true},
		{name: "duration", align: "center"},
		{name: "start_date", align: "center", width: 100},
		{name: "end_date", align: "center", width: 100}
	];

	gantt.templates.task_class = function(start, end, task) {
		switch (task.type) {
		case "process": return "gantt-chart-gray";
		case "task1": return "gantt-chart-green";
		case "task2": return "gantt-chart-blue";
		default: return "gantt-chart-bright-green";
		}
	};

	gantt.config.grid_width = 550;
	gantt.config.date_grid = "%d.%m.%y %H:%i";
	gantt.config.xml_date = "%Y-%m-%d %H:%i";
	gantt.config.row_height = 25;
	gantt.config.readonly = true;
	gantt.config.autosize = "y";

    gantt.templates.scale_cell_class = function(date) {
        if (".minute.hour.day.".indexOf("." + gantt.config.scale_unit + ".") > -1) {
	        if(date.getDay()==0 || date.getDay()==6) {
	            return "weekend";
	        }
	    }
    };

    gantt.templates.task_cell_class = function(item, date) {
        if (".minute.hour.day.".indexOf("." + gantt.config.scale_unit + ".") > -1) {
	        if(date.getDay()==0 || date.getDay()==6) {
	            return "weekend"
	        }
        }
    };

	setGanttChartScale('day');

	gantt.init('GanttChartDIV');
	gantt.parse(tasks);

	var scaleOnClickHandler = function(e) {
		e = e || window.event;
		var el = e.target || e.srcElement;
		var value = el.value;
		setGanttChartScale(value);
		gantt.parse(tasks);
	};

	var els = document.getElementsByName("gantt-chart-scale");

	for (var i = 0; i < els.length; i++) {
		els[i].onclick = scaleOnClickHandler;
	}
</script>

</tiles:put>

<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>
