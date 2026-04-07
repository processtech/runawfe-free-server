package ru.runa.wf.web.api;


import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * Сервлет для распознавания речи
 */
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 15    // 15MB
)
public class SpeechRecognitionServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(SpeechRecognitionServlet.class);

    // Валидация имени переменной (защита от инъекций)
    private static final Pattern VARIABLE_NAME_PATTERN = Pattern.compile("^[a-zA-Zа-яА-ЯёЁ0-9_\\[\\].\\-]+$");


    @Override
    public void init() throws ServletException {
        super.init();
        log.info(" SpeechRecognitionServlet initializing...");

        try {

            log.info(" VoskRecognitionService injected successfully.");
        } catch (Exception e) {
            log.error(" Failed to initialize VoskRecognitionService. Speech recognition will be unavailable.", e);

        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Установка кодировки ответа сразу
        response.setCharacterEncoding(Charsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");


        String variableName = request.getParameter("variableName");
        Part audioPart = null;

        try {
            // 1. Валидация имени переменной
            if (variableName == null || !VARIABLE_NAME_PATTERN.matcher(variableName).matches()) {
                log.warn("Invalid variable name received: {}", variableName);
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid variable name");
                return;
            }

            // 2. Получение файла
            try {
                audioPart = request.getPart("audio");
            } catch (Exception e) {
                log.error("Failed to parse multipart request", e);
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid multipart format");
                return;
            }

            if (audioPart == null || audioPart.getSize() == 0) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Empty audio file");
                return;
            }

            // 3. Конвертация аудио
            byte[] pcmData;
            try (InputStream inputStream = audioPart.getInputStream()) {
                byte[] fileBytes = ByteStreams.toByteArray(inputStream);


            }

            // 4. Распознавание
            String text = "test";
            log.info("Transcribed '{}': '{}'", variableName, text);

            // 5. Формирование ответа
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("text", text);
            jsonResponse.put("variableName", variableName);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getOutputStream().write(jsonResponse.toString().getBytes(Charsets.UTF_8));
            response.getOutputStream().flush();

        } catch (IllegalStateException e) {
            log.warn("Recognition service unavailable: {}", e.getMessage());
            sendError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Bad request: {}", e.getMessage());
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Transcription failed for variable: {}", variableName, e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Processing error: " + e.getMessage());
        } finally {
            // Закрытие ресурса на всякий случай
            if (audioPart != null) {
                try {
                    audioPart.delete();
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        // Обработка preflight запросов (CORS), если фильтр не сработал
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        JSONObject errorJson = new JSONObject();
        if (status >= 500) {
            errorJson.put("error", "Internal Server Error"); // Как то так
        } else {
            errorJson.put("error", message);
        }
        response.getOutputStream().write(errorJson.toString().getBytes(Charsets.UTF_8));
        response.getOutputStream().flush();
    }
}