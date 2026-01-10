/*
 * Copyright (c) 2016 Villu Ruusmann
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.dmg.pmml.DataDictionary;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.DefineFunction;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Expression;
import org.dmg.pmml.Field;
import org.dmg.pmml.HasDiscreteDomain;
import org.dmg.pmml.Header;
import org.dmg.pmml.Model;
import org.dmg.pmml.OpType;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.PMML;
import org.dmg.pmml.TransformationDictionary;
import org.dmg.pmml.Version;

public class PMMLEncoder {

	private Map<String, DataField> dataFields = new LinkedHashMap<>();

	private Map<String, DerivedField> derivedFields = new LinkedHashMap<>();

	private Map<String, DefineFunction> defineFunctions = new LinkedHashMap<>();


	public PMML encodePMML(){

		if(!Collections.disjoint(this.dataFields.keySet(), this.derivedFields.keySet())){
			throw new IllegalStateException();
		}

		List<DataField> dataFields = new ArrayList<>(this.dataFields.values());

		DataDictionary dataDictionary = new DataDictionary();

		if(!dataFields.isEmpty()){
			(dataDictionary.getDataFields()).addAll(dataFields);
		}

		List<DerivedField> derivedFields = new ArrayList<>(this.derivedFields.values());
		List<DefineFunction> defineFunctions = new ArrayList<>(this.defineFunctions.values());

		TransformationDictionary transformationDictionary = null;

		if(!derivedFields.isEmpty() || !defineFunctions.isEmpty()){
			transformationDictionary = new TransformationDictionary();

			if(!derivedFields.isEmpty()){
				(transformationDictionary.getDerivedFields()).addAll(derivedFields);
			} // End if

			if(!defineFunctions.isEmpty()){
				(transformationDictionary.getDefineFunctions()).addAll(defineFunctions);
			}
		}

		Header header = encodeHeader();

		PMML pmml = new PMML(PMMLEncoder.VERSION.getVersion(), header, dataDictionary)
			.setTransformationDictionary(transformationDictionary);

		return pmml;
	}

	public Header encodeHeader(){
		return PMMLUtil.createHeader(this);
	}

	public DataField getDataField(String name){
		return this.dataFields.get(name);
	}

	public void addDataField(DataField dataField){
		String name = checkName(dataField);

		this.dataFields.put(name, dataField);
	}

	public DataField createDataField(String name, OpType opType, DataType dataType){
		return createDataField(name, opType, dataType, null);
	}

	public DataField createDataField(String name, OpType opType, DataType dataType, List<?> values){
		DataField dataField = new DataField(name, opType, dataType);

		if(values != null && !values.isEmpty()){
			FieldUtil.addValues(dataField, values);
		}

		addDataField(dataField);

		return dataField;
	}

	public DataField removeDataField(String name){
		DataField dataField = this.dataFields.remove(name);

		if(dataField == null){
			throw new SchemaException("Field " + ExceptionUtil.formatName(name) + " is not defined");
		}

		return dataField;
	}

	public DerivedField ensureDerivedField(String name, OpType opType, DataType dataType, Supplier<? extends Expression> expressionSupplier){
		DerivedField derivedField = getDerivedField(name);

		if(derivedField == null){
			Expression expression = expressionSupplier.get();

			derivedField = createDerivedField(name, opType, dataType, expression);
		}

		return derivedField;
	}

	public DerivedField getDerivedField(String name){
		return this.derivedFields.get(name);
	}

	public void addDerivedField(DerivedField derivedField){
		String name = checkName(derivedField);

		this.derivedFields.put(name, derivedField);
	}

	public DerivedField createDerivedField(String name, OpType opType, DataType dataType, Expression expression){
		DerivedField derivedField = new DerivedField(name, opType, dataType, expression);

		addDerivedField(derivedField);

		return derivedField;
	}

	public DerivedOutputField createDerivedField(Model model, OutputField outputField, boolean required){
		DerivedOutputField derivedField = new DerivedOutputField(model, outputField, required);

		addDerivedField(derivedField);

		return derivedField;
	}

	public DerivedField removeDerivedField(String name){
		DerivedField derivedField = this.derivedFields.remove(name);

		if(derivedField == null){
			throw new SchemaException("Field " + ExceptionUtil.formatName(name) + " is not defined");
		}

		return derivedField;
	}

	public Field<?> getField(String name){
		DataField dataField = getDataField(name);
		DerivedField derivedField = getDerivedField(name);

		if(dataField != null && derivedField != null){
			throw new IllegalStateException();
		} // End if

		if(dataField != null && derivedField == null){
			return dataField;
		} else

		if(dataField == null && derivedField != null){
			return derivedField;
		}

		throw new SchemaException("Field " + ExceptionUtil.formatName(name) + " is not defined");
	}

	public Field<?> toContinuous(String name){
		Field<?> field = getField(name);

		DataType dataType = field.requireDataType();
		switch(dataType){
			case INTEGER:
			case FLOAT:
			case DOUBLE:
				break;
			default:
				throw new InvalidFeatureException("Expected numeric data type, got " + ExceptionUtil.formatValue(dataType));
		}

		field.setOpType(OpType.CONTINUOUS);

		return field;
	}

	public Field<?> toCategorical(String name, List<?> values){
		return toDiscrete(name, OpType.CATEGORICAL, values);
	}

	public Field<?> toOrdinal(String name, List<?> values){
		return toDiscrete(name, OpType.ORDINAL, values);
	}

	public DefineFunction getDefineFunction(String name){
		return this.defineFunctions.get(name);
	}

	public void addDefineFunction(DefineFunction defineFunction){
		String name = checkName(defineFunction);

		this.defineFunctions.put(name, defineFunction);
	}

	public Map<String, DataField> getDataFields(){
		return this.dataFields;
	}

	public Map<String, DerivedField> getDerivedFields(){
		return this.derivedFields;
	}

	public Map<String, DefineFunction> getDefineFunctions(){
		return this.defineFunctions;
	}

	private Field<?> toDiscrete(String name, OpType opType, List<?> values){
		Field<?> field = getField(name);

		values:
		if(field instanceof HasDiscreteDomain){

			if(values == null || values.isEmpty()){
				break values;
			}

			List<?> existingValues = FieldUtil.getValues((Field & HasDiscreteDomain)field);
			if(existingValues != null && !existingValues.isEmpty()){

				if((existingValues).equals(values)){
					break values;
				}

				throw new InvalidFeatureException("Expected " + existingValues + " as categories, got " + values);
			}

			FieldUtil.addValues((Field & HasDiscreteDomain)field, values);
		}

		field.setOpType(opType);

		return field;
	}

	private String checkName(DefineFunction defineFunction){
		String name = defineFunction.requireName();

		if(this.defineFunctions.containsKey(name)){
			throw new NamingException("Function " + ExceptionUtil.formatName(name) + " is already defined")
				.setSolution("Choose a different name");
		}

		return name;
	}

	private String checkName(Field<?> field){
		String name = field.requireName();

		if(this.dataFields.containsKey(name) || this.derivedFields.containsKey(name)){
			throw new NamingException("Field " + ExceptionUtil.formatName(name) + " is already defined")
				.setSolution("Choose a different name");
		}

		return name;
	}

	public static final Version VERSION = Version.PMML_4_4;
}