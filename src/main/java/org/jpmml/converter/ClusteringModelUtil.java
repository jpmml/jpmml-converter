/*
 * Copyright (c) 2016 Villu Ruusmann
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

import org.dmg.pmml.Cluster;
import org.dmg.pmml.ClusteringField;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.Output;
import org.dmg.pmml.OutputField;

public class ClusteringModelUtil {

	private ClusteringModelUtil(){
	}

	static
	public List<ClusteringField> createClusteringFields(List<FieldName> names){
		return createClusteringFields(names, null);
	}

	static
	public List<ClusteringField> createClusteringFields(List<FieldName> names, List<Number> weights){

		if((weights != null) && (names.size() != weights.size())){
			throw new IllegalArgumentException();
		}

		List<ClusteringField> clusteringFields = new ArrayList<>();

		for(int i = 0; i < names.size(); i++){
			FieldName name = names.get(i);
			Number weight = (weights != null ? weights.get(i) : null);

			ClusteringField clusteringField = new ClusteringField(name);

			if(weight != null && !ValueUtil.isOne(weight)){
				clusteringField.setFieldWeight(ValueUtil.asDouble(weight));
			}

			clusteringFields.add(clusteringField);
		}

		return clusteringFields;
	}

	static
	public Output createOutput(FieldName name, List<Cluster> clusters){
		List<OutputField> outputFields = new ArrayList<>();
		outputFields.add(ModelUtil.createPredictedField(name));
		outputFields.addAll(ModelUtil.createAffinityFields(clusters));

		Output output = new Output(outputFields);

		return output;
	}
}