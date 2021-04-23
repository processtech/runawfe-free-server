package ru.runa.wfe.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.user.Actor;

import static org.testng.Assert.assertTrue;

public class NewMessagesForProcessTableBuilderTest {

    private final int messageLimit = 3;
    private final Process process = Mockito.mock(Process.class);
    private final Actor actor = Mockito.mock(Actor.class);
    private final List<ChatMessageFile> files = new ArrayList<>();

    @DataProvider
    public Object[][] getEndMoreMessageData() {
        return new Object[][]{
                {1, "И еше 1 новое сообщение"}, {2, "И еше 2 новых сообщения"}, {3, "И еше 3 новых сообщения"}, {4, "И еше 4 новых сообщения"},
                {5, "И еше 5 новых сообщений"}, {6, "И еше 6 новых сообщений"}, {7, "И еше 7 новых сообщений"}, {8, "И еше 8 новых сообщений"},
                {9, "И еше 9 новых сообщений"}, {10, "И еше 10 новых сообщений"}, {11, "И еше 11 новых сообщений"}, {12, "И еше 12 новых сообщений"},
                {13, "И еше 13 новых сообщений"}, {14, "И еше 14 новых сообщений"}, {15, "И еше 15 новых сообщений"}, {16, "И еше 16 новых сообщений"},
                {17, "И еше 17 новых сообщений"}, {18, "И еше 18 новых сообщений"}, {19, "И еше 19 новых сообщений"}, {20, "И еше 20 новых сообщений"},

                {21, "И еше 21 новое сообщение"}, {22, "И еше 22 новых сообщения"}, {23, "И еше 23 новых сообщения"}, {24, "И еше 24 новых сообщения"},
                {25, "И еше 25 новых сообщений"},

                {31, "И еше 31 новое сообщение"}, {32, "И еше 32 новых сообщения"}, {33, "И еше 33 новых сообщения"}, {34, "И еше 34 новых сообщения"},
                {35, "И еше 35 новых сообщений"},

                {41, "И еше 41 новое сообщение"}, {42, "И еше 42 новых сообщения"}, {43, "И еше 43 новых сообщения"}, {44, "И еше 44 новых сообщения"},
                {45, "И еше 45 новых сообщений"},

                {51, "И еше 51 новое сообщение"}, {52, "И еше 52 новых сообщения"}, {55, "И еше 55 новых сообщений"},
                {61, "И еше 61 новое сообщение"}, {62, "И еше 62 новых сообщения"}, {65, "И еше 65 новых сообщений"},
                {71, "И еше 71 новое сообщение"}, {72, "И еше 72 новых сообщения"}, {75, "И еше 75 новых сообщений"},
                {81, "И еше 81 новое сообщение"}, {82, "И еше 82 новых сообщения"}, {85, "И еше 85 новых сообщений"},
                {91, "И еше 91 новое сообщение"}, {92, "И еше 92 новых сообщения"}, {95, "И еше 95 новых сообщений"},

                {100, "И еше 100 новых сообщений"}, {101, "И еше 101 новое сообщение"}, {102, "И еше 102 новых сообщения"},
                {103, "И еше 103 новых сообщения"}, {104, "И еше 104 новых сообщения"}, {105, "И еше 105 новых сообщений"},

                {110, "И еше 110 новых сообщений"}, {111, "И еше 111 новых сообщений"}, {112, "И еше 112 новых сообщений"},
                {113, "И еше 113 новых сообщений"}, {114, "И еше 114 новых сообщений"}, {115, "И еше 115 новых сообщений"},

                {200, "И еше 200 новых сообщений"}, {201, "И еше 201 новое сообщение"}, {202, "И еше 202 новых сообщения"},
                {203, "И еше 203 новых сообщения"}, {204, "И еше 204 новых сообщения"}, {205, "И еше 205 новых сообщений"},

                {1000, "И еше 1000 новых сообщений"}, {1001, "И еше 1001 новое сообщение"}, {1002, "И еше 1002 новых сообщения"},
                {1003, "И еше 1003 новых сообщения"}, {1004, "И еше 1004 новых сообщения"}, {1005, "И еше 1005 новых сообщений"},
        };
    }

    @Test(dataProvider = "getEndMoreMessageData")
    public void givenAndMoreMessageCount_thenShouldContainAndMoreTextDependingNumber(int andMoreMessageCount, String andMoreTextDependingNumber) {
        int messageCount = messageLimit + andMoreMessageCount;
        Map<ChatMessage, List<ChatMessageFile>> filesByMessages = new HashMap<>(messageCount);
        for (int i = 0; i < messageCount; i++) {
            ChatMessage message = new ChatMessage();
            message.setCreateActor(actor);
            message.setText(Integer.toString(i));
            filesByMessages.put(message, files);
        }
        NewMessagesForProcessTableBuilder builder = new NewMessagesForProcessTableBuilder("", messageLimit, process,
                "processName", filesByMessages);
        String message = builder.build(true);
        assertTrue(message.contains(andMoreTextDependingNumber));
    }
}