/*
 * Copyright (c) 2026 Villu Ruusmann
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
import java.util.stream.Collectors;

import org.dmg.pmml.StringValue;

public class ExceptionUtil {

	private ExceptionUtil(){
	}

	static
	public String formatCount(int count, String singular){
		return formatCount(count, singular, singular + "s");
	}

	static
	public String formatCount(int count, String singular, String plural){

		if(count == 0){
			return "no" + " " + plural;
		} else

		if(count == 1){
			return "one" + " " + singular;
		} else

		if(count >= 2){
			return String.valueOf(count) + " " + plural;
		} else

		{
			throw new IllegalArgumentException();
		}
	}

	static
	public String formatClass(Class<?> clazz){
		return clazz.getTypeName();
	}

	static
	public String formatClasses(Iterable<Class<?>> clazzes){
		StringBuilder sb = new StringBuilder();

		for(Class<?> clazz : clazzes){

			if(sb.length() > 0){
				sb.append(", ");
			}

			sb.append(formatClass(clazz));
		}

		return sb.toString();
	}

	static
	public String formatClassName(String name){
		return name;
	}

	static
	public String formatClassNames(Iterable<String> names){
		StringBuilder sb = new StringBuilder();

		for(String name : names){

			if(sb.length() > 0){
				sb.append(", ");
			}

			sb.append(name);
		}

		return sb.toString();
	}

	static
	public String formatName(Feature feature){
		return formatName(feature.getName());
	}

	static
	public String formatName(String name){
		return "\'" + name + "\'";
	}

	static
	public String formatNames(Iterable<String> names){
		StringBuilder sb = new StringBuilder();

		for(String name : names){

			if(sb.length() > 0){
				sb.append(", ");
			}

			sb.append(formatName(name));
		}

		return sb.toString();
	}

	static
	public String formatNameList(List<? extends Feature> features){
		List<String> names = features.stream()
			.map(feature -> formatName(feature))
			.collect(Collectors.toList());

		return formatNameList(names);
	}

	static
	public String formatNameList(Iterable<String> names){
		return "[" + formatNames(names) + "]";
	}

	static
	public String formatNameSet(Iterable<String> names){
		return "{" + formatNames(names) + "}";
	}

	static
	public String formatLiteral(String value){
		return formatName(value);
	}

	static
	public String formatParameter(String value){
		return formatName(value);
	}

	static
	public <E extends Enum<E> & StringValue<E>> String formatValue(E value){
		return value.value();
	}

	static
	public <E extends Enum<E> & StringValue<E>> String formatValues(Iterable<E> values){
		StringBuilder sb = new StringBuilder();

		for(E value : values){

			if(sb.length() > 0){
				sb.append(", ");
			}

			sb.append(formatValue(value));
		}

		return sb.toString();
	}

	static
	public String formatVersion(String version){
		return formatName(version);
	}
}