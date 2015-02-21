/*
 * Copyright (c) 2014 Villu Ruusmann
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.stream.StreamResult;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.protobuf.CodedInputStream;
import org.dmg.pmml.PMML;
import org.jpmml.model.JAXBUtil;
import rexp.Rexp;

public class Main {

	@Parameter (
		names = "--pb-input",
		description = "ProtoBuf input file",
		required = true
	)
	private File input = null;

	@Parameter (
		names = "--pmml-output",
		description = "PMML output file",
		required = true
	)
	private File output = null;


	static
	public void main(String... args) throws Exception {
		Main main = new Main();

		JCommander commander = new JCommander(main);
		commander.setProgramName(Main.class.getName());

		try {
			commander.parse(args);
		} catch(ParameterException pe){
			commander.usage();

			System.exit(-1);
		}

		main.run();
	}

	public void run() throws Exception {
		Rexp.REXP rexp;

		InputStream is = new FileInputStream(this.input);

		try {
			logger.log(Level.INFO, "Parsing ProtoBuf..");

			CodedInputStream cis = CodedInputStream.newInstance(is);
			cis.setSizeLimit(Integer.MAX_VALUE);

			long start = System.currentTimeMillis();
			rexp = Rexp.REXP.parseFrom(cis);
			long end = System.currentTimeMillis();

			logger.log(Level.INFO, "Parsed ProtoBuf in " + (end - start) + " ms.");
		} catch(Exception e){
			logger.log(Level.SEVERE, "Failed to parse ProtoBuf", e);

			throw e;
		} finally {
			is.close();
		}

		ConverterFactory converterFactory = ConverterFactory.getInstance();

		Converter converter = converterFactory.getConverter(rexp);

		PMML pmml;

		try {
			logger.log(Level.INFO, "Converting model..");

			long start = System.currentTimeMillis();
			pmml = converter.convert(rexp);
			long end = System.currentTimeMillis();

			logger.log(Level.INFO, "Converted model in " + (end - start) + " ms.");
		} catch(Exception e){
			logger.log(Level.SEVERE, "Failed to convert model", e);

			throw e;
		}

		OutputStream os = new FileOutputStream(this.output);

		try {
			logger.log(Level.INFO, "Marshalling PMML..");

			long start = System.currentTimeMillis();
			JAXBUtil.marshalPMML(pmml, new StreamResult(os));
			long end = System.currentTimeMillis();

			logger.log(Level.INFO, "Marshalled PMML in " + (end - start) + " ms.");
		} catch(Exception e){
			logger.log(Level.SEVERE, "Failed to marshal PMML", e);

			throw e;
		} finally {
			os.close();
		}
	}

	public File getInput(){
		return this.input;
	}

	public void setInput(File input){

		if(input == null){
			throw new NullPointerException();
		}

		this.input = input;
	}

	public File getOutput(){
		return this.output;
	}

	public void setOutput(File output){

		if(output == null){
			throw new NullPointerException();
		}

		this.output = output;
	}

	private static final Logger logger = Logger.getLogger(Main.class.getName());
}