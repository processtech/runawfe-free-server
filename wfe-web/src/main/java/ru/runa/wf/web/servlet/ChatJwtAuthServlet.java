package ru.runa.wf.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ru.runa.common.web.Commons;
import ru.runa.wfe.user.User;
import ru.runa.wfe.auth.JwtUser;

public class ChatJwtAuthServlet extends HttpServlet {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final User user = Commons.getUser(request.getSession());
        final Token token = new Token(new JwtUser().tokenOf(user));

        response.setContentType("application/json");
        response.setCharacterEncoding(Charsets.UTF_8.name());
        response.getOutputStream().write(mapper.writer().writeValueAsBytes(token));
        response.getOutputStream().flush();
    }

    public static final class Token {
        private final String token;

        public Token(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }
}
