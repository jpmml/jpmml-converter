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

import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldRef;
import org.dmg.pmml.OpType;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.ResultFeature;

abstract
public class AbstractTransformation implements Transformation {

	@Override
	public OpType getOpType(OpType opType){
		return opType;
	}

	@Override
	public DataType getDataType(DataType dataType){
		return dataType;
	}

	@Override
	public ResultFeature getResultFeature(){
		return ResultFeature.TRANSFORMED_VALUE;
	}

	@Override
	public 	boolean isFinalResult(){
		return false;
	}

	@Override
	public OutputField createOutputField(OutputField outputField){
		return new OutputField(getName(outputField.getName()), getOpType(outputField.getOpType()), getDataType(outputField.getDataType()))
			.setResultFeature(getResultFeature())
			.setFinalResult(isFinalResult())
			.setExpression(createExpression(new FieldRef(outputField.getName())));
	}
}