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

import java.util.Objects;

import org.jpmml.model.ToStringHelper;

abstract
public class ProductFeature extends Feature {

	private Feature feature = null;

	private Number factor = null;


	public ProductFeature(PMMLEncoder encoder, Feature feature, Number factor){
		super(encoder, FeatureUtil.getName(feature), feature.getDataType());

		setFeature(feature);
		setFactor(factor);
	}

	@Override
	public int hashCode(){
		int result = super.hashCode();

		result = (31 * result) + Objects.hashCode(this.getFeature());
		result = (31 * result) + Objects.hashCode(this.getFactor());

		return result;
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof ProductFeature){
			ProductFeature that = (ProductFeature)object;

			return super.equals(object) && Objects.equals(this.getFeature(), that.getFeature()) && Objects.equals(this.getFactor(), that.getFactor());
		}

		return false;
	}

	@Override
	protected ToStringHelper toStringHelper(){
		return super.toStringHelper()
			.add("feature", getFeature())
			.add("factor", getFactor());
	}

	public Feature getFeature(){
		return this.feature;
	}

	private void setFeature(Feature feature){
		this.feature = Objects.requireNonNull(feature);
	}

	public Number getFactor(){
		return this.factor;
	}

	private void setFactor(Number factor){
		this.factor = Objects.requireNonNull(factor);
	}
}
