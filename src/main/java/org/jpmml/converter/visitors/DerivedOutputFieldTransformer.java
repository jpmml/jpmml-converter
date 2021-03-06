/*
 * Copyright (c) 2019 Villu Ruusmann
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
package org.jpmml.converter.visitors;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dmg.pmml.DerivedField;
import org.dmg.pmml.HasDerivedFields;
import org.dmg.pmml.LocalTransformations;
import org.dmg.pmml.Model;
import org.dmg.pmml.TransformationDictionary;
import org.dmg.pmml.VisitorAction;
import org.jpmml.converter.DerivedOutputField;
import org.jpmml.model.visitors.AbstractVisitor;

public class DerivedOutputFieldTransformer extends AbstractVisitor {

	@Override
	public VisitorAction visit(LocalTransformations localTransformations){
		processDerivedFields(localTransformations);

		return super.visit(localTransformations);
	}

	@Override
	public VisitorAction visit(TransformationDictionary transformationDictionary){
		processDerivedFields(transformationDictionary);

		return super.visit(transformationDictionary);
	}

	private void processDerivedFields(HasDerivedFields<?> hasDerivedFields){

		if(hasDerivedFields.hasDerivedFields()){
			List<DerivedField> derivedFields = hasDerivedFields.getDerivedFields();

			Map<Model, Integer> indices = new IdentityHashMap<>();

			for(Iterator<DerivedField> it = derivedFields.iterator(); it.hasNext(); ){
				DerivedField derivedField = it.next();

				if(derivedField instanceof DerivedOutputField){
					DerivedOutputField derivedOutputField = (DerivedOutputField)derivedField;

					Model model = derivedOutputField.getModel();

					Integer index = indices.get(model);
					if(index == null){
						index = 0;
					}

					derivedOutputField.addOutputField(index);

					index = (index + 1);

					indices.put(model, index);

					it.remove();
				}
			}
		}
	}
}