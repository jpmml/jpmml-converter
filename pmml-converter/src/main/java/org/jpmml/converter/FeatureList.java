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

import java.util.ArrayList;
import java.util.List;

public class FeatureList extends ArrayList<Feature> implements FeatureResolver {

	private List<String> names = null;


	public FeatureList(List<? extends Feature> features, List<String> names){
		super(features);

		if(names == null || features.size() != names.size()){
			throw new IllegalArgumentException();
		}

		setNames(names);
	}

	@Override
	public Feature resolveFeature(String name){
		List<String> names = getNames();

		int index = names.indexOf(name);
		if(index < 0){
			throw new ResolutionException("Feature \'" + name + "\' is undefined");
		}

		return get(index);
	}

	public List<String> getNames(){
		return this.names;
	}

	public void setNames(List<String> names){
		this.names = names;
	}
}