package upc.edu.gessi.repo.dto.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomDateDeserializer extends JsonDeserializer<Date> {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String dateAsString = jsonParser.getText();
        try {
            return dateFormat.parse(dateAsString);
        } catch (ParseException e) {
            return null;
        }
    }
}
