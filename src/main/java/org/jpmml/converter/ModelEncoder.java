/*
 * Copyright (c) 2017 Villu Ruusmann
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
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.dmg.pmml.DataField;
import org.dmg.pmml.Extension;
import org.dmg.pmml.Field;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Model;
import org.dmg.pmml.ModelStats;
import org.dmg.pmml.PMML;
import org.dmg.pmml.UnivariateStats;
import org.jpmml.converter.mining.MiningModelUtil;
import org.jpmml.converter.visitors.FeatureExpander;
import org.jpmml.converter.visitors.ModelCleanerBattery;
import org.jpmml.converter.visitors.PMMLCleanerBattery;
import org.jpmml.model.visitors.VisitorBattery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelEncoder extends PMMLEncoder {

	private List<Model> transformers = new ArrayList<>();

	private Map<FieldName, List<Decorator>> decorators = new LinkedHashMap<>();

	private Map<Model, List<FeatureImportance>> featureImportances = new LinkedHashMap<>();

	private Map<FieldName, UnivariateStats> univariateStats = new LinkedHashMap<>();


	public PMML encodePMML(Model model){
		PMML pmml = encodePMML();

		List<Model> transformers = getTransformers();
		if(transformers.size() > 0){
			List<Model> models = new ArrayList<>(transformers);

			if(model != null){
				models.add(model);
			}

			model = MiningModelUtil.createModelChain(models);
		} // End if

		if(model != null){
			pmml.addModels(model);

			VisitorBattery modelCleanerBattery = new ModelCleanerBattery();
			modelCleanerBattery.applyTo(pmml);

			MiningSchema miningSchema = model.getMiningSchema();

			List<MiningField> miningFields = miningSchema.getMiningFields();
			for(MiningField miningField : miningFields){
				FieldName name = miningField.getName();

				DataField dataField = getDataField(name);
				if(dataField == null){
					throw new IllegalArgumentException("Field " + name.getValue() + " is not referentiable");
				}

				List<Decorator> decorators = getDecorators(name);
				if(decorators != null){

					for(Decorator decorator : decorators){
						decorator.decorate(miningField);
					}
				}

				UnivariateStats univariateStats = getUnivariateStats(name);
				if(univariateStats != null){
					ModelStats modelStats = ModelUtil.ensureModelStats(model);

					modelStats.addUnivariateStats(univariateStats);
				}
			}

			encodeFeatureImportances(pmml);
		}

		VisitorBattery pmmlCleanerBattery = new PMMLCleanerBattery();
		pmmlCleanerBattery.applyTo(pmml);

		return pmml;
	}

	public List<Model> getTransformers(){
		return this.transformers;
	}

	public void addTransformer(Model transformer){
		this.transformers.add(transformer);
	}

	public List<Decorator> getDecorators(FieldName name){
		return this.decorators.get(name);
	}

	public void addDecorator(DataField dataField, Decorator decorator){
		addDecorator(dataField.getName(), decorator);
	}

	public void addDecorator(FieldName name, Decorator decorator){
		List<Decorator> decorators = this.decorators.get(name);

		if(decorators == null){
			decorators = new ArrayList<>();

			this.decorators.put(name, decorators);
		}

		decorators.add(decorator);
	}

	public void addFeatureImportance(Feature feature, Number importance){
		addFeatureImportance(null, feature, importance);
	}

	public void addFeatureImportance(Model model, Feature feature, Number importance){
		List<FeatureImportance> featureImportances = this.featureImportances.get(model);

		if(featureImportances == null){
			featureImportances = new ArrayList<>();

			this.featureImportances.put(model, featureImportances);
		}

		featureImportances.add(new FeatureImportance(feature, importance));
	}

	public void transferFeatureImportances(Model model){
		transferFeatureImportances(null, model);
	}

	public void transferFeatureImportances(Model left, Model right){
		List<FeatureImportance> featureImportances = this.featureImportances.remove(left);

		if(featureImportances != null && !featureImportances.isEmpty()){
			this.featureImportances.put(right, featureImportances);
		}
	}

	public Map<Model, List<FeatureImportance>> getFeatureImportances(){
		return this.featureImportances;
	}

	public UnivariateStats getUnivariateStats(FieldName name){
		return this.univariateStats.get(name);
	}

	public void putUnivariateStats(UnivariateStats univariateStats){
		putUnivariateStats(univariateStats.getField(), univariateStats);
	}

	public void putUnivariateStats(FieldName name, UnivariateStats univariateStats){
		this.univariateStats.put(name, univariateStats);
	}

	private void encodeFeatureImportances(PMML pmml){
		Map<Model, List<FeatureImportance>> modelFeatureImportances = getFeatureImportances();

		if(modelFeatureImportances.isEmpty()){
			return;
		} // End if

		if(modelFeatureImportances.containsKey(null)){
			throw new IllegalStateException();
		}

		Map<Model, Set<FieldName>> expandableFeatures = (modelFeatureImportances.entrySet()).stream()
			.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().stream()
				.map(featureImportance -> (featureImportance.getFeature()).getName())
				.collect(Collectors.toSet())
			));

		FeatureExpander featureExpander = new FeatureExpander(expandableFeatures);
		featureExpander.applyTo(pmml);

		Collection<? extends Map.Entry<Model, List<FeatureImportance>>> entries = modelFeatureImportances.entrySet();
		for(Map.Entry<Model, List<FeatureImportance>> entry : entries){
			Model model = entry.getKey();
			List<FeatureImportance> featureImportances = entry.getValue();

			Map<FieldName, Set<Field<?>>> featureFields = featureExpander.getExpandedFeatures(model);
			if(featureFields == null){
				throw new IllegalArgumentException();
			}

			ListMultimap<FieldName, Number> fieldImportances = ArrayListMultimap.create();

			for(FeatureImportance featureImportance : featureImportances){
				FieldName name = (featureImportance.getFeature()).getName();
				Number importance = featureImportance.getImportance();

				if(ValueUtil.isZero(importance)){
					continue;
				}

				Set<Field<?>> fields = featureFields.get(name);
				if(fields == null){
					logger.warn("Unused feature \'" + name.getValue() + "\' has non-zero importance");

					continue;
				}

				Double fieldImportance = (importance.doubleValue() / fields.size());

				for(Field<?> field : fields){
					FieldName fieldName = field.getName();

					fieldImportances.put(fieldName, fieldImportance);
				}
			}

			MiningSchema miningSchema = model.getMiningSchema();

			if(miningSchema != null && miningSchema.hasMiningFields()){
				List<MiningField> miningFields = miningSchema.getMiningFields();

				for(MiningField miningField : miningFields){
					FieldName name = miningField.getName();
					MiningField.UsageType usageType = miningField.getUsageType();

					switch(usageType){
						case ACTIVE:
							break;
						default:
							continue;
					}

					List<Number> fieldImportance = fieldImportances.get(name);
					if(fieldImportance != null){
						Double fieldImportanceSum = fieldImportance.stream()
							.collect(Collectors.summingDouble(Number::doubleValue));

						miningField.setImportance(fieldImportanceSum);
					}
				}

				List<FieldName> names = new ArrayList<>();
				List<Number> importances = new ArrayList<>();

				for(FeatureImportance featureImportance : featureImportances){
					names.add(FeatureUtil.getName(featureImportance.getFeature()));
					importances.add(featureImportance.getImportance());
				}

				Map<String, List<?>> nativeFeatureImportances = new LinkedHashMap<>();
				nativeFeatureImportances.put("data:name", names);
				nativeFeatureImportances.put("data:importance", importances);

				Extension extension = new Extension()
					.setName(Extensions.FEATURE_IMPORTANCES)
					.addContent(PMMLUtil.createInlineTable(nativeFeatureImportances));

				miningSchema.addExtensions(extension);
			}
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(ModelEncoder.class);

	static
	private class FeatureImportance {

		private Feature feature = null;

		private Number importance = null;


		private FeatureImportance(Feature feature, Number importance){
			setFeature(feature);
			setImportance(importance);
		}

		public Feature getFeature(){
			return this.feature;
		}

		private void setFeature(Feature feature){
			this.feature = feature;
		}

		public Number getImportance(){
			return this.importance;
		}

		private void setImportance(Number importance){
			this.importance = importance;
		}
	}
}