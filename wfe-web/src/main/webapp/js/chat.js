	var addReplyButtonText = "Ответить";

	//определение языка браузера
	var languageText = (window.navigator.language ||
		window.navigator.systemLanguage ||
		window.navigator.userLanguage);
	languageText = languageText.substr(0, 2).toLowerCase();

	//зона для дропа файлов
	var dropZone = $("#dropZ");
	//прикрепленнные файлы
	var attachedFiles = [];
	//размер входного файла (20 мб)
	var fileInp = 1024 * 1024 * 20;
	var attachedFilesBase64 = {};
	//зона прикрепленных файлов
	var filesTable = $("<table/>");
	filesTable.attr("id", "filesTable");
	$("#attachedArea").append(filesTable);
	//зона прикрепленных сообщений
	var messReplyTable = $("<table/>");
	messReplyTable.attr("id", "messReplyTable");
	$("#attachedArea").append(messReplyTable);
	//заставка загрузки файлов
	var progressBar = $("<div/>");
	progressBar.attr("id", "progressBar");
	progressBar.text("0/0");
	progressBar.hide();
	$("#modalFooter").append(progressBar);
	//-----
	//message
	let messageBody = $("<table/>").addClass("selectionTextQuote");
	//date
	let dateTr0 = $("<div/>");
	dateTr0.addClass("datetr").append(/*data.message.dateTime*/);
	messageBody.append($("<tr/>").append($("<td/>").append(dateTr0).append($("<div/>").addClass("author").text(/*data.message.author*/ "" + ":")).append($("<div/>").addClass("messageText").attr("textMessagId", /*data.message.id*/0).attr("id", "messageText" +/*mesIndex*/0).append(/*text0*/""))));

	function fileToBase64(file) {
		return new Promise(function (resolve, reject) {
			var reader = new FileReader();
			var buffer = new ArrayBuffer();
			reader.onload = function (e) {
				buffer = e.target.result;
				attachedFilesBase64[file.name] = btoa(buffer);
				resolve();
			}
			reader.onerror = function (error) {
				reject(error);
			}
			reader.readAsBinaryString(file)
		});
	}
	// -----------приём файлов
	//проверка браузера
	/*if (typeof (window.FileReader) != "undefined") {
		//поддерживает
		$("html").bind("dragover", function () {
			dropZone.show();
			dropZone.addClass("dropZActive");
			return false;
		});

		$("html").bind("dragleave", function (event) {
			if (event.relatedTarget == null) {
				dropZone.removeClass("dropZActive");
			}
			return false;
		});

		dropZone[0].ondragover = function () {
			dropZone.addClass("dropZActiveFocus");
			return false;
		};

		dropZone[0].ondragleave = function () {
			dropZone.removeClass("dropZActiveFocus");
			return false;
		};

		dropZone[0].ondrop = function (event) {
			event.preventDefault();
			let files = event.dataTransfer.files;
			for (let i = 0; i < files.length; i++) {
				if ("size" in files[i]) {
					var fileSize = files[i].size;

				}
				else {
					var fileSize = files[i].fileSize;
				}
				if (fileSize < fileInp) {
					attachedFiles.push(files[i]);
					//создаем отметку о прикреплении
					let newFile = $("<tr/>");
					newFile.append($("<td/>").text(attachedFiles[attachedFiles.length - 1].name));
					let deleteFileButton = $("<button/>");
					deleteFileButton.text("X");
					deleteFileButton.addClass("btnFileChat");
					deleteFileButton.attr("fileNumber", attachedFiles.length - 1);
					deleteFileButton.attr("type", "button");
					deleteFileButton.click(deleteAttachedFile);
					newFile.append($("<td/>").append(deleteFileButton));
					$("#filesTable").append(newFile);
				}
				else {
					alert(errorMessFilePart1 + (fileSize - fileInp) + errorMessFilePart2 + fileInp / (1073741824) + " Gb / " + fileInp + "bite");
				}
			}
			dropZone.hide();
			dropZone.removeClass("dropZActive");
			dropZone.removeClass("dropZActiveFocus");
		};
	}
	//альтернатива - fileInput
	$("#fileInput").change(function () {
		let files = $(this)[0].files;
		for (let i = 0; i < files.length; i++) {
			attachedFiles.push(files[i]);
			//создаем отметку о прикреплении
			let newFile = $("<tr/>");
			newFile.append($("<td/>").text(attachedFiles[attachedFiles.length - 1].name));
			let deleteFileButton = $("<button/>");
			deleteFileButton.text("X");
			deleteFileButton.addClass("btnFileChat");
			deleteFileButton.attr("fileNumber", attachedFiles.length - 1);
			deleteFileButton.attr("type", "button");
			deleteFileButton.click(deleteAttachedFile);
			newFile.append($("<td/>").append(deleteFileButton));
			$("#filesTable").append(newFile);
			this.val = {};
		}
	});*/
	//удаление прикрепленного к сообщению файла (для таблички прикрепленных файлов)
	function deleteAttachedFile() {
		attachedFiles.splice($(this).attr("fileNumber"));
		$(this).closest("tr").remove();
		return false;
	}