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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.transform.stream.StreamResult;

import com.google.protobuf.CodedInputStream;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.ArchiveBatch;
import org.jpmml.evaluator.Batch;
import org.jpmml.model.JAXBUtil;
import rexp.Rexp;
import rexp.Rexp.REXP;

abstract
public class ConverterTest {

	static
	public Batch createBatch(final Converter converter, String name, String dataset){
		Batch result = new ArchiveBatch(name, dataset){

			@Override
			public InputStream getModel(){
				try {
					InputStream is = open("/pb/" + getName() + getDataset() + ".pb");

					try {
						return convert(converter, is);
					} finally {
						is.close();
					}
				} catch(Exception e){
					throw new AssertionError(e);
				}
			}
		};

		return result;
	}

	static
	private InputStream convert(Converter converter, InputStream is) throws Exception {
		CodedInputStream cis = CodedInputStream.newInstance(is);

		REXP rexp = Rexp.REXP.parseFrom(cis);

		PMML pmml = converter.convert(rexp);

		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			JAXBUtil.marshalPMML(pmml, new StreamResult(os));

			byte[] buffer = os.toByteArray();

			return new ByteArrayInputStream(buffer);
		} finally {
			os.close();
		}
	}
}