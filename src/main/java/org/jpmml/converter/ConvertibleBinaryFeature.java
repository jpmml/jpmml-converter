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

import org.dmg.pmml.DataType;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Expression;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.NormDiscrete;
import org.dmg.pmml.OpType;
import org.dmg.pmml.TypeDefinitionField;

public class ConvertibleBinaryFeature extends BinaryFeature {

	private PMMLEncoder encoder = null;


	public ConvertibleBinaryFeature(PMMLEncoder encoder, TypeDefinitionField field, String value){
		super(field, value);

		setEncoder(encoder);
	}

	public ConvertibleBinaryFeature(PMMLEncoder encoder, FieldName name, DataType dataType, String value){
		super(name, dataType, value);

		setEncoder(encoder);
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		PMMLEncoder encoder = getEncoder();

		FieldName name = FieldName.create(getName() + "=" + getValue());

		DerivedField derivedField = encoder.getDerivedField(name);
		if(derivedField == null){
			Expression expression = createExpression();

			derivedField = encoder.createDerivedField(name, OpType.CONTINUOUS, DataType.DOUBLE, expression);
		}

		ContinuousFeature feature = new ContinuousFeature(derivedField);

		return feature;
	}

	protected Expression createExpression(){
		NormDiscrete normDiscrete = new NormDiscrete(getName(), getValue());

		return normDiscrete;
	}

	public PMMLEncoder getEncoder(){
		return this.encoder;
	}

	private void setEncoder(PMMLEncoder encoder){
		this.encoder = encoder;
	}
}