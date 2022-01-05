/*
 * Copyright (c) 2015 Villu Ruusmann
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
package org.jpmml.converter.visitors;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.dmg.pmml.Field;

public class FieldUtil {

	private FieldUtil(){
	}

	static
	public <F extends Field<?>> Map<String, F> nameMap(Collection<? extends F> fields){
		Map<String, F> fieldMap = new LinkedHashMap<>(2 * fields.size());

		for(F field : fields){
			String name = field.requireName();

			F previousField = fieldMap.put(name, field);
			if(previousField != null){
				throw new IllegalArgumentException("Fields " + format(field) + " and " + format(previousField) + " have the same name " + name);
			}
		}

		return fieldMap;
	}

	static
	public <F extends Field<?>> Set<String> nameSet(Collection<? extends F> fields){
		Map<String, F> fieldMap = nameMap(fields);

		return fieldMap.keySet();
	}

	static
	public <F extends Field<?>> Collection<F> selectAll(Collection<? extends F> fields, Set<String> names){
		return selectAll(fields, names, false);
	}

	static
	public <F extends Field<?>> Collection<F> selectAll(Collection<? extends F> fields, Set<String> names, boolean allowPartialSelection){
		Map<String, F> fieldMap = new LinkedHashMap<>(2 * names.size());

		for(F field : fields){
			String name = field.requireName();

			if(!names.contains(name)){
				continue;
			}

			F previousField = fieldMap.put(name, field);
			if(previousField != null){
				throw new IllegalArgumentException("Fields " + format(field) + " and " + format(previousField) + " have the same name " + name);
			}
		}

		if(!(allowPartialSelection) && (fieldMap.size() < names.size())){
			Set<String> unmatchedNames = new LinkedHashSet<>(names);
			unmatchedNames.removeAll(fieldMap.keySet());

			throw new IllegalArgumentException("Name(s) " + unmatchedNames + " do not match any fields");
		}

		return fieldMap.values();
	}

	static
	private String format(Field<?> field){
		return String.valueOf(field);
	}
}