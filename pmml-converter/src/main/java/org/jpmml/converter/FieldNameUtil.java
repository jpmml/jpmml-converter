/*
 * Copyright (c) 2020 Villu Ruusmann
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dmg.pmml.DataType;
import org.dmg.pmml.DefineFunction;
import org.dmg.pmml.OpType;

public class FieldNameUtil {

	private FieldNameUtil(){
	}

	static
	public String create(DataType dataType, Object... args){
		return create((dataType.name()).toLowerCase(), args);
	}

	static
	public String create(OpType opType, Object... args){
		return create((opType.name()).toLowerCase(), args);
	}

	static
	public String create(DefineFunction defineFunction, Object... args){
		return create(defineFunction.requireName(), args);
	}

	static
	public String create(String function, Object... args){
		return format(function, Arrays.asList(args));
	}

	static
	public String create(String function, List<?> args){
		return format(function, args);
	}

	static
	public String select(String name, int index){

		if(index < 0){
			throw new IllegalArgumentException();
		}

		return name + ("[" + index + "]");
	}

	static
	private String format(String function, List<?> args){

		if(args == null || args.isEmpty()){
			return function;
		} else

		{
			Stream<?> argStream;

			if(args.size() <= 5){
				argStream = args.stream();
			} else

			{
				argStream = Stream.of(
					args.subList(0, 2).stream(),
					Stream.of(".."),
					args.subList(args.size() - 2, args.size()).stream()
				).flatMap(x -> x);
			}

			return argStream
				.map(FieldNameUtil::toString)
				.collect(Collectors.joining(", ", function + "(", ")"));
		}
	}

	static
	private String toString(Object object){

		if(object instanceof Feature){
			Feature feature = (Feature)object;

			object = FeatureUtil.getName(feature);
		}

		return String.valueOf(object);
	}
}