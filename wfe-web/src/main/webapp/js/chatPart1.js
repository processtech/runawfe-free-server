$(document).ready(function() {
var attachedPosts=[];
//флаг - блок чата
var lockFlag = false;
//зона для дропа файлов
var dropZone=$("#dropZ");
//зона прикрепленных файлов
var filesTable=$("<table/>");
filesTable.attr("id", "filesTable");
$("#attachedArea").append(filesTable);
//зона прикрепленных сообщений
var messReplyTable=$("<table/>");
messReplyTable.attr("id", "messReplyTable");
$("#attachedArea").append(messReplyTable);

var attachedFiles=[];
//максимальный размер файла
var maxFileSize=1000000000; //около 1 гб

// флаг развернутого чата (0 - свернут, 1 - развернут)
var switchCheak=0;
var chatForm=document.getElementById("ChatForm");

// кнопка открытия чата
var btnOpenChat = document.getElementById("openChatButton");
var btnLoadNewMessage=document.getElementById("loadNewBessageButton");
var btnOp=document.getElementById("btnOp");
var imgButton=document.getElementById("imgButton");

// флаг обозначающий состояние(развернут или свернут) чат
var flagRollExpandChat=0;

// закрытие (сворачивание) чата
var span = document.getElementById("close");

// нумерация сообщений = количество загруженных сообщений, если небыло удалений
var lastMessageIndex=0;
var minMassageId = -1;
var maxMassageId = -1;
var currentMessageId = -1;
var numberNewMessages = 0;
var blocOldMes=0;
var inputH=document.getElementById("message");
//шаг - по сколько сообщений подгружается
var messagesStep = 20;

var chatSocketURL = "ws://" + document.location.host + "/wfe/chatSoket?chatId=" + $("#ChatForm").attr("chatId");
var chatSocket = new WebSocket(chatSocketURL);
chatSocket.onmessage = onMessage;
$("#btnCl").hide();

//фунцкия отправляет запрос на выдачу count старых сообщений
function newxtMessages(count){
	let newMessage={};
	newMessage.chatId=$("#ChatForm").attr("chatId");
	newMessage.type="getMessages";
	newMessage.lastMessageId=minMassageId;
	newMessage.Count = count; // количество начальных сообщений
	let firstMessages = JSON.stringify(newMessage);
	chatSocket.send(firstMessages);
}

// функция пишущая кол-во непрочитанных сообщений = numberNewMessages
function updatenumberNewMessages(numberNewMessages0){
	numberNewMessages = numberNewMessages0;
	document.getElementById("countNewMessages").innerHTML="" + numberNewMessages + "";
}

//функция отправляет по сокету id последнего прочитонного сообщния
function updateLastReadMessage(){
	let newSend0={};
	newSend0.chatId=$("#ChatForm").attr("chatId");
	newSend0.type="setChatUserInfo";
	newSend0.currentMessageId=currentMessageId;
	let sendObject0 = JSON.stringify(newSend0);
	chatSocket.send(sendObject0);
}

//действия при открытии сокета
chatSocket.onopen=function(){
	// запрос 20 последних сообщений
	newxtMessages(messagesStep);
	// запрос текущей информации по юзеру
	let newMessage2={};
	newMessage2.chatId=$("#ChatForm").attr("chatId");
	newMessage2.type="getChatUserInfo";
	let sendObject0 = JSON.stringify(newMessage2);
	chatSocket.send(sendObject0);
}

//действия при закрытии сокета
chatSocket.onclose = function(){
	$(".modal-body").append("<table ><td>" + "потерянно соединение с чатом сервера" + "</td></table >");
}

// -----------onClick функции:

// подгрузка старых сообщений
btnLoadNewMessage.onclick=function(){
	if(blocOldMes == 0){
	blocOldMes=1;
	// запрос 20 сообщений старых
	newxtMessages(messagesStep);
	}
}

// кнопка открытия чата
btnOpenChat.onclick = function() {
	if(chatForm != null){
		chatForm.style.display = "block";
		switchCheak=1;
		// "в прочитанные все"
		currentMessageId = maxMassageId;
		//прокрутка
		$("#messReply"+(lastMessageIndex - numberNewMessages)).scrollView();
		updatenumberNewMessages(0);
		updateLastReadMessage();
	}
}

// закрытие (сворачивание) чата
span.onclick = function() {
	chatForm.style.display = "none";
	switchCheak=0;
}

//открыти настроек чата
$(".modalSettings").click(function() {
	$(".modalSetting").css({"display":"block"});
});

//закрытие настроек  чата 
$(".closeButtonModalSetting").click(function(){

	$(".modalSetting").css({"display":"none"});
});

//принять изменения в настроках из чата
$(".acceptSettingsModal").click(function(){
	
});

//
$("#message").keyup(function(){
	let value=$(this).val();
	if(value=="@"){
		$(this).append("<table class=\"tableModalNameSetMessage\"><tr><td>Игорь</td></tr><table>")
	}
});

// кнопка "отправить"
btnSend.onclick=function send() {
	if(lockFlag == false){
		let message = document.getElementById("message").value;
		//ищем ссылки
		message=message.replace(/(^|[^\/\"\'\>\w])(http\:\/\/)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='$2$3$4'>$2$3$4</a>");
		message=message.replace(/(^|[^\/\"\'\>\w])(https\:\/\/)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='$2$3$4'>$2$3$4</a>");
		message=message.replace(/(^|[^\/\"\'\>\w])(www\.)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='http://$2$3$4'>$2$3$4</a>");
		message = message.replace(/\n/g, "<br/>");
		let idHierarchyMessage="";
		for(var i=0;i<attachedPosts.length;i++){
			idHierarchyMessage += attachedPosts[ i ] + ":";
		}
		// сокет
		let newMessage={};
		newMessage.message=message;
		newMessage.chatId=$("#ChatForm").attr("chatId");
		newMessage.idHierarchyMessage = idHierarchyMessage;
		newMessage.type="newMessage";
		if(attachedFiles.length > 0){
			newMessage.haveFile=true;
			lockFlag = true;
		}
		else{
			newMessage.haveFile=false;
		}
		//отправка
		chatSocket.send(JSON.stringify(newMessage));
		// чистим "ответы"
		let addReplys0 = document.getElementsByClassName("addReply");
		for(let i=0; i<addReplys0.length; i++){
			$(addReplys0[ i ]).text("Ответить");
			$(addReplys0[ i ]).attr("flagAttach", "false");
		}
		attachedPosts=[];
		$("#messReplyTable").html("");
	}
}
 
// кнопка развернуть/свернуть чат
btnOp.onclick=function(){
	if(flagRollExpandChat == 0){
		flagRollExpandChat=1;
		$(".modal-content").css({
			width: $(".modal-content").width() + 300,
			height: $(".modal-content").height() + 350,
		});

		$(".modal-body").css({
			width: $(".modal-body").width() + 300,
			height: $(".modal-body").height() + 250,
		});

		$("#attachedArea").css({
			height: $("#attachedArea").height() + 50,
		});
		
		$(".modal-header-dragg").css({
			width: $(".modal-header-dragg").width() + 300,
		});
		

		$(".modal-footer").css({
			height: $(".modal-footer").height() + 30,
		});
		
		dropZone.css({
			height: $(".modal-footer").height() + 30,
		});

		imgButton.src="/wfe/images/chat_expand.png";
	}else if(flagRollExpandChat == 1){
		flagRollExpandChat=0;
		$(".modal-content").css({
			width: $(".modal-content").width() - 300,
			height: $(".modal-content").height() - 350,
		});

		$(".modal-body").css({
			width: $(".modal-body").width() - 300,
			height: $(".modal-body").height() - 250,
		});

		$("#attachedArea").css({
			height: $("#attachedArea").height() - 50,
		});
		
		$(".modal-header-dragg").css({
			width: $(".modal-header-dragg").width() - 300,
		});
		

		$(".modal-footer").css({
			height: $(".modal-footer").height() - 30,
		});
		
		dropZone.css({
			height: $(".modal-footer").height() - 30,
		});
		
		imgButton.src="/wfe/images/chat_roll_up.png";
	}
}

$.fn.scrollView = function () {
	return this.each(function () {
			$(".modal-body").animate({
					scrollTop: $(this).offset().top
			}, 1);
	});
}

// -----------приём файлов
//проверка браузера
if (typeof(window.FileReader) != 'undefined') {
    //поддерживает
	$("html").bind("dragover", function(){
		dropZone.show();
		dropZone.addClass("dropZActive");
		return false;
	});
	
	$("html").bind("dragleave", function(event){
		if(event.relatedTarget == null){
			dropZone.hide();
			dropZone.removeClass("dropZActive");
		}
		return false;
	});
	dropZone[0].ondragover = function() {
		//тут смена класса
		dropZone.addClass("dropZActiveFocus");
	    return false;
	};
	dropZone[0].ondragleave = function() {
		//тут обратная смена класса/его удаление
		dropZone.removeClass("dropZActiveFocus");
	    return false;
	};
	
	dropZone[0].ondrop = function(event) {
	    event.preventDefault();
	    let files = event.dataTransfer.files;
	    for(let i = 0; i<files.length ;i++){
	    	attachedFiles.push(files[i]);
		    //создаем отметку о прикреплении
		    let newFile=$("<tr/>");
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
	    dropZone.hide();
		dropZone.removeClass("dropZActive");
		dropZone.removeClass("dropZActiveFocus");
	};
}
else{
	//если drag - drop не оддерживается
}
//альтернатива - fileInput
$("#fileInput").change(function() {
	let files = 	$(this)[0].files;
    for(let i = 0; i<files.length ;i++){
    	attachedFiles.push(files[i]);
	    //создаем отметку о прикреплении
	    let newFile=$("<tr/>");
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
});
//удаление прикрепленных к сообщению файлов (для не отправленных сообщений)
function deleteAttachedFile(){
	attachedFiles.splice($(this).attr("fileNumber"));
	$(this).parent().parent().remove();
	return false;
}

// -----------функции реализующие механники чата:

// ajax запрос иерархии сообщений, вернет Promise ajax запроса
function hierarhyCheak(messageId){
	let urlString = "/wfe/ajaxcmd?command=GetHierarhyLevel&chatId=" + $("#ChatForm").attr("chatId") + "&messageId=" + messageId;	
	return $.ajax({
		type: "POST",
		url: urlString,
		dataType: "json",
		contentType: "application/json; charset=UTF-8",
		processData: false,
		success: function(data) {}
	});
}

//функция для разворачивания вложенных сообщений
function hierarchyOpen(){
	if($(this).attr("openFlag") == 1){
		let thisElem=$(".openHierarchy")[ 0 ];
		$(this).next(".loadedHierarchy")[ 0 ].style.display="none";
		$(this).attr("openFlag","0");
		$(this).text("Развернуть вложенные сообщения");
		return 0;
	}
	else{
		let thisElem=$(".openHierarchy")[ 0 ];
		if($(this).attr("loadFlag") == 1){
			$(this).next(".loadedHierarchy")[ 0 ].style.display="block";
			$(this).attr("openFlag","1");
			$(this).text("Свернуть");
			return 0;
		}else{
			let thisElem=$(".openHierarchy")[ 0 ];
			let element=this;
			hierarhyCheak($(element).attr("mesId")).then(ajaxRet=>{
				messagesRetMass = getAttachedMessagesArray(ajaxRet);
				for(let i=0; i<messagesRetMass.length; i++){
					$(this).next(".loadedHierarchy").append(messagesRetMass[ i ]);
				}
				$(element).attr("loadFlag", "1");
				$(this).attr("openFlag","1");
				$(this).text("Свернуть");
				return 0;
			});
		}
	}
}

// функция возвращающая массив блоков вложенных сообщений
function getAttachedMessagesArray(data) {
	let outputArray=[];
	if(data.newMessage == 0){
		for(let mes=0;mes<data.messages.length;mes++){
			if(data.messages[ mes ].text != null){
				let messageBody = $("<table/>").addClass("quote");
				messageBody.append($("<tr/>").addClass("selectionTextAdditional").append($("<td/>").text("Цитата: " + data.messages[ mes ].author)));
				messageBody.append($("<tr/>").append($("<td/>").text(data.messages[ mes ].text)));
				if(data.messages[ mes ].hierarchyMessageFlag == 1){
					let openHierarchy0 = $("<a/>").addClass("openHierarchy");
					openHierarchy0.attr("type", "button");
					openHierarchy0.attr("mesId", data.messages[ mes ].id);
					openHierarchy0.attr("loadFlag", 0);
					openHierarchy0.attr("openFlag", 0);
					openHierarchy0.text("Развернуть вложенные сообщения");
					openHierarchy0.click(hierarchyOpen);
					messageBody.append($("<tr/>").append($("<td/>").append(openHierarchy0).append($("<div/>").addClass("loadedHierarchy"))));
				}
				outputArray.push(messageBody);
			}
		}
		return outputArray;
	}
}

// функция установки нового сообщения пришедшего с сервера в чат
function addMessages(data){
	if(data.newMessage == 0){
		for(let mes=0; mes < data.messages.length; mes++){
			if(data.messages[ mes ].text != null){
				if((minMassageId > data.messages[ mes ].id) || (minMassageId == -1)){
					minMassageId = data.messages[ mes ].id;
				}
				if((maxMassageId < data.messages[ mes ].id)){
					maxMassageId = data.messages[ mes ].id;
				}
				let text0 = data.messages[ mes ].text;
				//создаем сообщение
				let messageBody=$("<table/>").addClass("selectionTextQuote");
				messageBody.append($("<tr/>").append($("<td/>").append($("<div/>").addClass("author").text(data.messages[ mes ].author + ":")).append($("<div/>").addClass("messageText").attr("id","messageText"+lastMessageIndex).append(text0))));
				// "развернуть"
				if(data.messages[ mes ].hierarchyMessageFlag == 1){
					let openHierarchyA0 = $("<a/>");
					openHierarchyA0.addClass("openHierarchy");
					openHierarchyA0.attr("mesId", data.messages[ mes ].id);
					openHierarchyA0.attr("loadFlag", 0);
					openHierarchyA0.attr("openFlag", 0);
					openHierarchyA0.text("Развернуть вложенные сообщения");
					openHierarchyA0.click(hierarchyOpen);
					messageBody.append($("<tr/>").append($("<td/>").append(openHierarchyA0).append($("<div/>").addClass("loadedHierarchy"))));
				}
				// "ответить"
				let addReplyA0 = $("<a/>");
				addReplyA0.addClass("addReply");
				addReplyA0.attr("id", "messReply"+lastMessageIndex);
				addReplyA0.attr("messageIndex", lastMessageIndex)
				addReplyA0.attr("mesId", data.messages[ mes ].id);
				addReplyA0.attr("flagAttach", "false");
				addReplyA0.text(" Ответить");
				addReplyA0.click(messReplyClickFunction);
				
				let dateTr0=$("<tr/>");
				dateTr0.append($("<td/>").append("<hr class='hr-dashed'>").append(data.messages[ mes ].dateTime).append("<hr class='hr-dashed'>"));
				dateTr0.append($("<td/>").append($("<div/>").addClass("hr-dashed-vertical").append(addReplyA0)));
				messageBody.append(dateTr0);
				//файлы
				if(data.messages[ mes ].haveFile == true){
					let fileTr0 = $("<tr/>");
					let fileTable = $("<table/>");
					fileTable.addClass("fileHolder");
					for(let i = 0; i < data.messages[ mes ].fileIdArray.length; i++){
						let fileIdTr = $("<tr/>");
						fileIdTr.append($("<td/>").append("<a href='/wfe/chatFileOutput?fileId=" + data.messages[ mes ].fileIdArray[i].id + "' download='" + data.messages[ mes ].fileIdArray[i].name + "'>" + data.messages[ mes ].fileIdArray[i].name + "</a>"));
						fileTable.append(fileIdTr);
					}
					fileTr0.append($("<td/>").append(fileTable).append("<hr class='hr-dashed'>"));
					messageBody.append(fileTr0);
				}
				// админ
				if($(".modal-body").attr("admin") == "true"){
					let deleterMessageA0 = $("<a/>");
					deleterMessageA0.addClass("deleterMessage");
					deleterMessageA0.attr("id", "messDeleter"+(lastMessageIndex));
					deleterMessageA0.attr("mesId", data.messages[ mes ].id);
					deleterMessageA0.text("удалить");
					messageBody.append($("<tr/>").append($("<td/>").append(deleterMessageA0)));
				}
				// конец
				// установка сообщения
				if(data.old == false){
					$(".modal-body").append(messageBody);
					if(switchCheak == 0){// +1 непрочитанное сообщение
						updatenumberNewMessages(numberNewMessages + 1);
					}
					else{
						currentMessageId = maxMassageId;
						updateLastReadMessage();
					}
				}
				else{
					$(".modal-body").children().first().after(messageBody);
				}
				if($(".modal-body").attr("admin") == "true"){
					document.getElementById("messDeleter" + (lastMessageIndex)).onclick=deleteMessage;
				}
				lastMessageIndex += 1;
			}
		}
	}
}

//функция для кнопки "ответить" (прикрепляет сообщение)
function messReplyClickFunction(){
	if(lockFlag == false){
		if($(this).attr("flagAttach") == "false"){
			attachedPosts.push($(this).attr("mesId"));
			$(this).attr("flagAttach", "true");
			$(this).text("Отменить");
		    //создаем отметку о прикреплении
		    let newMessReply=$("<tr/>");
		    newMessReply.append($("<td/>").css({"max-width": $("#attachedArea").width()-30, "white-space": "nowrap"}).text("прикрепленное сообщение:" + $("#messageText" + $(this).attr("messageIndex")).text()));
		    let deleteMessReplyButton = $("<button/>");
		    deleteMessReplyButton.text("X");
		    //deleteMessReplyButton.addClass("");
		    deleteMessReplyButton.attr("id", "deleteMessReply" + $(this).attr("messageIndex"));
		    deleteMessReplyButton.attr("mesIndex", $(this).attr("messageIndex"));
		    deleteMessReplyButton.attr("type", "button");
		    deleteMessReplyButton.click(deleteAttachedMessage);
		    newMessReply.append($("<td/>").append(deleteMessReplyButton));
		    $("#messReplyTable").append(newMessReply);
		}
		else{
			$(this).text("Ответить");
			$(this).attr("flagAttach", "false");
			let pos0 = attachedPosts.indexOf($(this).attr("mesId"), 0);
			attachedPosts.splice(pos0, 1);
			$("#deleteMessReply" + $(this).attr("messageIndex")).parent().parent().remove();
		}
	}
}

//функция открепления сообщений
function deleteAttachedMessage(){
	let pos0 = attachedPosts.indexOf($("#messReply"+$(this).attr("mesIndex")).attr("mesId"), 0);
	attachedPosts.splice(pos0, 1);
	$("#messReply" + $(this).attr("mesIndex")).text("Ответить");
	$("#messReply" + $(this).attr("mesIndex")).attr("flagAttach", "false");
	$(this).parent().parent().remove();
}

// удаление сообщений
function deleteMessage(){
	if(confirm("Вы действительно хотите удалить сообщение? Отменить это действие будет невозможно")){
		let newMessage={};
		newMessage.messageId=$(this).attr("mesId");
		newMessage.chatId=$("#ChatForm").attr("chatId");
		newMessage.type="deleteMessage";
		chatSocket.send(JSON.stringify(newMessage));
		$(this).parent().parent().parent().parent().remove();
	}
}

function nextStepLoadFile(messageId, FileIndex){
	// Создаем форму с несколькими значениями
	let form0 = new FormData();
	form0.append("file", attachedFiles[0]);
	// отправляем через xhr
	let xhr = new XMLHttpRequest();
	let endFlag = false;
	if(attachedFiles.length == FileIndex+1){
		xhr.onload = function() {
			attachedFiles=[];
			$("#filesTable").html("");
			lockFlag=false;
		};
		xhr.open("post", "/wfe/chatFileInput" + "?fileName=" + attachedFiles[FileIndex].name + "&messageId=" +messageId + "&endFlag=true", false);
		xhr.send(form0);
	}
	else{
		xhr.onload = function() {
			FileIndex++;
			nextStepLoadFile(messageId, FileIndex);
		};
		xhr.open("post", "/wfe/chatFileInput" + "?fileName=" + attachedFiles[FileIndex].name + "&messageId=" +messageId + "&endFlag=false", false);
		xhr.send(form0);
	}
}

// приём с сервера
function onMessage(event) {
	let message0 = JSON.parse(event.data);
	if(message0.messType == "newMessages"){
		addMessages(message0);
	}
	else if(message0.messType == "deblocOldMes"){
		blocOldMes=0;
	}
	else if(message0.messType == "ChatUserInfo"){
		if(switchCheak == 0){
			if(currentMessageId<message0.lastMessageId){
				currentMessageId = message0.lastMessageId;
			}
			updatenumberNewMessages(message0.numberNewMessages);
			// дозапрос всех непрочитанных сообщений
			if(numberNewMessages>lastMessageIndex){
				newxtMessages(numberNewMessages-lastMessageIndex);
			}
		}
	}
	else if(message0.messType == "nextStepLoadFile"){
		nextStepLoadFile(message0.messageId, 0);
		let newMessage={};
		newMessage.messageId=message0.messageId;
		newMessage.chatId=$("#ChatForm").attr("chatId");
		newMessage.type="sendToChat";
		chatSocket.send(JSON.stringify(newMessage));
	}
}
// конец
});
