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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.google.common.base.Function;
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
import org.dmg.pmml.RealSparseArray;
import org.dmg.pmml.Timestamp;
import org.dmg.pmml.Value;

public class PMMLUtil {

	private PMMLUtil(){
	}

	static
	public Header createHeader(Class<?> clazz){
		Package _package = clazz.getPackage();

		return createHeader(_package.getImplementationTitle(), _package.getImplementationVersion());
	}

	static
	public Header createHeader(String name, String version){
		Application application = new Application()
			.setName(name)
			.setVersion(version);

		return createHeader(application);
	}

	static
	public Header createHeader(Application application){
		Date now = new Date();

		// XML Schema "dateTime" data format (corresponds roughly to ISO 8601)
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dateFormat.setTimeZone(PMMLUtil.UTC);

		Timestamp timestamp = new Timestamp()
			.addContent(dateFormat.format(now));

		Header header = new Header()
			.setApplication(application)
			.setTimestamp(timestamp);

		return header;
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
	public List<String> getValues(DataField dataField){
		return getValues(dataField, Value.Property.VALID);
	}

	static
	public List<String> getValues(DataField dataField, Value.Property property){
		List<String> result = new ArrayList<>();

		List<Value> values = dataField.getValues();
		for(Value value : values){

			if((value.getProperty()).equals(property)){
				result.add(value.getValue());
			}
		}

		return result;
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