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

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.dmg.pmml.DataField;
import org.dmg.pmml.Entity;
import org.dmg.pmml.FeatureType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldUsageType;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.Value;
import org.jpmml.model.visitors.FieldReferenceFinder;

public class ModelUtil {

	private ModelUtil(){
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
	public MiningSchema createMiningSchema(FieldReferenceFinder fieldReferenceFinder){
		return createMiningSchema(null, fieldReferenceFinder);
	}

	static
	public MiningSchema createMiningSchema(DataField targetDataField, FieldReferenceFinder fieldReferenceFinder){
		return createMiningSchema((targetDataField != null ? targetDataField.getName() : null), Lists.newArrayList(fieldReferenceFinder.getFieldNames()));
	}

	static
	public MiningSchema createMiningSchema(FieldName targetName, List<FieldName> activeNames){
		List<MiningField> miningFields = Lists.newArrayList();

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

}