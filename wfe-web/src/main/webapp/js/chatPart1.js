
var modal = document.getElementById('myModal');
var btn = document.getElementById("myBtn");
var btn2 = document.getElementById("btnSend");
var btnOp=document.getElementById("btnOp");


        var span = document.getElementsByClassName("close")[0];
        //var message = document.getElementById("message").value;


        btn.onclick = function() {
            modal.style.display = "block";
        }
        
        btnOp.onclick=function(){
        	alert('Ok');
        	$('.modal').css({
        		height:'400px',
        	    width: '400px'
        	});
        }




        function Reload() {
            var src = $(".modal-body").attr("src");
            var pos = src.indexOf('timestamp');
            if (pos >= 0) {
                src = src.substr(0, pos);
            } else {
                src = src + '&';
            }
            src = src + "timestamp=" + new Date().getTime();
            $(".modal-body").attr("src", src);
        }
        btn2.onclick=function send() {
            //var message = document.getElementById("message").value;

           // alert(message);
            //$(".modal-body").append("<table ><tr><td>"+ message + "</td></tr></table >");

           // $(window).load(function() {
           //     window.setInterval("Reload()",1000);
           // });




            //document.getElementById('modal-body').innerHtml = newContent;
            var name = "Гость";
            /* Здесь блок отправки POST-запроса с данными (например, через Ajax) */
        }


        span.onclick = function() {
            modal.style.display = "none";
        }

        window.onclick=function (ev) {
            if(ev.target==modal){
                modal.style.display="none";
            }
        }

      //функция свернуть/развернуть большие переменные
      