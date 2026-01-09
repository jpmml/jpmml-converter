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

import java.util.List;
import java.util.Objects;

import org.dmg.pmml.DataType;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Field;
import org.dmg.pmml.OpType;

public class FeatureUtil {

	private FeatureUtil(){
	}

	static
	public Feature createFeature(Field<?> field, PMMLEncoder encoder){
		DataType dataType = field.requireDataType();
		OpType opType = field.requireOpType();

		switch(dataType){
			case STRING:
				return new StringFeature(encoder, field);
			case INTEGER:
			case FLOAT:
			case DOUBLE:
				switch(opType){
					case CONTINUOUS:
						return new ContinuousFeature(encoder, field);
					default:
						return new ObjectFeature(encoder, field){

							@Override
							public ContinuousFeature toContinuousFeature(){
								PMMLEncoder encoder = getEncoder();

								DerivedField derivedField = (DerivedField)encoder.toContinuous(getName());

								return new ContinuousFeature(encoder, derivedField);
							}
						};
				}
			case BOOLEAN:
				return new BooleanFeature(encoder, field);
			default:
				return new ObjectFeature(encoder, field);
		}
	}

	static
	public String getName(Feature feature){

		if(feature instanceof HasDerivedName){
			HasDerivedName hasDerivedName = (HasDerivedName)feature;

			return hasDerivedName.getDerivedName();
		}

		return feature.getName();
	}

	static
	public Feature findLabelFeature(List<? extends Feature> features, ScalarLabel scalarLabel){

		if(scalarLabel.isAnonymous()){
			throw new IllegalArgumentException();
		}

		return findFeature(features, scalarLabel.getName());
	}

	static
	public Feature findFeature(List<? extends Feature> features, String name){

		for(Feature feature : features){

			if(Objects.equals(feature.getName(), name)){
				return feature;
			}
		}

		return null;
	}
}