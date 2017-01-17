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
package org.jpmml.converter.tree;

import java.util.List;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.Output;
import org.dmg.pmml.OutputField;
import org.jpmml.converter.CategoricalLabel;
import org.jpmml.converter.ContinuousLabel;
import org.jpmml.converter.Label;
import org.jpmml.converter.ModelUtil;
import org.jpmml.converter.Schema;

public class TreeModelUtil {

	private TreeModelUtil(){
	}

	static
	public Output createNodeOutput(Schema schema){
		List<OutputField> outputFields;

		Label label = schema.getLabel();
		if(label instanceof CategoricalLabel){
			CategoricalLabel categoricalLabel = (CategoricalLabel)label;

			outputFields = ModelUtil.createProbabilityFields(categoricalLabel.getValues());
		} else

		if(label instanceof ContinuousLabel){
			outputFields = null;
		} else

		{
			throw new IllegalArgumentException();
		}

		Output output = new Output(outputFields)
			.addOutputFields(ModelUtil.createEntityIdField(FieldName.create("nodeId")));

		return output;
	}
}