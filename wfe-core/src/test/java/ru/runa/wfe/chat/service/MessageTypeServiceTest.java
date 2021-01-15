package ru.runa.wfe.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.runa.wfe.chat.dto.*;
import ru.runa.wfe.chat.socket.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class MessageTypeServiceTest {

    @Autowired
    private MessageTypeService service;

    @Test
    public void newMessageTest() throws IOException {
        final String NEW_MESSAGE =
            "{\"message\":\"New message\"," +
            "\"processId\":\"1\"," +
            "\"idHierarchyMessage\":\"\"," +
            "\"messageType\":\"newMessage\"," +
            "\"isPrivate\":false," +
            "\"privateNames\":\"\"," +
            "\"haveFile\":false}";

        ChatDto dto = service.convertJsonToDto(NEW_MESSAGE);
        ChatSocketMessageHandler<? extends ChatDto> handler = service.getHandlerByMessageType(dto.getClass());
        Assert.assertTrue(dto instanceof ChatNewMessageDto);
        Assert.assertEquals("New message", ((ChatNewMessageDto) dto).getMessage());
        Assert.assertEquals("1", ((ChatNewMessageDto) dto).getProcessId());
        Assert.assertEquals("", ((ChatNewMessageDto) dto).getIdHierarchyMessage());
        Assert.assertFalse(((ChatNewMessageDto) dto).getIsPrivate());
        Assert.assertEquals("", ((ChatNewMessageDto) dto).getPrivateNames());
        Assert.assertFalse(((ChatNewMessageDto) dto).getIsHaveFile());
        Assert.assertTrue(handler instanceof AddNewMessageHandler);
    }

    @Test
    public void readMessageTest() throws IOException {
        final String READ_MESSAGE =
            "{\"processId\":\"1\"," +
            "\"messageType\":\"readMessage\"," +
            "\"currentMessageId\":\"26\"}\n";

        ChatDto dto = service.convertJsonToDto(READ_MESSAGE);
        ChatSocketMessageHandler<? extends ChatDto> handler = service.getHandlerByMessageType(dto.getClass());
        Assert.assertTrue(dto instanceof ChatReadMessageDto);
        Assert.assertEquals(26, (long) ((ChatReadMessageDto) dto).getCurrentMessageId());
        Assert.assertTrue(handler instanceof ReadMessageHandler);
    }

    @Test
    public void editMessageTest() throws IOException {
        final String EDIT_MESSAGE =
            "{\"message\":\"Edit message\"," +
            "\"processId\":\"1\"," +
            "\"messageType\":\"editMessage\"," +
            "\"editMessageId\":\"20\"}\n";

        ChatDto dto = service.convertJsonToDto(EDIT_MESSAGE);
        ChatSocketMessageHandler<? extends ChatDto> handler = service.getHandlerByMessageType(dto.getClass());
        Assert.assertTrue(dto instanceof ChatEditMessageDto);
        Assert.assertEquals("Edit message", ((ChatEditMessageDto) dto).getMessage());
        Assert.assertEquals(20, (long) ((ChatEditMessageDto) dto).getEditMessageId());
        Assert.assertTrue(handler instanceof EditMessageHandler);
    }

    @Test
    public void deleteMessageTest() throws IOException {
        final String DELETE_MESSAGE =
            "{\"messageId\":\"20\"," +
            "\"processId\":\"1\"," +
            "\"messageType\":\"deleteMessage\"}\n";

        ChatDto dto = service.convertJsonToDto(DELETE_MESSAGE);
        ChatSocketMessageHandler<? extends ChatDto> handler = service.getHandlerByMessageType(dto.getClass());
        Assert.assertTrue(dto instanceof ChatDeleteMessageDto);
        Assert.assertEquals(20, (long) ((ChatDeleteMessageDto) dto).getMessageId());
        Assert.assertTrue(handler instanceof DeleteMessageHandler);
    }

    @Test
    public void getMessageTest() throws IOException {
        final String GET_MESSAGES =
            "{\"processId\":\"1\"," +
            "\"messageType\":\"getMessages\"," +
            "\"lastMessageId\":7," +
            "\"count\":20}";

        ChatDto dto = service.convertJsonToDto(GET_MESSAGES);
        ChatSocketMessageHandler<? extends ChatDto> handler = service.getHandlerByMessageType(dto.getClass());
        Assert.assertTrue(dto instanceof ChatGetMessagesDto);
        Assert.assertEquals(7, (long) ((ChatGetMessagesDto) dto).getLastMessageId());
        Assert.assertEquals(20, ((ChatGetMessagesDto) dto).getCount());
        Assert.assertTrue(handler instanceof GetMessagesMessageHandler);
    }

    @Test
    public void endLoadFilesTest() throws IOException {
        final String END_LOAD_FILES =
                "{\"messageType\":\"ChatDto\"}";

        ChatDto dto = service.convertJsonToDto(END_LOAD_FILES);
        ChatSocketMessageHandler<? extends ChatDto> handler = service.getHandlerByMessageType(dto.getClass());
        Assert.assertTrue(handler instanceof EndLoadFilesMessageHandler);
    }

    @Configuration
    public static class ContextConfiguration {

        @Bean
        public Map<Class<? extends ChatDto>, ChatSocketMessageHandler<? extends ChatDto>> handlerByMessageType() {
            Map<Class<? extends ChatDto>, ChatSocketMessageHandler<? extends ChatDto>> handlersByMessageType = new HashMap<>();

            handlersByMessageType.put(ChatNewMessageDto.class, new AddNewMessageHandler());
            handlersByMessageType.put(ChatReadMessageDto.class, new ReadMessageHandler());
            handlersByMessageType.put(ChatEditMessageDto.class, new EditMessageHandler());
            handlersByMessageType.put(ChatDeleteMessageDto.class, new DeleteMessageHandler());
            handlersByMessageType.put(ChatGetMessagesDto.class, new GetMessagesMessageHandler());
            handlersByMessageType.put(ChatDto.class, new EndLoadFilesMessageHandler());

            return handlersByMessageType;
        }

        @Bean
        public ObjectMapper chatObjectMapper() {
            return new ObjectMapper();
        }

        @Bean
        MessageTypeService messageTypeService() {
            return new MessageTypeService();
        }
    }
}
