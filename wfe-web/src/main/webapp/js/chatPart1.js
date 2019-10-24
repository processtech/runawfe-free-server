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
		$(".modal-body").scrollTop(0);
		for(let i=1; i<data.messages.length; i++){
			addMessages(data.messages[i]);
		}
		if(numberNewMessages == 0)
			newMessagesHeight = $("#modal-body").height();
		chatSocketURL = "ws://" + document.location.host + "/wfe/chatSoket?chatId=" + $("#ChatForm").attr("chatId");
		chatSocket = new WebSocket(chatSocketURL);
		chatSocket.onmessage = onMessage;
		//действия при открытии сокета
		chatSocket.onopen=function(){
		}
		//действия при закрытии сокета
		chatSocket.onclose = function(){
			$(".modal-body").append("<table ><td>" + "потерянно соединение с чатом сервера" + "</td></table >");
		}
		//установка скрол-функции отслеживания непрочитанных
		$("#modal-body").bind("load scroll", scrollNewMessages);
	}
});
$("#btnCl").hide();

//скрол-функция отслеживания непрочитанных
function scrollNewMessages(){
	if($("#modal-body").scrollTop() > newMessagesHeight){
		//пересекли черту
		let scrollTop0 = $("#modal-body").scrollTop();
		let newIndex = newMessageIndex - numberNewMessages;
		let i = newIndex;
		for(; i<numberNewMessages; i++){
			let message0 = $("#messBody" + i);
			if(message0.offset().top < scrollTop0){
				newMessagesHeight += $("#messBody" + i).height();
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
		//прокрутка
		$("#messReply"+(newMessageIndex - numberNewMessages)).scrollView(".modal-body");
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
			//ToDO
			//$("#messReplyTable").html("<img src=\"http://lh5.ggpht.com/-eglPTUEmd7I/UIePRUwEfvI/AAAAAAAAAEw/dkL3SmB7z7A/s9000/beautiful%2Bnature%2B2.jpg\" class=\"image\" />");
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
var heightModalContent=($(window).height()-$(".modal-content").height())*0.8;
btnOp.onclick=function(){
	if(flagRollExpandChat == 0){
		flagRollExpandChat=1;
		$(".modal-content").css({
			width: $(".modal-content").width() + 300,
			height: $(".modal-content").height() + heightModalContent,
		});
		$(".modal-body").css({
			width: $(".modal-body").width() + 300,
			height: ($(".modal-content").height() + heightModalContent)*0.50,
		});
		$("#attachedArea").css({
			"margin-top":"26px",
			height: $("#attachedArea").height() + 50,

			width: 90.5+"%",
		});
		$(".modal-header-dragg").css({
			width: $(".modal-header-dragg").width() + 300,
		});
		$(".modal-footer").css({
			height: $(".modal-footer").height() + 30,
		});
		$('.messageUserMention').css({
			"margin-top" : (-1)*$('#message').height()+"px",
			height: 90+"px",	
			width: 212+"px",
		})
		dropZone.css({
			width: 90.5+"%",
			height: $(".modal-footer").height() + 30,
		});
		imgButton.src="/wfe/images/chat_expand.png";
	}else if(flagRollExpandChat == 1){
		flagRollExpandChat=0;
		$(".modal-content").css({
			width: $(".modal-content").width() - 300,
			height: $(".modal-content").height() - heightModalContent,
		});

		$(".modal-body").css({
			width: $(".modal-body").width() - 300,
			height: ($(".modal-content").height())*0.65,
		});

		$("#attachedArea").css({
			height: $("#attachedArea").height() - 50,
			width: 87.5+"%",
		});
		
		$(".modal-header-dragg").css({
			width: $(".modal-header-dragg").width() - 300,
		});
		$(".modal-footer").css({
			height: $(".modal-footer").height() - 30,
		});
		$('.messageUserMention').css({
			"margin-top" : (-1) * $('#message').height()+10+"px",
			height: 50+"px",
			
			width: 225+"px",
		});
		dropZone.css({
			width: 87.5+"%",
		});
		imgButton.src="/wfe/images/chat_roll_up.png";
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

$("#message").keydown(function keyupUserNames(event){//не забыть оптимизировать if на проверки (userNamePositionFlag == true) !!!
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
				messageBody.attr("id", "messBody"+mesIndex);
				messageBody.attr("mesId", data.messages[ mes ].id);
				messageBody.attr("messageIndex", mesIndex);
				messageBody.append($("<tr/>").append($("<td/>").append($("<div/>").addClass("author").text(data.messages[ mes ].author + ":")).append($("<div/>").addClass("messageText").attr("textMessagId", data.messages[ mes ].id).attr("id","messageText"+mesIndex).append(text0))));
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
					deleterMessageA0.attr("id", "messDeleter"+(mesIndex));
					deleterMessageA0.attr("mesId", data.messages[ mes ].id);
					deleterMessageA0.text("удалить");
					deleterMessageA0.click(deleteMessage);
					messageBody.append($("<tr/>").append($("<td/>").append(deleterMessageA0)));
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
						$(".modal-body").append(messageBody);
						//
						//newMesViewChacker(messageBody);
						/*
						messageBody.viewportChecker({
							classToAdd: "AddedClass",
							classToRemove: "newMessageClass",
							repeat: false,
							callbackFunction: function(elem, action){
								if (action === 'add') { // Если класс добавлен
									$(elem).addClass("InViewport");
									$(elem).removeClass("newMessageClass");
									currentMessageId = $(elem).attr("mesId");
									updatenumberNewMessages(newMessageIndex -1 - $(elem).attr("messageIndex"));
									updateLastReadMessage();
									$(elem).unbind("viewportChecker");
								}
							}
						});
						*/
					}
					else{
						if($(".modal-body").scrollTop() >= $(".modal-body")[0].scrollHeight - $(".modal-body")[0].clientHeight){
							$(".modal-body").append(messageBody);
							updatenumberNewMessages(0);
							currentMessageId = maxMassageId;
							updateLastReadMessage();
						}
						else{
							messageBody.addClass("newMessageClass");
							$(".modal-body").append(messageBody);
							updatenumberNewMessages(numberNewMessages + 1);
							
							//
							//newMesViewChacker(messageBody);
							/*
							messageBody.viewportChecker({
								classToAdd: "AddedClass",
								classToRemove: "newMessageClass",
								repeat: false,
								callbackFunction: function(elem, action){
									if (action === 'add') { // Если класс добавлен
										$(elem).addClass("InViewport");
										$(elem).removeClass("newMessageClass");
										currentMessageId = $(elem).attr("mesId");
										updatenumberNewMessages(newMessageIndex -1 - $(elem).attr("messageIndex"));
										updateLastReadMessage();
										$(elem).unbind("viewportChecker");
									}
								}
							});
							*/
							
							// 
						}
					}
				}
				else{
					$(".modal-body").children().first().after(messageBody);
					newMessagesHeight += messageBody.height();
				}
			}
		}
	}
}

//функция подключения непрочитанных
/*
function newMesViewChacker(mesBody){
	if(newMessagesHeight < mesBody.offset().top){
		newMessagesHeight = mesBody.offset().top;
	}
}
*/

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
// конец
});