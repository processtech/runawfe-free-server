package ru.runa.wfe.rest.impl;

import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.chat.dto.WfChatRoom;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.chat.logic.ChatFileLogic;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.rest.auth.AuthUser;

@RestController
@RequestMapping("/chat")
@Transactional
public class ChatController {

    @Autowired
    private ChatLogic chatLogic;

    @Autowired
    private ChatFileLogic chatFileLogic;

    @GetMapping
    public Collection<WfChatRoom> getChatRooms(@AuthenticationPrincipal AuthUser authUser) {
        return chatLogic.getChatRooms(authUser.getUser(), BatchPresentationFactory.CHAT_ROOMS.createDefault());
    }

    @GetMapping("/{processId}")
    public Collection<MessageAddedBroadcast> getChatMessages(@AuthenticationPrincipal AuthUser authUser,
            @PathVariable long processId) {
        return chatLogic.getMessages(authUser.getUser(), processId);
    }

    @GetMapping("/file")
    public ResponseEntity<Resource> getFile(@AuthenticationPrincipal AuthUser authUser, @RequestParam long fileId) {
        ChatMessageFileDto file = chatFileLogic.getById(authUser.getUser(), fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentLength(file.getBytes().length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new ByteArrayResource(file.getBytes()));
    }
}
