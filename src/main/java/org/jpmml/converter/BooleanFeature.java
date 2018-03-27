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

import java.util.Arrays;

import org.dmg.pmml.DataType;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Field;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.NormDiscrete;
import org.dmg.pmml.OpType;

public class BooleanFeature extends CategoricalFeature implements HasDerivedName {

	public BooleanFeature(PMMLEncoder encoder, Field<?> field){
		super(encoder, field, Arrays.asList("false", "true"));
	}

	@Override
	public FieldName getDerivedName(){
		return FieldName.create((getName()).getValue() + "=true");
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		PMMLEncoder encoder = ensureEncoder();

		FieldName derivedName = getDerivedName();

		DerivedField derivedField = encoder.getDerivedField(derivedName);
		if(derivedField == null){
			NormDiscrete normDiscrete = new NormDiscrete(getName(), "true");

			derivedField = encoder.createDerivedField(derivedName, OpType.CONTINUOUS, DataType.DOUBLE, normDiscrete);
		}

		return new ContinuousFeature(encoder, derivedField);
	}
}