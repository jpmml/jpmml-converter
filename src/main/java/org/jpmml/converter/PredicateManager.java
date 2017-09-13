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

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.dmg.pmml.Array;
import org.dmg.pmml.DataType;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.SimplePredicate;
import org.dmg.pmml.SimpleSetPredicate;

public class PredicateManager {

	private Interner<Predicate> interner = Interners.newStrongInterner();


	public Predicate createSimpleSetPredicate(Feature feature, List<String> values){

		if(values.size() == 1){
			String value = values.get(0);

			return createSimplePredicate(feature, SimplePredicate.Operator.EQUAL, value);
		}

		Predicate predicate = new InternableSimpleSetPredicate()
			.setField(feature.getName())
			.setBooleanOperator(SimpleSetPredicate.BooleanOperator.IS_IN)
			.setArray(createArray(feature.getDataType(), values));

		return intern(predicate);
	}

	public Predicate createSimplePredicate(Feature feature, SimplePredicate.Operator operator, String value){
		Predicate predicate = new InternableSimplePredicate()
			.setField(feature.getName())
			.setOperator(operator)
			.setValue(value);

		return intern(predicate);
	}

	public Predicate intern(Predicate predicate){
		return this.interner.intern(predicate);
	}

	static
	private Array createArray(DataType dataType, List<String> values){

		switch(dataType){
			case STRING:
				return new Array(Array.Type.STRING, ValueUtil.formatArray(values));
			case INTEGER:
				return new Array(Array.Type.INT, ValueUtil.formatArray(values));
			case DOUBLE:
			case FLOAT:
				return new Array(Array.Type.REAL, ValueUtil.formatArray(values));
			default:
				throw new IllegalArgumentException();
		}
	}
}