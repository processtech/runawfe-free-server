
```bash
curl -X POST "http://localhost:8080/wfe/speech/recognize" \
  -F "variableName=testAudio" \
  -F "audio=@/path/to/test.wav"
```