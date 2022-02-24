/*
 * Copyright (c) 2020 Villu Ruusmann
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
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.dmg.pmml.DataField;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Field;
import org.dmg.pmml.LocalTransformations;
import org.dmg.pmml.Model;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.PMMLObject;
import org.dmg.pmml.Visitor;
import org.dmg.pmml.VisitorAction;
import org.dmg.pmml.mining.MiningModel;
import org.dmg.pmml.mining.Segmentation;
import org.jpmml.model.UnsupportedElementException;
import org.jpmml.model.visitors.AbstractVisitor;

public class FeatureExpander extends DeepFieldResolver {

	private Map<Model, Set<String>> features = null;

	private Map<Model, Map<String, Set<Field<?>>>> expandedFeatures = new IdentityHashMap<>();


	public FeatureExpander(Map<Model, Set<String>> features){
		this.features = Objects.requireNonNull(features);
	}

	@Override
	public void reset(){
		super.reset();

		this.features.clear();
		this.expandedFeatures.clear();
	}

	@Override
	public PMMLObject popParent(){
		PMMLObject parent = super.popParent();

		if(parent instanceof Model){
			Model model = (Model)parent;

			processModel(model);
		}

		return parent;
	}

	private void processModel(Model model){
		FieldDependencyResolver fieldDependencyResolver = getFieldDependencyResolver();

		MiningModel parentMiningModel = null;

		Set<String> features = this.features.get(model);
		if(features == null){
			parentMiningModel = getParent(this.features.keySet());

			if(parentMiningModel != null){
				features = this.features.get(parentMiningModel);
			} // End if

			if(features == null){
				return;
			}
		}

		Collection<Field<?>> modelFields = getFields(model);

		if((model instanceof MiningModel) || hasParent(MiningModel.class)){
			Collection<DerivedField> extraLocalDerivedFields = fieldDependencyResolver.getLocalDerivedFields();

			if(!extraLocalDerivedFields.isEmpty()){
				modelFields = new HashSet<>(modelFields);
				modelFields.addAll(extraLocalDerivedFields);
			}
		}

		Collection<Field<?>> featureFields = FieldUtil.selectAll(modelFields, features, true);

		Map<String, DerivedField> localDerivedFields = Collections.emptyMap();

		LocalTransformations localTransformations = model.getLocalTransformations();
		if(localTransformations != null && localTransformations.hasDerivedFields()){
			localDerivedFields = FieldUtil.nameMap(localTransformations.getDerivedFields());
		} // End if

		if(parentMiningModel != null){

			if(localDerivedFields.isEmpty()){
				return;
			}

			featureFields.retainAll(localDerivedFields.values());
		}

		Map<String, DerivedField> globalDerivedFields = FieldUtil.nameMap(fieldDependencyResolver.getGlobalDerivedFields());

		Map<String, Set<Field<?>>> expandedFields;

		if(parentMiningModel != null){
			expandedFields = ensureExpandedFeatures(parentMiningModel);
		} else

		{
			expandedFields = ensureExpandedFeatures(model);
		}

		for(Field<?> featureField : featureFields){
			String name = featureField.requireName();

			if(featureField instanceof DataField){
				expandedFields.put(name, Collections.singleton(featureField));
			} else

			if(featureField instanceof DerivedField){
				DerivedField derivedField = (DerivedField)featureField;

				Set<Field<?>> expandedFeatureFields = new HashSet<>();
				expandedFeatureFields.add(derivedField);

				if(model instanceof MiningModel){
					MiningModel miningModel = (MiningModel)model;

					Segmentation segmentation = miningModel.requireSegmentation();

					Set<DerivedField> extraLocalDerivedFields = new HashSet<>();

					Visitor visitor = new AbstractVisitor(){

						@Override
						public VisitorAction visit(LocalTransformations localTransformations){

							if(localTransformations != null && localTransformations.hasDerivedFields()){
								extraLocalDerivedFields.addAll(localTransformations.getDerivedFields());
							}

							return VisitorAction.CONTINUE;
						}
					};
					visitor.applyTo(segmentation);

					fieldDependencyResolver.expand(expandedFeatureFields, extraLocalDerivedFields);
				}

				fieldDependencyResolver.expand(expandedFeatureFields, new HashSet<>(localDerivedFields.values()));
				fieldDependencyResolver.expand(expandedFeatureFields, new HashSet<>(globalDerivedFields.values()));

				expandedFields.put(name, expandedFeatureFields);
			} else

			if(featureField instanceof OutputField){
				expandedFields.put(name, Collections.singleton(featureField));
			} else

			{
				throw new UnsupportedElementException(featureField);
			}
		}
	}

	private MiningModel getParent(Set<Model> models){
		Deque<PMMLObject> parents = getParents();

		for(PMMLObject parent : parents){

			if(parent instanceof MiningModel){
				MiningModel miningModel = (MiningModel)parent;

				if(models.contains(miningModel)){
					return miningModel;
				}
			}
		}

		return null;
	}

	private Map<String, Set<Field<?>>> ensureExpandedFeatures(Model model){
		Map<Model, Map<String, Set<Field<?>>>> expandedFeatures = getExpandedFeatures();

		Map<String, Set<Field<?>>> result = expandedFeatures.get(model);
		if(result == null){
			result = new HashMap<>();

			expandedFeatures.put(model, result);
		}

		return result;
	}

	public Map<String, Set<Field<?>>> getExpandedFeatures(Model model){
		Map<Model, Map<String, Set<Field<?>>>> expandedFeatures = getExpandedFeatures();

		return expandedFeatures.get(model);
	}

	public Map<Model, Set<String>> getFeatures(){
		return this.features;
	}

	public Map<Model, Map<String, Set<Field<?>>>> getExpandedFeatures(){
		return this.expandedFeatures;
	}

	private boolean hasParent(Class<? extends PMMLObject> clazz){
		Deque<PMMLObject> parents = getParents();

		for(PMMLObject parent : parents){

			if(clazz.isInstance(parent)){
				return true;
			}
		}

		return false;
	}
}