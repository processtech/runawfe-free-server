<%@ page import="java.util.List" %>
<%@ page import="org.json.simple.JSONArray" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="ru.runa.wfe.service.delegate.Delegates" %>
<%@ page import="ru.runa.wfe.user.User" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<%!
    private JSONArray getChatsMenuData(User user) {
        List<Long> chatIds = Delegates.getChatService().getActiveChatIds(user);
        List<Long> countMessages = Delegates.getChatService().getNewMessagesCounts(user, chatIds);
        JSONArray data = new JSONArray();
        for (int i = 0; i < countMessages.size(); i++) {
            JSONObject object = new JSONObject();
            object.put("processId", chatIds.get(i));
            object.put("numberOfMessages", countMessages.get(i));
            data.add(object);
        }
        return data;
    }
%>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
</head>
<body>
<script type="text/javascript">
    $(document).ready(function () {
        let chatsMenuButton = $('a:contains("!swich_chats!")');
        if (typeof (chatsMenuButton) != "undefined") {
            chatsMenuButton.html("Чаты").attr("id", "switch_chats");
            let divTableChats = $("<div/>");
            let divTableMain = $("<div/>");
            divTableMain.attr("id", "mainTableSwitchChats");
            divTableChats.attr("class", "modalSwitchingWindow");
            divTableMain.append(divTableChats);
            let divTableHeaderChats = $("<div/>");
            divTableHeaderChats.attr("class", "headTableSwitchChat")
            divTableHeaderChats.append("<div style='padding-left:5px;'>Переключение между чатами</div>");
            divTableMain.append(divTableHeaderChats);
            divTableMain.append(divTableChats);
            $("body > table > tbody > tr:nth-child(2) > td.systemMenu > table.tab > tbody").append(divTableMain);
            $('#switch_chats').click(function () {
                $(".modalSwitchingWindow").html();
                if ($("#mainTableSwitchChats").css("display") === "none") {
                    $("#mainTableSwitchChats").css({"display": "block"});
                } else {
                    $("#mainTableSwitchChats").css({"display": "none"});
                }
                return false;
            });
            initializeChatsMenu();
        }

        function initializeChatsMenu() {
            let data = JSON.parse('<%=getChatsMenuData(Commons.getUser(request.getSession()))%>');
            $(".modalSwitchingWindow").append("<tr><th class='list'>Список чатов</th><th  class='list'>Количество сообщений</th></tr>");
            let idRowListChats = $("<tr/>");
            idRowListChats.attr("id", 0);
            let numberOfUnreadMessages = $("<td/>").attr("class", "readMes");
            idRowListChats.append($("<td/>"));
            idRowListChats.append(numberOfUnreadMessages);
            for (let i = 0; i < data.length; i++) {
                let cloneIdRowListChats = idRowListChats.clone();
                cloneIdRowListChats.attr("id", "switchChat" + data[i].processId);
                cloneIdRowListChats.attr("processId", data[i].processId);
                let linkProcess = $("<a/>");
                linkProcess.attr("href", "/wfe/manage_process.do?id=" + data[i].processId);
                linkProcess.append("processId " + data[i].processId);
                cloneIdRowListChats.children().first().append(linkProcess);
                cloneIdRowListChats.children(".readMes").append(data[i].numberOfMessages);
                cloneIdRowListChats.children(".readMes").attr("id", "numberNewMessages" + data[i].processId)
                if (data[i].numberOfMessages > 0) {
                    cloneIdRowListChats.children(".readMes").attr("class", "newMessagesChatClass");
                } else {
                    cloneIdRowListChats.children(".readMes").attr("class", "noNewMessagesChatClass");
                }
                $(".modalSwitchingWindow").append(cloneIdRowListChats);
            }
            $(".modalSwitchingWindow td").addClass("list");
        }
    });
</script>
</body>
</html>
