package ru.runa.wfe.vosk;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Local;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;


import javax.ejb.Stateless;
import ru.runa.wfe.SpeechRecognitionService;
import ru.runa.wfe.commons.SystemProperties;

@Stateless
@Local(SpeechRecognitionService.class)
public class VoskRecognitionService implements SpeechRecognitionService {

    private static final Logger log = LoggerFactory.getLogger(VoskRecognitionService.class);

    private Model model;
    private BlockingQueue<Recognizer> recognizerPool;
    private volatile boolean available = false;
    private volatile boolean initialized = false;
    private String errorMessage = null;

    @PostConstruct
    private void init() {
        log.info("Initializating speech Recognition Service");
        try {
        initialized = true;
        if (!SystemProperties.isSpeechRecognitionEnabled()) {
            log.info("Speech recognition DISABLED via system property 'speech.recognition.enabled=false'. Model will NOT be loaded.");
            available = false;
            errorMessage = "Speech recognition disabled by administrator";
            return;
        }

        // 1. Приоритет №1
        String modelPath = SystemProperties.getVoskModelPath();
        log.info("Loading vosk on path '{}'...", modelPath);
        // 2. Приоритет №2: Ненужен ?

        // 🔴 КРИТИЧЕСКАЯ ПРОВЕРКА: Если путь не задан И функция включена — ошибка конфигурации
        if (modelPath == null || modelPath.isEmpty()) {
            String msg = "Vosk model path not configured! Set 'vosk.model.path' in System Settings → User Interface";
            log.error("❌ {}", msg);
            errorMessage = msg;
            available = false;
            return;
        }

        log.info("Initializing Vosk with a path: {}", modelPath);

        try {
            LibVosk.setLogLevel(LogLevel.INFO);

            File modelDir = new File(modelPath);

            // Проверка существования пути
            if (!modelDir.exists() || !modelDir.isDirectory()) {
                // Попытка найти модель внутри ресурсов (если она упакована в jar/war)
                java.net.URL resource = getClass().getClassLoader().getResource(modelPath);
                if (resource != null) {
                    modelDir = new File(resource.getFile());
                    log.info("Модель найдена в ресурсах: {}", modelDir.getAbsolutePath());
                } else {
                    throw new IOException("The model's directory was not found either in the file system or in the resources.:" + modelPath);
                }
            }

            model = new Model(modelDir.getAbsolutePath());
            log.info(" Vosk model loaded successfully from: {}", modelDir.getAbsolutePath());

            int poolSize = SystemProperties.getVoskRecognizersSize();
            recognizerPool = new ArrayBlockingQueue<>(poolSize);
            // Инициализация пула распознавателей
            for (int i = 0; i < poolSize; i++) {
                recognizerPool.offer(new Recognizer(model, 16000.0f));
            }
            available = true;

        } catch (Exception e) {
            log.error(" Failed to load Vosk model from {}", modelPath, e);
            errorMessage = "Ошибка загрузки модели распознавания: " + e.getMessage();
            available = false;
        }
        } catch (Throwable t) {
            log.error("Failed to initialize Vosk recognition service", t);
            available = false;
            errorMessage = t.getMessage();
            // не выбрасываем исключение дальше ?
        }

    }

    @PreDestroy
    public void shutdown() {
        if (recognizerPool != null) {
            Recognizer rec;
            while ((rec = recognizerPool.poll()) != null) {
                rec.close();
            }
            recognizerPool.clear();
            recognizerPool = null;
        }
        if (model != null) {
            model.close();
            model = null;
        }

        available = false;
        log.info("Vosk service shutdown complete");
    }

    @Override
    public String recognize(byte[] pcmData) {
        ensureInitialized();
        if (!available) {
            throw new IllegalStateException(errorMessage != null ? errorMessage : "Recognition unavailable");
        }

        Recognizer recognizer = null;

        try {
            recognizer = recognizerPool.take();
            recognizer.reset();

            recognizer.acceptWaveForm(pcmData, pcmData.length);

            String json = recognizer.getFinalResult();
            return parseVoskJson(json);

        } catch (Exception e) {
            throw new RuntimeException("Recognition error: " + e.getMessage(), e);
        } finally {
            if (recognizer != null) {
                recognizerPool.offer(recognizer);
            }
        }
    }

    private String parseVoskJson(String json) {

        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonObject obj = reader.readObject();
            return obj.containsKey("text") && !obj.isNull("text")
                    ? obj.getString("text").trim()
                    : null;
        } catch (Exception e) {
            log.warn("Failed to parse Vosk JSON: {}", json, e);
            return null;
        }
    }

    public synchronized void reload() {
        log.info(" Reloading VoskRecognitionService...");

        shutdown();

        init();
    }

    private void ensureInitialized() {
        if (!available) {// (1) первая проверка без блокировки
            synchronized (this) {// (2) захват монитора
                if (!available) {// (3) вторая проверка под блокировкой
                    if (!initialized) {
                        init();
                    }// (4) инициализация
                }
            }
        }
    }

    /**
     * Принимает аудио в формате WAV (16kHz, 16-bit, mono, PCM) от клиента.
     * Проверяет заголовок и возвращает данные как есть.
     *
     * @param audioData   сырые байты аудио
     * @param contentType MIME-тип (должен быть audio/wav)
     * @return те же байты, если файл валиден
     * @throws IllegalArgumentException если формат не подходит
     */
    public byte[] convertToFormat(byte[] audioData, String contentType) {
        log.warn("Received audio: contentType={}, size={} bytes", contentType, audioData.length);

        //  проверка MIME-типа не нужна так как с фронта отправляется только waw

        //   Валидация WAV-заголовка (минимальная проверка)
        if (!isValidWavHeader(audioData)) {
            log.error("Invalid WAV header");
            throw new IllegalArgumentException("Invalid WAV file format");
        }

        log.warn(" Valid WAV file, returning {} bytes", audioData.length);

        byte[] pcm = Arrays.copyOfRange(audioData, 44, audioData.length);

        log.warn("PCM size: {} bytes", pcm.length);

        return pcm;
    }

    /**
     * Проверяет, что байты начинаются с корректного WAV-заголовка (RIFF...WAVE)
     */
    private static boolean isValidWavHeader(byte[] data) {
        if (data == null || data.length < 44) {  // Минимальный размер WAV-заголовка
            return false;
        }
        // RIFF descriptor
        if (data[0] != 'R' || data[1] != 'I' || data[2] != 'F' || data[3] != 'F') {
            return false;
        }
        // WAVE descriptor (offset 8)
        if (data[8] != 'W' || data[9] != 'A' || data[10] != 'V' || data[11] != 'E') {
            return false;
        }
        return true;
    }
}