$(document).ready(function() {
//
var newMessagesHeight = 0;
//
var attachedPosts=[];
//переменные редактирования сообщения
var editMessageFlag=false;
var editMessageId = -1;
//переменные отслеживания @user
var userNamePosition = -1;
var userNamePositionFlag = false;
var userLoadFlag = false;
var userList = [];
var userNameLength=0;
var userNameTable = $("<table/>");
userNameTable.addClass("tableModalNameSetMessage");
userNameTable.attr("id", "userNameTable");
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
var progressBar=$("<div/>");
progressBar.attr("id", "progressBar");
progressBar.text("0/0");
progressBar.hide();
$("#modalFooter").append(progressBar);
var attachedFiles=[];
//проверка размера входного файла
var fileInp=1024 * 1024 * 1024;//1 гб
//количество символов после которых встлывает предупреждение
var characterSize=1024;
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
var newMessageIndex=0;
var oldMessagesIndex = -1;//только для старых, идет в отрицательные
var minMassageId = -1;
var maxMassageId = -1;
var currentMessageId = -1;
var numberNewMessages = 0;
var blocOldMes=0;
var inputH=document.getElementById("message");
//шаг - по сколько сообщений подгружается
var messagesStep = 20;

//флаг выбран ли какой нибудь блок из списка предложенных при вводе @
var listUserNameFastInput=1;
var userNameTableLength=0;

//запрос на инициализацию
var chatSocket = null;
var chatSocketURL = null;
let urlString = "/wfe/ajaxcmd?command=ChatInitialize&chatId=" + $("#ChatForm").attr("chatId") + "&messageCount=" + messagesStep;	
$.ajax({
	type: "POST",
	url: urlString,
	dataType: "json",
	contentType: "application/json; charset=UTF-8",
	processData: false,
	success: function(data) {
		currentMessageId = data.lastMessageId;
		addMessages(data.messages[0]);
		$("#modal-body").scrollTop(0);
		for(let i=1; i<data.messages.length; i++){
			addMessages(data.messages[i]);
		}
		if(numberNewMessages == 0){
			newMessagesHeight = $("#modal-body")[0].scrollHeight - $("#modal-body").height();
			updatenumberNewMessages(0);
			$("#modal-body").scrollTop($("#modal-body")[0].scrollHeight);
		}
		else{
			$("#modal-body").scrollTop($("#messBody0")[0].offsetTop);
		}
		chatSocketURL = "ws://" + document.location.host + "/wfe/chatSoket?chatId=" + $("#ChatForm").attr("chatId");
		chatSocket = new WebSocket(chatSocketURL);
		chatSocket.onmessage = onMessage;
		//действия при открытии сокета
		chatSocket.onopen=function(){
		}
		//действия при закрытии сокета
		chatSocket.onclose = function(){
			$("#modal-body").append("<table ><td>" + "потерянно соединение с чатом сервера" + "</td></table >");
		}
		//скролл к непрочитанным
		//установка скрол-функции отслеживания непрочитанных
		$("#modal-body").bind("load scroll", scrollNewMessages);
	}
});
$("#btnCl").hide();

//реальный размер элемента
function getElmHeight(node) {
   return node.outerHeight(true);
}

//скрол-функция отслеживания непрочитанных
function scrollNewMessages(){
	let modalBody = $("#modal-body");
	if(modalBody.scrollTop() > newMessagesHeight){
		let scrollTop0 = modalBody.scrollTop() + modalBody.height();
		let newIndex = newMessageIndex - numberNewMessages;
		let i = newIndex;
		for(; i<newMessageIndex; i++){
			let message0 = $("#messBody" + i);
			if(message0[0].offsetTop < scrollTop0){
				newMessagesHeight += getElmHeight($("#messBody" + i));
				message0.addClass("InViewport");
				message0.removeClass("newMessageClass");
			}
			else{
				i--;
				message0 = $("#messBody" + i);
				currentMessageId = message0.attr("mesId");
				updatenumberNewMessages(newMessageIndex -1 - message0.attr("messageIndex"));
				updateLastReadMessage();
				//
				return 0;
			}
		}
		i--;
		message0 = $("#messBody" + i);
		currentMessageId = message0.attr("mesId");
		updatenumberNewMessages(newMessageIndex -1 - message0.attr("messageIndex"));
		updateLastReadMessage();
	}
}

$("#modal-body").resize(function(){
	if(numberNewMessages>0){
		newMessagesHeight = $("#messBody" + (newMessageIndex - numberNewMessages))[0].offsetTop - $("#modal-body").height();
	}
	else{
		newMessagesHeight = $("#modal-body")[0].scrollHeight - $("#modal-body").height();
	}
});

//фунцкия отправляет запрос на выдачу count старых сообщений
function newxtMessages(count){
	let newMessage={};
	newMessage.chatId=$("#ChatForm").attr("chatId");
	newMessage.type="getMessages";
	newMessage.lastMessageId=minMassageId;
	newMessage.Count = count; // количество сообщений
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
		//установка границы скролла непрочитанных
		if(numberNewMessages == 0){
			newMessagesHeight = $("#modal-body")[0].scrollHeight - $("#modal-body").height();
			$("#modal-body").scrollTop($("#modal-body")[0].scrollHeight);
		}
		else{
			newMessagesHeight = $("#messBody" + (newMessageIndex - numberNewMessages))[0].offsetTop - $("#modal-body").height();
			$("#modal-body").scrollTop(newMessagesHeight);
		}
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

// кнопка "отправить"
 function sendMessage() {
	deleteUserNameTable();
	if(lockFlag == false){
		if(editMessageFlag == false){
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
			$("#message").val("");
			// чистим "ответы"
			let addReplys0 = document.getElementsByClassName("addReply");
			for(let i=0; i<addReplys0.length; i++){
				$(addReplys0[ i ]).text("Ответить");
				$(addReplys0[ i ]).attr("flagAttach", "false");
			}
			attachedPosts=[];
			$("#messReplyTable").remove();
			//$("#$fileInput").text("Файл не выбран");
		}
		else{//редактирование сообщения
			if(confirm("Вы действительно хотите отредактировать сообщение? Отменить это действие будет невозможно")){
				let message = document.getElementById("message").value;
				//ищем ссылки
				message=message.replace(/(^|[^\/\"\'\>\w])(http\:\/\/)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='$2$3$4'>$2$3$4</a>");
				message=message.replace(/(^|[^\/\"\'\>\w])(https\:\/\/)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='$2$3$4'>$2$3$4</a>");
				message=message.replace(/(^|[^\/\"\'\>\w])(www\.)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='http://$2$3$4'>$2$3$4</a>");
				message = message.replace(/\n/g, "<br/>");
				let newMessage={};
				newMessage.message=message;
				newMessage.chatId=$("#ChatForm").attr("chatId");
				newMessage.type="editMessage";
				newMessage.editMessageId = editMessageId;
				$("#message").val(""); 
				chatSocket.send(JSON.stringify(newMessage));
				editMessageId=-1;
				editMessageFlag=false;
			}
			else{
				$("#message").val("");
				editMessageId=-1;
				editMessageFlag=false;
			}
		}
	}
	return 0;
}
btnSend.onclick=sendMessage;
 
$("#message").keydown(function(e){
	if(e.ctrlKey && e.keyCode == 13){
		sendMessage();
	}
});

//кнопка развернуть/свернуть чат
btnOp.onclick=function(){
	if(flagRollExpandChat == 0){
		flagRollExpandChat=1;
		$(".modal-content").css({
			width: $(".modal-content").width() + 300,
		});
		$(".modal-header-dragg").css({
			width: $(".modal-header-dragg").width() + 300,
		});
		$('.messageUserMention').css({
			"margin-top" : (-1)*$('#message').height()+"px",
			height: 90+"px",	
			width: 212+"px",
		})
		dropZone.css({
			height: $("#attachedArea").height(),
		});
		imgButton.src="/wfe/images/chat_expand.png";
		if(numberNewMessages>0){
			newMessagesHeight = $("#messBody" + (newMessageIndex - numberNewMessages))[0].offsetTop - ($("#modal-body").height()+$("#messBody" + (newMessageIndex - numberNewMessages)).css('padding'));
		}
		else{
			newMessagesHeight = $("#messBody" + (newMessageIndex - numberNewMessages))[0].offsetTop - ($("#modal-body").height()+$("#messBody" + (newMessageIndex - numberNewMessages)).css('padding'));
		}
	}else if(flagRollExpandChat == 1){
		flagRollExpandChat=0;
		$(".modal-content").css({
			width: $(".modal-content").width() - 300,
		});
		$("#attachedArea").css({
			height: $("#attachedArea").height() - 50,
		});
		
		$(".modal-header-dragg").css({
			width: $(".modal-header-dragg").width() - 300,
		});
		$('.messageUserMention').css({
			"margin-top" : (-1) * $('#message').height()+10+"px",
			height: 50+"px",			
			width: 225+"px",
		});
		imgButton.src="/wfe/images/chat_roll_up.png";
		if(numberNewMessages>0){
			newMessagesHeight = $("#messBody" + (newMessageIndex - numberNewMessages))[0].offsetTop - ($("#modal-body").height()+$("#messBody" + (newMessageIndex - numberNewMessages)).css('padding'));
		}
		else{
			newMessagesHeight = $("#modal-body")[0].scrollHeight - ($("#modal-body").height()+$("#messBody" + (newMessageIndex - numberNewMessages)).css('padding'));
		}
	}
}

//-----скролл
$.fn.scrollView = function (selector) {
	return this.each(function () {
			$(selector).animate({
					scrollTop: this.offsetTop
			}, 1);
	});
}

//------------------вставка юзеров
//ajax запрос иерархии сообщений, вернет Promise ajax запроса
function getUsersNames(){
	let urlString = "/wfe/ajaxcmd?command=GetUsersNamesForChat&chatId=" + $("#ChatForm").attr("chatId");
	return $.ajax({
		type: "POST",
		url: urlString,
		dataType: "json",
		contentType: "application/json; charset=UTF-8",
		processData: false,
		success: function(data) {}
	});
}

//@
function enterClickUserNames(event){
	let userNameText = $(this).text().slice(userNameLength+1) + " ";
	$("#message").val($("#message").val().slice(0, userNamePosition+userNameLength+1) + userNameText + $("#message").val().slice(userNamePosition+userNameLength+1));
	deleteUserNameTable();
}

//полная очистка таблицы вставки userNameTable и её переменных
function deleteUserNameTable(){
	userNameTable.detach();
	userNameTable.html("");
	$(".messageUserMention").css({"display":"none"});
	userNameLength=0;
	userNamePositionFlag = false;
}

//обновить таблицу userNameTable
function updateUserNameTable(enterUserName){
	userNameTable.html("");
	userNameTableLength = 0;
	for(let i=0;i<userList.length;i++){
		let partName = userList[i].slice(0,userNameLength);
		if(partName==enterUserName){
			let userNameBlockList=$("<li/>");
			userNameBlockList.attr("id","idListUserNameTr"+userNameTableLength);
			userNameBlockList.addClass("list");
			userNameBlockList.click(enterClickUserNames);
			userNameTable.append(userNameBlockList.text("@"+userList[i]));
			userNameTableLength++;
		}
	}
	$("#idListUserNameTr"+0).addClass("selected");
}
$("#message").keydown(function keydownUserNames(event){//не забыть оптимизировать if на проверки (userNamePositionFlag == true) !!!
	if(event.key == "Backspace"){
		if(userNamePositionFlag == true){
			if(this.selectionStart == userNamePosition+1){
				//отмена
				deleteUserNameTable();
			}
			else{
				if(this.selectionStart <= userNamePosition){
					userNamePosition--;
				}
				else{//this.selectionStart > userNamePosition
					if(this.selectionStart <= userNamePosition+userNameLength+1){
						userNameLength--;
						//обновляем таблицу
						updateUserNameTable(this.value.slice(userNamePosition+1, this.selectionStart-1) + this.value.slice(this.selectionStart,userNamePosition+userNameLength+2));
					}
				}
			}
		}
	}
	else if(event.which === 38){
			$("#idListUserNameTr"+listUserNameFastInput).removeClass('selected');
			if(listUserNameFastInput==0){
				listUserNameFastInput=userNameTableLength;
			}else
			{
				listUserNameFastInput--;
			}
			$("#idListUserNameTr"+listUserNameFastInput).addClass('selected');
			$("#idListUserNameTr"+listUserNameFastInput).scrollView(".messageUserMention");
		if(userNamePositionFlag == true){
			event.preventDefault();
			event.stopPropagation();
		    return false;
		}
	}
	else  if(event.which === 40){
			$("#idListUserNameTr"+listUserNameFastInput).removeClass('selected');
			if(listUserNameFastInput<userNameTableLength){
				listUserNameFastInput++;
			}else
			{
				listUserNameFastInput=0;
			}
			$("#idListUserNameTr"+listUserNameFastInput).addClass('selected');
			$("#idListUserNameTr"+listUserNameFastInput).scrollView(".messageUserMention");
		if(userNamePositionFlag == true){
			event.preventDefault();
			event.stopPropagation();
		    return false;
	    }
	}
	else if(event.key == "Delete"){
		if(userNamePositionFlag == true){
			if(this.selectionStart == userNamePosition){
				userNamePositionFlag = false;
				//отмена
				deleteUserNameTable();
			}
			else{
				if(this.selectionStart < userNamePosition){
					userNamePosition--;
				}
				else{//this.selectionStart >= userNamePosition
					if(this.selectionStart < userNamePosition+userNameLength+1){
						userNameLength--;
						//обновляем таблицу
						updateUserNameTable(this.value.slice(userNamePosition+1, this.selectionStart) + this.value.slice(this.selectionStart+1,userNamePosition+userNameLength+2));
					}
				}
			}
		}
	}
	else if ((event.key == " ") || (event.key == "Enter")){
		if(userNamePositionFlag == true){
			$("#idListUserNameTr"+listUserNameFastInput).click();
		}
	}
	else if(event.key == "@"){
		userNamePosition = this.selectionStart;
		//заполнение таблицы
		if(userLoadFlag == false){
			getUsersNames().then(function(data){
				userLoadFlag = true;
				userList = data.names;
				userList.sort();
				if(userNamePositionFlag == true){
					userNameTable.html("");
				}
				else{
					$(".messageUserMention").append(userNameTable);
					userNamePositionFlag = true;
				}
				for(let i=0;i<userList.length;i++){
					let userNameBlockList=$("<div/>");
					userNameBlockList.addClass("list");
					userNameBlockList.attr("id","idListUserNameTr"+i);
					userNameBlockList.click(enterClickUserNames);
					userNameBlockList.text("@"+userList[i]);
					userNameTable.append(userNameBlockList);
				}
				$("#idListUserNameTr"+0).addClass("selected");
				userNameTableLength = userList.length-1;
				$(".messageUserMention").css({"display":"block"});
			});
		}
		else{
			if(userNamePositionFlag == true){
				userNameTable.html("");
			}
			else{
				$(".messageUserMention").append(userNameTable);
				userNamePositionFlag = true;
			}
			for(let i=0;i<userList.length;i++){
				let userNameBlockList=$("<div/>");
				userNameBlockList.addClass("list");
				userNameBlockList.attr("id","idListUserNameTr"+i);
				userNameBlockList.click(enterClickUserNames);
				userNameBlockList.text("@"+userList[i]);
				userNameTable.append(userNameBlockList);
			}
			$("#idListUserNameTr"+0).addClass("selected");
			userNameTableLength = userList.length-1;
			$(".messageUserMention").css({"display":"block"});
		}
	}
	else if((userNamePositionFlag == true)&&( (this.selectionStart) > (userNamePosition) )&&( (this.selectionStart) < (userNameLength+2+userNamePosition) )&&(event.key.length==1)&&(/^[A-Za-z0-9]+$/.test(event.key))){
		userNameLength++;
		//обновляем таблицу
		updateUserNameTable(this.value.slice(userNamePosition+1, this.selectionStart) + event.key + this.value.slice(this.selectionStart,userNamePosition+userNameLength+1));
	}
});

$("#modalFooter").children().first().after("<div class=\"warningText\">"+$('#message').val().length+"/"+characterSize+"</div>");
$("#message").keyup(function keyupUserNames(event){
	$(".warningText").html($('#message').val().length+"/"+characterSize);
	if($("#message").val().length>characterSize){
		$(".warningText").css({"color":"red"});
	}
	else{
		$(".warningText").css({"color":"black"});
	}
});


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
			//dropZone.hide();
			dropZone.removeClass("dropZActive");
		}
		return false;
	});
	dropZone[0].ondragover = function() {
		dropZone.addClass("dropZActiveFocus");
			return false;
	};
	dropZone[0].ondragleave = function() {
		dropZone.removeClass("dropZActiveFocus");
			return false;
	};
	
	dropZone[0].ondrop = function(event) {
		event.preventDefault();
		let files = event.dataTransfer.files;
		for(let i = 0; i<files.length ;i++){
			if ('size' in files[i]) {
                var fileSize = files[i].size;
                
            }
            else {
                var fileSize = files[i].fileSize;
            }
			if(fileSize<fileInp){
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
			else{
				alert("Ошибка. Размер файла превышен на "+(fileSize-fileInp)+" байт, максимальный размер файла = " + fileInp/(1073741824) + " гб / " + fileInp + "байт");
			}
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
	let files = $(this)[0].files;
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
		$(this)[0].val(null);
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
	if((data != undefined) && (data.newMessage == 0)){
		for(let mes=0; mes < data.messages.length; mes++){
			if(data.messages[ mes ].text != null){
				if((minMassageId > data.messages[ mes ].id) || (minMassageId == -1)){
					minMassageId = data.messages[ mes ].id;
				}
				if((maxMassageId < data.messages[ mes ].id)){
					maxMassageId = data.messages[ mes ].id;
				}
				let text0 = data.messages[ mes ].text;
				let mesIndex = 0;
				if(data.old == false){
					mesIndex = newMessageIndex;
					newMessageIndex++;
				}
				else{
					mesIndex = oldMessagesIndex;
					oldMessagesIndex--;
				}
				
				//создаем сообщение
				let messageBody=$("<table/>").addClass("selectionTextQuote");
				let dateTr0=$("<div/>");
				dateTr0.addClass("datetr").append(data.messages[ mes ].dateTime);
				messageBody.attr("id", "messBody"+mesIndex);
				messageBody.attr("mesId", data.messages[ mes ].id);
				messageBody.attr("messageIndex", mesIndex);
				messageBody.append($("<tr/>").append($("<td/>").append(dateTr0).append($("<div/>").addClass("author").text(data.messages[ mes ].author + ":")).append($("<div/>").addClass("messageText").attr("textMessagId", data.messages[ mes ].id).attr("id","messageText"+mesIndex).append(text0))));
				
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
				addReplyA0.attr("id", "messReply"+mesIndex);
				addReplyA0.attr("messageIndex", mesIndex)
				addReplyA0.attr("mesId", data.messages[ mes ].id);
				addReplyA0.attr("flagAttach", "false");
				addReplyA0.text(" Ответить");
				addReplyA0.click(messReplyClickFunction);
				
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
					fileTr0.append($("<td/>").append(fileTable));
					messageBody.append(fileTr0);
				}
				messageBody.append($("<tr/>").append($("<td/>").append($("<div/>").append(addReplyA0))));
				
				// админ
				if($("#modal-body").attr("admin") == "true"){
					let deleterMessageA0 = $("<a/>");
					deleterMessageA0.addClass("deleterMessage");
					deleterMessageA0.attr("id", "messDeleter"+(mesIndex));
					deleterMessageA0.attr("mesId", data.messages[ mes ].id);
					deleterMessageA0.text("x");
					deleterMessageA0.click(deleteMessage);
					messageBody.prepend($("<tr/>").append($("<td/>").append(deleterMessageA0)));
					
				}
				//редактирование сообщения кнопка
				if(data.coreUser == true){ //исправить в сокете, что бы давалось сообщениям???
					let editMessage0 = $("<a/>");
					editMessage0.attr("id", "messEdit"+(mesIndex));
					editMessage0.attr("mesId", data.messages[ mes ].id);
					editMessage0.attr("mesIndex", mesIndex);
					editMessage0.text("редактировать");
					editMessage0.click(editMessage);
					messageBody.append($("<tr/>").append($("<td/>").append(editMessage0)));
				}
				// конец
				
				// установка сообщения
				if(data.old == false){
					if(switchCheak == 0){// +1 непрочитанное сообщение
						updatenumberNewMessages(numberNewMessages + 1);
						messageBody.addClass("newMessageClass");
						$("#modal-body").append(messageBody);
					}
					else{
						if($("#modal-body").scrollTop() >= $("#modal-body")[0].scrollHeight - $("#modal-body")[0].clientHeight){
							$("#modal-body").append(messageBody);
							updatenumberNewMessages(0);
							currentMessageId = maxMassageId;
							updateLastReadMessage();
							newMessagesHeight += getElmHeight(messageBody);
							$("#modal-body").scrollTop($("#modal-body")[0].scrollHeight);
						}
						else{
							messageBody.addClass("newMessageClass");
							$("#modal-body").append(messageBody);
							updatenumberNewMessages(numberNewMessages + 1);
						}
					}
				}
				else{
					$("#modal-body").children().first().after(messageBody);
					newMessagesHeight += getElmHeight(messageBody);
				}
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
				newMessReply.append($("<td/>").text("прикрепленное сообщение:" + $("#messageText" + $(this).attr("messageIndex")).text()));
				let deleteMessReplyButton = $("<button/>");
				deleteMessReplyButton.text("X");
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

//редактирование сообщений
function editMessage(){
	editMessageId = $(this).attr("mesId");
	editMessageFlag=true;
	$("#message").val($("#messageText"+$(this).attr("mesIndex")).text());
}

function nextStepLoadFile(messageId, FileIndex){
	progressBar.show();
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
			progressBar.hide();
		};
		xhr.upload.onprogress = function(event) {
			progressBar.text(event.loaded + ' / ' + event.total);
		  }
		xhr.open("post", "/wfe/chatFileInput" + "?fileName=" + attachedFiles[FileIndex].name + "&messageId=" +messageId + "&endFlag=true", false);
		xhr.send(form0);
	}
	else{
		xhr.onload = function() {
			FileIndex++;
			nextStepLoadFile(messageId, FileIndex);
		};
		xhr.upload.onprogress = function(event) {
			progressBar.text(event.loaded + ' / ' + event.total);
		  }
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
	else if(message0.messType == "editMessage"){
		let mesSelector = $("[textMessagId='"+message0.mesId+"']");
		if((mesSelector != null) && (mesSelector != undefined)){
			mesSelector.text(message0.newText);
		}
	}
}

//перемещение окна
var windowChat = $('.modal-content')[0];
var tagetDrug = $('#modal-header-dragg')[0];
var dragMaster = (function() {

    var dragObject
    var dragTarget
	var mouseOffset

	// получить сдвиг target относительно курсора мыши
	function getMouseOffset(target, e) {
		var docPos	= getPosition(target)
		return {x:e.pageX - docPos.x, y:e.pageY - docPos.y}
	}

	function mouseUp(){
        dragTarget = null
		// очистить обработчики, т.к перенос закончен
		document.onmousemove = null
		document.onmouseup = null
		document.ondragstart = null
		document.body.onselectstart = null
	}

	function mouseMove(e){

		with(dragObject.style) {
			position = 'absolute'
			if (mouseOffset.y <= e.pageY){ 
					if(e.pageY<($('body').height()+$('.modal-content').height()))
					top = e.pageY - mouseOffset.y + 'px'
			}
			if (mouseOffset.x <= e.pageX){
				let a=$('body').width();
				if(e.pageX<($('body').width()))
				left = e.pageX - mouseOffset.x + 'px'
			}
		}
		return false
	}

	function mouseDown(e) {
		if (e.which!=1) return		
		
		dragTarget  = this

		// получить сдвиг элемента относительно курсора мыши
		mouseOffset = getMouseOffset(this, e)

		// эти обработчики отслеживают процесс и окончание переноса
		document.onmousemove = mouseMove
		document.onmouseup = mouseUp

		// отменить перенос и выделение текста при клике на тексте
		document.ondragstart = function() { return false }
		document.body.onselectstart = function() { return false }

		return false
	}

	return {
		dragWindow: function(element1,element2){
            element1.onmousedown = mouseDown;
            dragObject=element2;
		}
	}

}())

function getPosition(e){
	//сдвиг  области для перемещения
	var offsetL=  - $('#modal-header-dragg').position().left;
	var left = offsetL;
	var top  = 0

	while (e.offsetParent){
		left += e.offsetLeft
		top  += e.offsetTop
		e	 = e.offsetParent
	}

	left += e.offsetLeft
	top  += e.offsetTop

	return {x:left, y:top}
}
dragMaster.dragWindow(tagetDrug,windowChat);
// конец
});