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
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.dmg.pmml.DataType;
import org.dmg.pmml.Entity;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Output;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.ResultFeature;
import org.dmg.pmml.Target;

public class ModelUtil {

	private ModelUtil(){
	}

	static
	public MiningSchema createMiningSchema(Schema schema){
		return createMiningSchema(schema.getTargetField(), schema.getActiveFields());
	}

	static
	public MiningSchema createMiningSchema(FieldName targetField, List<FieldName> activeFields){
		List<MiningField> miningFields = new ArrayList<>();

		if(targetField != null){
			miningFields.add(createMiningField(targetField, MiningField.UsageType.TARGET));
		}

		Function<FieldName, MiningField> function = new Function<FieldName, MiningField>(){

			@Override
			public MiningField apply(FieldName name){
				return createMiningField(name);
			}
		};

		miningFields.addAll(Lists.transform(activeFields, function));

		MiningSchema miningSchema = new MiningSchema(miningFields);

		return miningSchema;
	}

	static
	public MiningField createMiningField(FieldName name){
		return createMiningField(name, null);
	}

	static
	public MiningField createMiningField(FieldName name, MiningField.UsageType usageType){
		MiningField miningField = new MiningField(name)
			.setUsageType(usageType);

		return miningField;
	}

	static
	public Target createRescaleTarget(FieldName name, Double slope, Double intercept){
		Target target = new Target()
			.setField(name);

		if(slope != null && !ValueUtil.isOne(slope)){
			target.setRescaleFactor(slope);
		} // End if

		if(intercept != null && !ValueUtil.isZero(intercept)){
			target.setRescaleConstant(intercept);
		}

		return target;
	}

	static
	public Output createProbabilityOutput(Schema schema){
		List<String> targetCategories = schema.getTargetCategories();

		if(targetCategories == null || targetCategories.isEmpty()){
			return null;
		}

		Output output = new Output(createProbabilityFields(targetCategories));

		return output;
	}

	static
	public OutputField createAffinityField(String value){
		return createAffinityField(FieldName.create("affinity_" + value), value);
	}

	static
	public OutputField createAffinityField(FieldName name, String value){
		OutputField outputField = new OutputField(name, DataType.DOUBLE)
			.setResultFeature(ResultFeature.AFFINITY)
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
		OutputField outputField = new OutputField(name, DataType.STRING)
			.setResultFeature(ResultFeature.ENTITY_ID);

		return outputField;
	}

	static
	public OutputField createPredictedField(FieldName name, DataType dataType){
		OutputField outputField = new OutputField(name, dataType)
			.setResultFeature(ResultFeature.PREDICTED_VALUE);

		return outputField;
	}

	static
	public OutputField createProbabilityField(String value){
		return createProbabilityField(FieldName.create("probability_" + value), value);
	}

	static
	public OutputField createProbabilityField(FieldName name, String value){
		OutputField outputField = new OutputField(name, DataType.DOUBLE)
			.setResultFeature(ResultFeature.PROBABILITY)
			.setValue(value);

		return outputField;
	}

	static
	public List<OutputField> createProbabilityFields(List<String> values){
		Function<String, OutputField> function = new Function<String, OutputField>(){

			@Override
			public OutputField apply(String value){
				return createProbabilityField(value);
			}
		};

		return Lists.newArrayList(Lists.transform(values, function));
	}
}