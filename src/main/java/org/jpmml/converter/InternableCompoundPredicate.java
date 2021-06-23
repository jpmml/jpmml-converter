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
import java.util.Objects;

import org.dmg.pmml.CompoundPredicate;
import org.dmg.pmml.Predicate;

public class InternableCompoundPredicate extends CompoundPredicate {

	public InternableCompoundPredicate(){
	}

	public InternableCompoundPredicate(CompoundPredicate.BooleanOperator booleanOperator, List<Predicate> predicates){
		super(booleanOperator, predicates);
	}

	@Override
	public int hashCode(){
		int result = 0;

		result += (31 * result) + Objects.hashCode(getBooleanOperator());
		result += (31 * result) + Objects.hashCode(getPredicates());

		return result;
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof InternableCompoundPredicate){
			InternableCompoundPredicate that = (InternableCompoundPredicate)object;

			return Objects.equals(this.getBooleanOperator(), that.getBooleanOperator()) && Objects.equals(this.getPredicates(), that.getPredicates());
		}

		return false;
	}
}