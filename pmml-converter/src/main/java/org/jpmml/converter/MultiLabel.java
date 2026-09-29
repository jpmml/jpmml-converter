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

	private List<? extends ScalarLabel> scalarLabels = null;


	public MultiLabel(List<? extends ScalarLabel> scalarLabels){
		setScalarLabels(scalarLabels);
	}

	public MultiLabel expectSize(int size){
		List<? extends ScalarLabel> scalarLabels = getScalarLabels();

		if(scalarLabels.size() != size){
			throw new InvalidLabelException("Expected a multi-label with " + ExceptionUtil.formatCount(size, "element") + ", got " + scalarLabels.size());
		}

		return this;
	}

	@Override
	public int hashCode(){
		return (31 * super.hashCode()) + Objects.hashCode(getScalarLabels());
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof MultiLabel){
			MultiLabel that = (MultiLabel)object;

			return super.equals(object) && Objects.equals(this.getScalarLabels(), that.getScalarLabels());
		}

		return false;
	}

	@Override
	protected ToStringHelper toStringHelper(){
		return super.toStringHelper()
			.add("scalarLabels", getScalarLabels());
	}

	public int size(){
		List<? extends ScalarLabel> scalarLabels = getScalarLabels();

		return scalarLabels.size();
	}

	public ScalarLabel getScalarLabel(int index){
		List<? extends ScalarLabel> scalarLabels = getScalarLabels();

		return scalarLabels.get(index);
	}

	public List<? extends ScalarLabel> getScalarLabels(){
		return this.scalarLabels;
	}

	private void setScalarLabels(List<? extends ScalarLabel> scalarLabels){
		this.scalarLabels = Objects.requireNonNull(scalarLabels);
	}
}