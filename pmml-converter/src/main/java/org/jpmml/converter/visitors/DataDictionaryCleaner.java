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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.dmg.pmml.DataDictionary;
import org.dmg.pmml.DataField;
import org.dmg.pmml.Field;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Model;
import org.dmg.pmml.PMML;
import org.dmg.pmml.PMMLObject;

/**
 * <p>
 * A Visitor that removes redundant {@link DataField data fields} from the {@link DataDictionary data dictionary}.
 * </p>
 */
public class DataDictionaryCleaner extends ActiveFieldFinder {

	private Set<Field<?>> nonActiveFields = new LinkedHashSet<>();


	@Override
	public void reset(){
		super.reset();

		this.nonActiveFields.clear();
	}

	@Override
	public PMMLObject popParent(){
		PMMLObject parent = super.popParent();

		if(parent instanceof Model){
			Model model = (Model)parent;

			processModel(model);
		} else

		if(parent instanceof PMML){
			PMML pmml = (PMML)parent;

			DataDictionary dataDictionary = pmml.getDataDictionary();
			if(dataDictionary != null){
				processDataDictionary(dataDictionary);
			}
		}

		return parent;
	}

	private void processModel(Model model){
		Set<Field<?>> nonActiveFields = getNonActiveFields();

		MiningSchema miningSchema = model.getMiningSchema();
		if(miningSchema != null && miningSchema.hasMiningFields()){
			Set<String> names = new LinkedHashSet<>();

			List<MiningField> miningFields = miningSchema.getMiningFields();
			for(MiningField miningField : miningFields){
				String name = miningField.getName();

				MiningField.UsageType usageType = miningField.getUsageType();
				switch(usageType){
					case ACTIVE:
						break;
					default:
						names.add(name);
						break;
				}
			}

			if(names.size() > 0){
				Collection<Field<?>> modelFields = getFields(model);

				nonActiveFields.addAll(FieldUtil.selectAll(modelFields, names));
			}
		}
	}

	private void processDataDictionary(DataDictionary dataDictionary){

		if(dataDictionary.hasDataFields()){
			List<DataField> dataFields = dataDictionary.getDataFields();

			Set<DataField> referencedDataFields = getReferencedDataFields();

			dataFields.retainAll(referencedDataFields);
		}
	}

	private Set<DataField> getReferencedDataFields(){
		FieldDependencyResolver fieldDependencyResolver = getFieldDependencyResolver();

		Set<Field<?>> fields = new HashSet<>(getActiveFields());
		fields.addAll(getNonActiveFields());

		fieldDependencyResolver.expand(fields, fieldDependencyResolver.getLocalDerivedFields());
		fieldDependencyResolver.expand(fields, fieldDependencyResolver.getGlobalDerivedFields());

		return (Set)fields;
	}

	private Set<Field<?>> getNonActiveFields(){
		return this.nonActiveFields;
	}
}