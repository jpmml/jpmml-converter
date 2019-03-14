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

import java.util.Objects;

import org.dmg.pmml.Expression;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldRef;

public class FunctionTransformation extends AbstractTransformation {

	private String function = null;


	public FunctionTransformation(String function){
		setFunction(function);
	}

	@Override
	public FieldName getName(FieldName name){
		String function = getFunction();

		return withPrefix(name, function);
	}

	@Override
	public Expression createExpression(FieldRef fieldRef){
		String function = getFunction();

		return PMMLUtil.createApply(function, fieldRef);
	}

	public String getFunction(){
		return this.function;
	}

	private void setFunction(String function){
		this.function = Objects.requireNonNull(function);
	}
}