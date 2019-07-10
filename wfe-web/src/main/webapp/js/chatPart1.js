$(document).ready(function() {
	var attachedPosts=[];
	//флаг развернутого чата (0 - свернут, 1 - развернут)
	var switchCheak=0;
	var chatForm=document.getElementById('ChatForm');
	//кнопка открытия чата
	var btnOpenChat = document.getElementById("openChatButton");
	
	var btnLoadNewMessage=document.getElementById("loadNewBessageButton");
	var btn2 = document.getElementById("btnSend");
	var btnOp=document.getElementById("btnOp");
	var imgButton=document.getElementById("imgButton");
	//флаг обозначающий состояние(развернут или свернут)ли чат
	let flagRollExpandChat=0;
	//закрытие (сворачивание) чата
	var span = document.getElementById("close");
	//нумерация сообщений = количество загруженных сообщений, если небыло удалений
	var lastMessageIndex=0;
	var minMassageId = -1;
	var maxMassageId = -1;
	var currentMessageId = -1;
	var numberNewMessages = 0;
	
	var blocOldMes=0;
	//тест сокет
	var chatSocketURL = "ws://" + document.location.host + "/wfe/chatSoket?chatId="+$('#ChatForm').attr('chatId');
	var chatSocket = new WebSocket(chatSocketURL);
	chatSocket.onmessage = onMessage;
	function newxtMessages(count0){
		   let newMessage={};
		   newMessage.chatId=$('#ChatForm').attr('chatId');
		   newMessage.type="getMessages";
		   newMessage.lastMessageId=minMassageId;
		   newMessage.Count = count0; //количество начальных сообщений
		   let firstMessages = JSON.stringify(newMessage);
		   chatSocket.send(firstMessages);
	}
	//функция пишущая кол-во непрочитанных сообщений = numberNewMessages
	function updatenumberNewMessages(numberNewMessages0){ //доделать в конец функции вывод numberNewMessages!
		numberNewMessages = numberNewMessages0;
	}
	chatSocket.onopen=function(){
		//запрос 20 последних сообщений
		newxtMessages(20);
	    //запрос текущей информации по юзеру
		   let newMessage2={};
		   newMessage2.chatId=$('#ChatForm').attr('chatId');
		   newMessage2.type="getChatUserInfo";
		   let sendObject0 = JSON.stringify(newMessage2);
		   chatSocket.send(sendObject0);
	}
	chatSocket.onclose = function(){
		$(".modal-body").append("<table ><td>" + "потерянно соединение с чатом сервера"+ "</td></table >");
	}
//-----------
	btnLoadNewMessage.onclick=function(){
		if(blocOldMes==0){
		   blocOldMes=1;
		//запрос 20 сообщений старых
		   newxtMessages(20);
		}
	}
	//кнопка открытия чата
	btnOpenChat.onclick = function() {
		if(chatForm!=null){
			chatForm.style.display = "block";
			switchCheak=1;
			//"в прочитанные все"
			currentMessageId = maxMassageId;
			numberNewMessages=0;
			let newSend0={};
			newSend0.chatId=$('#ChatForm').attr('chatId');
			newSend0.type="setChatUserInfo";
			newSend0.currentMessageId=currentMessageId;
			let sendObject0 = JSON.stringify(newSend0);
			chatSocket.send(sendObject0);
		}
	}
	//закрытие (сворачивание) чата
	span.onclick = function() {
		chatForm.style.display = "none";
		switchCheak=0;
	}
      	     
   $('#btnCl').hide();
   var inputH=document.getElementById("message");
   var heightModalC=$('.modal-content').height();
   var widthModalC=$('.modal-content').width();
   
   /*
	$('textarea').keypress(function(e) {
	    if (e.which == 13){
	    var text = $(this).val();
		$(this).val(text + '\n ');
	    return false;
	    }
	});*/
   btnSend.onclick=function send() {
   let message = document.getElementById("message").value;
   message = message.replace(/\n/g, "<br/>");
   let idHierarchyMessage="";

   for(var i=0;i<attachedPosts.length;i++){
	   idHierarchyMessage+=attachedPosts[i]+":";
   }
   //сокет
   let newMessage={};
   newMessage.message=message;
   newMessage.chatId=$('#ChatForm').attr('chatId');
   newMessage.idHierarchyMessage = idHierarchyMessage;
   newMessage.type="newMessage";
   chatSocket.send(JSON.stringify(newMessage));
   //чистим "ответы"
   let addReplys0 = document.getElementsByClassName("addReply");
   for(let i=0; i<addReplys0.length; i++){
		$(addReplys0[i]).text("Ответить");
		$(addReplys0[i]).attr("flagAttach", "false");
   }
   //
   attachedPosts=[];
   }
  
   //кнопка развернуть/свернуть чат
   btnOp.onclick=function(){
	   if(flagRollExpandChat==0){
		flagRollExpandChat=1;
	   	$('.modal-content').css({
	   		width: widthModalC+300,
	   	    height: heightModalC+300,
	   	});
	   	
	   	$('.modal-body').css({
	   		height:'630px',
	   	    width: '590px',
	   	     
	   	});
		$('.modal-header').css({
	   		
	   	    width: '600px',
	   	});
		$('.modal-header-dragg').css({
	   		
	   	    width: '530px',
	   	});
		$('.modal-footer').css({
	   		
	   	    height: '80px',
	   	});
		imgButton.src="/wfe/images/chat_expand.png";
	   }else if(flagRollExpandChat==1){
		   
		   flagRollExpandChat=0;
		   $('.modal-content').css({
				 width: '346px',
				 height: '506px',
		   	});
		  
			$('.modal-body').css({
				width: '304px',
		   	    height: '396px',
		   	});	
			$('.modal-header').css({
		   		
		   	    width: '316px',
		   	});
			$('.modal-header-dragg').css({
		   		
		   	    width: '220px',
		   	});
			$('.modal-footer').css({
		   		
		   	    height: '53px',
		   	});
			imgButton.src="/wfe/images/chat_roll_up.png";
	   }
   }
   
   function hierarhyCheak(messageId){
	 let urlString = "/wfe/ajaxcmd?command=GetHierarhyLevel&chatId="+$('#ChatForm').attr('chatId')+"&messageId="+messageId;	
	 return $.ajax({
			type: "POST",
			url: urlString,
			dataType: "json",
			contentType: "application/json; charset=UTF-8",
			processData: false,
			success: function(data) {
				let ajaxRet="";
				if(data.newMessage==0){
					for(let mes=0;mes<data.messages.length;mes++){
						if(data.messages[mes].text !=null){
							let messageBody="<table class=\"quote\"><tr class=\"selectionTextAdditional\"><td>Цитата:"+data.messages[mes].author+"</td></tr><tr><td>"+ data.messages[mes].text ;
							let hierarhyMass="";
							//тут получаем id вложенных
							if(data.messages[mes].hierarchyMessageFlag==1){
								hierarhyMass+="<tr><td><a class=\"openHierarchy\" mesId=\""+data.messages[mes].id+"\" loadFlag=\"0\" openFlag=\"0\">Развернуть</a><div class=\"loadedHierarchy\"></div></td></tr>";
							}
							messageBody+="</td></tr>" + hierarhyMass;
							messageBody+= "</table >";
							
							ajaxRet=ajaxRet+messageBody;
							
						}
				}
					return ajaxRet;
			}//if(data.newMessage==0) конец
			}
	 });
   }
   
   function addOnClickHierarchyOpen(){
	   let elements = $(".openHierarchy");
		   elements.off().on( "click", function(event){
			   if($(this).attr("openFlag")==1){
				   let thisElem=$(".openHierarchy")[0];
				   $(this).next(".loadedHierarchy")[0].style.display="none";
					$(this).attr("openFlag","0");
					$(this).text("Развернуть");
					return 0;
			   }
			   else{
				   let thisElem=$(".openHierarchy")[0];
					if($(this).attr("loadFlag")==1){
						$(this).next(".loadedHierarchy")[0].style.display="block";
						$(this).attr("openFlag","1");
						$(this).text("Свернуть");
						return 0;
					}else{
						let thisElem=$(".openHierarchy")[0];
						let element=this;
						hierarhyCheak($(element).attr("mesId")).then(ajaxRet=>{
							$(this).next(".loadedHierarchy").append(getMessagesText(ajaxRet));						
							addOnClickHierarchyOpen();
							$(element).attr("loadFlag", "1");
							$(this).attr("openFlag","1");
							$(this).text("Свернуть");
							return 0;
						});
					}
				}
			});
   }
	function getMessagesText(data) {
		let ajaxRet="";
		if(data.newMessage==0){
			for(let mes=0;mes<data.messages.length;mes++){
				if(data.messages[mes].text !=null){
					let messageBody="<table class=\"quote\"><tr class=\"selectionTextAdditional\"><td>Цитата:"+data.messages[mes].author+"</td></tr><tr><td>"+ data.messages[mes].text ;
					let hierarhyMass="";
					//тут получаем id вложенных
					if(data.messages[mes].hierarchyMessageFlag==1){
						hierarhyMass+="<tr><td><a class=\"openHierarchy\" mesId=\""+data.messages[mes].id+"\" loadFlag=\"0\" openFlag=\"0\">Развернуть</a><div class=\"loadedHierarchy\"></div></td></tr>";
					}
					messageBody+="</td></tr>" + hierarhyMass;
					messageBody+= "</table >";
					ajaxRet=ajaxRet+messageBody;
					
				}
		}
			return ajaxRet;
	}//if(data.newMessage==0) конец
	}
	
	//функция приёма сообщения
	function addMessages(data){
		if(data.newMessage==0){
			for(let mes=0;mes<data.messages.length;mes++){
				if(data.messages[mes].text !=null){
					if((minMassageId>data.messages[mes].id) || (minMassageId==-1)){
						minMassageId=data.messages[mes].id;
					}
					
					if((maxMassageId<data.messages[mes].id)){
						maxMassageId=data.messages[mes].id;
					}
					
					let text0 = data.messages[mes].text;
					text0.replace(/(?[^\.*])\n/ig,"<br/>");
					var messageBody="<table class=\"selectionTextQuote\"><tr><td><div class=\"author\" class=\"author\">"+data.messages[mes].author+"</div> :"+text0 ;
					var hierarhyMass="";
					//тут получаем id вложенных
					if(data.messages[mes].hierarchyMessageFlag==1){
						hierarhyMass+="<tr><td><a class=\"openHierarchy\" mesId=\""+data.messages[mes].id+"\" loadFlag=\"0\" openFlag=\"0\">Развернуть</a><div class=\"loadedHierarchy\"></div></td></tr>";
					}
					//"развернуть"
					messageBody+="</td></tr>" + hierarhyMass;
					//"ответить"
					messageBody+= "<tr><td><hr class=\"hr-dashed\">"+ data.messages[mes].dateTime + "<hr class=\"hr-dashed\"></td><td><div class=\"hr-dashed-vertical\"><a class=\"addReply\" id=\"messReply"+(lastMessageIndex)+"\" mesId=\""+data.messages[mes].id+"\" flagAttach=\"false\"> Ответить</a></div></td></tr>";
					//админ
					if($(".modal-body").attr("admin")=="true"){
						messageBody+="<tr><td>"+"<a class=\"deleterMessage\" id=\"messDeleter"+(lastMessageIndex)+"\" mesId=\""+data.messages[mes].id+"\">удалить</a>"+"</td></tr>";
					}
					//конец
					messageBody+="</table >";
					
					if(data.old == 0){
						$(".modal-body").append(messageBody);
						if(switchCheak==0){//+1 непрочитанное сообщение
							updatenumberNewMessages(numberNewMessages+1);
						}
					}
					else{
						$(".modal-body").children().first().after(messageBody);
					}
					
					document.getElementById("messReply"+(lastMessageIndex/*+mes*/)).onclick=function(){
						if($(this).attr("flagAttach")=="false"){
							attachedPosts.push($(this).attr("mesId"));
							$(this).attr("flagAttach", "true");
							$(this).text("Отменить");
						}
						else{
							$(this).text("Ответить");
							$(this).attr("flagAttach", "false");
							let pos0 = attachedPosts.indexOf($(this).attr("mesId"), 0);
							attachedPosts.splice(pos0, 1);
						}
					}
					document.getElementById("messDeleter"+(lastMessageIndex/*+mes*/)).onclick=deleteMessage;			
					addOnClickHierarchyOpen();
					lastMessageIndex+=1;
				}
			}
		}//if(data.newMessage==0) конец
	}
	//бновление свернутого чата, пока не работает (30,05,20019)
	function addNewMessagesCount(data){
		if(data.newMessageCount>0){
			 document.getElementById("indicateNewMessage").append("<td>ok</td>");
		}
	}
	//удаление сообщений
	function deleteMessage(){
		   //сокет
		if(confirm("Вы действительно хотите удалить сообщение? Отменить это действие будет невозможно")){
		   let newMessage={};
		   newMessage.messageId=$(this).attr("mesId");
		   newMessage.chatId=$('#ChatForm').attr('chatId');
		   newMessage.type="deleteMessage";
		   chatSocket.send(JSON.stringify(newMessage));
		   $(this).parent().parent().parent().parent().remove();
		}
	}
	//тест сокет
	//приём с сервера
	function onMessage(event) {
		let messsage0 = JSON.parse(event.data);
		if(messsage0.messType=="newMessages"){
			addMessages(messsage0);
		}
		else if(messsage0.messType=="deblocOldMes"){
			blocOldMes=0;
		}
		else if(messsage0.messType=="ChatUserInfo"){
			if(switchCheak == 0){
				if(currentMessageId<messsage0.lastMessageId)
					currentMessageId = messsage0.lastMessageId;
				updatenumberNewMessages(messsage0.numberNewMessages);
				//дозапрос всех непрочитанных сообщений
				if(numberNewMessages>lastMessageIndex)
					newxtMessages(numberNewMessages-lastMessageIndex);
			}
		}
	}
	//конец
   });