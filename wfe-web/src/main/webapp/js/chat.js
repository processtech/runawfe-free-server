	var chatSocket = null;

	//переменные для вставки текста
	var textPrivateMessage = "Приватное сообщение:";
	var textEnterMessage = "Введите текст сообщения";
	var textDragFile = "Перетащите сюда файл";
	var textBtnSend = "Отправить";
	var addReplyButtonText = "Ответить";
	var errorMessFilePart1 = "Ошибка. Размер файла превышен на ";
	var errorMessFilePart2 = " байт, максимальный размер файла = ";

	//определение языка браузера
	var languageText = (window.navigator.language ||
		window.navigator.systemLanguage ||
		window.navigator.userLanguage);
	languageText = languageText.substr(0, 2).toLowerCase();

	//шаблон модального окна чата
	var modalHeaderChat = '<table class="box"><tbody><tr><th class="box"><button id="btnOp" type="button"><img id="imgButton" alt="resize" src="/wfe/images/chat_roll_up.png"></button><div id="modal-header-dragg" class="modal-header-dragg"></div><span id="close" class="ui-icon ui-icon-closethick ui-state-highlight" style="cursor: pointer; float: right; margin: 1px;"></span></th></tr></tbody></table>';
	var modalFooterChat = '<div class="checkBoxContainer">' + textPrivateMessage + '<input type="checkbox" id="checkBoxPrivateMessage"></div><div class="warningText"></div><ul class="messageUserMention"></ul><textarea placeholder="' + textEnterMessage + '" id="message" name="message"></textarea><div style="display:flex;padding-top: 5px; padding-left: 5px;"><button id="btnSend" type="button">' + textBtnSend + '</button><input size="0" id="fileInput" multiple="true" type="file"></div><div id="dropZ" class="dropZ" style="display: none;">' + textDragFile + '</div><div id="attachedArea"></div>';

	$("#ChatForm").append('<div class="modal-content"/>');
	$(".modal-content").html(modalHeaderChat);
	$(".modal-content").append('<div id="modal-body" class="modal-body"/>');
	$(".modal-content").append('<div id="modalFooter" class="modal-footer"/>');
	$(".modal-footer").append(modalFooterChat);

	var rowSMCount = $('.tab tr').size();
	if (rowSMCount > 9) {
		$(".modal-body").attr("admin", "true");
	}


	//прикрепленные сообщения
	var attachedPosts = [];
	//переменные редактирования сообщения
	var editMessageFlag = false;
	var editMessageId = -1;
	//зона для дропа файлов
	var dropZone = $("#dropZ");
	//прикрепленнные файлы
	var attachedFiles = [];
	//размер входного файла
	var fileInp = 1024 * 1024 * 20;//20 мб
	//constans 
	var newMessageType = "newMessage";
	var editMessageType = "editMessage";
	var deleteMessageType = "deleteMessage";
	var readMessageType = "readMessage";

	var attachedFilesBase64 = {};

	//стартовые объекты
	//таблица имен быстрой вставки
	var userNameTable = $("<table/>");
	userNameTable.addClass("tableModalNameSetMessage");
	userNameTable.attr("id", "userNameTable");
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

	function checkEmptyMessage() {
		return (message.value == "") && (attachedPosts.length == 0) && (attachedFiles.length == 0);
	}
	function sendMessageHandler() {
		if (checkEmptyMessage() == false) {
			let message = document.getElementById("message").value;
			//ищем ссылки
			message = message.replace(/(^|[^\/\"\'\>\w])(http\:\/\/)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='$2$3$4'>$2$3$4</a>");
			message = message.replace(/(^|[^\/\"\'\>\w])(https\:\/\/)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='$2$3$4'>$2$3$4</a>");
			message = message.replace(/(^|[^\/\"\'\>\w])(www\.)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='http://$2$3$4'>$2$3$4</a>");
			message = message.replace(/\r?\n/g, "<br />");
			let newMessage = {};
			newMessage.message = message;
			newMessage.processId = $("#ChatForm").attr("processId");
			if (editMessageFlag == false) {
				newMessage.messageType = newMessageType;
				newMessage.isPrivate = $("#isPrivate").prop("checked");
				let idHierarchyMessage = "";
				for (let i = 0; i < attachedPosts.length; i++) {
					idHierarchyMessage += attachedPosts[i] + ":";
				}
				newMessage.idHierarchyMessage = idHierarchyMessage;
				if (attachedFiles.length > 0) {
					addFilesToMessage(attachedFiles, newMessage)
				} else {
					console.info(newMessage);
					sendToChatNewMessage(newMessage);
				}
			} else {
				newMessage.messageType = editMessageType;
				newMessage.editMessageId = editMessageId;
				sendBinaryMessage(chatSocket, newMessage);
				$("#message").val("");
			}
		}
		return 0;
	}
	function deleteMessageHandler(id) {
		if (confirm("Are you sure?")) {
			let newMessage = {};
			newMessage.messageId = id;
			newMessage.processId = $("#ChatForm").attr("processId");
			newMessage.messageType = deleteMessageType;
			sendBinaryMessage(chatSocket, newMessage);
		}
	}
	// отправка нового сообщения
	function sendToChatNewMessage(message) {
		sendBinaryMessage(chatSocket, message)
		$("#message").val("");
		// чистим "ответы"
		let addReplys0 = document.getElementsByClassName("addReply");
		for (let i = 0; i < addReplys0.length; i++) {
			$(addReplys0[i]).text(addReplyButtonText);
			$(addReplys0[i]).attr("flagAttach", "false");
		}
		attachedPosts = [];
		$("#checkBoxPrivateMessage").prop("checked", false);
		$("#messReplyTable").empty();
		$(".warningText").text("0/1024");
		$("#fileInput").val("");
		$("#tablePrivate table").empty();
		$("#privateBlock").css("display", "none");
	}

	function addFilesToMessage(files, message) {
		var fileToBase64Promises = [];
		for (var i = 0; i < files.length; i++) {
			fileToBase64Promises.push(fileToBase64(files[i]));
		}
		Promise.all(fileToBase64Promises).then(function () {
			message.files = attachedFilesBase64;
			sendToChatNewMessage(message);
			attachedFilesBase64 = {};
			attachedFiles = [];
			$("#progressBar").css({ "display": "none" });
			$("#filesTable").empty();
		}).catch(function (error) {
			alert(error.message);
		});
	}

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

	function initChatSocket(socket) {
		chatSocket = socket;
	}