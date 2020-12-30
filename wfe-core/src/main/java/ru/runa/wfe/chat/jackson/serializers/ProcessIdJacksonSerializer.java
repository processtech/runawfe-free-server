package ru.runa.wfe.chat.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import ru.runa.wfe.execution.Process;

public class ProcessIdJacksonSerializer extends StdSerializer<Process> {

    public ProcessIdJacksonSerializer() {
        this(null);
    }

    public ProcessIdJacksonSerializer(Class<Process> process) {
        super(process);
    }

    @Override
    public void serialize(Process value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeString(value.getId().toString());
    }

}
