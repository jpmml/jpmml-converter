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

import org.dmg.pmml.Apply;
import org.dmg.pmml.DataType;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Field;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.OpType;
import org.jpmml.model.ToStringHelper;

public class PowerFeature extends Feature implements HasDerivedName {

	private int power = 1;


	public PowerFeature(PMMLEncoder encoder, Field<?> field, int power){
		this(encoder, field.getName(), field.getDataType(), power);
	}

	public PowerFeature(PMMLEncoder encoder, Feature feature, int power){
		this(encoder, feature.getName(), feature.getDataType(), power);
	}

	public PowerFeature(PMMLEncoder encoder, FieldName name, DataType dataType, int power){
		super(encoder, name, dataType);

		setPower(power);
	}

	@Override
	public FieldName getDerivedName(){
		return FieldName.create((getName()).getValue() + "^" + getPower());
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		PMMLEncoder encoder = ensureEncoder();

		FieldName derivedName = getDerivedName();

		DerivedField derivedField = encoder.getDerivedField(derivedName);
		if(derivedField == null){
			Apply apply = PMMLUtil.createApply("pow", ref(), PMMLUtil.createConstant(getPower()));

			derivedField = encoder.createDerivedField(derivedName, OpType.CONTINUOUS, DataType.DOUBLE, apply);
		}

		return new ContinuousFeature(encoder, derivedField);
	}

	@Override
	public int hashCode(){
		return (31 * super.hashCode()) + getPower();
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof PowerFeature){
			PowerFeature that = (PowerFeature)object;

			return super.equals(object) && (this.getPower() == that.getPower());
		}

		return false;
	}

	@Override
	protected ToStringHelper toStringHelper(){
		return super.toStringHelper()
			.add("power", getPower());
	}

	public int getPower(){
		return this.power;
	}

	private void setPower(int power){
		this.power = power;
	}
}