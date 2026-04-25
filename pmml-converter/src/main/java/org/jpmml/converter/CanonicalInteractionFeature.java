/*
 * Copyright (c) 2026 Villu Ruusmann
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
import java.util.Objects;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.dmg.pmml.DataType;

class CanonicalInteractionFeature extends InteractionFeature {

	CanonicalInteractionFeature(PMMLEncoder encoder, String name, DataType dataType, List<? extends Feature> inputFeatures){
		super(encoder, name, dataType, inputFeatures);
	}

	@Override
	public int hashCode(){
		int result = 0;

		result = (31 * result) + Objects.hashCode(this.getDataType());
		result = (31 * result) + Objects.hashCode(this.getCanonicalFlattenedFeatures());

		return result;
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof CanonicalInteractionFeature){
			CanonicalInteractionFeature that = (CanonicalInteractionFeature)object;

			return (this.getDataType() == that.getDataType()) && Objects.equals(this.getCanonicalFlattenedFeatures(), that.getCanonicalFlattenedFeatures());
		}

		return false;
	}

	private Multiset<? extends Feature> getCanonicalFlattenedFeatures(){
		return HashMultiset.create(getFlattenedFeatures());
	}
}