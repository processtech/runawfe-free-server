package ru.runa.wfe.chat.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.runa.wfe.chat.ChatLocalizationService;
import ru.runa.wfe.user.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatSessionUtils {

    public static final String CLIENT_LOCALE = "clientLocale";

    public static User getUser(Session session) {
        return (User) session.getUserProperties().get("user");
    }

    public static Locale getClientLocale(HandshakeRequest request) {
        Map<String, List<String>> headers = request.getHeaders();
        List<LanguageRange> ranges = new ArrayList<>();
        final List<String> acceptLanguages = headers.get("Accept-Language");
        if (acceptLanguages == null || acceptLanguages.isEmpty()) {
            return ChatLocalizationService.DEFAULT_LOCALE;
        }

        for (String languageHeader : acceptLanguages) {
            ranges.addAll(Locale.LanguageRange.parse(languageHeader));
        }
        return Locale.lookup(ranges, ChatLocalizationService.SUPPORTED_CHAT_LOCALES);
    }
}
