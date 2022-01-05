/*
 * Copyright (c) 2015 Villu Ruusmann
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
package org.jpmml.converter.visitors;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dmg.pmml.DataField;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Field;
import org.dmg.pmml.LocalTransformations;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Model;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.PMMLObject;
import org.dmg.pmml.mining.MiningModel;
import org.dmg.pmml.mining.Segment;
import org.dmg.pmml.mining.Segmentation;

/**
 * <p>
 * A Visitor that removes redundant {@link MiningField mining fields} from the {@link MiningSchema mining schema}.
 * </p>
 */
public class MiningSchemaCleaner extends DeepFieldResolver {

	@Override
	public PMMLObject popParent(){
		PMMLObject parent = super.popParent();

		if(parent instanceof MiningModel){
			MiningModel miningModel = (MiningModel)parent;

			Set<Field<?>> activeFields = processMiningModel(miningModel);

			clean(miningModel, activeFields);
		} else

		if(parent instanceof Model){
			Model model = (Model)parent;

			Set<Field<?>> activeFields = processModel(model);

			clean(model, activeFields);
		}

		return parent;
	}

	private Set<Field<?>> processMiningModel(MiningModel miningModel){
		Set<Field<?>> activeFields = DeepFieldResolverUtil.getActiveFields(this, miningModel);

		Set<String> activeFieldNames = new HashSet<>();

		Segmentation segmentation = miningModel.requireSegmentation();

		List<Segment> segments = segmentation.getSegments();
		for(Segment segment : segments){
			Model model = segment.requireModel();

			MiningSchema miningSchema = model.requireMiningSchema();

			List<MiningField> miningFields = miningSchema.getMiningFields();
			for(MiningField miningField : miningFields){
				String fieldName = miningField.getName();

				MiningField.UsageType usageType = miningField.getUsageType();
				switch(usageType){
					case ACTIVE:
						activeFieldNames.add(fieldName);
						break;
					default:
						break;
				}
			}
		}

		Collection<Field<?>> modelFields = getFields(miningModel);

		activeFields.addAll(FieldUtil.selectAll(modelFields, activeFieldNames, true));

		expandDerivedFields(miningModel, activeFields);

		return activeFields;
	}

	private Set<Field<?>> processModel(Model model){
		Set<Field<?>> activeFields = DeepFieldResolverUtil.getActiveFields(this, model);

		expandDerivedFields(model, activeFields);

		return activeFields;
	}

	private void expandDerivedFields(Model model, Set<Field<?>> fields){
		FieldDependencyResolver fieldDependencyResolver = getFieldDependencyResolver();

		fieldDependencyResolver.expand(fields, fieldDependencyResolver.getGlobalDerivedFields());

		LocalTransformations localTransformations = model.getLocalTransformations();
		if(localTransformations != null && localTransformations.hasDerivedFields()){
			fieldDependencyResolver.expand(fields, new HashSet<>(localTransformations.getDerivedFields()));
		}
	}

	private void clean(Model model, Set<Field<?>> activeFields){
		MiningSchema miningSchema = model.requireMiningSchema();

		List<MiningField> miningFields = miningSchema.getMiningFields();

		Map<String, Field<?>> activeFieldMap = FieldUtil.nameMap(activeFields);

		for(Iterator<MiningField> it = miningFields.iterator(); it.hasNext(); ){
			MiningField miningField = it.next();

			String fieldName = miningField.getName();

			MiningField.UsageType usageType = miningField.getUsageType();
			switch(usageType){
				case ACTIVE:
					if(!(activeFieldMap).containsKey(fieldName)){
						it.remove();
					}
					break;
				default:
					break;
			}

			activeFieldMap.remove(fieldName);
		}

		activeFields = new LinkedHashSet<>(activeFieldMap.values());

		for(Field<?> activeField : activeFields){
			MiningField miningField = new MiningField(activeField);

			miningSchema.addMiningFields(miningField);
		}

		miningFields = miningSchema.getMiningFields();

		Comparator<MiningField> comparator = new Comparator<MiningField>(){

			@Override
			public int compare(MiningField left, MiningField right){
				int order;

				order = Integer.compare(getGroup(left), getGroup(right));
				if(order != 0){
					return order;
				}

				order = Integer.compare(getFieldType(left), getFieldType(right));
				if(order != 0){
					return order;
				}

				return 0;
			}

			private int getGroup(MiningField miningField){
				MiningField.UsageType usageType = miningField.getUsageType();

				switch(usageType){
					case PREDICTED:
					case TARGET:
						return -1;
					default:
						return usageType.ordinal();
				}
			}

			private int getFieldType(MiningField miningField){
				String fieldName = miningField.getName();

				Field<?> field = activeFieldMap.get(fieldName);

				if(field instanceof DataField){
					return 0;
				} else

				if(field instanceof DerivedField){
					return 1;
				} else

				if(field instanceof OutputField){
					return 2;
				} else

				{
					return 3;
				}
			}
		};

		miningFields.sort(comparator);
	}
}