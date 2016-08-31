var UNIQUENAME = new Object();

$(document).ready(function() {
    $('#buttonAddUNIQUENAME').click(function() {
        dialogUNIQUENAME();
    });
    $.UNIQUENAME = $('<div></div>').dialog({
        title: 'DIALOG_TITLE',
        width: 300, modal: true, autoOpen: false, 
        overlay: {backgroundColor: '#000', opacity: 0.5}
    });
    var dialogContent = "<select id='groupFilterUNIQUENAME' style='width: 100%; margin-bottom:4px;'>GROUP_OPTIONS</select>";
    dialogContent += "<input type='button' id='buttonAllUNIQUENAME' onclick='addGroupUNIQUENAME();' value='SELECT_ALL_LABEL' style='margin-bottom:4px;'/>";
    dialogContent += "<input id='nameFilterUNIQUENAME' style='width: 100%;  margin-bottom:4px;'><div id='dialogContentUNIQUENAME' style='height: 300px; overflow-y: scroll; margin-bottom:4px;'></div>";
    $.UNIQUENAME.html(dialogContent);
    $("#nameFilterUNIQUENAME").keyup(function() {
        dialogUNIQUENAME();
    });
    $("#groupFilterUNIQUENAME").change(function() {
        dialogUNIQUENAME();
    });
});

function dialogUNIQUENAME() {
    $.getJSON(
        "JSON_URL",
        {tag: "SelectEmployeesFromGroups", qualifier: "UNIQUENAME", group: $("#groupFilterUNIQUENAME").val(), hint: $("#nameFilterUNIQUENAME").val(), t: new Date().getTime()},
        function(data) {
            $("#dialogContentUNIQUENAME").html("");
            $.each(data, function(i, item) {
                var divTitle = "";
                var divClass = "";
                var addEnabled = false;
                if (item.exclusion.length != 0) {
                    divClass = "actorSelected";
                    divTitle = item.exclusion;
                } else if (UNIQUENAME[item.code] != undefined) {
                    divClass = "actorSelected";
                    divTitle = "ACTOR_SELECTED_INFO";
                } else {
                    addEnabled = true;
                    divTitle = item.title;
                }
                var actorDiv = "<div id='divUNIQUENAME" + item.code + "' class='" + divClass + "' title='" + divTitle + "'>";
                if (addEnabled) {
                    actorDiv += "<a code='" + item.code + "' href='javascript:addUNIQUENAME(\""+item.code+"\", \""+item.name+"\");'>"+item.name+"</a>";
                } else {
                    actorDiv += item.name;
                }
                actorDiv += "</div>";
                $("#dialogContentUNIQUENAME").append(actorDiv);
            });
        }
    );
    $.UNIQUENAME.dialog('open');
}

function addGroupUNIQUENAME() {
    $("#dialogContentUNIQUENAME a").each(function() {
        addUNIQUENAME($(this).attr("code"), $(this).html());
    });
}

function addUNIQUENAME(code, name) {
    var rowIndex = getSizeUNIQUENAME();
    console.log("Adding actor " + rowIndex + " (" + code + ")");
    var e = "<div row='" + rowIndex + "'  style='margin-bottom:4px;'>";
    e += "<input type='hidden' name='VARIABLENAME["+rowIndex+"]' value='"+code+"' /> ";
    e += "<input value='"+name+"' readonly='true' /> " ;
    e += " <input type='button'  onclick='removeUNIQUENAME(this);' style='width: 30px;' value=' - '/>";
    e += "</div>";
    $('#buttonAddUNIQUENAME').before(e);
    UNIQUENAME[code] = name;
    var actorDiv = $("#divUNIQUENAME" + code);
    actorDiv.html(name);
    actorDiv.attr("title", "ACTOR_SELECTED_INFO");
    actorDiv.addClass("actorSelected");
    //$.UNIQUENAME.dialog("close");
    updateSizeUNIQUENAME(1);
    $("#UNIQUENAME").trigger("onActorAdded", [rowIndex]);
}

function removeUNIQUENAME(button) {
    var div = $(button).closest("div");
    var rowIndex = parseInt(div.attr("row"));
    var size = getSizeUNIQUENAME();
    var code = div.find("input").val();
    console.log("Removing actor " + rowIndex + " (" + code + ")");
    UNIQUENAME[code] = undefined;
    div.remove();
    for (var i = rowIndex; i < size - 1; i++) {
        updateIndexesUNIQUENAME(i + 1, i);
    }
    updateSizeUNIQUENAME(-1);
    $("#UNIQUENAME").trigger("onActorRemoved", [rowIndex]);
}

function updateIndexesUNIQUENAME(oldIndex, newIndex) {
    $("div[row='"+oldIndex+"'] input").each(function() {
        updateIndexedNameUNIQUENAME($(this), oldIndex, newIndex);
    });
    $("div[row='"+oldIndex+"']").attr("row", newIndex);
}

function updateIndexedNameUNIQUENAME(element, oldIndex, newIndex) {
    var name = element.attr("name");
    if (name == null) {
        console.log("name is null in ", element);
        return;
    }
    name = name.replace("[" + oldIndex + "]", "[" + newIndex + "]");
    element.attr("name", name);
}
function getSizeUNIQUENAME() {
    return parseInt($("input[name='VARIABLENAME.size']").val());
}
function updateSizeUNIQUENAME(delta) {
    var sizeInput = $("input[name='VARIABLENAME.size']");
    sizeInput.val(parseInt(sizeInput.val()) + delta);
    console.log("List size = " + getSizeUNIQUENAME());
}
