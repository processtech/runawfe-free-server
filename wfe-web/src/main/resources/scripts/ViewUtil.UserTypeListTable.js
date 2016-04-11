
var jsonInputArrayUNIQUENAME = JSONDATATEMPLATE;

$(document).ready(function() {
	
	try {
		
		$(this).tableConstructorUNIQUENAME(
							jsonInputArrayUNIQUENAME, 
							"#containerUNIQUENAME", 
							"SORTFIELDNAMEVALUE", 
							DIMENTIONALVALUE, 
							SELECTABLEVALUE,
							"DECTSELECTNAME");
		
	} catch(e) {
		console.error("ready: %s", e.message);
	}
});

(function($) {
	
	
	var methods = {
	
		"init" : function (input, container, sort, dim, sel, outname) {
			
			console.info("init: input: %s container: %s sort: %s dim: %s sel: %s outname: %s", input, container, sort, dim, sel, outname);
			
			var out;
			
			if (sel) {
				out = $("<input>");
				out.attr("type", "hidden");
				out.attr("name", outname);
				out.attr("value", "");
				$(container).append(out);
			}
			
			$(this).data({
				jsonInputArray : input,	
				sortFieldName : sort,
				containerName : container,
				isSelectable : sel,
				outContainer : out
				});
			
			try {
				for (var i = 0; i < $(this).data().jsonInputArray.length; i++) {
					if (typeof $(this).data().jsonInputArray[i] != "string") {
						stringifyValues($(this).data().jsonInputArray[i]);
						continue;
					}
					$(this).data().jsonInputArray[i] = JSON.parse($(this).data().jsonInputArray[i]);
					stringifyValues($(this).data().jsonInputArray[i]);
				}
			} catch(e) {
				console.error("init: %s", e.message);
			}
			
			if (dim) {
				$(this).tableConstructorUNIQUENAME("makeAsMultiDimentionalTable");
			} else {
				$(this).tableConstructorUNIQUENAME("makeAsTwoDimentionalTable");
			}
		},
		
		"getPreferedTitles" : function () {
			
			var titles = [];
			for (var i = 0; i < $(this).data().jsonInputArray.length; i++) {
				for (var field in $(this).data().jsonInputArray[i]) {
					if ($.inArray(field, titles) == -1) {
						titles.push(field);
					}
				}
			}
			return titles;
		},
		
		"makeAsTwoDimentionalTable" : function () {
			
			var titles = $(this).tableConstructorUNIQUENAME("getPreferedTitles");
			var columns = [];
			try {
				for (var i = 0; i < $(this).data().jsonInputArray.length; i++) {
					var row = [];
					for (var j = 0; j < titles.length; j++) {
						if ($(this).data().jsonInputArray[i][titles[j]]) {
							row.push($(this).data().jsonInputArray[i][titles[j]]);
						} else {
							row.push("");
						}
					}
					columns.push(row);
				}
				sortRows($.inArray($(this).data().sortFieldName, titles), columns);
				$($(this).data().containerName).TidyTable({
					enableCheckbox: $(this).data().isSelectable,
					enableMenu:     false,
					reverseSortDir: false,
					responsive:     false
				},
				{
					columnTitles: titles,
					columnValues: columns,
					sortByPattern: getSortValuePattern,
					selectCallback : $(this).tableConstructorUNIQUENAME("getSelectCallback"),
					postProcess: {
						table:  applyCheboxStyle,
						column: function(col){},
						menu:   function(menu){}
					}
				});
			} catch(e) {
				console.error("makeAsTwoDimentionalTable: %s", e.message);
			}
		},
		
		"makeAsMultiDimentionalTable" : function () {
			
			var titles = [];
			var columns = [];
			var fields = $(this).tableConstructorUNIQUENAME("getPreferedTitles");
			try {
				if ($(this).data().sortFieldName != "null" && 
						$.inArray($(this).data().sortFieldName, fields) != -1) {
					titles.push($(this).data().sortFieldName);
					titles.push("");
				}
				for (var i = 0; i < $(this).data().jsonInputArray.length; i++) {
					var subrows = [];
					for (var j = 0; j < fields.length; j++) {
						if ($(this).data().jsonInputArray[i][fields[j]]) {
							subrows.push([fields[j], $(this).data().jsonInputArray[i][fields[j]]]);
						} else {
							subrows.push([fields[j], ""]);
						}
					}
					var subtable = $("<div>");
					subtable.TidyTable({
						enableCheckbox: false,
						enableMenu:     false,
						reverseSortDir: false,
						responsive:     false
					},
					{
						columnTitles: [],
						columnValues: subrows,
						postProcess: {
							table:  applyCheboxStyle,
							column: function(col){},
							menu:   function(menu){}
						}
					});
					if ($(this).data().sortFieldName != "null") {
						if ($(this).data().jsonInputArray[i][$(this).data().sortFieldName]) {
							columns.push([$(this).data().jsonInputArray[i][$(this).data().sortFieldName], subtable]);
						} else {
							columns.push(["", subtable]);
						}
					} else {
						columns.push([subtable]);
					}
				}
				if ($(this).data().sortFieldName != "null" && 
						$.inArray($(this).data().sortFieldName, fields) != -1) {
					sortRows(0, columns);
				}
				$($(this).data().containerName).TidyTable({
					enableCheckbox: $(this).data().isSelectable,
					enableMenu:     false,
					reverseSortDir: false,
					responsive:     false
				},
				{
					columnTitles: titles,
					columnValues: columns,
					sortByPattern: getSortValuePattern,
					selectCallback: $(this).tableConstructorUNIQUENAME("getSelectCallback"),
					postProcess: {
						table:  applyCheboxStyle,
						column: function(col){},
						menu:   function(menu){}
					}
				});
			} catch(e) {
				console.error("makeAsMultiDimentionalTable: %s", e.message);
			}
		},
	
		"getSelectCallback" : function() {
			var data = {
					inputArray : $(this).data().jsonInputArray,
					out : $(this).data().outContainer
			};
			return function(table) {
				if (!table) {
					setOutValue(data.out, []);
					return;
				}
				try {
					var rows = table.find("tr");
					var selectedArray = [];
					for (var i = 0; i < data.inputArray.length; i++) {
						var jsonObjValues = [];
						jsonObjValues[0] = null;
						var jsonObj = data.inputArray[i];
						console.log("getSelectCallback.callback: i: %s jsonObj: %s", i, JSON.stringify(jsonObj));
						for (var key in jsonObj) {
							jsonObjValues.push(jsonObj[key]);
						}
						for (var j = 0; j < rows.length; j++) {
							var row = $(rows[j]);
							var match = true;
							var cells = row.find("td");
							if (cells.length < 2) {
								match = false;
							}
							for (var k = 1; k < cells.length; k++) {
								var cellvalue = $(cells[k]).html();
								var jsonvalue = jsonObjValues[k];
								console.log("getSelectCallback.callback: i: %s j: %s k: %s cellvalue: %s value: %s", i, j, k, cellvalue, jsonvalue);
								if (compareStringsByWords(cellvalue, jsonvalue)) {
									continue;
								}
								match = false;
								break;
							}
							console.log("getSelectCallback.callback: i: %s j: %s match: %s checked: %s", i, j, match, row.find(":checkbox").first().prop("checked"));
							if (match && row.find(":checkbox").first().prop("checked")) {
								selectedArray.push(jsonObj);
							}
							if (match) {
								break;
							}
						}
					}
					setOutValue(data.out, selectedArray);
				} catch(ex) {
					console.error("getSelectCallback.callback: %s", ex.message);
				}
			};
		}
	};
	
	$.fn.tableConstructorUNIQUENAME = function(method) {
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		}
		else
		if (typeof method === "object" || !method) {
			return methods.init.apply(this, arguments);
		}
		else {
			console.error("call: method: %s does not exist", method);
		}
	};
	
	function sortRows (col_num, rows) {
		if (rows.length <= 1 || col_num < 0 || col_num >= rows[0].length) {
			return;
		}
		rows.sort(function(a, b) {
			var v1 = getSortValuePattern(col_num, a[col_num]);
			var v2 = getSortValuePattern(col_num, b[col_num]);
			return v1 < v2 ? -1 : (v1 > v2 ? 1 : 0);
		});
	}
	
	function getSortValuePattern(col_num, val) {
		/*int pattern*/
		if (isNumber(val)) {
			return val;
		}
		/*skip objects*/
		if (typeof val != "string") {
			val = val.toString();
		}
		/*direct convert to number*/
		try {
			var num = parseInt(val);
			if (!(/\D/g).test(val) && isNumber(num)) {
				return num;
			}
		} catch(e) {
			
		}
		/*other convert to number patterns*/
		var res;
		try {
			/*date pattern*/
			var datere = /(\d{2})\.(\d{2})\.(\d{4})\s+(\d{2}):(\d{2})/g;
			var matchdate = datere.exec(val)
			if (matchdate) {
				res = matchdate[3] + matchdate[2] + matchdate[1] + matchdate[4] + matchdate[5];
				res = parseInt(res);
			} else {
				res = val;
			}
		} catch(e) {
			return val;
		}
		return res;
	};
	
	function setOutValue(elem, jsonvalobj) {
		try {
			var stringify = "";
			for (var i = 0; i < jsonvalobj.length; i++) {
				if (i != 0) {
					stringify += ", "
				}
				stringify += JSON.stringify(jsonvalobj[i]).replace(/<br>/g, "\\r\\n");
			}
			console.info("setOutValue: elem: %s stringify: %s", elem.attr("name"), stringify);
			elem.attr("value", stringify);
		} catch(e) {
			console.error("setOutValue: %s", e.message);
		}
	};
	
	function stringifyValues(jsonObj) {
		for (key in jsonObj) {
			if (typeof jsonObj[key] == "string") {
				
			} else if (jsonObj[key]) {
				jsonObj[key] = jsonObj[key].toString();
			} else {
				jsonObj[key] = "";
			}
			jsonObj[key] = jsonObj[key].replace(/\r\n/g, "<br>").replace(/\n/g, "<br>");
		}
	};
	
	function isNumber(val) {
		return typeof val == "number" || (typeof val == "object" && val.constructor === Number);
	};
	
	function applyCheboxStyle(table) {
		table.find(":checkbox").css("margin","3px 3px 3px 4px");
		table.find(":checkbox").css("width","30px");
	};
	
	function compareStringsByWords(s1, s2) {
		var a1 = s1.split(/[\s+<br>\s+]+|\s+/ig);
		var a2 = s2.split(/[\s+<br>\s+]+|\s+/ig);
		if (a1.length != a2.length) {
			return false;
		}
		var result = true;
		for (var i = 0, len = a1.length; i < len; i++) {
			if (a1[i] == a2[i]) {
				continue;
			}
			result = false;
			break;
		}
		return result;
	};

})(jQuery);

