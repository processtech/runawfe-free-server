$(document).ready(function () {
        var formIdSelector = '#processForm';
        var target = GLOBAL_APP_CONTEXT + "ajaxcmd?command=postTaskFormDraft&taskId=" + $(formIdSelector + " input[name='id']").val();


        if (0 == $(formIdSelector + ' input:not([type="hidden"]):not([type="SUBMIT"])').length) {
            // страница без полей ввода
            return;
        }

        var saveData = function () {
            var data = $(formIdSelector).serializeArray();
            var filtered = data.filter(function (item) {
                return item.name !== 'org.apache.struts.taglib.html.TOKEN'
                    && item.name !== '/submitTaskDispatcher';
            });

            $.ajax({
                url: target,
                global: false,
                type: 'POST',
                data: $.param(filtered),
                success: function (response) {
                },
                error: function (xhr, status, error) {
                    console.error("Error save form draft:", error);
                }
            });
        }

        // сохраняем по таймеру (на случай если из розетки комп выдернут))
        var saveDataEvery5Sec = function () {
            saveData();

            setTimeout(saveDataEvery5Sec, 5000);
        }

        saveDataEvery5Sec();


        // сохраняем при выходе
        $(window).on('beforeunload', function (e) {
            saveData();
        });
    }
);

