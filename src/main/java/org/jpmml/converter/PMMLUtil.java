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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import org.dmg.pmml.Array;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldUsageType;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.OpType;
import org.dmg.pmml.Value;

public class PMMLUtil {

	private PMMLUtil(){
	}

	static
	public DataField createDataField(String name, boolean categorical){
		return createDataField(name, categorical ? DataType.STRING : DataType.DOUBLE);
	}

	static
	public DataField createDataField(String name, String type){
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
	public DataField createDataField(String name, DataType dataType){
		DataField dataField = new DataField()
			.withName(FieldName.create(name));

		dataField = refineDataField(dataField, dataType);

		return dataField;
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
				return dataField.withDataType(DataType.STRING)
					.withOpType(OpType.CATEGORICAL);
			case DOUBLE:
				return dataField.withDataType(DataType.DOUBLE)
					.withOpType(values.size() > 0 ? OpType.CATEGORICAL : OpType.CONTINUOUS);
			case INTEGER:
				return dataField.withDataType(DataType.INTEGER)
					.withOpType(values.size() > 0 ? OpType.CATEGORICAL : OpType.CONTINUOUS);
			case BOOLEAN:
				return dataField.withDataType(DataType.BOOLEAN)
					.withOpType(OpType.CATEGORICAL);
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
	public List<MiningField> createMiningFields(FieldCollector fieldCollector){
		return createMiningFields(fieldCollector, FieldUsageType.ACTIVE);
	}

	static
	public List<MiningField> createMiningFields(FieldCollector fieldCollector, final FieldUsageType usageType){
		Set<FieldName> names = fieldCollector.getFields();

		Function<FieldName, MiningField> function = new Function<FieldName, MiningField>(){

			@Override
			public MiningField apply(FieldName name){
				MiningField miningField = new MiningField(name)
					.withUsageType(usageType);

				return miningField;
			}
		};

		List<MiningField> miningFields = Lists.newArrayList(Iterables.transform(names, function));

		Collections.sort(miningFields, new MiningFieldComparator());

		return miningFields;
	}

	static
	public Array createArray(DataType dataType, List<Value> values){
		String value = formatArrayValue(values);

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
	public String formatValue(Number number){
		double value = number.doubleValue();

		if(DoubleMath.isMathematicalInteger(value)){
			return Long.toString(number.longValue());
		}

		return Double.toString(value);
	}

	static
	public String formatArrayValue(List<Value> values){
		StringBuilder sb = new StringBuilder();

		String sep = "";

		for(Value value : values){
			String string = value.getValue();

			sb.append(sep);

			sep = " ";

			if(string.indexOf(' ') > -1){
				sb.append('\"').append(string).append('\"');
			} else

			{
				sb.append(string);
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
}