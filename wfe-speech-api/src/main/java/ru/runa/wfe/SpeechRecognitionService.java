package ru.runa.wfe;


//todo или лучше в core ?
public interface SpeechRecognitionService {
    /**
     * Распознаёт речь из аудиоданных.
     *
     * @param audioData аудио в формате, поддерживаемом реализацией
     * @return распознанный текст или null, если не удалось
     */
    String recognize(byte[] audioData);

    /**
     * Принимает аудио от клиента.
     * Проверяет на соотвествие формату и возвращает данные как есть.
     * @param audioData сырые байты аудио
     * @param contentType MIME-тип
     * @return коректные формат данных
     */
    byte[] convertToFormat(byte[] audioData, String contentType);
}