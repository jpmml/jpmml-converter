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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.dmg.pmml.Field;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.LocalTransformations;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Model;
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

			Set<FieldName> activeFieldNames = processMiningModel(miningModel);

			clean(miningModel, activeFieldNames);
		} else

		if(parent instanceof Model){
			Model model = (Model)parent;

			Set<FieldName> activeFieldNames = processModel(model);

			clean(model, activeFieldNames);
		}

		return parent;
	}

	private Set<FieldName> processMiningModel(MiningModel miningModel){
		Set<Field<?>> activeFields = DeepFieldResolverUtil.getActiveFields(this, miningModel);

		Set<FieldName> activeFieldNames = new HashSet<>();

		Segmentation segmentation = miningModel.getSegmentation();

		List<Segment> segments = segmentation.getSegments();
		for(Segment segment : segments){
			Model model = segment.getModel();

			if(model == null){
				continue;
			}

			MiningSchema miningSchema = model.getMiningSchema();

			List<MiningField> miningFields = miningSchema.getMiningFields();
			for(MiningField miningField : miningFields){
				FieldName name = miningField.getName();

				MiningField.UsageType usageType = miningField.getUsageType();
				switch(usageType){
					case ACTIVE:
						activeFieldNames.add(name);
						break;
					default:
						break;
				}
			}
		}

		Collection<Field<?>> modelFields = getFields(miningModel);

		activeFields.addAll(FieldUtil.selectAll(modelFields, activeFieldNames, true));

		expandDerivedFields(miningModel, activeFields);

		return FieldUtil.nameSet(activeFields);
	}

	private Set<FieldName> processModel(Model model){
		Set<Field<?>> activeFields = DeepFieldResolverUtil.getActiveFields(this, model);

		expandDerivedFields(model, activeFields);

		return FieldUtil.nameSet(activeFields);
	}

	private void expandDerivedFields(Model model, Set<Field<?>> fields){
		FieldDependencyResolver fieldDependencyResolver = getFieldDependencyResolver();

		fieldDependencyResolver.expand(fields, fieldDependencyResolver.getGlobalDerivedFields());

		LocalTransformations localTransformations = model.getLocalTransformations();
		if(localTransformations != null && localTransformations.hasDerivedFields()){
			fieldDependencyResolver.expand(fields, new HashSet<>(localTransformations.getDerivedFields()));
		}
	}

	private void clean(Model model, Set<FieldName> activeFieldNames){
		MiningSchema miningSchema = model.getMiningSchema();

		activeFieldNames = new LinkedHashSet<>(activeFieldNames);

		List<MiningField> miningFields = miningSchema.getMiningFields();

		for(Iterator<MiningField> it = miningFields.iterator(); it.hasNext(); ){
			MiningField miningField = it.next();

			FieldName name = miningField.getName();

			MiningField.UsageType usageType = miningField.getUsageType();
			switch(usageType){
				case ACTIVE:
					if(!(activeFieldNames).contains(name)){
						it.remove();
					}
					break;
				default:
					break;
			}

			activeFieldNames.remove(name);
		}

		for(FieldName activeFieldName : activeFieldNames){
			MiningField miningField = new MiningField(activeFieldName);

			miningSchema.addMiningFields(miningField);
		}
	}
}