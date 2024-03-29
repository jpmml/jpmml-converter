/*
 * Copyright (c) 2022 Villu Ruusmann
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

import org.jpmml.model.ToStringHelper;

public class MultiLabel extends Label {

	private List<? extends Label> labels = null;


	public MultiLabel(List<? extends Label> labels){
		setLabels(labels);
	}

	@Override
	public int hashCode(){
		return (31 * super.hashCode()) + Objects.hashCode(getLabels());
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof MultiLabel){
			MultiLabel that = (MultiLabel)object;

			return super.equals(object) && Objects.equals(this.getLabels(), that.getLabels());
		}

		return false;
	}

	@Override
	protected ToStringHelper toStringHelper(){
		return super.toStringHelper()
			.add("labels", getLabels());
	}

	public Label getLabel(int index){
		List<? extends Label> labels = getLabels();

		return labels.get(index);
	}

	public List<? extends Label> getLabels(){
		return this.labels;
	}

	private void setLabels(List<? extends Label> labels){
		this.labels = Objects.requireNonNull(labels);
	}
}