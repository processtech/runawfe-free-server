package ru.runa.wfe;


//todo или лучше в core ?
public interface SpeechRecognitionService {
    /**
     * Распознаёт речь из аудиоданных.
     * @param audioData аудио в формате, поддерживаемом реализацией (например, PCM 16kHz)
     * @return распознанный текст или null, если не удалось
     */
    String recognize(byte[] audioData);
}