
var numCounter_UNIQUENAME = START_COUNTER;
$(document).ready(function() {
    $('#btnAdd_UNIQUENAME').click(function() {
        updateDialogContent_UNIQUENAME();
    });
    $.editDialog_UNIQUENAME = $('<div></div>').dialog({
        width: 300, modal:true, autoOpen: false, 
        overlay: {backgroundColor: '#000', opacity: 0.5}
    });
    $.editDialog_UNIQUENAME.html("<input id='dialogFilter_UNIQUENAME' style='width: 100%;'><div id='dialogContent_UNIQUENAME' style='height: 300px; overflow-y: scroll;'></div>");
    $("#dialogFilter_UNIQUENAME").change(function() {
        updateDialogContent_UNIQUENAME();
    });
    $("#dialogFilter_UNIQUENAME").keyup(function() {
        updateDialogContent_UNIQUENAME();
    });
});

function updateDialogContent_UNIQUENAME() {
    $("#dialogContent_UNIQUENAME").html("");
    $.getJSON(
        "jsonUrl",
        {"component": "ActorsMultiSelect", qualifier: "VARIABLE", hint: $("#dialogFilter_UNIQUENAME").val()},
        function(data) {
            $.each(data, function(i, item) {
                $("#dialogContent_UNIQUENAME").append("<div><a href='javascript:addActor_UNIQUENAME(\""+item.id+"\", \""+escapeQuotesForHtmlContext(item.name)+"\");'>"+item.name+"</a></div>");
            });
        }
    );
    $.editDialog_UNIQUENAME.dialog('open');
}

function addActor_UNIQUENAME(id, name) {
    numCounter_UNIQUENAME++;
    var divId = "div_UNIQUENAME" + numCounter_UNIQUENAME;
    var e = "<div id='" + divId + "'>";
    e += "<input type='hidden' name='VARIABLE' value='"+id+"' /> " + name;
    e += " <a href='javascript:{}' onclick='$(\"#"+divId+"\").remove();'>[ X ]</a>";
    e += "</div>";
    $('#actorsMultiSelectCnt_UNIQUENAME').append(e);
    $.editDialog_UNIQUENAME.dialog("close");
}