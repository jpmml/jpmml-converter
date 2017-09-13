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

import org.dmg.pmml.Application;
import org.dmg.pmml.Apply;
import org.dmg.pmml.Array;
import org.dmg.pmml.Constant;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.Expression;
import org.dmg.pmml.Header;
import org.dmg.pmml.Interval;
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
	public List<String> getValues(DataField dataField){
		return getValues(dataField, null);
	}

	static
	public List<String> getValues(DataField dataField, Value.Property property){
		List<String> result = new ArrayList<>();

		if(property == null){
			property = Value.Property.VALID;
		}

		List<Value> pmmlValues = dataField.getValues();
		for(Value pmmlValue : pmmlValues){

			if((property).equals(pmmlValue.getProperty())){
				result.add(pmmlValue.getValue());
			}
		}

		return result;
	}

	static
	public void addValues(DataField dataField, List<String> values){
		addValues(dataField, values, null);
	}

	static
	public void addValues(DataField dataField, List<String> values, Value.Property property){

		if((Value.Property.VALID).equals(property)){
			property = null;
		}

		List<Value> pmmlValues = dataField.getValues();
		for(String value : values){
			Value pmmlValue = new Value(value)
				.setProperty(property);

			pmmlValues.add(pmmlValue);
		}
	}

	static
	public void addIntervals(DataField dataField, List<Interval> intervals){
		(dataField.getIntervals()).addAll(intervals);
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
	public Array createStringArray(List<?> values){
		Array array = new Array(Array.Type.STRING, ValueUtil.formatArray(values));

		return array;
	}

	static
	public Array createIntArray(List<Integer> values){
		Array array = new Array(Array.Type.INT, ValueUtil.formatArray(values));

		return array;
	}

	static
	public Array createRealArray(List<? extends Number> values){
		Array array = new Array(Array.Type.REAL, ValueUtil.formatArray(values));

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

	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
}