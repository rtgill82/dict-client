package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

class SimpleSQLParser {

	private InputStream _stream = null;

	private boolean semicolon_found = false;
	private boolean in_statement = false;
	private boolean in_quotes = false;
	// private boolean in_comment = false;

	SimpleSQLParser(InputStream stream)
			throws IOException {
		_stream = stream;
	}

	public String getNextStatement()
			throws IOException {
		ByteArrayOutputStream output_stream = new ByteArrayOutputStream();

		int bytes_read = 0;
		while (true) {
			int c = _stream.read();
			if (c == -1 && bytes_read == 0) return null;
			if (c == -1) break;

			if (Character.isWhitespace(c)
					&& in_statement == false) {
				continue;
			}

			if (!Character.isWhitespace(c)
					&& in_statement == false) {
				in_statement = true;
			}

			switch (c) {
			case '\'':
				output_stream.write(c);
				bytes_read += 1;
				if (in_quotes) {
					c = _stream.read();
					output_stream.write(c);
					bytes_read += 1;
					if (c != '\'') {
						in_quotes = false;
					}
				} else {
					in_quotes = true;
				}
				break;

			case ';':
				if (!in_quotes) {
					semicolon_found = true;
				} else {
					output_stream.write(c);
					bytes_read += 1;
				}
				break;

			default:
				output_stream.write(c);
				bytes_read += 1;
				break;
			}

			if (semicolon_found) {
				semicolon_found = false;
				in_statement = false;
				break;
			}
		}

		return output_stream.toString();
	}
}
