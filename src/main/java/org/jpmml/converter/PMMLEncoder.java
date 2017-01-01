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

import org.dmg.pmml.DataDictionary;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.DefineFunction;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Expression;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.Header;
import org.dmg.pmml.OpType;
import org.dmg.pmml.PMML;
import org.dmg.pmml.TransformationDictionary;
import org.dmg.pmml.TypeDefinitionField;

public class PMMLEncoder {

	private Map<FieldName, DataField> dataFields = new LinkedHashMap<>();

	private Map<FieldName, DerivedField> derivedFields = new LinkedHashMap<>();

	private Map<String, DefineFunction> defineFunctions = new LinkedHashMap<>();


	public PMML encodePMML(){

		if(!Collections.disjoint(this.dataFields.keySet(), this.derivedFields.keySet())){
			throw new IllegalArgumentException();
		}

		List<DataField> dataFields = new ArrayList<>(this.dataFields.values());

		DataDictionary dataDictionary = new DataDictionary();

		if(dataFields.size() > 0){
			(dataDictionary.getDataFields()).addAll(dataFields);
		}

		List<DerivedField> derivedFields = new ArrayList<>(this.derivedFields.values());
		List<DefineFunction> defineFunctions = new ArrayList<>(this.defineFunctions.values());

		TransformationDictionary transformationDictionary = null;

		if(derivedFields.size() > 0 || defineFunctions.size() > 0){
			transformationDictionary = new TransformationDictionary();

			if(derivedFields.size() > 0){
				(transformationDictionary.getDerivedFields()).addAll(derivedFields);
			} // End if

			if(defineFunctions.size() > 0){
				(transformationDictionary.getDefineFunctions()).addAll(defineFunctions);
			}
		}

		Header header = encodeHeader();

		PMML pmml = new PMML("4.3", header, dataDictionary)
			.setTransformationDictionary(transformationDictionary);

		return pmml;
	}

	public Header encodeHeader(){
		return PMMLUtil.createHeader(getClass());
	}

	public TypeDefinitionField getField(FieldName name){
		DataField dataField = getDataField(name);
		DerivedField derivedField = getDerivedField(name);

		if(dataField != null && derivedField == null){
			return dataField;
		} else

		if(dataField == null && derivedField != null){
			return derivedField;
		}

		throw new IllegalArgumentException();
	}

	public DataField getDataField(FieldName name){
		return this.dataFields.get(name);
	}

	public void addDataField(DataField dataField){
		FieldName name = dataField.getName();

		checkName(name);

		this.dataFields.put(name, dataField);
	}

	public DataField createDataField(FieldName name, OpType opType, DataType dataType){
		DataField dataField = new DataField(name, opType, dataType);

		addDataField(dataField);

		return dataField;
	}

	public DerivedField getDerivedField(FieldName name){
		return this.derivedFields.get(name);
	}

	public void addDerivedField(DerivedField derivedField){
		FieldName name = derivedField.getName();

		checkName(name);

		this.derivedFields.put(name, derivedField);
	}

	public DerivedField createDerivedField(FieldName name, OpType opType, DataType dataType, Expression expression){
		DerivedField derivedField = new DerivedField(opType, dataType)
			.setName(name)
			.setExpression(expression);

		addDerivedField(derivedField);

		return derivedField;
	}

	public DefineFunction getDefineFunction(String name){
		return this.defineFunctions.get(name);
	}

	public void addDefineFunction(DefineFunction defineFunction){
		String name = defineFunction.getName();

		if(name == null){
			throw new NullPointerException();
		} // End if

		if(this.defineFunctions.containsKey(name)){
			throw new IllegalArgumentException(name);
		}

		this.defineFunctions.put(name, defineFunction);
	}

	protected Map<FieldName, DataField> getDataFields(){
		return this.dataFields;
	}

	protected Map<FieldName, DerivedField> getDerivedFields(){
		return this.derivedFields;
	}

	private void checkName(FieldName name){

		if(name == null){
			throw new NullPointerException();
		} // End if

		if(this.dataFields.containsKey(name) || this.derivedFields.containsKey(name)){
			throw new IllegalArgumentException((name.getValue()).toString());
		}
	}
}