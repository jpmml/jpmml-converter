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

import java.util.List;
import java.util.Objects;

import org.dmg.pmml.DataType;
import org.dmg.pmml.Decorable;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Expression;
import org.dmg.pmml.Extension;
import org.dmg.pmml.Interval;
import org.dmg.pmml.Model;
import org.dmg.pmml.OpType;
import org.dmg.pmml.Output;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.Value;
import org.dmg.pmml.Visitor;
import org.dmg.pmml.VisitorAction;

public class DerivedOutputField extends DerivedField implements Decorable {

	private Model model = null;

	private OutputField outputField = null;

	private boolean required = false;


	public DerivedOutputField(Model model, OutputField outputField, boolean required){
		setModel(model);
		setOutputField(outputField);
		setRequired(required);
	}

	@Override
	public String getName(){
		OutputField outputField = getOutputField();

		return outputField.getName();
	}

	@Override
	public DerivedOutputField setName(String name){
		OutputField outputField = getOutputField();

		outputField.setName(name);

		return this;
	}

	@Override
	public String getDisplayName(){
		OutputField outputField = getOutputField();

		return outputField.getDisplayName();
	}

	@Override
	public DerivedOutputField setDisplayName(String displayName){
		OutputField outputField = getOutputField();

		outputField.setDisplayName(displayName);

		return this;
	}

	@Override
	public OpType getOpType(){
		OutputField outputField = getOutputField();

		return outputField.getOpType();
	}

	@Override
	public DerivedOutputField setOpType(OpType opType){
		OutputField outputField = getOutputField();

		outputField.setOpType(opType);

		return this;
	}

	@Override
	public DataType getDataType(){
		OutputField outputField = getOutputField();

		return outputField.getDataType();
	}

	@Override
	public DerivedOutputField setDataType(DataType dataType){
		OutputField outputField = getOutputField();

		outputField.setDataType(dataType);

		return this;
	}

	@Override
	public boolean hasExtensions(){
		OutputField outputField = getOutputField();

		return outputField.hasExtensions();
	}

	@Override
	public List<Extension> getExtensions(){
		OutputField outputField = getOutputField();

		return outputField.getExtensions();
	}

	@Override
	public Expression getExpression(){
		return null;
	}

	@Override
	public DerivedOutputField setExpression(Expression expression){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasIntervals(){
		return false;
	}

	@Override
	public List<Interval> getIntervals(){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasValues(){
		OutputField outputField = getOutputField();

		return outputField.hasValues();
	}

	@Override
	public List<Value> getValues(){
		OutputField outputField = getOutputField();

		return outputField.getValues();
	}

	public void addOutputField(){
		addOutputField(-1);
	}

	public void addOutputField(int index){
		Model model = getModel();
		OutputField outputField = getOutputField();

		Output output = ModelUtil.ensureOutput(model);

		List<OutputField> outputFields = output.getOutputFields();

		if(index > -1){
			outputFields.add(index, outputField);
		} else

		{
			outputFields.add(outputField);
		}
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

	public boolean isRequired(){
		return this.required;
	}

	private void setRequired(boolean required){
		this.required = required;
	}

	@Override
	public VisitorAction accept(Visitor visitor){
		return super.accept(visitor);
	}
}