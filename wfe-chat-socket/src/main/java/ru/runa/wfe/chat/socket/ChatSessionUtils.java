package ru.runa.wfe.chat.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketSession;
import ru.runa.wfe.chat.ChatLocalizationService;
import ru.runa.wfe.user.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatSessionUtils {
    public static final String USER_ATTRIBUTE = "user";
    public static final String CLIENT_LOCALE = "clientLocale";

    public static User getUser(WebSocketSession session) {
        return (User) session.getAttributes().get(USER_ATTRIBUTE);
    }

    public static Locale getClientLocale(ServerHttpRequest request) {
        final Map<String, List<String>> headers = request.getHeaders();
        final List<Locale.LanguageRange> ranges = new ArrayList<>();
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
