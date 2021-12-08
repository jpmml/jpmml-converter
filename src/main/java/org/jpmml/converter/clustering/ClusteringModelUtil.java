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
package org.jpmml.converter.clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.dmg.pmml.DataType;
import org.dmg.pmml.OpType;
import org.dmg.pmml.Output;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.clustering.Cluster;
import org.dmg.pmml.clustering.ClusteringField;
import org.jpmml.converter.ContinuousFeature;
import org.jpmml.converter.Feature;
import org.jpmml.converter.ModelUtil;
import org.jpmml.converter.ValueUtil;

public class ClusteringModelUtil {

	private ClusteringModelUtil(){
	}

	static
	public List<ClusteringField> createClusteringFields(List<? extends Feature> features){
		return createClusteringFields(features, null);
	}

	static
	public List<ClusteringField> createClusteringFields(List<? extends Feature> features, List<? extends Number> weights){

		if((weights != null) && (features.size() != weights.size())){
			throw new IllegalArgumentException();
		}

		List<ClusteringField> clusteringFields = new ArrayList<>();

		for(int i = 0; i < features.size(); i++){
			Feature feature = features.get(i);
			Number weight = (weights != null ? weights.get(i) : null);

			ContinuousFeature continuousFeature = feature.toContinuousFeature();

			ClusteringField clusteringField = new ClusteringField(continuousFeature.getName());

			if(weight != null && !ValueUtil.isOne(weight)){
				clusteringField.setFieldWeight(weight);
			}

			clusteringFields.add(clusteringField);
		}

		return clusteringFields;
	}

	static
	public Output createOutput(String name, DataType dataType, List<Cluster> clusters){
		Output output = new Output();

		List<String> ids = clusters.stream()
			.map(cluster -> cluster.getId())
			.collect(Collectors.toList());

		List<OutputField> outputFields = output.getOutputFields();
		outputFields.add(ModelUtil.createPredictedField(name, OpType.CATEGORICAL, DataType.STRING, ids));
		outputFields.addAll(ModelUtil.createAffinityFields(dataType, clusters));

		return output;
	}
}