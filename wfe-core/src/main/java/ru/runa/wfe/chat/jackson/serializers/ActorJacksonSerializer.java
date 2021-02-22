package ru.runa.wfe.chat.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ru.runa.wfe.user.Actor;
import java.io.IOException;

public class ActorJacksonSerializer extends StdSerializer<Actor> {

    public ActorJacksonSerializer() {
        super(Actor.class);
    }

    @Override
    public void serialize(Actor actor, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeString(actor.getName());
    }

}
