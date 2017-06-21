package br.com.wasys.library.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Date;

import br.com.wasys.library.utils.DateUtils;

public class JsonDateDeserializer extends JsonDeserializer<Date> {

	@Override
	public Date deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
		String text = parser.getText();
		if (StringUtils.isBlank(text)) {
			return null;
		}
		else {
			return DateUtils.parse(text);
		}
	}
}
