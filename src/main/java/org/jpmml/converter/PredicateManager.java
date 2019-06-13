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


	public Predicate createSimpleSetPredicate(Feature feature, List<?> values){

		if(values.size() == 1){
			Object value = values.get(0);

			return createSimplePredicate(feature, SimplePredicate.Operator.EQUAL, value);
		}

		Predicate predicate = new InternableSimpleSetPredicate(feature.getName(), SimpleSetPredicate.BooleanOperator.IS_IN, createArray(feature.getDataType(), values));

		return intern(predicate);
	}

	public Predicate createSimplePredicate(Feature feature, SimplePredicate.Operator operator, Object value){
		Predicate predicate = new InternableSimplePredicate(feature.getName(), operator, value);

		return intern(predicate);
	}

	public Predicate intern(Predicate predicate){
		return this.interner.intern(predicate);
	}

	static
	private Array createArray(DataType dataType, List<?> values){

		switch(dataType){
			case STRING:
				return PMMLUtil.createStringArray(values);
			case INTEGER:
				// XXX
				return PMMLUtil.createIntArray((List)values);
			case DOUBLE:
			case FLOAT:
				// XXX
				return PMMLUtil.createRealArray((List)values);
			default:
				throw new IllegalArgumentException();
		}
	}
}