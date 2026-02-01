/*
 * Copyright (c) 2025 Villu Ruusmann
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

import org.dmg.pmml.DataType;
import org.dmg.pmml.Field;
import org.dmg.pmml.HasDiscreteDomain;

public class OrdinalFeature extends DiscreteFeature {

	public <F extends Field<F> & HasDiscreteDomain<F>> OrdinalFeature(PMMLEncoder encoder, F field){
		super(encoder, field);
	}

	public OrdinalFeature(PMMLEncoder encoder, Field<?> field, List<?> values){
		super(encoder, field, values);
	}

	public OrdinalFeature(PMMLEncoder encoder, Feature feature, List<?> values){
		super(encoder, feature, values);
	}

	public OrdinalFeature(PMMLEncoder encoder, String name, DataType dataType, List<?> values){
		super(encoder, name, dataType, values);
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		CategoricalFeature categoricalFeature = toCategoricalFeature();

		return categoricalFeature.toContinuousFeature();
	}

	@Override
	public IndexFeature toCategoricalFeature(){
		throw new UnsupportedOperationException();
	}

	@Override
	public OrdinalFeature expectCardinality(int size){
		return (OrdinalFeature)super.expectCardinality(size);
	}
}