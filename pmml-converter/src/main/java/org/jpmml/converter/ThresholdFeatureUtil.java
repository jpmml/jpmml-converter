/*
 * Copyright (c) 2021 Villu Ruusmann
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

import org.dmg.pmml.CompoundPredicate;
import org.dmg.pmml.False;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.SimplePredicate;
import org.dmg.pmml.SimpleSetPredicate;

public class ThresholdFeatureUtil {

	private ThresholdFeatureUtil(){
	}

	static
	public Predicate createPredicate(ThresholdFeature thresholdFeature, List<?> values, Object missingValue, PredicateManager predicateManager){
		boolean checkMissing = values.remove(missingValue);

		Predicate valuesPredicate = (!values.isEmpty() ? predicateManager.createPredicate(thresholdFeature, values) : null);
		Predicate missingValuePredicate = (checkMissing ? predicateManager.createSimplePredicate(thresholdFeature, SimplePredicate.Operator.IS_MISSING, null) : null);

		if(valuesPredicate != null){

			if(missingValuePredicate != null){
				return predicateManager.createCompoundPredicate(CompoundPredicate.BooleanOperator.SURROGATE, valuesPredicate, missingValuePredicate);
			}

			return valuesPredicate;
		} // End if

		if(missingValuePredicate != null){
			return missingValuePredicate;
		}

		return False.INSTANCE;
	}

	static
	public boolean isMissingValueSafe(Predicate predicate){

		if(predicate instanceof SimplePredicate){
			SimplePredicate simplePredicate = (SimplePredicate)predicate;

			SimplePredicate.Operator operator = simplePredicate.requireOperator();
			switch(operator){
				case IS_MISSING:
					return true;
				case IS_NOT_MISSING:
					return false;
				default:
					return false;
			}
		} else

		if(predicate instanceof SimpleSetPredicate){
			SimpleSetPredicate simpleSetPredicate = (SimpleSetPredicate)predicate;

			return false;
		} else

		if(predicate instanceof CompoundPredicate){
			CompoundPredicate compoundPredicate = (CompoundPredicate)predicate;

			CompoundPredicate.BooleanOperator booleanOperator = compoundPredicate.requireBooleanOperator();
			switch(booleanOperator){
				case SURROGATE:
					return true;
				default:
					return false;
			}
		} else

		if(predicate instanceof False){
			return false;
		}

		throw new IllegalArgumentException();
	}
}