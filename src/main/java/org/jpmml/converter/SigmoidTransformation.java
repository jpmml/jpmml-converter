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

import org.dmg.pmml.Expression;
import org.dmg.pmml.FieldRef;

public class SigmoidTransformation extends AbstractTransformation {

	private Number multiplier = null;


	public SigmoidTransformation(Number multiplier){
		setMultiplier(multiplier);
	}

	@Override
	public Expression createExpression(FieldRef fieldRef){
		return createExpression(getMultiplier(), fieldRef);
	}

	public Number getMultiplier(){
		return this.multiplier;
	}

	private void setMultiplier(Number multiplier){

		if(multiplier == null){
			throw new IllegalArgumentException();
		}

		this.multiplier = multiplier;
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

		return PMMLUtil.createApply("/", PMMLUtil.createConstant(one), PMMLUtil.createApply("+", PMMLUtil.createConstant(one), PMMLUtil.createApply("exp", PMMLUtil.createApply("*", PMMLUtil.createConstant(multiplier), fieldRef))));
	}
}