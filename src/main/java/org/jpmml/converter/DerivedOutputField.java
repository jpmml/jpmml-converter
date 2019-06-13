/*
 * Copyright (c) 2019 Villu Ruusmann
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

import java.util.Objects;

import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Output;
import org.dmg.pmml.OutputField;

public class DerivedOutputField extends DerivedField {

	private Output output = null;

	private OutputField outputField = null;


	public DerivedOutputField(Output output, OutputField outputField){
		super(outputField.getName(), outputField.getOpType(), outputField.getDataType(), null);

		setOutput(output);
		setOutputField(outputField);
	}

	public void addOutputField(){
		Output output = getOutput();
		OutputField outputField = getOutputField();

		output.addOutputFields(outputField);
	}

	public Output getOutput(){
		return this.output;
	}

	private void setOutput(Output output){
		this.output = Objects.requireNonNull(output);
	}

	public OutputField getOutputField(){
		return this.outputField;
	}

	private void setOutputField(OutputField outputField){
		this.outputField = Objects.requireNonNull(outputField);
	}
}