package upc.edu.gessi.repo.dto.serializer;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.List;

public class NonEmptyListSerializer extends JsonSerializer<List<?>> {

    @Override
    public void serialize(List<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null && !value.isEmpty()) {
            gen.writeObject(value);
        }
    }
}
