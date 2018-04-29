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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dmg.pmml.DataDictionary;
import org.dmg.pmml.DataField;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Model;
import org.dmg.pmml.ModelStats;
import org.dmg.pmml.PMML;
import org.dmg.pmml.UnivariateStats;
import org.dmg.pmml.Visitor;
import org.jpmml.model.visitors.DataDictionaryCleaner;
import org.jpmml.model.visitors.MiningSchemaCleaner;
import org.jpmml.model.visitors.TransformationDictionaryCleaner;

public class ModelEncoder extends PMMLEncoder {

	private Map<FieldName, List<Decorator>> decorators = new LinkedHashMap<>();

	private Map<FieldName, UnivariateStats> univariateStats = new LinkedHashMap<>();


	public PMML encodePMML(Model model){
		PMML pmml = encodePMML();

		pmml.addModels(model);

		List<? extends Visitor> visitors = Arrays.asList(new MiningSchemaCleaner(), new TransformationDictionaryCleaner(), new DataDictionaryCleaner());
		for(Visitor visitor : visitors){
			visitor.applyTo(pmml);
		}

		MiningSchema miningSchema = model.getMiningSchema();

		List<MiningField> miningFields = miningSchema.getMiningFields();
		for(MiningField miningField : miningFields){
			FieldName name = miningField.getName();

			List<Decorator> decorators = getDecorators(name);
			if(decorators == null){
				continue;
			}

			DataField dataField = getDataField(name);
			if(dataField == null){
				throw new IllegalArgumentException();
			}

			for(Decorator decorator : decorators){
				decorator.decorate(dataField, miningField);
			}
		}

		DataDictionary dataDictionary = pmml.getDataDictionary();

		List<DataField> dataFields = dataDictionary.getDataFields();
		for(DataField dataField : dataFields){
			UnivariateStats univariateStats = getUnivariateStats(dataField.getName());

			if(univariateStats == null){
				continue;
			}

			ModelStats modelStats = model.getModelStats();
			if(modelStats == null){
				modelStats = new ModelStats();

				model.setModelStats(modelStats);
			}

			modelStats.addUnivariateStats(univariateStats);
		}

		return pmml;
	}

	public List<Decorator> getDecorators(FieldName name){
		return this.decorators.get(name);
	}

	public void addDecorator(FieldName name, Decorator decorator){
		List<Decorator> decorators = this.decorators.get(name);

		if(decorators == null){
			decorators = new ArrayList<>();

			this.decorators.put(name, decorators);
		}

		decorators.add(decorator);
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
}