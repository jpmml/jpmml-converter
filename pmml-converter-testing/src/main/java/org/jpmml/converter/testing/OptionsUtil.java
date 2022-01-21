/*
 * Copyright (c) 2022 Villu Ruusmann
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
package org.jpmml.converter.testing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OptionsUtil {

	private OptionsUtil(){
	}

	static
	public List<Map<String, Object>> generateOptionsMatrix(Map<String, Object> options){
		List<Map<String, Object>> result = new ArrayList<>();

		Collection<? extends Map.Entry<String, Object>> entries = options.entrySet();
		for(Map.Entry<String, Object> entry : entries){
			String key = entry.getKey();
			Object value = entry.getValue();

			if(value instanceof Object[]){
				Object[] array = (Object[])value;

				List<Map<String, Object>> arrayResult = new ArrayList<>();

				for(int i = 0; i < array.length; i++){
					arrayResult.addAll(extendOptions(result, key, array[i]));
				}

				result = arrayResult;
			} else

			{
				result = extendOptions(result, key, value);
			}
		}

		return result;
	}

	static
	private List<Map<String, Object>> extendOptions(List<Map<String, Object>> rows, String key, Object value){
		List<Map<String, Object>> result = new ArrayList<>();

		if(rows.isEmpty()){
			Map<String, Object> options = new LinkedHashMap<>();
			options.put(key, value);

			result.add(options);
		} else

		{
			for(Map<String, Object> row : rows){
				Map<String, Object> options = new LinkedHashMap<>(row);
				options.put(key, value);

				result.add(options);
			}
		}

		return result;
	}
}