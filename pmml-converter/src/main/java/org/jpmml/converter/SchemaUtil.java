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

public class SchemaUtil {

	private SchemaUtil(){
	}

	static
	public void checkSize(int size, DiscreteLabel discreteLabel, List<? extends Feature> features){

		if((discreteLabel.size() * features.size()) != size){
			throw new IllegalArgumentException("Expected " + size + " elements, got " + (discreteLabel.size() * features.size()) + " elements");
		}
	}

	static
	public void checkSize(int size, DiscreteLabel discreteLabel){

		if(discreteLabel.size() != size){
			throw new IllegalArgumentException("Expected " + size + " target categories, got " + discreteLabel.size() + " target categories");
		}
	}

	static
	public void checkSize(int size, List<? extends Feature> features){

		if(features.size() != size){
			throw new IllegalArgumentException("Expected " + size + " feature(s), got " + features.size() + " feature(s)");
		}
	}

	static
	public void checkSize(int size, CategoricalFeature categoricalFeature){

		if(categoricalFeature.size() != size){
			throw new IllegalArgumentException("Expected " + size + " categories, got " + categoricalFeature.size() + " categories");
		}
	}
}