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
package org.jpmml.converter.visitors;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.dmg.pmml.Field;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.LocalTransformations;
import org.dmg.pmml.Model;
import org.dmg.pmml.Output;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.VisitorAction;
import org.dmg.pmml.mining.MiningModel;
import org.dmg.pmml.mining.Segment;
import org.dmg.pmml.mining.Segmentation;
import org.jpmml.model.visitors.ActiveFieldFinder;
import org.jpmml.model.visitors.FieldResolver;

public class DeepFieldResolverUtil {

	private DeepFieldResolverUtil(){
	}

	static
	public Set<Field<?>> getActiveFields(FieldResolver resolver, MiningModel miningModel){
		Collection<Field<?>> modelFields = getModelFields(resolver, miningModel);

		Set<Field<?>> activeFields = new LinkedHashSet<>();

		Segmentation segmentation = miningModel.getSegmentation();

		List<Segment> segments = segmentation.getSegments();
		for(Segment segment : segments){
			Predicate predicate = segment.getPredicate();

			if(predicate != null){
				Set<FieldName> names = ActiveFieldFinder.getFieldNames(predicate);

				if(names.size() > 0){
					Collection<Field<?>> segmentFields = resolver.getFields(miningModel, segmentation, segment);

					activeFields.addAll(FieldUtil.selectAll(segmentFields, names));
				}
			}
		}

		Output output = miningModel.getOutput();
		if(output != null){
			Set<FieldName> names = ActiveFieldFinder.getFieldNames(output);

			if(names.size() > 0){
				activeFields.addAll(FieldUtil.selectAll(modelFields, names));
			}

			activeFields.removeAll(output.getOutputFields());
		}

		Segmentation.MultipleModelMethod multipleModelMethod = segmentation.getMultipleModelMethod();
		switch(multipleModelMethod){
			case MODEL_CHAIN:
				Collection<Field<?>> segmentationFields = resolver.getFields(miningModel, segmentation);
				segmentationFields.removeAll(modelFields);

				activeFields.removeAll(segmentationFields);
				break;
			default:
				break;
		}

		return activeFields;
	}

	static
	public Set<Field<?>> getActiveFields(FieldResolver resolver, Model model){
		Collection<Field<?>> modelFields = getModelFields(resolver, model);

		Set<Field<?>> activeFields = new LinkedHashSet<>();

		ActiveFieldFinder activeFieldFinder = new ActiveFieldFinder(){

			@Override
			public VisitorAction visit(LocalTransformations localTransformations){
				return VisitorAction.SKIP;
			}
		};

		Set<FieldName> names = ActiveFieldFinder.getFieldNames(activeFieldFinder, model);

		activeFields.addAll(FieldUtil.selectAll(modelFields, names));

		Output output = model.getOutput();
		if(output != null){
			activeFields.removeAll(output.getOutputFields());
		}

		return activeFields;
	}

	static
	private Collection<Field<?>> getModelFields(FieldResolver resolver, Model model){
		Output output = model.getOutput();

		if(output != null && output.hasOutputFields()){
			return resolver.getFields(model, output);
		} else

		{
			return resolver.getFields(model);
		}
	}
}