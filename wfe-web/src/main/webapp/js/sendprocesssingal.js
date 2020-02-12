function addRoutingRow() {
  var tableId = 'routing';
  var table = document.getElementById('routing');
  var len = table.getElementsByTagName("tr").length - 1;
  var index = len - 1;
  var row = table.insertRow(len);
  var cell1 = row.insertCell(0);
  var cell2 = row.insertCell(1);
  cell1.setAttribute("class", "list");
  cell2.setAttribute("class", "list");
  cell1.innerHTML = '<input type="TEXT" name="' + tableId + 'Param(' + index + ')"/>';
  cell2.innerHTML = '<input type="TEXT" name="' + tableId + 'Value(' + index + ')"/>';
}

function addPayloadRow() {
  var table = document.getElementById('payload');
  var len = table.getElementsByTagName("tr").length - 1;
  var index = len - 1;
  var row = table.insertRow(len);
  var cell1 = row.insertCell(0);
  var cell2 = row.insertCell(1);
  cell1.setAttribute("class", "list");
  cell2.setAttribute("class", "list");
  cell1.innerHTML = '<input type="TEXT" name="payloadParam(' + index + ')"/>';
  cell2.innerHTML = '<input type="TEXT" style="width:200px" name="payloadValue(' + index + ')"/> <select name="payloadType(' + index + ')"><option value="string" selected>string</option><option value="boolean">boolean</option><option value="text">text</option><option value="time">time</option><option value="datetime">datetime</option><option value="date">date</option><option value="executor">executor</option><option value="double">double</option><option value="processref">processref</option><option value="hidden">hidden</option><option value="executor">executor</option><option value="integer">integer</option><option value="bigdecimal">bigdecimal</option><option value="formattedText">formattedText</option> </select> ';
}
