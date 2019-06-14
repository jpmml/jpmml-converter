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

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.dmg.pmml.Cell;
import org.dmg.pmml.FieldName;
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
			Set<FieldName> activeFieldNames = new LinkedHashSet<>();
			Set<FieldName> targetFieldNames = new LinkedHashSet<>();
			Set<FieldName> outputFieldNames = new LinkedHashSet<>();

			MiningSchema miningSchema = model.getMiningSchema();
			if(miningSchema != null && miningSchema.hasMiningFields()){
				List<MiningField> miningFields = miningSchema.getMiningFields();

				for(MiningField miningField : miningFields){
					FieldName name = miningField.getName();

					MiningField.UsageType usageType = miningField.getUsageType();
					switch(usageType){
						case ACTIVE:
							activeFieldNames.add(name);
							break;
						case PREDICTED:
						case TARGET:
							targetFieldNames.add(name);
							break;
						default:
							break;
					}
				}
			}

			Visitor visitor = new AbstractVisitor(){

				@Override
				public VisitorAction visit(OutputField outputField){
					FieldName name = outputField.getName();

					outputFieldNames.add(name);

					return super.visit(outputField);
				}
			};
			visitor.applyTo(model);

			clean(modelVerification, activeFieldNames, targetFieldNames, outputFieldNames);
		}
	}

	private void clean(ModelVerification modelVerification, Set<FieldName> activeFieldNames, Set<FieldName> targetFieldNames, Set<FieldName> outputFieldNames){
		VerificationFields verificationFields = modelVerification.getVerificationFields();
		InlineTable inlineTable = modelVerification.getInlineTable();

		if(verificationFields == null || !verificationFields.hasVerificationFields()){
			return;
		} // End if

		if(inlineTable == null || !inlineTable.hasRows()){
			return;
		}

		boolean hasOutput = false;

		for(VerificationField verificationField : verificationFields){
			FieldName name = verificationField.getField();

			hasOutput |= outputFieldNames.contains(name);
		}

		// Model verification supports two modes - the target field mode (single field) and the output field mode (preferred, multiple fields).
		// If some verification field refers to an output field,
		// then assume that the end user intended to perform model verification in the output field mode,
		// and get rid of all target field-related data.
		Set<FieldName> names = new HashSet<>();
		names.addAll(activeFieldNames);
		names.addAll(hasOutput ? outputFieldNames : targetFieldNames);

		Set<String> retainedColumns = new LinkedHashSet<>();

		for(Iterator<VerificationField> it = verificationFields.iterator(); it.hasNext(); ){
			VerificationField verificationField = it.next();

			FieldName name = verificationField.getField();
			String column = verificationField.getColumn();

			if(!names.contains(name)){
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