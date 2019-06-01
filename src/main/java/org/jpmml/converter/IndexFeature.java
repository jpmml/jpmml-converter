/*
 * Copyright (c) 2019 Villu Ruusmann
 *
 * This file is part of JPMML-SparkML
 *
 * JPMML-SparkML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-SparkML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-SparkML.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.converter;

import java.util.List;
import java.util.function.Supplier;

import org.dmg.pmml.DataType;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Field;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldRef;
import org.dmg.pmml.OpType;

public class IndexFeature extends CategoricalFeature {

	public IndexFeature(PMMLEncoder encoder, Field<?> field, List<? extends Number> values){
		this(encoder, field.getName(), field.getDataType(), values);
	}

	public IndexFeature(PMMLEncoder encoder, Feature feature, List<? extends Number> values){
		this(encoder, feature.getName(), feature.getDataType(), values);
	}

	public IndexFeature(PMMLEncoder encoder, FieldName name, DataType dataType, List<? extends Number> values){
		super(encoder, name, dataType, values);
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		PMMLEncoder encoder = ensureEncoder();

		// XXX: Cannot derive a global field from a local field
		try {
			encoder.getField(getName());
		} catch(IllegalArgumentException iae){
			return new ContinuousFeature(encoder, this);
		}

		Supplier<FieldRef> fieldRefSupplier = () -> {
			return IndexFeature.this.ref();
		};

		DerivedField derivedField = encoder.ensureDerivedField(FeatureUtil.createName("continuous", this), OpType.CONTINUOUS, getDataType(), fieldRefSupplier);

		return new ContinuousFeature(encoder, derivedField);
	}
}