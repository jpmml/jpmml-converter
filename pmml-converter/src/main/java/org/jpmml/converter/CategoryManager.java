/*
 * Copyright (c) 2018 Villu Ruusmann
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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class CategoryManager extends ValueManager<Set<Object>> {

	public CategoryManager(){
	}

	public CategoryManager(Map<String, Set<Object>> valueMap){
		super(valueMap);
	}

	public CategoryManager fork(String name, Collection<Object> values){
		return fork(name, new LinkedHashSet<>(values));
	}

	@Override
	public CategoryManager fork(String name, Set<Object> values){
		Map<String, Set<Object>> valueMap = new LinkedHashMap<>(getValueMap());

		valueMap.put(name, values);

		return new CategoryManager(valueMap);
	}

	public Predicate<Object> getValueFilter(String name){
		Set<Object> values = getValue(name);

		Predicate<Object> predicate = new Predicate<Object>(){

			@Override
			public boolean test(Object value){

				if(values != null){
					return values.contains(value);
				}

				return true;
			}
		};

		return predicate;
	}
}