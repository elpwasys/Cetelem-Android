package br.com.wasys.library.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Date;

import br.com.wasys.library.utils.DateUtils;

public class JsonDateSerializer extends JsonSerializer<Date> {
	
	@Override
	public void serialize(Date value, JsonGenerator generator, SerializerProvider serializers) throws IOException, JsonProcessingException {
		if (value == null) {
			generator.writeNull();
		}
		else {
			generator.writeString(DateUtils.format(value));
		}
	}
}
