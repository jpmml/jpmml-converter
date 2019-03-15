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

import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;

public class WildcardFeature extends Feature {

	public WildcardFeature(PMMLEncoder encoder, DataField dataField){
		super(encoder, dataField.getName(), dataField.getDataType());
	}

	public CategoricalFeature toCategoricalFeature(List<?> values){
		PMMLEncoder encoder = ensureEncoder();

		DataField dataField = (DataField)encoder.toCategorical(getName(), values);

		if((DataType.BOOLEAN).equals(dataField.getDataType()) && (BooleanFeature.VALUES).equals(values)){
			return new BooleanFeature(encoder, dataField);
		}

		return new CategoricalFeature(encoder, dataField);
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		PMMLEncoder encoder = ensureEncoder();

		DataField dataField = (DataField)encoder.toContinuous(getName());

		return new ContinuousFeature(encoder, dataField);
	}
}