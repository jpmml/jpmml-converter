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
import org.dmg.pmml.OpType;
import org.dmg.pmml.Value;

public class WildcardFeature extends Feature {

	public WildcardFeature(PMMLEncoder encoder, DataField dataField){
		super(encoder, dataField.getName(), dataField.getDataType());
	}

	public CategoricalFeature toCategoricalFeature(List<String> values){
		PMMLEncoder encoder = ensureEncoder();

		DataField dataField = encoder.getDataField(getName());
		if(dataField == null){
			throw new IllegalArgumentException();
		}

		dataField.setOpType(OpType.CATEGORICAL);

		for(String value : values){
			dataField.addValues(new Value(value));
		}

		CategoricalFeature feature = new CategoricalFeature(encoder, dataField);

		return feature;
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		PMMLEncoder encoder = ensureEncoder();

		DataField dataField = encoder.getDataField(getName());
		if(dataField == null){
			throw new IllegalArgumentException();
		}

		dataField.setOpType(OpType.CONTINUOUS);

		ContinuousFeature feature = new ContinuousFeature(encoder, dataField);

		return feature;
	}
}