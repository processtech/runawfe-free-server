package ru.runa.wfe.rest.impl;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.dto.WfUserDto;
import ru.runa.wfe.user.Actor;

@RestController
@RequestMapping("/user/")
@Transactional
public class UserApiController {

    @PostMapping("profile")
    public WfUserDto getUser(@AuthenticationPrincipal AuthUser authUser) {
        Actor user = authUser.getUser().getActor();
        //TODO замапить DTO с профилем пользователя
        return null;
    }
}
