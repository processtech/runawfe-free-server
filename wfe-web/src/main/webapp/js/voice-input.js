/**
 * Voice Input Module for RunaWFE
 * Optimized: Single conversion path, global AudioContext, fixed resource leaks.
 */
(function() {
    'use strict';

    if (window.__voiceInputLoaded) {
        console.log('[VoiceInput] Already loaded, skipping');
        return;
    }
    window.__voiceInputLoaded = true;
    console.log('[VoiceInput] Module initializing...');

    const CONFIG = {
        maxRecordingTime: 30000,
        sampleRate: 16000,
        endpoint: '/api/speech/transcribe'
    };

    const activeRecordings = {};

    // ✅ ОПТИМИЗАЦИЯ 1: Создаем AudioContext один раз при старте модуля
    // Это предотвращает утечки и ошибки "Too many AudioContexts"
    const GlobalAudioContext = new (window.AudioContext || window.webkitAudioContext)();

    /**
     * Определяет поддерживаемый MIME-тип.
     * Примечание: MediaRecorder все равно запишет в сжатом формате (opus/webm),
     * поэтому декодирование в PCM неизбежно.
     */
    function getSupportedMimeType() {
        const types = [
            'audio/webm;codecs=opus', // Лучшее качество/размер обычно здесь
            'audio/ogg;codecs=opus',
            'audio/webm',
            'audio/ogg'
        ];
        for (const type of types) {
            if (MediaRecorder.isTypeSupported(type)) {
                return type;
            }
        }
        return '';
    }

    /**
     * Конвертирует AudioBuffer в 16-bit PCM WAV.
     * Выполняет ресемплинг, если частота не совпадает с целевой.
     */
    async function audioBufferToPcmWav(audioBuffer, targetSampleRate = 16000) {
        let sourceBuffer = audioBuffer;

        // Ресемплинг только если частоты не совпадают
        if (audioBuffer.sampleRate !== targetSampleRate) {
            const length = Math.ceil(audioBuffer.duration * targetSampleRate);
            const offlineCtx = new OfflineAudioContext(1, length, targetSampleRate);
            const source = offlineCtx.createBufferSource();
            source.buffer = audioBuffer;
            source.connect(offlineCtx.destination);
            source.start();
            sourceBuffer = await offlineCtx.startRendering();
        }

        return convertToPcmWav(sourceBuffer);
    }

    function convertToPcmWav(buffer) {
        const numChannels = 1;  // Mono обязательно для Vosk
        const sampleRate = buffer.sampleRate;
        const bytesPerSample = 2;  // 16-bit
        const blockAlign = numChannels * bytesPerSample;
        const byteRate = sampleRate * blockAlign;
        const dataLength = buffer.length * blockAlign;
        const bufferLength = 44 + dataLength;

        const arrayBuffer = new ArrayBuffer(bufferLength);
        const view = new DataView(arrayBuffer);

        // Заголовок WAV
        writeString(view, 0, 'RIFF');
        view.setUint32(4, bufferLength - 8, true);
        writeString(view, 8, 'WAVE');
        writeString(view, 12, 'fmt ');
        view.setUint32(16, 16, true);
        view.setUint16(20, 1, true);   // PCM
        view.setUint16(22, numChannels, true);
        view.setUint32(24, sampleRate, true);
        view.setUint32(28, byteRate, true);
        view.setUint16(32, blockAlign, true);
        view.setUint16(34, 16, true);
        writeString(view, 36, 'data');
        view.setUint32(40, dataLength, true);

        // Смешивание каналов в моно и конвертация в Int16
        let offset = 44;
        const channelData = buffer.getChannelData(0); // Берем первый канал

        // Если стерео, нужно микшировать, но MediaRecorder мы просим 1 канал (channelCount: 1)
        // Так что берем просто данные.

        for (let i = 0; i < channelData.length; i++) {
            // Clamp value between -1 and 1
            let sample = Math.max(-1, Math.min(1, channelData[i]));
            // Convert float to 16-bit integer
            sample = sample < 0 ? sample * 32768 : sample * 32767;
            view.setInt16(offset, sample, true);
            offset += 2;
        }

        return arrayBuffer;
    }

    function writeString(view, offset, string) {
        for (let i = 0; i < string.length; i++) {
            view.setUint8(offset + i, string.charCodeAt(i));
        }
    }

    /**
     * Основная функция переключения записи
     */
    window.toggleVoiceInput = async function(button, variableName, ctxPath) {
        console.log('[VoiceInput] Toggle for:', variableName);
        console.log('[VoiceInput] ctxPath for:', ctxPath);

        const textarea = document.getElementById('txt_' + variableName);
        const status = document.getElementById('status_' + variableName);
        const micIcon = document.getElementById('mic_' + variableName);

        if (!textarea || !status || !micIcon) {
            console.error('[VoiceInput] UI elements not found');
            return;
        }

        // --- РЕЖИМ 1: Встроенное распознавание браузера (Google/Safari) ---
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

        // Если браузер умеет сам распознавать И мы не записываем сейчас через MediaRecorder
        if (SpeechRecognition && !activeRecordings[variableName]) {
             // Проверка: если уже идет распознавание браузером
             if (activeRecordings[variableName] && activeRecordings[variableName].isSpeechRec) {
                 activeRecordings[variableName].stop();
                 delete activeRecordings[variableName];
                 status.textContent = '';
                 micIcon.style.transform = 'none';
                 return;
             }

             console.log('[VoiceInput] Using Browser SpeechRecognition');
             const recognition = new SpeechRecognition();
             recognition.lang = 'ru-RU';
             recognition.interimResults = false;
             recognition.maxAlternatives = 1;

             recognition.onstart = () => {
                 status.textContent = '🎤 Слушаю (браузер)...';
                 micIcon.style.transform = 'scale(1.3)';
             };
             recognition.onresult = (event) => {
                 const text = event.results[0][0].transcript;
                 textarea.value = (textarea.value ? textarea.value + ' ' : '') + text;
                 textarea.dispatchEvent(new Event('change', { bubbles: true }));
                 status.textContent = '✓';
             };
             recognition.onerror = (e) => {
                 console.error('[SpeechRecognition] error:', e);
                 status.textContent = '✗';
             };
             recognition.onend = () => {
                 micIcon.style.transform = 'none';
                 if (activeRecordings[variableName] === recognition) {
                     delete activeRecordings[variableName];
                     setTimeout(() => { if(status.textContent === '✓') status.textContent = ''; }, 2000);
                 }
             };

             recognition.start();
             activeRecordings[variableName] = recognition;
             activeRecordings[variableName].isSpeechRec = true; // Метка, что это не MediaRecorder
             return;
        }

        // --- РЕЖИМ 2: Запись и отправка на Vosk (На  сервер) ---
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            alert('Голосовой ввод не поддерживается этим браузером');
            return;
        }

        if (!activeRecordings[variableName]) {
            // СТАРТ ЗАПИСИ
            console.log('[VoiceInput] Starting MediaRecorder...');
            try {
                const stream = await navigator.mediaDevices.getUserMedia({
                    audio: {
                        channelCount: 1, // Сразу просим моно, чтобы не микшировать потом
                        sampleRate: CONFIG.sampleRate,
                        echoCancellation: true, // Включаем шумодав браузера, будет чище
                        noiseSuppression: true,
                        autoGainControl: true
                    }
                });

                const mimeType = getSupportedMimeType();
                const mediaRecorder = new MediaRecorder(stream, mimeType ? { mimeType } : undefined);
                const chunks = [];

                mediaRecorder.ondataavailable = e => {
                    if (e.data.size > 0) chunks.push(e.data);
                };

                mediaRecorder.onstop = async () => {
                    console.log('[VoiceInput] Processing recorded audio...');
                    try {
                        status.textContent = '⏳ Обработка...';
                        micIcon.style.opacity = '0.5';

                        // 1. Склеиваем чанки в Blob
                        const audioBlob = new Blob(chunks, { type: mimeType || 'audio/webm' });

                        // 2. Декодируем в AudioBuffer (используем ГЛОБАЛЬНЫЙ контекст)
                        const arrayBuffer = await audioBlob.arrayBuffer();
                        const audioBuffer = await GlobalAudioContext.decodeAudioData(arrayBuffer);

                        // 3. Конвертируем в PCM WAV (16kHz, 16bit, Mono)
                        const pcmWavBuffer = await audioBufferToPcmWav(audioBuffer, CONFIG.sampleRate);
                        const pcmBlob = new Blob([pcmWavBuffer], { type: 'audio/wav' });

                        console.log(`[VoiceInput] Ready to send: ${pcmBlob.size} bytes`);

                        // 4. Отправка на сервер
                        const formData = new FormData();
                        formData.append('audio', pcmBlob, 'voice.wav');
                        formData.append('variableName', variableName);

                        const resp = await fetch(ctxPath + CONFIG.endpoint, {
                            method: 'POST',
                            body: formData,
                            credentials: 'include'
                        });

                        const contentType = resp.headers.get('content-type');
                        if (contentType && contentType.includes('text/html')) {
                            throw new Error('Session expired (HTML response)');
                        }

                        if (!resp.ok) {
                            const errText = await resp.text();
                            throw new Error(`Server error ${resp.status}: ${errText}`);
                        }

                        const data = await resp.json();
                        if (data.text && data.text.trim()) {
                            textarea.value = (textarea.value ? textarea.value + ' ' : '') + data.text.trim();
                            textarea.dispatchEvent(new Event('change', { bubbles: true }));
                            status.textContent = '✓';
                            setTimeout(() => { if(status.textContent === '✓') status.textContent = ''; }, 2000);
                        } else {
                            status.textContent = '⚠ Пусто';
                        }

                    } catch (e) {
                        console.error('[VoiceInput] Processing failed:', e);
                        status.textContent = '✗ Ошибка';
                        alert('Ошибка обработки голоса: ' + e.message);
                    } finally {
                        micIcon.style.opacity = '1';
                        micIcon.style.transform = 'none';
                        micIcon.style.filter = 'none';
                        // Останавливаем треки потока
                        stream.getTracks().forEach(t => t.stop());
                        delete activeRecordings[variableName];
                    }
                };

                mediaRecorder.start(1000); // timeslice для получения данных порциями
                activeRecordings[variableName] = { mediaRecorder, stream, isSpeechRec: false };

                // UI
                button.style.borderColor = '#e74c3c';
                micIcon.style.transform = 'scale(1.3)';
                micIcon.style.filter = 'drop-shadow(0 0 3px #e74c3c)';
                status.textContent = 'Запись...';

                // Авто-стоп
                setTimeout(() => {
                    if (activeRecordings[variableName] && !activeRecordings[variableName].isSpeechRec) {
                        activeRecordings[variableName].mediaRecorder.stop();
                    }
                }, CONFIG.maxRecordingTime);

            } catch (err) {
                console.error('[VoiceInput] Mic access error:', err);
                status.textContent = '🚫';
                alert('Нет доступа к микрофону: ' + err.message);
            }
        } else {
            // СТОП ЗАПИСИ (если это MediaRecorder)
            if (!activeRecordings[variableName].isSpeechRec) {
                console.log('[VoiceInput] Stopping recording...');
                try {
                    activeRecordings[variableName].mediaRecorder.stop();
                } catch (e) {
                    console.error('[VoiceInput] Stop error:', e);
                    delete activeRecordings[variableName];
                }
            }
        }
    };

    // Обработчик клика (делегирование)
    document.addEventListener('click', function(e) {
        const btn = e.target.closest('.btn-mic');
        if (!btn) return;
        const variableName = btn.dataset.variable;
        const ctxPath = btn.dataset.ctx;
        if (variableName && ctxPath) {
            window.toggleVoiceInput(btn, variableName, ctxPath);
        }
    });

    console.log('[VoiceInput] ✅ Module ready');
})();