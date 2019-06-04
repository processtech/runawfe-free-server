$(document).ready(function() {
	var attachedPosts=[];
	var switchCheak=0;
	var chatForm=document.getElementById('ChatForm');
	var btn = document.getElementById("openChatButton");
	var btn2 = document.getElementById("btnSend");
	var btnOp=document.getElementById("btnOp");
	var imgButton=document.getElementById("imgButton");
	var flag=1;
	var span = document.getElementById("close");
	var lastMessageId=0;
	//тест сокет
	var chatSocket = new WebSocket("ws://localhost:8080/wfe/actions");
	chatSocket.onmessage = onMessage;
	chatSocket.onclose = function(){
		$(".modal-body").append("<table ><td>" + "потерянно соединение с чатом сервера"+ "</td></table >");
	}
//-----------
	btn.onclick = function() {
		if(chatForm!=null){
			chatForm.style.display = "block";
			switchCheak=1;
		
		}
	}

	span.onclick = function() {
		chatForm.style.display = "none";
		switchCheak=0;
	}
      	     
   $('#btnCl').hide();
   var inputH=document.getElementById("message");
   var heightModalC=$('.modal-content').height();
   var widthModalC=$('.modal-content').width();
   inputH.style.width = "250px";
   inputH.style.height = "25px";
   btnSend.onclick=function send() { 
   let message = document.getElementById("message").value;
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
   attachedPosts=[];
   }
  
   btnOp.onclick=function(){
	   if(flag==1){
		flag=0;
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
		
		inputH.style.width = "600px";
		inputH.style.height = "70px";
		imgButton.src="/wfe/images/chat_roll_up.png";
	   }else if(flag==0){
		   imgButton.src="/wfe/images/chat_expand.png";
		   flag=1;
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
			inputH.style.width = "250px";
			inputH.style.height = "25px";
			//$('#btnOp').show();
		   	//$('#btnCl').hide();
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
								hierarhyMass+="<tr><td><a class=\"openHierarchy\" mesNumber=\""+data.messages[mes].id+"\" loadFlag=\"0\" openFlag=\"0\">Развернуть</a><div class=\"loadedHierarchy\"></div></td></tr>";
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
						//$(this).next(".loadedHierarchy")[0].append(hierarhyCheak($(this).attr("mesNumber")));
						hierarhyCheak($(element).attr("mesNumber")).then(ajaxRet=>{
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
	   //}
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
						hierarhyMass+="<tr><td><a class=\"openHierarchy\" mesNumber=\""+data.messages[mes].id+"\" loadFlag=\"0\" openFlag=\"0\">Развернуть</a><div class=\"loadedHierarchy\"></div></td></tr>";
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
					var messageBody="<table class=\"selectionTextQuote\"><tr><td>"+data.messages[mes].author+":"+data.messages[mes].text ;
					var hierarhyMass="";
					//тут получаем id вложенных
					if(data.messages[mes].hierarchyMessageFlag==1){
						hierarhyMass+="<tr><td><a class=\"openHierarchy\" mesNumber=\""+data.messages[mes].id+"\" loadFlag=\"0\" openFlag=\"0\">Развернуть</a><div class=\"loadedHierarchy\"></div></td></tr>";
					}
					messageBody+="</td></tr>" + hierarhyMass;
					messageBody+= "<tr><td>"+ data.messages[mes].dateTime + "</td><td><a class=\"addReply\" id=\"messReply"+(lastMessageId)+"\" mesNumber=\""+data.messages[mes].id+"\"> Ответить</a></td></tr></table >";
					$(".modal-body").append(messageBody);
					document.getElementById("messReply"+(lastMessageId+mes)).onclick=function(){
						attachedPosts.push($(this).attr("mesNumber"));
					}
					addOnClickHierarchyOpen();
					lastMessageId+=1;
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
	//тест сокет
	//приём с сервера
	function onMessage(event) {
		
		let messsage0 = JSON.parse(event.data);
		if(messsage0.messType=="newMessages"){
			addMessages(messsage0);
		}
	}
	
	//конец
   });