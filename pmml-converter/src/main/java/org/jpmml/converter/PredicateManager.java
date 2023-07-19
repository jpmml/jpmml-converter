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

import org.dmg.pmml.Array;
import org.dmg.pmml.CompoundPredicate;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.SimplePredicate;
import org.dmg.pmml.SimpleSetPredicate;
import org.jpmml.model.PMMLObjectCache;

public class PredicateManager {

	private PMMLObjectCache<Predicate> cache = new PMMLObjectCache<>();


	public Predicate createPredicate(Feature feature, List<?> values){

		if(values.size() == 1){
			Object value = values.get(0);

			return createSimplePredicate(feature, SimplePredicate.Operator.EQUAL, value);
		}

		return createSimpleSetPredicate(feature, SimpleSetPredicate.BooleanOperator.IS_IN, values);
	}

	public Predicate createSimplePredicate(Feature feature, SimplePredicate.Operator operator, Object value){
		Predicate predicate = new SimplePredicate(feature.getName(), operator, value);

		return intern(predicate);
	}

	public Predicate createSimpleSetPredicate(Feature feature, SimpleSetPredicate.BooleanOperator booleanOperator, List<?> values){
		Array array = PMMLUtil.createArray(feature.getDataType(), values);

		Predicate predicate = new SimpleSetPredicate(feature.getName(), booleanOperator, array);

		return intern(predicate);
	}

	public Predicate createCompoundPredicate(CompoundPredicate.BooleanOperator booleanOperator, Predicate... predicates){
		Predicate predicate = new CompoundPredicate(booleanOperator, null)
			.addPredicates(predicates);

		return intern(predicate);
	}

	public Predicate intern(Predicate predicate){
		return this.cache.intern(predicate);
	}
}