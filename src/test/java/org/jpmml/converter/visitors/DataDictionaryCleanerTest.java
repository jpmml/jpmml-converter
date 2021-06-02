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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.dmg.pmml.DataDictionary;
import org.dmg.pmml.DataField;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Model;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Visitor;
import org.dmg.pmml.VisitorAction;
import org.dmg.pmml.mining.MiningModel;
import org.jpmml.converter.ModelUtil;
import org.jpmml.model.ChainedSegmentationTest;
import org.jpmml.model.FieldNameUtil;
import org.jpmml.model.NestedSegmentationTest;
import org.jpmml.model.ResourceUtil;
import org.jpmml.model.visitors.AbstractVisitor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DataDictionaryCleanerTest {

	@Test
	public void cleanChained() throws Exception {
		PMML pmml = ResourceUtil.unmarshal(ChainedSegmentationTest.class);

		DataDictionary dataDictionary = pmml.getDataDictionary();

		checkFields(FieldNameUtil.create("y", "x1", "x2", "x3", "x4"), dataDictionary.getDataFields());

		updateUsageType(pmml, MiningField.UsageType.SUPPLEMENTARY);

		DataDictionaryCleaner cleaner = new DataDictionaryCleaner();
		cleaner.applyTo(pmml);

		checkFields(FieldNameUtil.create("y", "x1", "x2", "x3", "x4"), dataDictionary.getDataFields());

		updateUsageType(pmml, MiningField.UsageType.ACTIVE);

		cleaner.reset();

		cleaner.applyTo(pmml);

		checkFields(FieldNameUtil.create("y", "x1", "x2", "x3"), dataDictionary.getDataFields());

		List<Model> models = pmml.getModels();
		models.clear();

		cleaner.reset();

		cleaner.applyTo(pmml);

		checkFields(Collections.emptySet(), dataDictionary.getDataFields());
	}

	@Test
	public void cleanNested() throws Exception {
		PMML pmml = ResourceUtil.unmarshal(NestedSegmentationTest.class);

		DataDictionary dataDictionary = pmml.getDataDictionary();

		checkFields(FieldNameUtil.create("y", "x1", "x2", "x3", "x4", "x5"), dataDictionary.getDataFields());

		DataDictionaryCleaner cleaner = new DataDictionaryCleaner();
		cleaner.applyTo(pmml);

		checkFields(FieldNameUtil.create("x1", "x2", "x3", "x4", "x5"), dataDictionary.getDataFields());

		List<Model> models = pmml.getModels();
		models.clear();

		cleaner.reset();

		cleaner.applyTo(pmml);

		checkFields(Collections.emptySet(), dataDictionary.getDataFields());
	}

	static
	private void checkFields(Set<FieldName> names, Collection<DataField> dataFields){
		assertEquals(names, FieldUtil.nameSet(dataFields));
	}

	static
	private void updateUsageType(PMML pmml, MiningField.UsageType usageType){
		Visitor visitor = new AbstractVisitor(){

			@Override
			public VisitorAction visit(MiningSchema miningSchema){
				Model model = (Model)getParent();

				if(model instanceof MiningModel){
					MiningModel miningModel = (MiningModel)model;

					List<MiningField> miningFields = miningSchema.getMiningFields();
					if(miningFields.size() > 4){
						List<MiningField> extraMiningFields = miningFields.subList(4, miningFields.size());

						extraMiningFields.clear();
					}

					miningSchema.addMiningFields(ModelUtil.createMiningField(FieldName.create("x4"), usageType));
				}

				return super.visit(miningSchema);
			}
		};
		visitor.applyTo(pmml);
	}
}