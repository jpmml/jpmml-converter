/*
 * Copyright (c) 2024 Villu Ruusmann
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.jpmml.model.SAXUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

public class SAXTransformerUtil {

	private SAXTransformerUtil(){
	}

	static
	public void transform(File input, File output, XMLFilter... filters) throws IOException, TransformerConfigurationException, ParserConfigurationException, SAXException {
		SAXTransformerFactory transformerFactory = (SAXTransformerFactory)TransformerFactory.newInstance();

		try(OutputStream os = new FileOutputStream(output)){
			TransformerHandler transformerHandler = transformerFactory.newTransformerHandler();
			transformerHandler.setResult(new StreamResult(os));

			XMLReader xmlReader = SAXUtil.createFilteredReader(SAXUtil.createXMLReader(), filters);
			xmlReader.setContentHandler(transformerHandler);

			try(InputStream is = new FileInputStream(input)){
				xmlReader.parse(new InputSource(is));
			}
		}
	}
}