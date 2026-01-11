/*
 * Copyright (c) 2019 Villu Ruusmann
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

import com.google.common.base.CaseFormat;

public class SchemaUtil {

	private SchemaUtil(){
	}

	static
	public void checkSize(int size, DiscreteLabel discreteLabel, List<? extends Feature> features){

		if((discreteLabel.size() * features.size()) != size){
			throw new ConversionException("Expected " + ExceptionUtil.formatCount(size, "element") + ", got " + (discreteLabel.size() * features.size()));
		}
	}

	static
	public void checkSize(int size, List<? extends Feature> features){

		if(features.size() != size){
			throw new SchemaException("Expected " + ExceptionUtil.formatCount(size, "feature") + ", got " + features.size());
		}
	}

	static
	public String formatTypeString(Class<?> clazz){

		if(clazz.isAnonymousClass()){
			clazz = clazz.getSuperclass();
		}

		String clazzName = clazz.getSimpleName();

		return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazzName).replace('_', ' ');
	}
}