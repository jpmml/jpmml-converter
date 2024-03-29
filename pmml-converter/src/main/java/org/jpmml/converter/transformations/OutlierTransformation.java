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

import org.dmg.pmml.DataType;
import org.dmg.pmml.Decision;
import org.dmg.pmml.Decisions;
import org.dmg.pmml.OpType;

abstract
public class OutlierTransformation extends AbstractBusinessDecision {

	@Override
	public String getName(String name){
		return "outlier";
	}

	@Override
	public OpType getOpType(OpType opType){
		return OpType.CATEGORICAL;
	}

	@Override
	public DataType getDataType(DataType dataType){
		return DataType.BOOLEAN;
	}

	@Override
	public Decisions createDecisions(){
		Decisions decisions = new Decisions()
			.setBusinessProblem("Is this sample an outlier?")
			.addDecisions(
				new Decision(true)
					.setDescription("Outlier"),
				new Decision(false)
					.setDescription("Not outlier")
			);

		return decisions;
	}
}