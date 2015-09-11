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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import org.dmg.pmml.Application;
import org.dmg.pmml.Apply;
import org.dmg.pmml.Array;
import org.dmg.pmml.Constant;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.Entity;
import org.dmg.pmml.Expression;
import org.dmg.pmml.FeatureType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldUsageType;
import org.dmg.pmml.Header;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.OpType;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.Timestamp;
import org.dmg.pmml.Value;

public class PMMLUtil {

	private PMMLUtil(){
	}

	static
	public DataField createDataField(FieldName name, boolean categorical){
		return createDataField(name, categorical ? DataType.STRING : DataType.DOUBLE);
	}

	static
	public DataField createDataField(FieldName name, String type){
		DataType dataType;

		if("factor".equals(type)){
			dataType = DataType.STRING;
		} else

		if("numeric".equals(type)){
			dataType = DataType.DOUBLE;
		} else

		if("logical".equals(type)){
			dataType = DataType.BOOLEAN;
		} else

		{
			throw new IllegalArgumentException(type);
		}

		return createDataField(name, dataType);
	}

	static
	public DataField createDataField(FieldName name, DataType dataType){
		DataField dataField = new DataField()
			.setName(name);

		dataField = refineDataField(dataField, dataType);

		return dataField;
	}

	static
	public List<DataField> createDataFields(FieldCollector fieldCollector){
		Set<FieldName> names = fieldCollector.getFields();

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
		DataType dataType = getDataType(dataField.getValues());

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

		return Lists.transform(values, function);
	}

	static
	public MiningField createMiningField(FieldName name){
		return createMiningField(name, null);
	}

	static
	public MiningField createMiningField(FieldName name, FieldUsageType usageType){
		MiningField miningField = new MiningField(name)
			.setUsageType(usageType);

		return miningField;
	}

	static
	public OutputField createAffinityField(String value){
		return createAffinityField(FieldName.create("affinity_" + value), value);
	}

	static
	public OutputField createAffinityField(FieldName name, String value){
		OutputField outputField = new OutputField(name)
			.setFeature(FeatureType.AFFINITY)
			.setValue(value);

		return outputField;
	}

	static
	public List<OutputField> createAffinityFields(List<? extends Entity> entities){
		Function<Entity, OutputField> function = new Function<Entity, OutputField>(){

			@Override
			public OutputField apply(Entity entity){
				return createAffinityField(entity.getId());
			}
		};

		return Lists.newArrayList(Lists.transform(entities, function));
	}

	static
	public OutputField createEntityIdField(FieldName name){
		OutputField outputField = new OutputField(name)
			.setFeature(FeatureType.ENTITY_ID);

		return outputField;
	}

	static
	public OutputField createPredictedField(FieldName name){
		OutputField outputField = new OutputField(name)
			.setFeature(FeatureType.PREDICTED_VALUE);

		return outputField;
	}

	static
	public OutputField createProbabilityField(String value){
		return createProbabilityField(FieldName.create("probability_" + value), value);
	}

	static
	public OutputField createProbabilityField(FieldName name, String value){
		OutputField outputField = new OutputField(name)
			.setFeature(FeatureType.PROBABILITY)
			.setValue(value);

		return outputField;
	}

	static
	public List<OutputField> createProbabilityFields(DataField dataField){
		Function<Value, OutputField> function = new Function<Value, OutputField>(){

			@Override
			public OutputField apply(Value value){
				return createProbabilityField(value.getValue());
			}
		};

		return Lists.newArrayList(Lists.transform(dataField.getValues(), function));
	}

	static
	public Header createHeader(){
		Application application = new Application()
			.setName("JPMML-Converter")
			.setVersion("1.0-SNAPSHOT");

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
	public MiningSchema createMiningSchema(List<DataField> dataFields){
		return createMiningSchema(dataFields.get(0), dataFields.subList(1, dataFields.size()));
	}

	static
	public MiningSchema createMiningSchema(DataField targetDataField, List<DataField> activeDataFields){
		Function<DataField, FieldName> function = new Function<DataField, FieldName>(){

			@Override
			public FieldName apply(DataField dataField){

				if(dataField == null){
					return null;
				}

				return dataField.getName();
			}
		};

		return createMiningSchema(function.apply(targetDataField), Lists.transform(activeDataFields, function));
	}

	static
	public MiningSchema createMiningSchema(FieldCollector fieldCollector){
		return createMiningSchema(null, fieldCollector);
	}

	static
	public MiningSchema createMiningSchema(DataField targetDataField, FieldCollector fieldCollector){
		return createMiningSchema((targetDataField != null ? targetDataField.getName() : null), Lists.newArrayList(fieldCollector.getFields()));
	}

	static
	public MiningSchema createMiningSchema(FieldName targetName, List<FieldName> activeNames){
		List<MiningField> miningFields = new ArrayList<>();

		if(targetName != null){
			miningFields.add(createMiningField(targetName, FieldUsageType.TARGET));
		}

		Function<FieldName, MiningField> function = new Function<FieldName, MiningField>(){

			@Override
			public MiningField apply(FieldName name){
				return createMiningField(name);
			}
		};

		miningFields.addAll(Lists.transform(activeNames, function));

		MiningSchema miningSchema = new MiningSchema(miningFields);

		return miningSchema;
	}

	static
	public Apply createApply(String function, Expression... expressions){
		Apply apply = new Apply(function)
			.addExpressions(expressions);

		return apply;
	}

	static
	public Constant createConstant(Object object){
		Constant constant = new Constant(formatValue(object));

		if(object instanceof Double){
			constant.setDataType(DataType.DOUBLE);
		}

		return constant;
	}

	static
	public Array createArray(DataType dataType, List<Value> values){
		Function<Value, String> function = new Function<Value, String>(){

			@Override
			public String apply(Value value){
				return value.getValue();
			}
		};

		String value = formatArrayValue(Lists.transform(values, function));

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
	public String formatValue(Object object){

		if(object instanceof Number){
			Number number = (Number)object;

			return formatValue(number);
		}

		return (object != null ? object.toString() : null);
	}

	static
	public String formatValue(Number number){
		double value = number.doubleValue();

		if(DoubleMath.isMathematicalInteger(value)){
			return Long.toString(number.longValue());
		}

		return Double.toString(value);
	}

	static
	public String formatArrayValue(List<String> values){
		StringBuilder sb = new StringBuilder();

		String sep = "";

		for(String value : values){
			sb.append(sep);

			sep = " ";

			if(value.indexOf(' ') > -1){
				sb.append('\"').append(value).append('\"');
			} else

			{
				sb.append(value);
			}
		}

		return sb.toString();
	}

	static
	public DataType getDataType(List<Value> values){

		if(values.isEmpty()){
			throw new IllegalArgumentException();
		}

		DataType dataType = DataType.INTEGER;

		for(Value value : values){
			String string = value.getValue();

			switch(dataType){
				case INTEGER:
					try {
						Integer.parseInt(string);

						continue;
					} catch(NumberFormatException nfe){
						dataType = DataType.DOUBLE;
					}
					// Falls through
				case DOUBLE:
					try {
						Double.parseDouble(string);

						continue;
					} catch(NumberFormatException nfe){
						dataType = DataType.STRING;
					}
					// Falls through
				case STRING:
					return dataType;
				default:
					throw new IllegalArgumentException();
			}
		}

		return dataType;
	}

	static
	public DataType getDataType(String string){

		try {
			Integer.parseInt(string);

			return DataType.INTEGER;
		} catch(NumberFormatException nfe){
			// Ignored
		}

		try {
			Double.parseDouble(string);

			return DataType.DOUBLE;
		} catch(NumberFormatException nfe){
			// Ignored
		}

		return DataType.STRING;
	}

	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
}