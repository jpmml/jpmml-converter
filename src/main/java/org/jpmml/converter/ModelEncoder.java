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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.dmg.pmml.Field;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.InlineTable;
import org.dmg.pmml.MathContext;
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

	private Map<Model, ListMultimap<FieldName, Decorator>> decorators = new LinkedHashMap<>();

	private Map<Model, ListMultimap<Feature, Number>> featureImportances = new LinkedHashMap<>();

	private Map<Model, List<UnivariateStats>> univariateStats = new LinkedHashMap<>();


	public PMML encodePMML(Model model){
		PMML pmml = encodePMML();

		if(model != null){
			transferContent(null, model);
		}

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

			encodeDecorators(pmml);
			encodeFeatureImportances(pmml);
			encodeUnivariateStats(pmml);
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

	public Map<Model, ListMultimap<FieldName, Decorator>> getDecorators(){
		return this.decorators;
	}

	public void addDecorator(Field<?> field, Decorator decorator){
		addDecorator(null, field, decorator);
	}

	public void addDecorator(Model model, Field<?> field, Decorator decorator){
		Map<Model, ListMultimap<FieldName, Decorator>> modelDecorators = getDecorators();

		ListMultimap<FieldName, Decorator> decorators = modelDecorators.get(model);
		if(decorators == null){
			decorators = ArrayListMultimap.create();

			modelDecorators.put(model, decorators);
		}

		FieldName name = field.getName();

		decorators.put(name, decorator);
	}

	public Map<Model, ListMultimap<Feature, Number>> getFeatureImportances(){
		return this.featureImportances;
	}

	public void addFeatureImportance(Feature feature, Number importance){
		addFeatureImportance(null, feature, importance);
	}

	public void addFeatureImportance(Model model, Feature feature, Number importance){
		Map<Model, ListMultimap<Feature, Number>> modelFeatureImportances = getFeatureImportances();

		ListMultimap<Feature, Number> featureImportances = modelFeatureImportances.get(model);
		if(featureImportances == null){
			featureImportances = ArrayListMultimap.create();

			modelFeatureImportances.put(model, featureImportances);
		}

		featureImportances.put(feature, importance);
	}

	public Map<Model, List<UnivariateStats>> getUnivariateStats(){
		return this.univariateStats;
	}

	public void addUnivariateStats(UnivariateStats pmmlUnivariateStats){
		addUnivariateStats(null, pmmlUnivariateStats);
	}

	public void addUnivariateStats(Model model, UnivariateStats pmmlUnivariateStats){
		Map<Model, List<UnivariateStats>> modelUnivariateStats = getUnivariateStats();

		List<UnivariateStats> univariateStats = modelUnivariateStats.get(model);
		if(univariateStats == null){
			univariateStats = new ArrayList<>();

			modelUnivariateStats.put(model, univariateStats);
		}

		univariateStats.add(pmmlUnivariateStats);
	}

	public void transferContent(Model left, Model right){
		transferDecorators(left, right);
		transferFeatureImportances(left, right);
		transferUnivariateStats(left, right);
	}

	public void transferDecorators(Model left, Model right){
		transferValue(this.decorators, left, right);
	}

	public void transferFeatureImportances(Model left, Model right){
		transferValue(this.featureImportances, left, right);
	}

	public void transferUnivariateStats(Model left, Model right){
		transferValue(this.univariateStats, left, right);
	}

	private void encodeDecorators(PMML pmml){
		Map<Model, ListMultimap<FieldName, Decorator>> modelDecorators = getDecorators();

		if(modelDecorators.isEmpty()){
			return;
		} // End if

		if(modelDecorators.containsKey(null)){
			throw new IllegalStateException();
		}

		Collection<Map.Entry<Model, ListMultimap<FieldName, Decorator>>> entries = modelDecorators.entrySet();
		for(Map.Entry<Model, ListMultimap<FieldName, Decorator>> entry : entries){
			Model model = entry.getKey();
			ListMultimap<FieldName, Decorator> decorators = entry.getValue();

			MiningSchema miningSchema = model.getMiningSchema();

			if(miningSchema != null && miningSchema.hasMiningFields()){
				List<MiningField> miningFields = miningSchema.getMiningFields();

				for(MiningField miningField : miningFields){
					FieldName name = miningField.getName();

					List<Decorator> fieldDecorators = decorators.get(name);
					if(fieldDecorators != null && !fieldDecorators.isEmpty()){

						for(Decorator fieldDecorator : fieldDecorators){
							fieldDecorator.decorate(miningField);
						}
					}
				}
			}
		}
	}

	private void encodeFeatureImportances(PMML pmml){
		Map<Model, ListMultimap<Feature, Number>> modelFeatureImportances = getFeatureImportances();

		if(modelFeatureImportances.isEmpty()){
			return;
		} // End if

		if(modelFeatureImportances.containsKey(null)){
			throw new IllegalStateException();
		}

		Map<Model, Set<FieldName>> expandableFeatures = (modelFeatureImportances.entrySet()).stream()
			.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().keySet().stream()
				.map(feature -> feature.getName())
				.collect(Collectors.toSet())
			));

		FeatureExpander featureExpander = new FeatureExpander(expandableFeatures);
		featureExpander.applyTo(pmml);

		Collection<? extends Map.Entry<Model, ListMultimap<Feature, Number>>> entries = modelFeatureImportances.entrySet();
		for(Map.Entry<Model, ListMultimap<Feature, Number>> entry : entries){
			Model model = entry.getKey();
			ListMultimap<Feature, Number> featureImportances = entry.getValue();

			MathContext mathContext = model.getMathContext();
			Collection<Map.Entry<Feature, Number>> featureImportanceEntries = featureImportances.entries();

			Map<FieldName, Set<Field<?>>> featureFields = featureExpander.getExpandedFeatures(model);
			if(featureFields == null){
				throw new IllegalArgumentException();
			}

			ListMultimap<FieldName, Number> fieldImportances = ArrayListMultimap.create();

			for(Map.Entry<Feature, Number> featureImportanceEntry : featureImportanceEntries){
				FieldName name = (featureImportanceEntry.getKey()).getName();
				Number importance = featureImportanceEntry.getValue();

				if(ValueUtil.isZero(importance)){
					continue;
				}

				Set<Field<?>> fields = featureFields.get(name);
				if(fields == null){
					logger.warn("Unused feature \'" + name.getValue() + "\' has non-zero importance");

					continue;
				}

				Number fieldImportance = ValueUtil.divide(mathContext, importance, fields.size());

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
					if(fieldImportance != null && !fieldImportance.isEmpty()){
						miningField.setImportance(ValueUtil.sum(mathContext, fieldImportance));
					}
				}

				List<FieldName> names = new ArrayList<>();
				List<Number> importances = new ArrayList<>();

				for(Map.Entry<Feature, Number> featureImportanceEntry : featureImportanceEntries){
					names.add(FeatureUtil.getName(featureImportanceEntry.getKey()));
					importances.add(featureImportanceEntry.getValue());
				}

				Map<String, List<?>> nativeFeatureImportances = new LinkedHashMap<>();
				nativeFeatureImportances.put("data:name", names);
				nativeFeatureImportances.put("data:importance", importances);

				List<Number> nonZeroImportances = importances.stream()
					.filter(importance -> !ValueUtil.isZero(importance))
					.collect(Collectors.toList());

				InlineTable inlineTable = PMMLUtil.createInlineTable(nativeFeatureImportances)
					.addExtensions(PMMLUtil.createExtension("numberOfImportances", String.valueOf(importances.size())))
					.addExtensions(PMMLUtil.createExtension("numberOfNonZeroImportances", String.valueOf(nonZeroImportances.size())))
					.addExtensions(PMMLUtil.createExtension("sumOfImportances", String.valueOf(ValueUtil.sum(mathContext, importances))));

				if(!nonZeroImportances.isEmpty()){
					Comparator<Number> comparator = new Comparator<Number>(){

						@Override
						public int compare(Number left, Number right){
							return Double.compare(left.doubleValue(), right.doubleValue());
						}
					};

					inlineTable
						.addExtensions(PMMLUtil.createExtension("minImportance", String.valueOf(Collections.min(nonZeroImportances, comparator))))
						.addExtensions(PMMLUtil.createExtension("maxImportance", String.valueOf(Collections.max(nonZeroImportances, comparator))));
				}

				miningSchema.addExtensions(PMMLUtil.createExtension(Extensions.FEATURE_IMPORTANCES, inlineTable));
			}
		}
	}

	private void encodeUnivariateStats(PMML pmml){
		Map<Model, List<UnivariateStats>> modelUnivariateStats = getUnivariateStats();

		if(modelUnivariateStats.isEmpty()){
			return;
		} // End if

		if(modelUnivariateStats.containsKey(null)){
			throw new IllegalStateException();
		}

		Collection<Map.Entry<Model, List<UnivariateStats>>> entries = modelUnivariateStats.entrySet();
		for(Map.Entry<Model, List<UnivariateStats>> entry : entries){
			Model model = entry.getKey();
			List<UnivariateStats> univariateStats = entry.getValue();

			Map<FieldName, UnivariateStats> fieldUnivariateStats = univariateStats.stream()
				.collect(Collectors.toMap(UnivariateStats::getField, Function.identity()));

			MiningSchema miningSchema = model.getMiningSchema();

			if(miningSchema != null && miningSchema.hasMiningFields()){
				List<MiningField> miningFields = miningSchema.getMiningFields();

				for(MiningField miningField : miningFields){
					FieldName name = miningField.getName();

					UnivariateStats pmmlUnivariateStats = fieldUnivariateStats.get(name);
					if(pmmlUnivariateStats != null){
						ModelStats modelStats = ModelUtil.ensureModelStats(model);

						modelStats.addUnivariateStats(pmmlUnivariateStats);
					}
				}
			}
		}
	}

	static
	private <K, V> void transferValue(Map<K, V> map, K left, K right){
		V value = map.remove(left);

		if(value != null){
			map.put(right, value);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(ModelEncoder.class);
}