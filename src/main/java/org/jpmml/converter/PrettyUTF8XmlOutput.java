/*
 * Copyright (c) 2015 Villu Ruusmann
 *
 * This file is part of JPMML-Converter
 *
 * JPMML-Converter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-Converter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-Converter.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.output.Encoded;
import com.sun.xml.bind.v2.runtime.output.IndentingUTF8XmlOutput;
import com.sun.xml.bind.v2.runtime.output.Pcdata;
import com.sun.xml.bind.v2.runtime.output.UTF8XmlOutput;
import org.xml.sax.SAXException;

/**
 * @see IndentingUTF8XmlOutput
 */
public class PrettyUTF8XmlOutput extends UTF8XmlOutput {

	private int depth = 0;

	private boolean textWritten = false;


	public PrettyUTF8XmlOutput(OutputStream os, Encoded[] localNames, CharacterEscapeHandler characterEscapeHandler){
		super(os, localNames, characterEscapeHandler);
	}

	@Override
	public void endDocument(boolean fragment) throws IOException, SAXException, XMLStreamException {
		write('\n');

		super.endDocument(fragment);
	}

	@Override
	public void beginStartTag(int prefix, String localName) throws IOException {
		indentStartTag();

		super.beginStartTag(prefix, localName);
	}

	@Override
	public void beginStartTag(Name name) throws IOException {
		indentStartTag();

		super.beginStartTag(name);
	}

	@Override
	public void endTag(int prefix, String localName) throws IOException {
		indentEndTag();

		super.endTag(prefix, localName);
	}

	@Override
	public void endTag(Name name) throws IOException {
		indentEndTag();

		super.endTag(name);
	}

	@Override
	public void text(String value, boolean needsSeparatingWhitespace) throws IOException {
		super.text(value, needsSeparatingWhitespace);

		this.textWritten = true;
	}

	@Override
	public void text(Pcdata value, boolean needsSeparatingWhitespace) throws IOException {
		super.text(value, needsSeparatingWhitespace);

		this.textWritten = true;
	}

	private void indentStartTag() throws IOException {
		closeStartTag();

		if(!this.textWritten){
			printIndent();
		}

		this.depth++;

		this.textWritten = false;
	}

	private void indentEndTag() throws IOException {
		this.depth--;

		if(!super.closeStartTagPending && !this.textWritten){
			printIndent();
		}

		this.textWritten = false;
	}

	private void printIndent() throws IOException {
		write('\n');

		int length = this.depth;

		while(length > PrettyUTF8XmlOutput.indent.length){
			write(PrettyUTF8XmlOutput.indent);

			length -= PrettyUTF8XmlOutput.indent.length;
		}

		write(PrettyUTF8XmlOutput.indent, 0, length);
	}

	private static final byte[] indent = new byte[1024];

	static {
		Arrays.fill(PrettyUTF8XmlOutput.indent, (byte)'\t');
	}
}