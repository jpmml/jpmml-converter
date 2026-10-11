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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.dmg.pmml.DataType;
import org.jpmml.model.ToStringHelper;

public class Schema {

	private ModelEncoder encoder = null;

	private Label label = null;

	private List<? extends Feature> features = null;


	public Schema(ModelEncoder encoder, Label label, List<? extends Feature> features){
		setEncoder(encoder);
		setLabel(label);
		setFeatures(features);
	}

	/**
	 * <p>
	 * Creates a schema that is shared by all child models of an aggregating ensemble model.
	 * </p>
	 *
	 * <p>
	 * The prediction of the parent is a direct aggregate of the predictions of its children.
	 * </p>
	 */
	public Schema toSegmentSchema(){
		Label label = getLabel();

		if(label instanceof ScalarLabel){
			ScalarLabel scalarLabel = (ScalarLabel)label;

			// The sole target field does not need to be named.
			// Drop the original target field name, so that nested models can fall back to using the default target field name (ie. null).
			return toAnonymousSchema();
		} else

		if(label instanceof MultiLabel){
			MultiLabel multiLabel = (MultiLabel)label;

			// Multiple target fields must all be named.
			// Carry the original target field names forward to nested models.
			return this;
		} else

		{
			throw new IllegalArgumentException();
		}
	}

	public Schema toLocalSchema(){
		return toLocalSchema(null);
	}

	/**
	 * <p>
	 * Creates a schema for a child model of a chaining ensemble model.
	 * </p>
	 *
	 * <p>
	 * The prediction of the child is consumed by some of its siblings, and is not exposed by the parent.
	 * </p>
	 */
	public Schema toLocalSchema(String name){
		ModelEncoder encoder = getEncoder();
		Label label = getLabel();

		if(label instanceof ScalarLabel){
			return toAnonymousSchema();
		} else

		if(label instanceof MultiLabel){
			MultiLabel multiLabel = (MultiLabel)label;

			if(name == null){
				name = "_";
			}

			List<ScalarLabel> scalarLabels = new ArrayList<>();

			for(int i = 0; i < multiLabel.size(); i++){
				ScalarLabel scalarLabel = multiLabel.getScalarLabel(i);

				String localName = encoder.checkField(FieldNameUtil.create(name, i + 1));

				scalarLabels.add(scalarLabel.toRenamedLabel(localName));
			}

			return toRelabeledSchema(new MultiLabel(scalarLabels));
		} else

		{
			throw new IllegalArgumentException();
		}
	}

	public Schema toLocalRegressorSchema(DataType dataType){
		return toLocalRegressorSchema(null, dataType);
	}

	public Schema toLocalRegressorSchema(String name, DataType dataType){
		Label label = getLabel();

		if(label instanceof ScalarLabel){
			return toAnonymousRegressorSchema(dataType);
		} else

		if(label instanceof MultiLabel){
			MultiLabel multiLabel = (MultiLabel)label;

			switch(dataType){
				case FLOAT:
				case DOUBLE:
					break;
				default:
					throw new IllegalArgumentException();
			}

			MultiLabel regressorMultiLabel = multiLabel.toTransformedLabel(scalarLabel -> new ContinuousLabel(scalarLabel.getName(), dataType));

			return toRelabeledSchema(regressorMultiLabel)
				.toLocalSchema(name);
		} else

		{
			throw new IllegalArgumentException();
		}
	}

	public Schema toRelabeledSchema(Label label){
		ModelEncoder encoder = getEncoder();
		List<? extends Feature> features = getFeatures();

		return new Schema(encoder, label, features);
	}

	public Schema toAnonymousSchema(){
		ScalarLabel scalarLabel = requireScalarLabel();

		return toRelabeledSchema(scalarLabel != null ? scalarLabel.toAnonymousLabel() : null);
	}

	public Schema toAnonymousRegressorSchema(DataType dataType){

		switch(dataType){
			case FLOAT:
			case DOUBLE:
				break;
			default:
				throw new IllegalArgumentException();
		}

		Label label = new ContinuousLabel(dataType);

		return toRelabeledSchema(label);
	}

	public Schema toEmptySchema(){
		ModelEncoder encoder = getEncoder();
		Label label = getLabel();

		return new Schema(encoder, label, Collections.emptyList());
	}

	public Schema toSubSchema(int[] indexes){
		ModelEncoder encoder = getEncoder();
		Label label = getLabel();
		List<? extends Feature> features = getFeatures();

		List<Feature> selectedFeatures = new ArrayList<>(indexes.length);

		for(int index : indexes){
			Feature feature = features.get(index);

			selectedFeatures.add(feature);
		}

		return new Schema(encoder, label, selectedFeatures);
	}

	public Schema toTransformedSchema(Function<Feature, Feature> function){
		ModelEncoder encoder = getEncoder();
		Label label = getLabel();
		List<? extends Feature> features = getFeatures();

		List<? extends Feature> transformedFeatures = features.stream()
			.map(function)
			.collect(Collectors.toList());

		return new Schema(encoder, label, transformedFeatures);
	}

	public ContinuousLabel requireContinuousLabel(){
		Label label = getLabel();

		try {
			return (ContinuousLabel)label;
		} catch(ClassCastException cce){
			throw new UnsupportedLabelException("Expected a continuous label, got " + ExceptionUtil.formatLabel(label), cce);
		}
	}

	public CategoricalLabel requireCategoricalLabel(){
		Label label = getLabel();

		try {
			return (CategoricalLabel)label;
		} catch(ClassCastException cce){
			throw new UnsupportedLabelException("Expected a categorical label, got " + ExceptionUtil.formatLabel(label), cce);
		}
	}

	public OrdinalLabel requireOrdinalLabel(){
		Label label = getLabel();

		try {
			return (OrdinalLabel)label;
		} catch(ClassCastException cce){
			throw new UnsupportedLabelException("Expected an ordinal label, got " + ExceptionUtil.formatLabel(label), cce);
		}
	}

	public ScalarLabel requireScalarLabel(){
		Label label = getLabel();

		try {
			return (ScalarLabel)label;
		} catch(ClassCastException cce){
			throw new UnsupportedLabelException("Expected a scalar label, got " + ExceptionUtil.formatLabel(label), cce);
		}
	}

	public MultiLabel requireMultiLabel(){
		Label label = getLabel();

		try {
			return (MultiLabel)label;
		} catch(ClassCastException cce){
			throw new UnsupportedLabelException("Expected multiple labels, got " + ExceptionUtil.formatLabel(label), cce);
		}
	}

	@Override
	public String toString(){
		ToStringHelper helper = toStringHelper();

		return helper.toString();
	}

	protected ToStringHelper toStringHelper(){
		return new ToStringHelper(this)
			.add("label", getLabel())
			.add("features", getFeatures());
	}

	public ModelEncoder getEncoder(){
		return this.encoder;
	}

	private void setEncoder(ModelEncoder encoder){
		this.encoder = Objects.requireNonNull(encoder);
	}

	public Label getLabel(){
		return this.label;
	}

	private void setLabel(Label label){
		this.label = label;
	}

	public Feature getFeature(int index){
		List<? extends Feature> features = getFeatures();

		return features.get(index);
	}

	public List<? extends Feature> getFeatures(){
		return this.features;
	}

	private void setFeatures(List<? extends Feature> features){
		this.features = Objects.requireNonNull(features);
	}
}