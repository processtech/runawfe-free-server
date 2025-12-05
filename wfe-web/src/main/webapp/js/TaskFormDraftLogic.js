/** formId должен совпадать с {@link ru.runa.wf.web.tag.WFFormTag.FORM_NAME}  от туда он и заполняется */
function initTaskFormDraftLogic(formId) {
    $(document).ready(function () {
            const formIdSelector = '#' + formId;
            const target = GLOBAL_APP_CONTEXT + "ajaxcmd?command=ajaxTaskFormDraft&taskId=" + $(formIdSelector + " input[name='id']").val();

            const saveData = function () {
                const data = $(formIdSelector).serializeArray();

                $.ajax({
                    url: target,
                    type: 'POST',
                    data: $.param(data),
                    success: function (response) {
                    },
                    error: function (xhr, status, error) {
                        console.error("Error save form draft:", error);
                    }
                });
            }

            // сохраняем по таймеру (на случай если из розетки комп выдернут))
            const saveDataEvery5Sec = function () {
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
}

