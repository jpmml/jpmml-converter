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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.OpType;

public class ScalarLabelUtil {

	private ScalarLabelUtil(){
	}

	static
	public ScalarLabel createScalarLabel(DataField dataField){
		DataType dataType = dataField.requireDataType();
		OpType opType = dataField.requireOpType();

		switch(opType){
			case CONTINUOUS:
				return new ContinuousLabel(dataField);
			case CATEGORICAL:
				return new CategoricalLabel(dataField);
			case ORDINAL:
				return new OrdinalLabel(dataField);
			default:
				throw new IllegalArgumentException();
		}
	}

	static
	public List<ScalarLabel> toScalarLabels(Label label){
		return toScalarLabels(ScalarLabel.class, label);
	}

	static
	public <E extends ScalarLabel> List<E> toScalarLabels(Class<? extends E> clazz, Label label){

		if(label instanceof ScalarLabel){
			ScalarLabel scalarLabel = (ScalarLabel)label;

			return Collections.singletonList(clazz.cast(scalarLabel));
		} else

		if(label instanceof MultiLabel){
			MultiLabel multiLabel = (MultiLabel)label;

			List<? extends Label> labels = multiLabel.getLabels();

			return labels.stream()
				.map(clazz::cast)
				.collect(Collectors.toList());
		} else

		{
			throw new IllegalArgumentException();
		}
	}
}