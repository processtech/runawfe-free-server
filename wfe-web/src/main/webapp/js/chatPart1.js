$(document).ready(function() {
	var attachedPosts=[];
	var switchCheak=0;
	var chatForm=document.getElementById('ChatForm');
	var btn = document.getElementById("myBtn");
	var btn2 = document.getElementById("btnSend");
	var btnOp=document.getElementById("btnOp");
	var flag=1;
	        	var span = document.getElementById("close");
	        	//var message = document.getElementById("message").value;

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
   var message = document.getElementById("message").value;
   var urlString = "/wfe/ajaxcmd?command=SendChatMessage&message="+message+"&chatId="+$('#ChatForm').attr('chatId');
   var today = new Date();
   var date = today.getFullYear()+'-'+(today.getMonth()+1)+'-'+today.getDate();
   var time = today.getHours() + ":" + today.getMinutes() + ":" + today.getSeconds();
   var dateTime = date+' '+time;   
   var shipmentСounter=0;
   function ajaxSendMessage(urlString,counter){
	   $.ajax({
			type: "POST",
			url: urlString,
			dataType: "json",
			contentType: "application/json; charset=UTF-8",
			processData: false,
			success: function(data) {
				if(data.text !=null)
					if(data.text!=0){
						counter++;
						if(counter>3){
							ajaxSendMessage(urlString,counter);
						}else {
							$(".modal-body").append("<table ><td>Error id:" + data.text+ "</td><tr><td>"+ dateTime + "</td></tr></table >");
						}
					}
			}
		});
   }
   ajaxSendMessage(urlString,shipmentСounter);
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
		inputH.style.width = "600px";
		inputH.style.height = "70px";
	   	//$('#btnOp').
	   	//$('#btnCl').show();
	   }else if(flag==0){
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
			inputH.style.width = "250px";
			inputH.style.height = "25px";
			//$('#btnOp').show();
		   	//$('#btnCl').hide();
	   }
	   	}
   //btnCl.onclick=function(){}
   
   function chatCheckCycle(){
	   var promise = new Promise(function(resolve, reject) {
		   var urlString = "/wfe/ajaxcmd?command=GettingChatMessage&chatId="+$('#ChatForm').attr('chatId')+"&lastMessageId="+0;
		   var lastMessageId=0;
	
		   //for(;;)
		   setInterval(() => {
			
		
		   {
			   if(switchCheak==1){
			   urlString = "/wfe/ajaxcmd?command=GettingChatMessage&chatId="+$('#ChatForm').attr('chatId')+"&lastMessageId="+lastMessageId;
				//$(".modal-body").append("<table ><tr><td>"+ lastMessageId + "</td></tr><tr><td>"+ /*dateTime +*/ "</td><td><a> Ответить</a></td></tr></table >");
			   $.ajax({
					type: "POST",
					url: urlString,
					dataType: "json",
					contentType: "application/json; charset=UTF-8",
					processData: false,
					success: function(data) {
						if(data.newMessage==1){
							if(data.lastMessageId!=null)
								lastMessageId=data.lastMessageId;
							if(data.text !=null){
								$(".modal-body").append("<table ><tr><td>"+ data.text + "</td></tr><tr><td>"+ data.dateTime + "</td><td><a id=\"messReply"+lastMessageId+"\" mesNumber=\""+lastMessageId+"\"> Ответить</a></td></tr></table >");
								document.getElementById("messReply"+lastMessageId).onclick=function(){
									attachedPosts.push($(this).attr("mesNumber"));
									for(var i=0;i<attachedPosts.length;i++){
										$(".modal-body").append("<table ><tr><td>"+ attachedPosts[i] + "</td></tr><tr><td>"+ /*dateTime +*/ "</td></tr></table >");
									}
									attachedPosts=[];
								}
							}
						}
					}
				});  
			   }else if(switchCheak==0){
				   urlString = "/wfe/ajaxcmd?command=CheckChatMessageIndicator";
				   document.getElementById("indicateNewMessage").append("<td>ok</td>");
					//$(".modal-body").append("<table ><tr><td>"+ lastMessageId + "</td></tr><tr><td>"+ /*dateTime +*/ "</td><td><a> Ответить</a></td></tr></table >");
				   $.ajax({
						type: "POST",
						url: urlString,
						dataType: "json",
						contentType: "application/json; charset=UTF-8",
						processData: false,
						success: function(data) {
							if(data.newMessageCount==0){
								
							}
						}
					}); 
			   }
		   }}, 1000);
		   //resolve(0);
		 });
   }
	chatCheckCycle();
 
   });



        

       