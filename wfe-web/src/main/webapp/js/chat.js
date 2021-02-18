//--------------------------------------------
//инициализация чата
function ajaxInitializationChat() {
	var chatSocket = null;

	//переменные для вставки текста
	var textLoadOldMessage = "Загрузить сообщения выше";
	var textPrivateMessage = "Приватное сообщение:";
	var textEnterMessage = "Введите текст сообщения";
	var textDragFile = "Перетащите сюда файл";
	var textBtnSend = "Отправить";
	var textHeader = "Чат процесса ";
	var editMessageButtonText = "редактировать";
	var addReplyButtonText = "Ответить";
	var removeReplyButtonText = "Отменить";
	var warningEditMessage = "Вы действительно хотите отредактировать сообщение? Отменить это действие будет невозможно";
	var warningRemoveMessage = "Вы действительно хотите удалить сообщение? Отменить это действие будет невозможно";
	var attachedMessageSignature = "Прикрепленное сообщение";
	var openHierarchySignature = "Развернуть вложенные сообщения";
	var closeHierarchySignature = "Свернуть";
	var quoteText = "Цитата";
	var errorMessFilePart1 = "Ошибка. Размер файла превышен на ";
	var errorMessFilePart2 = " байт, максимальный размер файла = ";

	//определение языка браузера
	var languageText = (window.navigator.language ||
		window.navigator.systemLanguage ||
		window.navigator.userLanguage);
	languageText = languageText.substr(0, 2).toLowerCase();

	//------------------функция локализации

	ajaxLocale();

	//шаблон модального окна чата
	var modalHeaderChat = '<table class="box"><tbody><tr><th class="box"><button id="btnOp" type="button"><img id="imgButton" alt="resize" src="/wfe/images/chat_roll_up.png"></button><div id="modal-header-dragg" class="modal-header-dragg"></div><span id="close" class="ui-icon ui-icon-closethick ui-state-highlight" style="cursor: pointer; float: right; margin: 1px;"></span></th></tr></tbody></table>';
	var modalFooterChat = '<div class="checkBoxContainer">' + textPrivateMessage + '<input type="checkbox" id="checkBoxPrivateMessage"></div><div class="warningText"></div><ul class="messageUserMention"></ul><textarea placeholder="' + textEnterMessage + '" id="message" name="message"></textarea><div style="display:flex;padding-top: 5px; padding-left: 5px;"><button id="btnSend" type="button">' + textBtnSend + '</button><input size="0" id="fileInput" multiple="true" type="file"></div><div id="dropZ" class="dropZ" style="display: none;">' + textDragFile + '</div><div id="attachedArea"></div>';

	$("#ChatForm").append('<div class="modal-content"/>');
	$(".modal-content").html(modalHeaderChat);
	$(".modal-content").append('<div id="modal-body" class="modal-body"/>');
	$(".modal-body").html('<button id="loadNewBessageButton" type="button">' + textLoadOldMessage + '</button>');

	$(".modal-content").append('<div id="modalFooter" class="modal-footer"/>');
	$(".modal-footer").append(modalFooterChat);

	$(".modal-content").resizable({
		handles: "s, e, w, se",
		minWidth: 267,
		minHeight: 587,
		alsoResize: "#attachedArea"
	});

	var rowSMCount = $('.tab tr').size();
	if (rowSMCount > 9) {
		$(".modal-body").attr("admin", "true");
	}

	//переменные
	//высота "непрочитанного" сообщения по скроллу
	var newMessagesHeight = 0;
	//прикрепленные сообщения
	var attachedPosts = [];
	//переменные редактирования сообщения
	var editMessageFlag = false;
	var editMessageId = -1;
	//переменные отслеживания @user
	var userNamePosition = -1;
	var userNamePositionFlag = false;
	var userLoadFlag = false;
	var userList = [];
	var userFullNameList = [];
	var userNameLength = 0;
	//зона для дропа файлов
	var dropZone = $("#dropZ");
	//прикрепленнные файлы
	var attachedFiles = [];
	//размер входного файла
	var fileInp = 1024 * 1024 * 20;//20 мб
	//количество символов после которых встлывает предупреждение
	var characterSize = 1024;
	//флаг развернутого чата (0 - свернут, 1 - развернут)
	var switchCheak = 0;
	//флаг обозначающий состояние(развернут или свернут) чат
	var flagRollExpandChat = 0;
	//флаг для первичной инициализации всех чатов во время откытия окна переключения чатов
	var flagPrimaryInitialization = 0;
	//нумерация сообщений = количество загруженных сообщений, если небыло удалений
	var newMessageIndex = 0;
	var oldMessagesIndex = -1;//только для старых, идет в отрицательные
	var minMassageId = -1;
	var maxMassageId = -1;
	//эту проверить! ToDo изменить использование???
	var currentMessageId = -1;
	var numberNewMessages = 0;
	//шаг - по сколько сообщений подгружается
	var messagesStep = 20;
	//флаг выбран ли какой нибудь блок из списка предложенных при вводе @
	var listUserNameFastInput = 1;
	var userNameTableLength = 0;
	//constans 
	var newMessageType = "newMessage";
	var editMessageType = "editMessage";
	var deleteMessageType = "deleteMessage";
	var getMessagesType = "getMessages";
	var readMessageType = "readMessage";

	var attachedFilesBase64 = {};

	//id task
	var idProcess = $("#ChatForm").attr("processId");

	//-------------------------------------------
	//переменные кнопок
	var btnOpenChat = document.getElementById("openChatButton");
	var btnLoadOldMessages = document.getElementById("loadNewBessageButton");
	var btnOp = document.getElementById("btnOp");
	var imgButton = document.getElementById("imgButton");
	//--------------------------------------------
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
	//"развернуть"
	let openHierarchyA0 = $("<a/>");
	openHierarchyA0.addClass("openHierarchy")
		.attr("loadFlag", 0)
		.attr("openFlag", 0)
		.attr("messageId", 0)
		.text(openHierarchySignature);
	//"ответить"
	let addReplyA0 = $("<a/>");
	addReplyA0.text(addReplyButtonText);
	addReplyA0.addClass("addReply");
	addReplyA0.attr("flagAttach", "false");
	addReplyA0.click(messReplyClickFunction);
	messageBody.append($("<tr/>").append($("<td/>").append(openHierarchyA0).append($("<div/>").addClass("loadedHierarchy"))));
	messageBody.append($("<tr/>").append($("<td/>").append($("<div/>").append(addReplyA0))));


	//---------------------------------------------
	//функции кнопок сообщения
	//----------вложенные сообщения
	//ajax запрос иерархии сообщений, вернет Promise ajax запроса
	function hierarhyCheak(messageId) {
		let urlString = "/wfe/ajaxcmd?command=GetHierarhyLevel&processId=" + idProcess + "&messageId=" + messageId;
		return $.ajax({
			type: "POST",
			url: urlString,
			dataType: "json",
			contentType: "application/json; charset=UTF-8",
			processData: false,
			success: function (data) { }
		});
	}
	//функция для разворачивания вложенных сообщений
	function hierarchyOpen() {
		if ($(this).attr("openFlag") == 1) {
			$(this).next(".loadedHierarchy")[0].style.display = "none";
			$(this).attr("openFlag", "0");
			$(this).text(openHierarchySignature);
			return 0;
		}
		else {
			let thisElem = $(".openHierarchy")[0];
			if ($(this).attr("loadFlag") == 1) {
				$(this).next(".loadedHierarchy")[0].style.display = "block";
				$(this).attr("openFlag", "1");
				$(this).text(closeHierarchySignature);
				return 0;
			} else {
				let element = this;
				hierarhyCheak($(element).attr("messageId")).then(function (ajaxRet) {
					messagesRetMass = getAttachedMessagesArray(ajaxRet);
					for (let i = 0; i < messagesRetMass.length; i++) {
						$(this).next(".loadedHierarchy").append(messagesRetMass[i]);
					}
					$(element).attr("loadFlag", "1");
					$(this).attr("openFlag", "1");
					$(this).text(closeHierarchySignature);
					return 0;
				});
			}
		}
	}
	// функция возвращающая массив блоков вложенных сообщений
	function getAttachedMessagesArray(data) {
		let outputArray = [];
		for (let mes = 0; mes < data.messages.length; mes++) {
			if (data.messages[mes].text != null) {
				let messageBody = $("<table/>").addClass("quote");
				messageBody.append($("<tr/>").addClass("selectionTextAdditional").append($("<td/>").text(quoteText + ":" + data.messages[mes].author)));
				messageBody.append($("<tr/>").append($("<td/>").text(data.messages[mes].text)));
				if (data.messages[mes].hierarchyMessage == 1) {
					let openHierarchy0 = $("<a/>").addClass("openHierarchy");
					openHierarchy0.attr("type", "button");
					openHierarchy0.attr("messageId", data.messages[mes].id);
					openHierarchy0.attr("loadFlag", 0);
					openHierarchy0.attr("openFlag", 0);
					openHierarchy0.text(openHierarchySignature);
					openHierarchy0.click(hierarchyOpen);
					messageBody.append($("<tr/>").append($("<td/>").append(openHierarchy0).append($("<div/>").addClass("loadedHierarchy"))));
				}
				outputArray.push(messageBody);
			}
		}
		return outputArray;
	}
	//-----------------прикрепление цитат
	//функция для кнопки "ответить" (прикрепляет сообщение)
	function messReplyClickFunction() {
		if ($(this).attr("flagAttach") == "false") {
			attachedPosts.push($(this).closest(".selectionTextQuote").attr("messageId"));
			$(this).attr("flagAttach", "true");
			$(this).text(removeReplyButtonText);
			//создаем отметку о прикреплении
			let newMessReply = $("<tr/>");
			newMessReply.append($("<td/>").text(attachedMessageSignature + ":" + $("#messageText" + $(this).closest(".selectionTextQuote").attr("messageIndex")).text()));
			let deleteMessReplyButton = $("<button/>");
			deleteMessReplyButton.text("X");
			deleteMessReplyButton.attr("id", "deleteMessReply" + $(this).closest(".selectionTextQuote").attr("messageIndex"));
			deleteMessReplyButton.attr("mesIndex", $(this).closest(".selectionTextQuote").attr("messageIndex"));
			deleteMessReplyButton.attr("type", "button");
			deleteMessReplyButton.click(deleteAttachedMessage);
			newMessReply.append($("<td/>").append(deleteMessReplyButton));
			$("#messReplyTable").append(newMessReply);
		}
	}
	//функция открепления сообщений
	function deleteAttachedMessage() {
		let pos0 = attachedPosts.indexOf($("#messBody" + $(this).attr("mesindex")).attr("messageId"), 0);
		attachedPosts.splice(pos0, 1);
		$("#messBody" + $(this).attr("mesindex")).find(".addReply").text(addReplyButtonText);
		$("#messBody" + $(this).attr("mesindex")).find(".addReply").attr("flagAttach", "false");
		$(this).closest("tr").remove();
	}
	//----остальные (малые)
	//удаление сообщений
	function deleteMessage() {
		if (confirm(warningRemoveMessage)) {
			let newMessage = {};
			newMessage.messageId = $(this).closest(".selectionTextQuote").attr("messageId");
			newMessage.processId = idProcess;
			newMessage.messageType = deleteMessageType;
			sendBinaryMessage(chatSocket, newMessage);
		}
	}
	//редактирование сообщений
	function editMessage() {
		editMessageId = $(this).closest(".selectionTextQuote").attr("messageId");
		editMessageFlag = true;
		$("#message").val($("#messageText" + $(this).closest(".selectionTextQuote").attr("messageindex")).text());
	}

	//--------------------------------------------------------
	//функции остальных кнопок чата (не в сообщении)
	//фунцкия отправляет запрос на выдачу count старых сообщений
	function newxtMessages(count) {
		let newMessage = {};
		newMessage.processId = idProcess;
		newMessage.messageType = getMessagesType;
		newMessage.lastMessageId = minMassageId;
		newMessage.count = count; // количество сообщений
		sendBinaryMessage(chatSocket, newMessage);
	}
	//подгрузка старых сообщений
	function loadOldMessages() {
		newxtMessages(messagesStep);
	}
	//кнопка открытия чата
	function openChat() {
		if ($(".modal-header-dragg").length) {
			$(".modal-header-dragg").text(textHeader + idProcess);
		}
		else {
			$("#head-name-chat").text(textHeader + idProcess);
		}
		$(".warningText").text("0/1024");
		if (document.getElementById("ChatForm") != null) {
			document.getElementById("ChatForm").style.display = "block";

			switchCheak = 1;
		}
		//установка границы скролла непрочитанных
		if (numberNewMessages == 0) {
			newMessagesHeight = $("#modal-body")[0].scrollHeight - $("#modal-body").height();
			$("#modal-body").scrollTop($("#modal-body")[0].scrollHeight);
		}
		else {
			newMessagesHeight = $("#messBody" + (newMessageIndex - numberNewMessages))[0].offsetTop - $("#modal-body").height();
			$("#modal-body").scrollTop(newMessagesHeight);
		}
	}
	//закрытие (сворачивание) чата
	function closeChat() {
		document.getElementById("ChatForm").style.display = "none";
		switchCheak = 0;
	}

	function checkEmptyMessage() {
		if ((message.value == "") && (attachedPosts.length == 0) && (attachedFiles.length == 0)) {
			return true;
		}
		else {
			return false;
		}
	}
	//кнопка "отправить"
	function sendMessageHandler() {
		deleteUserNameTable();
		if (checkEmptyMessage() == false) {
			if (editMessageFlag == false) {
				let message = document.getElementById("message").value;
				//ищем ссылки
				message = message.replace(/(^|[^\/\"\'\>\w])(http\:\/\/)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='$2$3$4'>$2$3$4</a>");
				message = message.replace(/(^|[^\/\"\'\>\w])(https\:\/\/)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='$2$3$4'>$2$3$4</a>");
				message = message.replace(/(^|[^\/\"\'\>\w])(www\.)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='http://$2$3$4'>$2$3$4</a>");
				message = message.replace(/\r?\n/g, "<br />");
				let idHierarchyMessage = "";
				for (var i = 0; i < attachedPosts.length; i++) {
					idHierarchyMessage += attachedPosts[i] + ":";
				}
				// сокет
				let newMessage = {};
				newMessage.message = message;
				newMessage.processId = idProcess;
				newMessage.idHierarchyMessage = idHierarchyMessage;
				newMessage.messageType = newMessageType;
				newMessage.isPrivate = $("#checkBoxPrivateMessage").prop("checked");
				if (attachedFiles.length > 0) {
					addFilesToMessage(attachedFiles, newMessage)
				}
				else {
					sendToChatNewMessage(newMessage);
				}
			}
			else {//редактирование сообщения
				if (confirm(warningEditMessage)) {
					let message = document.getElementById("message").value;
					//ищем ссылки
					message = message.replace(/(^|[^\/\"\'\>\w])(http\:\/\/)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='$2$3$4'>$2$3$4</a>");
					message = message.replace(/(^|[^\/\"\'\>\w])(https\:\/\/)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='$2$3$4'>$2$3$4</a>");
					message = message.replace(/(^|[^\/\"\'\>\w])(www\.)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='http://$2$3$4'>$2$3$4</a>");
					message = message.replace(/\r?\n/g, "<br />");
					let newMessage = {};
					newMessage.message = message;
					newMessage.processId = idProcess;
					newMessage.messageType = editMessageType;
					newMessage.editMessageId = editMessageId;
					sendBinaryMessage(chatSocket, newMessage);
					$("#message").val("");
				}
				else {
					$("#message").val("");
					editMessageId = -1;
					editMessageFlag = false;
				}
			}
		}
		return 0;
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
		$("#message").keyup(keyupUserNames);
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

	//кнопка увеличить/уменьшить чат
	function zoomInZoomOut() {
		if (flagRollExpandChat == 0) {
			flagRollExpandChat = 1;
			$(".modal-content").css({
				width: $(".modal-content").width() + 300,
			});

			$(".messageUserMention").css({
				"margin-top": (-1) * $("#message").height() + "px",
				height: 90 + "px",
				width: 212 + "px",
			})
			dropZone.css({
				height: $("#attachedArea").height(),
			});
			imgButton.src = "/wfe/images/chat_expand.png";
			if (numberNewMessages > 0) {
				newMessagesHeight = $("#messBody" + (newMessageIndex - numberNewMessages))[0].offsetTop - ($("#modal-body").height() + $("#messBody" + (newMessageIndex - numberNewMessages)).getSlicePx("padding"));
			}
			else {
				newMessagesHeight = $("#modal-body")[0].scrollHeight - ($("#modal-body").height());
			}
		} else if (flagRollExpandChat == 1) {
			flagRollExpandChat = 0;
			$(".modal-content").css({
				width: $(".modal-content").width() - 300,
			});
			$("#attachedArea").css({
				height: $("#attachedArea").height() - 50,
			});


			$(".messageUserMention").css({
				"margin-top": (-1) * $("#message").height() + 10 + "px",
				height: 50 + "px",
				width: 225 + "px",
			});
			imgButton.src = "/wfe/images/chat_roll_up.png";
			if (numberNewMessages > 0) {
				newMessagesHeight = $("#messBody" + (newMessageIndex - numberNewMessages))[0].offsetTop - ($("#modal-body").height() + $("#messBody" + (newMessageIndex - numberNewMessages)).getSlicePx("padding"));
			}
			else {

				newMessagesHeight = $("#modal-body")[0].scrollHeight - ($("#modal-body").height());
			}
		}
	}

	//---------------------------------------------
	//служебные функции
	//реальный размер элемента
	function getElmHeight(node) {
		return node.outerHeight(true);
	}
	//получение количество пикселей(без px)
	$.fn.getSlicePx = function (property) {
		return parseInt(this.css(property).slice(0, -2));
	};
	//----------------------------------------
	//"непрочитанные сообщения"
	//скрол-функция отслеживания непрочитанных
	function scrollNewMessages() {
		let modalBody = $("#modal-body");
		if (modalBody.scrollTop() > newMessagesHeight) {
			let scrollTop0 = modalBody.scrollTop() + modalBody.height();
			let newIndex = newMessageIndex - numberNewMessages;
			let i = newIndex;
			for (; i < newMessageIndex; i++) {
				let message0 = $("#messBody" + i);
				if (message0[0].offsetTop < scrollTop0) {
					newMessagesHeight += getElmHeight($("#messBody" + i));
					message0.addClass("InViewport");
					message0.removeClass("newMessageClass");
				}
				else {
					i--;
					message0 = $("#messBody" + i);
					currentMessageId = message0.attr("messageId");
					updatenumberNewMessages(newMessageIndex - 1 - message0.attr("messageIndex"));
					updateLastReadMessage();
					//
					return 0;
				}
			}
			i--;
			message0 = $("#messBody" + i);
			currentMessageId = message0.attr("messageId");
			updatenumberNewMessages(newMessageIndex - 1 - message0.attr("messageIndex"));
			updateLastReadMessage();
		}
	}
	//для изменения непрочитанных
	$("#modal-body").resize(function () {
		if (numberNewMessages > 0) {
			newMessagesHeight = $("#messBody" + (newMessageIndex - numberNewMessages))[0].offsetTop - $("#modal-body").height();
		}
		else {
			newMessagesHeight = $("#modal-body")[0].scrollHeight - $("#modal-body").height();
		}
	});
	//функция пишущая кол-во непрочитанных сообщений = numberNewMessages
	function updatenumberNewMessages(numberNewMessages0) {
		numberNewMessages = numberNewMessages0;
		document.getElementById("countNewMessages").innerHTML = "" + numberNewMessages + "";
		$("#numberNewMessages" + idProcess).text(numberNewMessages0);
		if (numberNewMessages > 0) {
			$(".countNewMessages").addClass("bgcdeadlineExpired");
		} else {
			$(".countNewMessages").removeClass("bgcdeadlineExpired");
		}
	}
	//функция отправляет по сокету id последнего прочитанного сообщния
	function updateLastReadMessage() {
		let newSend0 = {};
		newSend0.processId = idProcess;
		newSend0.messageType = readMessageType;
		newSend0.currentMessageId = currentMessageId + "";
		sendBinaryMessage(chatSocket, newSend0);
	}

	//--------------------------------------------------------------
	//вставка юзеров
	//ajax запрос иерархии сообщений, вернет Promise ajax запроса
	function getUsersNames() {
		let urlString = "/wfe/ajaxcmd?command=GetUsersNamesForChat&processId=" + idProcess;
		return $.ajax({
			type: "POST",
			url: urlString,
			dataType: "json",
			contentType: "application/json; charset=UTF-8",
			processData: false,
			success: function (data) { }
		});
	}

	function enterClickUserNames(event) {
		let userId = $(this).attr("userId");
		$("#message").val($("#message").val().slice(0, userNamePosition) + $("#message").val().slice(userNamePosition).replace(/^@\w*/gim, "@" + userList[userId] + " "));
		deleteUserNameTable();
	}
	//полная очистка таблицы вставки userNameTable и её переменных
	function deleteUserNameTable() {
		userNameTable.detach();
		userNameTable.empty();
		$(".messageUserMention").css({ "display": "none" });
		userNameLength = 0;
		userNamePositionFlag = false;
		$("#message").off();
		$("#message").keydown(firstKeyCheck);
	}
	//обновить таблицу userNameTable
	function updateUserNameTable(enterUserName) {
		userNameTable.empty();
		userNameTableLength = 0;
		for (let i = 0; i < userList.length; i++) {
			let partName = userList[i].slice(0, userNameLength);
			let partFullName = userFullNameList[i].slice(0, userNameLength);
			if (partName == enterUserName) {
				let userNameBlockList = $("<li/>");
				userNameBlockList.attr("id", "idListUserNameTr" + userNameTableLength);
				userNameBlockList.attr("userId", i);
				userNameBlockList.addClass("list");
				userNameBlockList.click(enterClickUserNames);
				userNameTable.append(userNameBlockList.text("@" + userFullNameList[i] + " ( " + userList[i] + " )"));
				userNameTableLength++;
			}
			else if (partFullName == enterUserName) {
				let userNameBlockList = $("<li/>");
				userNameBlockList.attr("id", "idListUserNameTr" + userNameTableLength);
				userNameBlockList.addClass("list");
				userNameBlockList.attr("userId", i);
				userNameBlockList.click(enterClickUserNames);
				userNameTable.append(userNameBlockList.text("@" + userFullNameList[i] + " ( " + userList[i] + " )"));
				userNameTableLength++;
			}
		}
		$("#idListUserNameTr" + 0).addClass("selected");
	}
	$("#message").keydown(firstKeyCheck);
	$("#message").keyup(keyupUserNames);
	function keyupUserNames(event) {
		$(".warningText").html($("#message").val().length + "/" + characterSize);
		if ($("#message").val().length > characterSize) {
			$(".warningText").css({ "color": "red" });
		}
		else {
			$(".warningText").css({ "color": "black" });
		}
	}
	$("#checkBoxPrivateMessage").change(function () {
		if (this.checked) {
			$("#privateBlock").css("display", "block");
			getUsersNames().then(fillingPrivateMessageRecipientTable);
		}
		else {
			$("#privateBlock").css("display", "none");
			$("#tablePrivate table").empty();
		}
	});

	function firstKeyCheck(event) {
		if (event.key == "@") {
			userNamePosition = this.selectionStart;
			//заполнение таблицы
			if (userLoadFlag == false) {
				getUsersNames().then(function (data) {
					userLoadFlag = true;
					userList = data.names;
					userFullNameList = data.fullNames;
					userList.sort();
					userFullNameList.sort();
					if (userNamePositionFlag == true) {
						userNameTable.empty();
					}
					else {
						$(".messageUserMention").append(userNameTable);
						userNamePositionFlag = true;
					}
					for (let i = 0; i < userList.length; i++) {
						let userNameBlockList = $("<div/>");
						userNameBlockList.addClass("list");
						userNameBlockList.attr("id", "idListUserNameTr" + i);
						userNameBlockList.attr("userId", i);
						userNameBlockList.click(enterClickUserNames);
						userNameBlockList.text("@" + userFullNameList[i] + " ( " + userList[i] + " )");
						userNameTable.append(userNameBlockList);
					}
					$("#idListUserNameTr" + 0).addClass("selected");
					userNameTableLength = userList.length - 1;
					$(".messageUserMention").css({ "display": "block" });
				});
			}
			else {
				if (userNamePositionFlag == true) {
					userNameTable.empty();
				}
				else {
					$(".messageUserMention").append(userNameTable);
					userNamePositionFlag = true;
				}
				for (let i = 0; i < userList.length; i++) {
					let userNameBlockList = $("<div/>");
					userNameBlockList.addClass("list");
					userNameBlockList.attr("id", "idListUserNameTr" + i);
					userNameBlockList.attr("userId", i);
					userNameBlockList.click(enterClickUserNames);
					userNameBlockList.text("@" + userFullNameList[i] + " ( " + userList[i] + " )");
					userNameTable.append(userNameBlockList);
				}
				$("#idListUserNameTr" + 0).addClass("selected");
				userNameTableLength = userList.length - 1;
				$(".messageUserMention").css({ "display": "block" });
			}
			$("#message").off();
			$("#message").keydown(secondKeyCheck);
		}
		//комбинация хоткея "отправить" (cntrl+enter)
		else if (event.ctrlKey && event.keyCode == 13) {
			sendMessageHandler();
			return false;
		}
	}

	function secondKeyCheck(event) {
		if (event.key == "Backspace") {
			if (userNamePositionFlag == true) {
				if (this.selectionStart == userNamePosition + 1) {
					//отмена
					deleteUserNameTable();
				}
				else {
					if (this.selectionStart <= userNamePosition) {
						userNamePosition--;
					}
					else {//this.selectionStart > userNamePosition
						if (this.selectionStart <= userNamePosition + userNameLength + 1) {
							userNameLength--;
							//обновляем таблицу
							updateUserNameTable(this.value.slice(userNamePosition + 1, this.selectionStart - 1) + this.value.slice(this.selectionStart, userNamePosition + userNameLength + 2));
						}
					}
				}
			}
		}
		else if (event.which === 38) {
			$("#idListUserNameTr" + listUserNameFastInput).removeClass("selected");
			if (listUserNameFastInput == 0) {
				listUserNameFastInput = userNameTableLength;
			} else {
				listUserNameFastInput--;
			}
			$("#idListUserNameTr" + listUserNameFastInput).addClass("selected");
			$("#idListUserNameTr" + listUserNameFastInput).scrollView(".messageUserMention");
			if (userNamePositionFlag == true) {
				event.preventDefault();
				event.stopPropagation();
				return false;
			}
		}
		else if (event.which === 40) {
			$("#idListUserNameTr" + listUserNameFastInput).removeClass("selected");
			if (listUserNameFastInput < userNameTableLength) {
				listUserNameFastInput++;
			} else {
				listUserNameFastInput = 0;
			}
			$("#idListUserNameTr" + listUserNameFastInput).addClass("selected");
			$("#idListUserNameTr" + listUserNameFastInput).scrollView(".messageUserMention");
			if (userNamePositionFlag == true) {
				event.preventDefault();
				event.stopPropagation();
				return false;
			}
		}
		else if (event.key == "Delete") {
			if (userNamePositionFlag == true) {
				if (this.selectionStart == userNamePosition) {
					userNamePositionFlag = false;
					//отмена
					deleteUserNameTable();
				}
				else {
					if (this.selectionStart < userNamePosition) {
						userNamePosition--;
					}
					else {//this.selectionStart >= userNamePosition
						if (this.selectionStart < userNamePosition + userNameLength + 1) {
							userNameLength--;
							//обновляем таблицу
							updateUserNameTable(this.value.slice(userNamePosition + 1, this.selectionStart) + this.value.slice(this.selectionStart + 1, userNamePosition + userNameLength + 2));
						}
					}
				}
			}
		}
		else if (event.key == "Enter") {
			if (userNamePositionFlag == true) {
				$("#idListUserNameTr" + listUserNameFastInput).click();
				return false;
			}
		}
		else if (event.key == " ") {
			deleteUserNameTable();
		}
		else if ((userNamePositionFlag == true) && ((this.selectionStart) > (userNamePosition)) && ((this.selectionStart) < (userNameLength + 2 + userNamePosition)) && (event.key.length == 1) && (/^[A-Za-z0-9]+$/.test(event.key))) {
			userNameLength++;
			//обновляем таблицу
			updateUserNameTable(this.value.slice(userNamePosition + 1, this.selectionStart) + event.key + this.value.slice(this.selectionStart, userNamePosition + userNameLength));
		}
	}
	// -----------приём файлов
	//проверка браузера
	if (typeof (window.FileReader) != "undefined") {
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
	else {
		//если drag - drop не оддерживается
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
	});
	//удаление прикрепленного к сообщению файла (для таблички прикрепленных файлов)
	function deleteAttachedFile() {
		attachedFiles.splice($(this).attr("fileNumber"));
		$(this).closest("tr").remove();
		return false;
	}

	//--------------------------------------------------------------
	//функции приема сообщений с сервера
	//функция установки нового сообщения пришедшего с сервера в чат
	function addMessageHandler(data) {
		if (data && data.text) {
			if (minMassageId > data.id || minMassageId === -1) {
				minMassageId = data.id;
			}
			if (maxMassageId < data.id) {
				maxMassageId = data.id;
			}
			let messageIndex = 0;
			if (!data.old) {
				messageIndex = newMessageIndex;
				newMessageIndex++;
			} else {
				messageIndex = oldMessagesIndex;
				oldMessagesIndex--;
			}

			//создаем сообщение
			var cloneMess = messageBody.clone();
			cloneMess.attr("id", "messBody" + messageIndex);
			cloneMess.attr("messageId", data.id);
			cloneMess.attr("messageIndex", messageIndex);
			cloneMess.find(".datetr").text();
			let date = data.createDate;
			var d = new Date(date);
			cloneMess.find(".author").text(data.author + ":");
			cloneMess.find(".datetr").text(d.getDate().toString() + "." + (d.getMonth() + 1).toString() + "." + d.getFullYear() + " " + d.getHours().toString() + ":" + d.getMinutes().toString());
			cloneMess.find(".messageText").attr("textMessagId", data.id).attr("id", "messageText" + messageIndex).html(data.text);
			// "развернуть"
			if (data.hierarchyMessage == 1) {
				cloneMess.find(".openHierarchy").attr("messageId", data.id)
					.click(hierarchyOpen);
			} else {
				cloneMess.find(".openHierarchy").remove();
			}
			cloneMess.find(".addReply").click(messReplyClickFunction);
			//файлы
			if (data.files.length > 0) {
				let fileTr0 = $("<tr/>");
				let fileTable = $("<table/>");
				fileTable.addClass("fileHolder");
				for (let i = 0; i < data.files.length; i++) {
					let fileIdTr = $("<tr/>");
					fileIdTr.append($("<td/>").append("<a href='/wfe/chatFileOutput?fileId=" + data.files[i].id + "' download='" + data.files[i].name + "'>" + data.files[i].name + "</a>"));
					fileTable.append(fileIdTr);
				}
				fileTr0.append($("<td/>").append(fileTable));
				cloneMess.append(fileTr0);
			}
			// админ
			if ($("#modal-body").attr("admin") == "true") {
				let deleterMessageA0 = $("<a/>");
				deleterMessageA0.addClass("deleterMessage");
				deleterMessageA0.text("x");
				deleterMessageA0.click(deleteMessage);
				cloneMess.prepend($("<tr/>").append($("<td/>").append(deleterMessageA0)));

			}
			//редактирование сообщения кнопка
			if (data.coreUser == true) { //исправить в сокете, что бы давалось сообщениям???
				let editMessage0 = $("<a/>");
				editMessage0.text(editMessageButtonText);
				editMessage0.click(editMessage);
				cloneMess.find(".addReply").parent(5).parent().append($("<td/>").append(editMessage0));
			}
			// конец
			// установка сообщения
			if (data.old == false) {
				if (switchCheak == 0) {// +1 непрочитанное сообщение
					updatenumberNewMessages(numberNewMessages + 1);
					cloneMess.addClass("newMessageClass");
					$("#modal-body").append(cloneMess);
				}
				else {
					if ($("#modal-body").scrollTop() >= $("#modal-body")[0].scrollHeight - $("#modal-body")[0].clientHeight) {
						$("#modal-body").append(cloneMess);
						updatenumberNewMessages(0);
						currentMessageId = maxMassageId;
						updateLastReadMessage();
						newMessagesHeight += getElmHeight(cloneMess);
						$("#modal-body").scrollTop($("#modal-body")[0].scrollHeight);
					}
					else {
						cloneMess.addClass("newMessageClass");
						$("#modal-body").append(cloneMess);
						updatenumberNewMessages(numberNewMessages + 1);
					}
				}
			}
			else {
				$("#modal-body").children().first().after(cloneMess);
				newMessagesHeight += getElmHeight(cloneMess);
			}
		}
	}

	function editMessageHandler(message) {
		let mesSelector = $("[textMessagId='" + message.id + "']");
		if ((mesSelector != null) && (mesSelector != undefined)) {
			mesSelector.text(message.text);
		}
	}

	function errorMessageHandler(message) {
		alert("Сообщение не отправлено. Error: " + message.message);
	}

	function deleteMessageHandler(message) {
		let deleteMessage = $("[messageId='" + message.id + "']");
		if ((deleteMessage != null) && (deleteMessage != undefined)) {
			deleteMessage.remove();
		}
	}

	//---------------------------------------------------------------------
	//перемещение окна
	var windowChat = $(".modal-content")[0];
	var tagetDrug = $("#modal-header-dragg")[0];
	var dragMaster = (function () {
		var dragObject
		var dragTarget
		var mouseOffset

		// получить сдвиг target относительно курсора мыши
		function getMouseOffset(target, e) {
			var docPos = getPosition(target)
			return { x: e.pageX - docPos.x, y: e.pageY - docPos.y }
		}

		function mouseUp() {
			dragTarget = null
			// очистить обработчики, т.к перенос закончен
			document.onmousemove = null
			document.onmouseup = null
			document.ondragstart = null
			document.body.onselectstart = null

		}

		function mouseMove(e) {
			with (dragObject.style) {
				position = "fixed"
				if (mouseOffset.y <= e.pageY) {
					if (e.pageY < ($("body").height() + $(".modal-content").height()))
						top = e.pageY - mouseOffset.y + "px"
				}
				if (mouseOffset.x <= e.pageX) {
					let a = $("body").width();
					if (e.pageX < ($("body").width()))
						left = e.pageX - mouseOffset.x + "px"
				}
			}
			return false
		}

		function mouseDown(e) {
			if (e.which != 1)
				return
			dragTarget = this
			// получить сдвиг элемента относительно курсора мыши
			mouseOffset = getMouseOffset(this, e)
			// эти обработчики отслеживают процесс и окончание переноса
			document.onmousemove = mouseMove
			document.onmouseup = mouseUp
			// отменить перенос и выделение текста при клике на тексте
			document.ondragstart = function () { return false }
			document.body.onselectstart = function () { return false }
			return false
		}

		return {
			dragWindow: function (element1, element2) {
				element1.onmousedown = mouseDown;
				dragObject = element2;
			}
		}
	}())

	function getPosition(e) {
		//сдвиг  области для перемещения
		var offsetL = - $("#modal-header-dragg").position().left;
		var left = offsetL;
		var top = 0

		while (e.offsetParent) {
			left += e.offsetLeft
			top += e.offsetTop
			e = e.offsetParent
		}

		left += e.offsetLeft
		top += e.offsetTop

		return { x: left, y: top }
	}

	if ((tagetDrug !== undefined) && (windowChat !== undefined)) {
		dragMaster.dragWindow(tagetDrug, windowChat);
	}

	function ajaxLocale() {
		let urlString = "/wfe/ajaxcmd?command=LocaleTextChat&language=" + languageText;
		$.ajax({
			type: "POST",
			url: urlString,
			async: false,
			dataType: "json",
			contentType: "application/json; charset=UTF-8",
			processData: false,
			success: function (data) {

				LocaleText(data);
			}
		});
	}

	function LocaleText(data) {
		$("#openChatButtonText").first().text(data.openChatButton);
		//$(".modalSwitchingWindowButton").text(data.switchChatButton);
		//$("#newMessagesIndicator").first().text(data.newMessageIndicator);
		//$("#newMessagesIndicator").last().text();
		textEnterMessage = data.textAreaMessagePalceholder;
		textPrivateMessage = data.privateMessageCheckbox;
		textDragFile = data.dropBlock;
		textBtnSend = data.buttonSendMessage;
		textLoadOldMessage = data.buttonLoadOldMessage;
		editMessageButtonText = data.editMessageButton;
		addReplyButtonText = data.addReplyInMessageButton;
		attachedMessageSignature = data.attachedMessage;
		removeReplyButtonText = data.removeReplyInMessageButton;
		warningEditMessage = data.warningEditMessage;
		warningRemoveMessage = data.warningRemoveMessage;
		openHierarchySignature = data.openHierarchy;
		closeHierarchySignature = data.closeHierarchy;
		quoteText = data.quoteText;
		errorMessFilePart1 = data.errorMessFilePart1;
		errorMessFilePart2 = data.errorMessFilePart2;
		textHeader = data.textHeader;

	}

	let urlString = "/wfe/ajaxcmd?command=ChatInitialize&processId=" + idProcess + "&messageCount=" + messagesStep;
	$.ajax({
		type: "POST",
		url: urlString,
		dataType: "json",
		contentType: "application/json; charset=UTF-8",
		processData: false,
		success: function (data) {
			let reSwitchCheak = switchCheak;
			switchCheak = 0;
			currentMessageId = data.lastMessageId;
			if (data.messages.length > 0) {
				addMessageHandler(JSON.parse(data.messages[0]));
				$("#modal-body").scrollTop(0);
			}
			for (let i = 1; i < data.messages.length; i++) {
				addMessageHandler(JSON.parse(data.messages[i]));
			}
			if (numberNewMessages == 0) {
				newMessagesHeight = $("#modal-body")[0].scrollHeight - $("#modal-body").height();
				updatenumberNewMessages(0);
			}
			chatSocket = establishWebSocketConnection({ "newMessage": addMessageHandler, "editMessage": editMessageHandler, "errorMessage": errorMessageHandler, "deleteMessage": deleteMessageHandler });
			//скролл к непрочитанным
			//установка скрол-функции отслеживания непрочитанных
			$("#modal-body").bind("load scroll", scrollNewMessages);
			switchCheak = reSwitchCheak;

			$("#btnCl").hide();

			btnLoadOldMessages.onclick = loadOldMessages;
			btnSend.onclick = sendMessageHandler;
			btnOpenChat.onclick = openChat;
			document.getElementById("close").onclick = closeChat;
			btnOp.onclick = zoomInZoomOut;

			if (switchCheak == 1) {
				openChat();
			}
		}
	});
}
//инициализации таблицы чатов
function ajaxAllInitializationChats() {
	function getAllChat(data) {
		$(".select-chat").html("<tr><th class='list'>Список чатов</th><th  class='list'>Количество сообщений</th></tr>");
		let idRowListChats = $("<tr/>");
		idRowListChats.attr("id", 0);
		let unreadMessagesCount = $("<td/>").attr("class", "readMes");
		idRowListChats.append($("<td/>"));
		idRowListChats.append(unreadMessagesCount);
		for (let i = 0; i < data.length; i++) {
			let cloneIdRowListChats = idRowListChats.clone();
			cloneIdRowListChats.attr("id", "switchChat" + data[i].processId);
			cloneIdRowListChats.attr("processId", data[i].processId);
			cloneIdRowListChats.children().first().append("processId " + data[i].processId);
			cloneIdRowListChats.children(".readMes").append(data[i].countMessage);
			cloneIdRowListChats.children(".readMes").attr("id", "numberNewMessages" + data[i].processId)
			if (data[i].countMessage > 0) {
				cloneIdRowListChats.children(".readMes").attr("class", "newMessagesChatClass");
			}
			else {
				cloneIdRowListChats.children(".readMes").attr("class", "noNewMessagesChatClass");
			}
			//$(".modalSwitchingWindowBody").append(cloneIdRowListChats);
			$(".select-chat").append(cloneIdRowListChats);
		}
		$(".select-chat td").addClass("list");
	}

	let urlString = "/wfe/ajaxcmd?command=SwitchChatsInitialize";
	$.ajax({
		type: "POST",
		url: urlString,
		dataType: "json",
		contentType: "application/json; charset=UTF-8",
		processData: false,
		success: function (data) {
			getAllChat(data);
		}
	});
}
//----------------------------------------------
//начальные действия
//
/*if (document.location.pathname != "/wfe/chat_page.do") {
	btnOpenChat.onclick = openChat;
	document.getElementById("close").onclick = closeChat;
	btnOp.onclick = zoomInZoomOut;
}
else {
	idProcess = 1 + "";
	switchCheak = 1;
	ajaxAllInitializationChats();
}*/

//$("#modalFooter").children().first().after("<div class=\"warningText\">"+$("#message").val().length+"/"+characterSize+"</div>");
//-----скролл
$.fn.scrollView = function (selector) {
	return this.each(function () {
		$(selector).animate({
			scrollTop: this.offsetTop
		}, 1);
	});
}