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
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;

import javax.xml.namespace.QName;

import jakarta.xml.bind.JAXBElement;
import org.dmg.pmml.Apply;
import org.dmg.pmml.Array;
import org.dmg.pmml.ComplexArray;
import org.dmg.pmml.Constant;
import org.dmg.pmml.DataType;
import org.dmg.pmml.DefineFunction;
import org.dmg.pmml.Expression;
import org.dmg.pmml.Extension;
import org.dmg.pmml.Field;
import org.dmg.pmml.FieldColumnPair;
import org.dmg.pmml.HasDiscreteDomain;
import org.dmg.pmml.Header;
import org.dmg.pmml.InlineTable;
import org.dmg.pmml.MapValues;
import org.dmg.pmml.NamespacePrefixes;
import org.dmg.pmml.NamespaceURIs;
import org.dmg.pmml.PMMLFunctions;
import org.dmg.pmml.RealSparseArray;
import org.dmg.pmml.Row;
import org.dmg.pmml.Timestamp;
import org.dmg.pmml.Value;
import org.jpmml.model.cells.InputCell;
import org.jpmml.model.cells.OutputCell;

public class PMMLUtil {

	private PMMLUtil(){
	}

	static
	public Extension createExtension(String name, String value){
		Extension extension = new Extension()
			.setName(name)
			.setValue(value);

		return extension;
	}

	static
	public Extension createExtension(String name, Object... content){
		Extension extension = new Extension()
			.setName(name)
			.addContent(content);

		return extension;
	}

	static
	public Header createHeader(PMMLEncoder encoder){
		Class<?> clazz = encoder.getClass();

		Application application = Application.getInstance();
		if(application != null){
			clazz = application.getClass();
		}

		Package _package = clazz.getPackage();

		return createHeader(_package.getImplementationTitle(), _package.getImplementationVersion());
	}

	static
	public Header createHeader(String name, String version){

		if(name == null){
			name = "JPMML-Converter";
		}

		org.dmg.pmml.Application pmmlApplication = new org.dmg.pmml.Application()
			.setName(name)
			.setVersion(version);

		return createHeader(pmmlApplication);
	}

	static
	public Header createHeader(org.dmg.pmml.Application pmmlApplication){
		Date now = new Date();

		// XML Schema "dateTime" data format (corresponds roughly to ISO 8601)
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dateFormat.setTimeZone(PMMLUtil.UTC);

		Timestamp timestamp = new Timestamp()
			.addContent(dateFormat.format(now));

		Header header = new Header()
			.setApplication(pmmlApplication)
			.setTimestamp(timestamp);

		return header;
	}

	static
	public <F extends Field<F> & HasDiscreteDomain<F>> List<?> getValues(F field){
		return getValues(field, null);
	}

	static
	public <F extends Field<F> & HasDiscreteDomain<F>> List<?> getValues(F field, Value.Property property){
		List<Object> result = new ArrayList<>();

		if(property == null){
			property = Value.Property.VALID;
		}

		List<Value> pmmlValues = field.getValues();
		for(Value pmmlValue : pmmlValues){

			if(property == pmmlValue.getProperty()){
				result.add(pmmlValue.requireValue());
			}
		}

		return result;
	}

	static
	public <F extends Field<F> & HasDiscreteDomain<F>> void addValues(F field, List<?> values){
		addValues(field, null, values);
	}

	static
	public <F extends Field<F> & HasDiscreteDomain<F>> void addValues(F field, Value.Property property, List<?> values){

		if(property == Value.Property.VALID){
			property = null;
		}

		List<Value> pmmlValues = field.getValues();
		for(Object value : values){
			Value pmmlValue = new Value(value)
				.setProperty(property);

			pmmlValues.add(pmmlValue);
		}
	}

	static
	public <F extends Field<F> & HasDiscreteDomain<F>> void clearValues(F field, Value.Property property){

		if(property == null){
			property = Value.Property.VALID;
		}

		List<Value> pmmlValues = field.getValues();
		for(Iterator<Value> it = pmmlValues.iterator(); it.hasNext(); ){
			Value pmmlValue = it.next();

			if(pmmlValue.getProperty() == property){
				it.remove();
			}
		}
	}

	static
	public Apply createApply(DefineFunction defineFunction, Expression... expressions){
		return createApply(defineFunction.requireName(), expressions);
	}

	static
	public Apply createApply(String function, Expression... expressions){
		Apply apply = new Apply(function)
			.addExpressions(expressions);

		return apply;
	}

	static
	public Constant createMissingConstant(){
		return createConstant(null, null);
	}

	static
	public Constant createConstant(Number value){

		if(value == null){
			throw new IllegalArgumentException();
		}

		return createConstant(value, TypeUtil.getDataType(value));
	}

	static
	public Constant createConstant(Object value, DataType dataType){
		Constant constant = new Constant(value)
			.setDataType(dataType)
			.setMissing(value == null);

		return constant;
	}

	static
	public MapValues createMapValues(String name, Map<?, ?> mapping){
		List<Object> inputValues = new ArrayList<>();
		List<Object> outputValues = new ArrayList<>();

		Collection<? extends Map.Entry<?, ?>> entries = mapping.entrySet();
		for(Map.Entry<?, ?> entry : entries){
			inputValues.add(entry.getKey());
			outputValues.add(entry.getValue());
		}

		return createMapValues(name, inputValues, outputValues);
	}

	static
	public MapValues createMapValues(String name, List<?> inputValues, List<?> outputValues){
		String inputColumn = NamespacePrefixes.JPMML_INLINETABLE + ":input";
		String outputColumn = NamespacePrefixes.JPMML_INLINETABLE + ":output";

		Map<String, List<?>> data = new LinkedHashMap<>();
		data.put(inputColumn, inputValues);
		data.put(outputColumn, outputValues);

		MapValues mapValues = new MapValues(outputColumn, PMMLUtil.createInlineTable(data))
			.addFieldColumnPairs(new FieldColumnPair(name, inputColumn));

		return mapValues;
	}

	static
	public Expression toNegative(Expression expression){

		if(expression instanceof Constant){
			Constant constant = (Constant)expression;

			constant.setValue(ValueUtil.toNegative(constant.getValue()));

			return constant;
		}

		return createApply(PMMLFunctions.MULTIPLY, createConstant(-1), expression);
	}

	static
	public Array createArray(DataType dataType, List<?> values){

		switch(dataType){
			case STRING:
				return PMMLUtil.createStringArray(values);
			case INTEGER:
				return PMMLUtil.createIntArray((List)values);
			case FLOAT:
			case DOUBLE:
				return PMMLUtil.createRealArray((List)values);
			default:
				throw new IllegalArgumentException();
		}
	}

	static
	public Array createStringArray(List<?> values){
		Array array = new ComplexArray()
			.setType(Array.Type.STRING)
			.setValue(values);

		return array;
	}

	static
	public Array createIntArray(List<Integer> values){
		Array array = new ComplexArray()
			.setType(Array.Type.INT)
			.setValue(values);

		return array;
	}

	static
	public Array createRealArray(List<? extends Number> values){
		Array array = new ComplexArray()
			.setType(Array.Type.REAL)
			.setValue(values);

		return array;
	}

	static
	public RealSparseArray createRealSparseArray(List<? extends Number> values, Double defaultValue){
		RealSparseArray sparseArray = new RealSparseArray()
			.setN(values.size())
			.setDefaultValue(defaultValue);

		List<Integer> indices = sparseArray.getIndices();
		List<Double> entries = sparseArray.getEntries();

		int index = 1;

		for(Number value : values){

			if(!ValueUtil.equals(value, defaultValue)){
				indices.add(index);
				entries.add(ValueUtil.asDouble(value));
			}

			index++;
		}

		return sparseArray;
	}

	static
	public InlineTable createInlineTable(Map<String, ? extends List<?>> data){
		return createInlineTable(Function.identity(), data);
	}

	static
	public <K> InlineTable createInlineTable(Function<K, String> function, Map<K, ? extends List<?>> data){
		int rows = 0;

		Map<K, QName> columns = new LinkedHashMap<>();

		{
			Collection<? extends Map.Entry<K, ? extends List<?>>> entries = data.entrySet();
			for(Map.Entry<K, ? extends List<?>> entry : entries){
				K column = entry.getKey();
				List<?> columnData = entry.getValue();

				if(rows == 0){
					rows = columnData.size();
				} else

				{
					if(rows != columnData.size()){
						throw new IllegalArgumentException();
					}
				}

				QName columnName;

				String tagName = function.apply(column);
				if(tagName.startsWith(NamespacePrefixes.JPMML_INLINETABLE + ":")){
					columnName = new QName(NamespaceURIs.JPMML_INLINETABLE, tagName.substring((NamespacePrefixes.JPMML_INLINETABLE + ":").length()), NamespacePrefixes.JPMML_INLINETABLE);
				} else

				{
					if(tagName.indexOf(':') > -1){
						throw new IllegalArgumentException(tagName);
					}

					columnName = new QName(PMMLEncoder.VERSION.getNamespaceURI(), tagName);
				}

				columns.put(column, columnName);
			}
		}

		QName inputColumnName = InputCell.QNAME;
		QName outputColumnName = OutputCell.QNAME;

		InlineTable inlineTable = new InlineTable();

		for(int i = 0; i < rows; i++){
			Row row = new Row();

			Collection<Map.Entry<K, QName>> entries = columns.entrySet();
			for(Map.Entry<K, QName> entry : entries){
				List<?> columnData = data.get(entry.getKey());

				Object value = columnData.get(i);
				if(value == null){
					continue;
				}

				QName columName = entry.getValue();

				Object cell;

				if((inputColumnName).equals(columName)){
					cell = new InputCell(value);
				} else

				if((outputColumnName).equals(columName)){
					cell = new OutputCell(value);
				} else

				{
					cell = new JAXBElement<>(columName, String.class, ValueUtil.asString(value));
				}

				row.addContent(cell);
			}

			inlineTable.addRows(row);
		}

		return inlineTable;
	}

	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
}