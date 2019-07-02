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
import org.dmg.pmml.Model;
import org.dmg.pmml.Output;
import org.dmg.pmml.OutputField;

public class DerivedOutputField extends DerivedField {

	private Model model = null;

	private OutputField outputField = null;


	public DerivedOutputField(Model model, OutputField outputField){
		super(outputField.getName(), outputField.getOpType(), outputField.getDataType(), null);

		setModel(model);
		setOutputField(outputField);
	}

	public void addOutputField(){
		Model model = getModel();
		OutputField outputField = getOutputField();

		Output output = ModelUtil.ensureOutput(model);

		output.addOutputFields(outputField);
	}

	public Model getModel(){
		return this.model;
	}

	private void setModel(Model model){
		this.model = Objects.requireNonNull(model);
	}

	public OutputField getOutputField(){
		return this.outputField;
	}

	private void setOutputField(OutputField outputField){
		this.outputField = Objects.requireNonNull(outputField);
	}
}