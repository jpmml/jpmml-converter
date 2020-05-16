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

import com.google.common.collect.ImmutableList;
import org.dmg.pmml.DataType;
import org.dmg.pmml.Field;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.NormDiscrete;

public class BooleanFeature extends CategoricalFeature implements HasDerivedName {

	public BooleanFeature(PMMLEncoder encoder, Field<?> field){
		this(encoder, field.getName());
	}

	public BooleanFeature(PMMLEncoder encoder, Feature feature){
		this(encoder, feature.getName());
	}

	public BooleanFeature(PMMLEncoder encoder, FieldName name){
		super(encoder, name, DataType.BOOLEAN, BooleanFeature.VALUES);
	}

	@Override
	public FieldName getDerivedName(){
		return FieldName.create((getName()).getValue() + "=true");
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		return toContinuousFeature(getDerivedName(), DataType.DOUBLE, () -> new NormDiscrete(getName(), Boolean.TRUE));
	}

	public static final Boolean VALUE_TRUE = true;
	public static final Boolean VALUE_FALSE = false;

	public static final List<Object> VALUES = ImmutableList.of(BooleanFeature.VALUE_FALSE, BooleanFeature.VALUE_TRUE);
}