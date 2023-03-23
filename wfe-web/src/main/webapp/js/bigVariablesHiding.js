$(document).ready(function () {
    function open() {
        $(this).text(labelCollapse);
        $(this).attr("opened", "true");
        if ($(this).attr("loaded") === 'false') {
            var urlString = "/wfe/ajaxcmd?command=getProcessVariableValue&processId=" + processId +
                "&index=" + $(this).attr('index') + "&date=" + $(this).attr('date') +
                "&variableName=" + $(this).attr('variableName');
            $.ajax({
                type: "POST",
                url: urlString,
                dataType: "json",
                contentType: "application/json; charset=UTF-8",
                processData: false,
                success: function (data) {
                    $('#content' + data.index).append(data.text);
                    $('#content' + data.index).show();
                }
            });
            $(this).attr("loaded", "true");
        } else {
            $('#content' + $(this).attr('index')).show();
        }
    }

    function close() {
        $(this).text(labelExpand);
        $(this).attr("opened", "false");
        $('#content' + $(this).attr('index')).hide();
    }

    var variableContent = $('.hiddenVariableContent');
    if (variableContent.length > 0) {
        $('.expandAllButton').click(function () {
            if ($(this).attr("opened") === 'false') {
                $(this).text(labelCollapseAll);
                $(this).attr("opened", "true");
                for (var i = 0; i < variableContent.length; i++) {
                    if (this !== variableContent[i] && $(variableContent[i]).attr("opened") === 'false') {
                        open.call(variableContent[i]);
                    }
                }
            } else {
                $(this).text(labelExpandAll);
                $(this).attr("opened", "false");
                for (var i = 0; i < variableContent.length; i++) {
                    if (this !== variableContent[i] && $(variableContent[i]).attr("opened") === 'true') {
                        close.call(variableContent[i]);
                    }
                }
            }
        });
        for (var i = 0; i < variableContent.length; i++) {
            $(variableContent[i]).click(function () {
                if ($(this).attr("opened") === 'false') {
                    open.call(this);
                } else {
                    close.call(this);
                }
            });
        }
    }
});
