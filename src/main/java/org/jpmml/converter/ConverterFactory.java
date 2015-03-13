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
package org.jpmml.converter;

import java.util.Map;

import com.google.common.collect.Maps;
import rexp.Rexp;
import rexp.Rexp.REXP;
import rexp.Rexp.STRING;

public class ConverterFactory {

	protected ConverterFactory(){
	}

	public Converter getConverter(REXP rexp){
		Rexp.REXP names = REXPUtil.attribute(rexp, "class");

		for(int i = 0; i < names.getStringValueCount(); i++){
			STRING name = names.getStringValue(i);

			Class<? extends Converter> clazz = ConverterFactory.converters.get(name.getStrval());
			if(clazz != null){

				try {
					return clazz.newInstance();
				} catch(Exception e){
					throw new IllegalArgumentException(e);
				}
			}
		}

		throw new IllegalArgumentException();
	}

	static
	public ConverterFactory getInstance(){
		return new ConverterFactory();
	}

	private static Map<String, Class<? extends Converter>> converters = Maps.newLinkedHashMap();

	static {
		converters.put("BinaryTree", BinaryTreeConverter.class);
		converters.put("gbm", GBMConverter.class);
		converters.put("kmeans", KMeansConverter.class);
		converters.put("randomForest", RandomForestConverter.class);
		converters.put("train", TrainConverter.class);
	}
}