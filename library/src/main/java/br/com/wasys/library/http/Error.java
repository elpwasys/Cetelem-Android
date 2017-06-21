package br.com.wasys.library.http;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

import br.com.wasys.library.enumerator.HttpStatus;

/**
 * 
 * Error
 * 31 de jul de 2016 18:38:36
 * @autor Everton Luiz Pascke
 */
public class Error {

	public HttpStatus status;
	public Set<String> messages;

	private static final String DEFAULT_SEPARATOR = ";";
	
	public Error() {
		this.status = HttpStatus.INTERNAL_SERVER_ERROR;
	}
	
	public Error(HttpStatus status) {
		this.status = status;
	}

	public Error(HttpStatus status, String... messages) {
		this.status = status;
		if (ArrayUtils.isNotEmpty(messages)) {
			for (String message : messages) {
				addMessage(message);
			}
		}
	}
	
	public void addMessage(String message) {
		if (messages == null) {
			messages = new HashSet<>();
		}
		messages.add(message);
	}

	public String getMessage() {
		return format(DEFAULT_SEPARATOR);
	}

	public String format(String separator) {
		StringBuilder builder = new StringBuilder();
		if (CollectionUtils.isNotEmpty(messages)) {
			for (String message : messages) {
				String text = StringUtils.trim(message);
				if (StringUtils.isNotBlank(text)) {
					if (builder.length() > 0) {
						if (!text.endsWith(separator)) {
							builder.append(separator);
						}
					}
					builder.append(text);
				}
			}
		}
		return String.valueOf(builder);
	}
}