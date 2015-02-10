/*
 * Copyright (c) 2014 Villu Ruusmann
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

import com.google.common.collect.Lists;
import org.dmg.pmml.Array;
import org.dmg.pmml.Cluster;
import org.dmg.pmml.ClusteringField;
import org.dmg.pmml.ClusteringModel;
import org.dmg.pmml.CompareFunctionType;
import org.dmg.pmml.ComparisonMeasure;
import org.dmg.pmml.DataDictionary;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.Header;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningFunctionType;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.OpType;
import org.dmg.pmml.Output;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.PMML;
import org.dmg.pmml.ResultFeatureType;
import org.dmg.pmml.SquaredEuclidean;
import rexp.Rexp;
import rexp.Rexp.STRING;

public class KMeansConverter extends Converter {

	@Override
	public PMML convert(Rexp.REXP kmeans){
		Rexp.REXP centers = REXPUtil.field(kmeans, "centers");
		Rexp.REXP size = REXPUtil.field(kmeans, "size");

		Rexp.REXP dim = REXPUtil.attribute(centers, "dim");
		Rexp.REXP dimnames = REXPUtil.attribute(centers, "dimnames");

		int rows = dim.getIntValue(0);
		int columns = dim.getIntValue(1);

		DataDictionary dataDictionary = new DataDictionary();

		MiningSchema miningSchema = new MiningSchema();

		ComparisonMeasure comparisonMeasure = new ComparisonMeasure(ComparisonMeasure.Kind.DISTANCE)
			.withMeasure(new SquaredEuclidean())
			.withCompareFunction(CompareFunctionType.ABS_DIFF);

		List<ClusteringField> clusteringFields = Lists.newArrayList();

		Rexp.REXP columnNames = dimnames.getRexpValue(1);
		for(int i = 0; i < columns; i++){
			STRING name = columnNames.getStringValue(i);

			DataField dataField = new DataField(FieldName.create(name.getStrval()), OpType.CONTINUOUS, DataType.DOUBLE);
			dataDictionary = dataDictionary.withDataFields(dataField);

			MiningField miningField = new MiningField(dataField.getName());
			miningSchema = miningSchema.withMiningFields(miningField);

			ClusteringField clusteringField = new ClusteringField(miningField.getName());

			clusteringFields.add(clusteringField);
		}

		List<Cluster> clusters = Lists.newArrayList();

		Rexp.REXP rowNames = dimnames.getRexpValue(0);
		for(int i = 0; i < rows; i++){
			STRING name = rowNames.getStringValue(i);

			Cluster cluster = new Cluster()
				.withName(name.getStrval())
				.withArray(encodeArray(REXPUtil.getRow(centers.getRealValueList(), i, rows, columns)))
				.withSize(size.getIntValue(i));

			clusters.add(cluster);
		}

		ClusteringModel clusteringModel = new ClusteringModel(MiningFunctionType.CLUSTERING, ClusteringModel.ModelClass.CENTER_BASED, rows, miningSchema, comparisonMeasure, clusteringFields, clusters);

		OutputField predictedValue = new OutputField(FieldName.create("predictedValue"))
			.withFeature(ResultFeatureType.PREDICTED_VALUE);

		Output output = new Output()
			.withOutputFields(predictedValue);

		clusteringModel = clusteringModel.withOutput(output);

		PMML pmml = new PMML("4.2", new Header(), dataDictionary)
			.withModels(clusteringModel);

		return pmml;
	}

	private Array encodeArray(List<Double> values){
		String value = formatArrayValue(values);

		Array array = new Array(Array.Type.REAL, value);

		return array;
	}

	static
	private String formatArrayValue(List<Double> values){
		StringBuilder sb = new StringBuilder();

		String sep = "";

		for(Double value : values){
			sb.append(sep);

			sb.append(PMMLUtil.formatValue(value));

			sep = " ";
		}

		return sb.toString();
	}
}