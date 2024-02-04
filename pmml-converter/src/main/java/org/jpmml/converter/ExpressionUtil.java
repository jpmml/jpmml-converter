/*
 * Copyright (c) 2024 Villu Ruusmann
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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.dmg.pmml.Apply;
import org.dmg.pmml.Constant;
import org.dmg.pmml.DataType;
import org.dmg.pmml.DefineFunction;
import org.dmg.pmml.Expression;
import org.dmg.pmml.FieldColumnPair;
import org.dmg.pmml.FieldRef;
import org.dmg.pmml.MapValues;
import org.dmg.pmml.NamespacePrefixes;
import org.dmg.pmml.PMMLFunctions;

public class ExpressionUtil {

	private ExpressionUtil(){
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
	public boolean isString(Expression expression, FeatureResolver featureResolver){
		DataType dataType = getDataType(expression, featureResolver);

		return (dataType == DataType.STRING);
	}

	static
	public DataType getDataType(Expression expression, FeatureResolver featureResolver){

		if(expression instanceof Constant){
			Constant constant = (Constant)expression;

			return constant.getDataType();
		} else

		if(expression instanceof FieldRef){
			FieldRef fieldRef = (FieldRef)expression;

			Feature feature = (featureResolver != null ? featureResolver.resolveFeature(fieldRef.requireField()) : null);
			if(feature == null){
				return null;
			}

			return feature.getDataType();
		} else

		if(expression instanceof Apply){
			Apply apply = (Apply)expression;

			String function = apply.requireFunction();
			switch(function){
				case PMMLFunctions.CEIL:
				case PMMLFunctions.FLOOR:
				case PMMLFunctions.ROUND:
					return DataType.INTEGER;
				case PMMLFunctions.ISMISSING:
				case PMMLFunctions.ISNOTMISSING:
				case PMMLFunctions.ISVALID:
				case PMMLFunctions.ISNOTVALID:
					return DataType.BOOLEAN;
				case PMMLFunctions.EQUAL:
				case PMMLFunctions.NOTEQUAL:
				case PMMLFunctions.LESSTHAN:
				case PMMLFunctions.LESSOREQUAL:
				case PMMLFunctions.GREATERTHAN:
				case PMMLFunctions.GREATEROREQUAL:
					return DataType.BOOLEAN;
				case PMMLFunctions.AND:
				case PMMLFunctions.OR:
					return DataType.BOOLEAN;
				case PMMLFunctions.NOT:
					return DataType.BOOLEAN;
				case PMMLFunctions.ISIN:
				case PMMLFunctions.ISNOTIN:
					return DataType.BOOLEAN;
				case PMMLFunctions.IF:
					{
						List<Expression> expressions = apply.getExpressions();

						if(expressions.size() > 1){
							DataType trueDataType = getDataType(expressions.get(1), featureResolver);

							if(expressions.size() > 2){
								DataType falseDataType = getDataType(expressions.get(2), featureResolver);

								if(Objects.equals(trueDataType, falseDataType)){
									return trueDataType;
								}

								return null;
							}

							return trueDataType;
						}
					}
					return null;
				case PMMLFunctions.CONCAT:
				case PMMLFunctions.LOWERCASE:
				case PMMLFunctions.SUBSTRING:
				case PMMLFunctions.TRIMBLANKS:
				case PMMLFunctions.UPPERCASE:
					return DataType.STRING;
				case PMMLFunctions.STRINGLENGTH:
					return DataType.INTEGER;
				case PMMLFunctions.REPLACE:
					return DataType.STRING;
				case PMMLFunctions.MATCHES:
					return DataType.BOOLEAN;
				case PMMLFunctions.FORMATDATETIME:
				case PMMLFunctions.FORMATNUMBER:
					return DataType.STRING;
				case PMMLFunctions.DATEDAYSSINCEYEAR:
				case PMMLFunctions.DATESECONDSSINCEMIDNIGHT:
				case PMMLFunctions.DATESECONDSSINCEYEAR:
					return DataType.INTEGER;
				default:
					return null;
			}
		}

		return null;
	}
}