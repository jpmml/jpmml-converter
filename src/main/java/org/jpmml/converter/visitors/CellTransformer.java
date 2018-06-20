/*
 * Copyright (c) 2018 Villu Ruusmann
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
package org.jpmml.converter.visitors;

import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dmg.pmml.Row;
import org.dmg.pmml.VisitorAction;
import org.jpmml.model.visitors.AbstractVisitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CellTransformer extends AbstractVisitor {

	private Document document = null;


	public CellTransformer(){
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);

		DocumentBuilder documentBuilder;

		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch(ParserConfigurationException pce){
			throw new RuntimeException(pce);
		}

		this.document = documentBuilder.newDocument();
	}

	@Override
	public VisitorAction visit(Row row){

		if(row.hasContent()){
			List<Object> content = row.getContent();

			transform(content);
		}

		return VisitorAction.CONTINUE;
	}

	private void transform(List<Object> content){
		ListIterator<Object> contentIt = content.listIterator();

		while(contentIt.hasNext()){
			Object object = contentIt.next();

			if(object instanceof JAXBElement){
				JAXBElement<?> jaxbElement = (JAXBElement<?>)object;

				QName name = jaxbElement.getName();

				Element domElement;

				if((name.getPrefix()).length() > 0){
					domElement = this.document.createElement(name.getPrefix() + ":" + name.getLocalPart());
				} else

				{
					domElement = this.document.createElement(name.getLocalPart());
				}

				domElement.setTextContent((String)jaxbElement.getValue());

				contentIt.set(domElement);
			}
		}
	}
}