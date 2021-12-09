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
package org.jpmml.converter.transformations;

import java.util.Objects;

import org.dmg.pmml.Expression;
import org.dmg.pmml.FieldRef;
import org.dmg.pmml.PMMLFunctions;
import org.jpmml.converter.FieldNameUtil;
import org.jpmml.converter.PMMLUtil;

public class SigmoidTransformation extends AbstractTransformation {

	private Number multiplier = null;


	public SigmoidTransformation(Number multiplier){
		setMultiplier(multiplier);
	}

	@Override
	public String getName(String name){
		return FieldNameUtil.create("sigmoid", name);
	}

	@Override
	public Expression createExpression(FieldRef fieldRef){
		return createExpression(getMultiplier(), fieldRef);
	}

	public Number getMultiplier(){
		return this.multiplier;
	}

	private void setMultiplier(Number multiplier){
		this.multiplier = Objects.requireNonNull(multiplier);
	}

	static
	public Expression createExpression(Number multiplier, FieldRef fieldRef){
		Number one;

		if(multiplier instanceof Float){
			one = 1f;
		} else

		if(multiplier instanceof Double){
			one = 1d;
		} else

		{
			throw new IllegalArgumentException();
		}

		return PMMLUtil.createApply(PMMLFunctions.DIVIDE, PMMLUtil.createConstant(one), PMMLUtil.createApply(PMMLFunctions.ADD, PMMLUtil.createConstant(one), PMMLUtil.createApply(PMMLFunctions.EXP, PMMLUtil.createApply(PMMLFunctions.MULTIPLY, PMMLUtil.createConstant(multiplier), fieldRef))));
	}
}