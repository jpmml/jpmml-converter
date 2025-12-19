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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import jakarta.xml.bind.JAXBElement;
import org.dmg.pmml.Cell;
import org.dmg.pmml.InlineTable;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Model;
import org.dmg.pmml.ModelVerification;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.PMMLObject;
import org.dmg.pmml.Row;
import org.dmg.pmml.VerificationField;
import org.dmg.pmml.VerificationFields;
import org.dmg.pmml.Visitor;
import org.dmg.pmml.VisitorAction;
import org.jpmml.model.visitors.AbstractVisitor;
import org.w3c.dom.Element;

/**
 * <p>
 * A Visitor that removes redundant columns from the model verification data table.
 * </p>
 */
public class ModelVerificationCleaner extends AbstractVisitor {

	@Override
	public PMMLObject popParent(){
		PMMLObject parent = super.popParent();

		if(parent instanceof Model){
			Model model = (Model)parent;

			processModel(model);
		}

		return parent;
	}

	private void processModel(Model model){
		ModelVerification modelVerification = model.getModelVerification();

		if(modelVerification != null){
			Set<String> activeFieldNames = new LinkedHashSet<>();
			Set<String> targetFieldNames = new LinkedHashSet<>();
			Set<String> outputFieldNames = new LinkedHashSet<>();

			MiningSchema miningSchema = model.requireMiningSchema();
			if(miningSchema.hasMiningFields()){
				List<MiningField> miningFields = miningSchema.getMiningFields();

				for(MiningField miningField : miningFields){
					String fieldName = miningField.requireName();

					MiningField.UsageType usageType = miningField.getUsageType();
					switch(usageType){
						case ACTIVE:
							activeFieldNames.add(fieldName);
							break;
						case PREDICTED:
						case TARGET:
							targetFieldNames.add(fieldName);
							break;
						default:
							break;
					}
				}
			}

			Visitor visitor = new AbstractVisitor(){

				@Override
				public VisitorAction visit(OutputField outputField){
					String name = outputField.requireName();

					outputFieldNames.add(name);

					return super.visit(outputField);
				}
			};
			visitor.applyTo(model);

			clean(modelVerification, activeFieldNames, targetFieldNames, outputFieldNames);
		}
	}

	private void clean(ModelVerification modelVerification, Set<String> activeFieldNames, Set<String> targetFieldNames, Set<String> outputFieldNames){
		VerificationFields verificationFields = modelVerification.requireVerificationFields();
		InlineTable inlineTable = modelVerification.requireInlineTable();

		boolean hasOutput = false;

		for(VerificationField verificationField : verificationFields){
			String fieldName = verificationField.requireField();

			hasOutput |= outputFieldNames.contains(fieldName);
		}

		// Model verification supports two modes - the target field mode (single field) and the output field mode (preferred, multiple fields).
		// If some verification field refers to an output field,
		// then assume that the end user intended to perform model verification in the output field mode,
		// and get rid of all target field-related data.
		Set<String> fieldNames = new HashSet<>();
		fieldNames.addAll(activeFieldNames);
		fieldNames.addAll(hasOutput ? outputFieldNames : targetFieldNames);

		Set<String> retainedColumns = new LinkedHashSet<>();

		for(Iterator<VerificationField> it = verificationFields.iterator(); it.hasNext(); ){
			VerificationField verificationField = it.next();

			String fieldName = verificationField.requireField();
			String column = verificationField.getColumn();

			if(!fieldNames.contains(fieldName)){
				it.remove();

				continue;
			}

			retainedColumns.add(column);
		}

		List<Row> rows = inlineTable.getRows();
		for(Row row : rows){

			if(!row.hasContent()){
				continue;
			}

			List<Object> cells = row.getContent();

			for(Iterator<?> it = cells.iterator(); it.hasNext(); ){
				Object cell = it.next();

				String column;

				if(cell instanceof Cell){
					Cell pmmlCell = (Cell)cell;

					column = formatColumn(pmmlCell.getName());
				} else

				if(cell instanceof JAXBElement){
					JAXBElement<?> jaxbElement = (JAXBElement<?>)cell;

					column = formatColumn(jaxbElement.getName());
				} else

				if(cell instanceof Element){
					Element domElement = (Element)cell;

					column = formatColumn(domElement.getPrefix(), domElement.getLocalName());
				} else

				{
					continue;
				} // End if

				if(!retainedColumns.contains(column)){
					it.remove();
				}
			}
		}
	}

	static
	private String formatColumn(QName xmlName){
		return formatColumn(xmlName.getPrefix(), xmlName.getLocalPart());
	}

	static
	private String formatColumn(String prefix, String localPart){

		if(prefix != null && !("").equals(prefix)){
			return prefix + ":" + localPart;
		}

		return localPart;
	}
}