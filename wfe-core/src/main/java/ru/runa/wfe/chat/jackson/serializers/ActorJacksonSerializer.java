package ru.runa.wfe.chat.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import ru.runa.wfe.user.Actor;

public class ActorJacksonSerializer extends StdSerializer<Actor> {

    public ActorJacksonSerializer() {
        this(null);
    }

    public ActorJacksonSerializer(Class<Actor> actor) {
        super(actor);
    }

    @Override
    public void serialize(Actor value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeString(value.getName());
    }

}
