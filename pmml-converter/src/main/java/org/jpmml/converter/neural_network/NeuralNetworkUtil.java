/*
 * Copyright (c) 2017 Villu Ruusmann
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
package org.jpmml.converter.neural_network;

import java.util.Arrays;
import java.util.List;

import org.dmg.pmml.DataType;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Expression;
import org.dmg.pmml.FieldRef;
import org.dmg.pmml.NormDiscrete;
import org.dmg.pmml.OpType;
import org.dmg.pmml.neural_network.Connection;
import org.dmg.pmml.neural_network.NeuralEntity;
import org.dmg.pmml.neural_network.NeuralInput;
import org.dmg.pmml.neural_network.NeuralInputs;
import org.dmg.pmml.neural_network.NeuralLayer;
import org.dmg.pmml.neural_network.NeuralNetwork;
import org.dmg.pmml.neural_network.NeuralOutput;
import org.dmg.pmml.neural_network.NeuralOutputs;
import org.dmg.pmml.neural_network.Neuron;
import org.jpmml.converter.BinaryFeature;
import org.jpmml.converter.BooleanFeature;
import org.jpmml.converter.CategoricalLabel;
import org.jpmml.converter.ContinuousFeature;
import org.jpmml.converter.ContinuousLabel;
import org.jpmml.converter.Feature;
import org.jpmml.converter.Label;
import org.jpmml.converter.ScalarLabelUtil;
import org.jpmml.converter.ValueUtil;

public class NeuralNetworkUtil {

	private NeuralNetworkUtil(){
	}

	static
	public NeuralInputs createNeuralInputs(List<? extends Feature> features, DataType dataType){
		NeuralInputs neuralInputs = new NeuralInputs();

		for(int i = 0; i < features.size(); i++){
			Feature feature = features.get(i);

			Expression expression;

			if(feature instanceof BinaryFeature){
				BinaryFeature binaryFeature = (BinaryFeature)feature;

				expression = new NormDiscrete(binaryFeature.getName(), binaryFeature.getValue());
			} else

			if(feature instanceof BooleanFeature){
				BooleanFeature booleanFeature = (BooleanFeature)feature;

				expression = new NormDiscrete(booleanFeature.getName(), BooleanFeature.VALUE_TRUE);
			} else

			{
				ContinuousFeature continuousFeature = feature.toContinuousFeature();

				expression = continuousFeature.ref();
			}

			DerivedField derivedField = new DerivedField(null, OpType.CONTINUOUS, dataType, expression);

			NeuralInput neuralInput = new NeuralInput()
				.setId("input/" + String.valueOf(i + 1))
				.setDerivedField(derivedField);

			neuralInputs.addNeuralInputs(neuralInput);
		}

		return neuralInputs;
	}

	static
	public Neuron createNeuron(List<? extends NeuralEntity> entities, List<? extends Number> weights, Number bias){

		if(entities.size() != weights.size()){
			throw new IllegalArgumentException();
		}

		Neuron neuron = new Neuron();

		for(int i = 0; i < entities.size(); i++){
			NeuralEntity entity = entities.get(i);
			Number weight = weights.get(i);

			if(weight == null || ValueUtil.isZeroLike(weight)){
				continue;
			}

			Connection connection = new Connection()
				.setFrom(entity.requireId())
				.setWeight(weight);

			neuron.addConnections(connection);
		}

		if(bias != null && !ValueUtil.isZeroLike(bias)){
			neuron.setBias(bias);
		}

		return neuron;
	}

	static
	public List<NeuralLayer> createBinaryLogisticTransformation(NeuralEntity entity){
		NeuralLayer inputLayer = new NeuralLayer()
			.setActivationFunction(NeuralNetwork.ActivationFunction.LOGISTIC);

		Neuron logisticNeuron = new Neuron()
			.setId("logistic/1")
			.setBias(null)
			.addConnections(new Connection(entity.requireId(), 1d));

		inputLayer.addNeurons(logisticNeuron);

		entity = logisticNeuron;

		NeuralLayer outputLayer = new NeuralLayer()
			.setActivationFunction(NeuralNetwork.ActivationFunction.IDENTITY);

		Neuron noEventNeuron = new Neuron()
			.setId("event/false")
			.setBias(1d)
			.addConnections(new Connection(entity.requireId(), -1d));

		Neuron eventNeuron = new Neuron()
			.setId("event/true")
			.setBias(null)
			.addConnections(new Connection(entity.requireId(), 1d));

		outputLayer.addNeurons(noEventNeuron, eventNeuron);

		return Arrays.asList(inputLayer, outputLayer);
	}

	static
	public NeuralOutputs createRegressionNeuralOutputs(List<? extends NeuralEntity> entities, Label label){
		List<ContinuousLabel> continuousLabels = ScalarLabelUtil.toScalarLabels(ContinuousLabel.class, label);

		if(entities.size() != continuousLabels.size()){
			throw new IllegalArgumentException();
		}

		NeuralOutputs neuralOutputs = new NeuralOutputs();

		for(int i = 0; i < entities.size(); i++){
			NeuralEntity entity = entities.get(i);
			ContinuousLabel continuousLabel = continuousLabels.get(i);

			DerivedField derivedField = new DerivedField(null, OpType.CONTINUOUS, continuousLabel.getDataType(), new FieldRef(continuousLabel.getName()));

			NeuralOutput neuralOutput = new NeuralOutput()
				.setOutputNeuron(entity.requireId())
				.setDerivedField(derivedField);

			neuralOutputs.addNeuralOutputs(neuralOutput);
		}

		return neuralOutputs;
	}

	static
	public NeuralOutputs createClassificationNeuralOutputs(List<? extends NeuralEntity> entities, CategoricalLabel categoricalLabel){
		categoricalLabel.expectCardinality(entities.size());

		NeuralOutputs neuralOutputs = new NeuralOutputs();

		for(int i = 0; i < entities.size(); i++){
			NeuralEntity entity = entities.get(i);

			DerivedField derivedField = new DerivedField(null, OpType.CATEGORICAL, categoricalLabel.getDataType(), new NormDiscrete(categoricalLabel.getName(), categoricalLabel.getValue(i)));

			NeuralOutput neuralOutput = new NeuralOutput()
				.setOutputNeuron(entity.requireId())
				.setDerivedField(derivedField);

			neuralOutputs.addNeuralOutputs(neuralOutput);
		}

		return neuralOutputs;
	}
}