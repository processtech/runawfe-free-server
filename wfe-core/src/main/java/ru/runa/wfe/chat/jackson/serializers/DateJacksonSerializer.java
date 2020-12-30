package ru.runa.wfe.chat.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateJacksonSerializer extends StdSerializer<Date> {

    public DateJacksonSerializer() {
        this(null);
    }

    public DateJacksonSerializer(Class<Date> date) {
        super(date);
    }

    @Override
    public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        jgen.writeString(sdf.format(value));
    }

}
