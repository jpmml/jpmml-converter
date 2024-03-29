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
package org.jpmml.converter.visitors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Field;
import org.dmg.pmml.HasDerivedFields;
import org.dmg.pmml.LocalTransformations;
import org.dmg.pmml.Model;
import org.dmg.pmml.PMML;
import org.dmg.pmml.PMMLObject;
import org.dmg.pmml.TransformationDictionary;
import org.jpmml.converter.DerivedOutputField;

/**
 * <p>
 * A Visitor that removes redundant {@link DerivedField derived fields} from global and local transformation dictionaries.
 * </p>
 */
public class TransformationDictionaryCleaner extends ActiveFieldFinder {

	@Override
	public PMMLObject popParent(){
		PMMLObject parent = super.popParent();

		if(parent instanceof Model){
			Model model = (Model)parent;

			LocalTransformations localTransformations = model.getLocalTransformations();
			if(localTransformations != null){
				processDerivedFields(localTransformations);
			}
		} else

		if(parent instanceof PMML){
			PMML pmml = (PMML)parent;

			TransformationDictionary transformationDictionary = pmml.getTransformationDictionary();
			if(transformationDictionary != null){
				processDerivedFields(transformationDictionary);
			}
		}

		return parent;
	}

	private void processDerivedFields(HasDerivedFields<?> hasDerivedFields){

		if(hasDerivedFields.hasDerivedFields()){
			List<DerivedField> derivedFields = hasDerivedFields.getDerivedFields();

			Set<DerivedField> activeDerivedFields = getActiveDerivedFields(derivedFields);

			for(Iterator<DerivedField> it = derivedFields.iterator(); it.hasNext(); ){
				DerivedField derivedField = it.next();

				boolean retain = activeDerivedFields.contains(derivedField);

				if(derivedField instanceof DerivedOutputField){
					DerivedOutputField derivedOutputField = (DerivedOutputField)derivedField;

					retain |= derivedOutputField.isRequired();
				} // End if

				if(!retain){
					it.remove();
				}
			}
		}
	}

	private Set<DerivedField> getActiveDerivedFields(Collection<DerivedField> derivedFields){
		FieldDependencyResolver fieldDependencyResolver = getFieldDependencyResolver();

		Set<Field<?>> activeFields = getActiveFields();

		Set<DerivedField> activeDerivedFields = new HashSet<>(derivedFields);
		activeDerivedFields.retainAll(activeFields);

		while(true){
			Set<Field<?>> fields = new HashSet<>(activeDerivedFields);

			fieldDependencyResolver.expand(fields, activeDerivedFields);

			activeFields.addAll(fields);

			// Removes all fields that are not derived fields
			fields.retainAll(derivedFields);

			if(fields.isEmpty()){
				break;
			}

			activeDerivedFields.addAll((Set)fields);
		}

		return activeDerivedFields;
	}
}