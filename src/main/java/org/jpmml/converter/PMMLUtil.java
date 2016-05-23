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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.dmg.pmml.Application;
import org.dmg.pmml.Apply;
import org.dmg.pmml.Array;
import org.dmg.pmml.Constant;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.Expression;
import org.dmg.pmml.Field;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.Header;
import org.dmg.pmml.OpType;
import org.dmg.pmml.RealSparseArray;
import org.dmg.pmml.Timestamp;
import org.dmg.pmml.Value;
import org.jpmml.model.visitors.FieldReferenceFinder;

public class PMMLUtil {

	private PMMLUtil(){
	}

	static
	public Header createHeader(String name, String version){
		Application application = new Application()
			.setName(name)
			.setVersion(version);

		// XML Schema "dateTime" data format (corresponds roughly to ISO 8601)
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dateFormat.setTimeZone(PMMLUtil.UTC);

		Date now = new Date();

		Timestamp timestamp = new Timestamp()
			.addContent(dateFormat.format(now));

		Header header = new Header()
			.setApplication(application)
			.setTimestamp(timestamp);

		return header;
	}

	static
	public DataField createDataField(FieldName name, boolean categorical){
		return createDataField(name, categorical ? DataType.STRING : DataType.DOUBLE);
	}

	static
	public DataField createDataField(FieldName name, DataType dataType){
		DataField dataField = new DataField()
			.setName(name);

		dataField = refineDataField(dataField, dataType);

		return dataField;
	}

	static
	public List<DataField> createDataFields(FieldReferenceFinder fieldReferenceFinder){
		Set<FieldName> names = fieldReferenceFinder.getFieldNames();

		Function<FieldName, DataField> function = new Function<FieldName, DataField>(){

			@Override
			public DataField apply(FieldName name){
				DataField dataField = new DataField()
					.setName(name);

				return dataField;
			}
		};

		List<DataField> dataFields = Lists.newArrayList(Iterables.transform(names, function));

		Collections.sort(dataFields, new FieldComparator<>());

		return dataFields;
	}

	static
	public DataField refineDataField(DataField dataField){
		Function<Value, String> function = new Function<Value, String>(){

			@Override
			public String apply(Value value){
				return value.getValue();
			}
		};

		DataType dataType = ValueUtil.getDataType(Lists.transform(dataField.getValues(), function));

		return refineDataField(dataField, dataType);
	}

	static
	public DataField refineDataField(DataField dataField, DataType dataType){
		List<Value> values = dataField.getValues();

		switch(dataType){
			case STRING:
				return dataField.setDataType(DataType.STRING)
					.setOpType(OpType.CATEGORICAL);
			case DOUBLE:
			case FLOAT:
				return dataField.setDataType(dataType)
					.setOpType(values.size() > 0 ? OpType.CATEGORICAL : OpType.CONTINUOUS);
			case INTEGER:
				return dataField.setDataType(DataType.INTEGER)
					.setOpType(values.size() > 0 ? OpType.CATEGORICAL : OpType.CONTINUOUS);
			case BOOLEAN:
				return dataField.setDataType(DataType.BOOLEAN)
					.setOpType(OpType.CATEGORICAL);
			default:
				throw new IllegalArgumentException();
		}
	}

	static
	public List<DataField> refineDataFields(List<DataField> dataFields, FieldTypeAnalyzer fieldTypeAnalyzer){

		for(DataField dataField : dataFields){
			DataType dataType = fieldTypeAnalyzer.getDataType(dataField.getName());

			if(dataType == null){
				continue;
			}

			dataField = refineDataField(dataField, dataType);
		}

		return dataFields;
	}

	static
	public List<Value> createValues(List<String> values){
		return createValues(values, null);
	}

	static
	public List<Value> createValues(List<String> values, final Value.Property property){
		Function<String, Value> function = new Function<String, Value>(){

			@Override
			public Value apply(String string){
				Value value = new Value(string)
					.setProperty(property);

				return value;
			}
		};

		return Lists.newArrayList(Lists.transform(values, function));
	}

	static
	public Apply createApply(String function, Expression... expressions){
		Apply apply = new Apply(function)
			.addExpressions(expressions);

		return apply;
	}

	static
	public Constant createConstant(Object value){
		Constant constant = new Constant(ValueUtil.formatValue(value));

		if(value instanceof Double){
			constant.setDataType(DataType.DOUBLE);
		}

		return constant;
	}

	static
	public Array createArray(DataType dataType, List<Value> values){
		Function<Value, String> function = new Function<Value, String>(){

			@Override
			public String apply(Value value){
				return ValueUtil.formatValue(value.getValue());
			}
		};

		String value = ValueUtil.formatArrayValue(Lists.transform(values, function));

		switch(dataType){
			case STRING:
				return new Array(Array.Type.STRING, value);
			case DOUBLE:
			case FLOAT:
				return new Array(Array.Type.REAL, value);
			case INTEGER:
				return new Array(Array.Type.INT, value);
			default:
				throw new IllegalArgumentException();
		}
	}

	static
	public Array createRealArray(List<? extends Number> values){
		Function<Number, String> function = new Function<Number, String>(){

			@Override
			public String apply(Number number){
				return ValueUtil.formatValue(number);
			}
		};

		String value = ValueUtil.formatArrayValue(Lists.transform(values, function));

		Array array = new Array(Array.Type.REAL, value);

		return array;
	}

	static
	public RealSparseArray createRealSparseArray(List<? extends Number> values, Double defaultValue){
		RealSparseArray sparseArray = new RealSparseArray()
			.setN(values.size())
			.setDefaultValue(defaultValue);

		int index = 1;

		for(Number value : values){

			if(!ValueUtil.equals(value, defaultValue)){
				sparseArray.addIndices(index);
				sparseArray.addEntries(ValueUtil.asDouble(value));
			}

			index++;
		}

		return sparseArray;
	}

	static
	public List<FieldName> getNames(List<? extends Field> fields){
		Function<Field, FieldName> function = new Function<Field, FieldName>(){

			@Override
			public FieldName apply(Field field){
				return field.getName();
			}
		};

		return Lists.newArrayList(Lists.transform(fields, function));
	}

	static
	public <F extends Field> F getField(FieldName name, List<F> fields){
		return getField(name, fields, 0);
	}

	static
	public <F extends Field> F getField(FieldName name, List<F> fields, int offset){

		for(int i = offset; i < fields.size(); i++){
			F field = fields.get(i);

			if((name).equals(field.getName())){
				return field;
			}
		}

		throw new IllegalArgumentException();
	}

	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
}