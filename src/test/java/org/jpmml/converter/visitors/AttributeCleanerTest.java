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

import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningFunction;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.OutlierTreatmentMethod;
import org.dmg.pmml.Output;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.PMMLAttributes;
import org.dmg.pmml.SimplePredicate;
import org.dmg.pmml.True;
import org.dmg.pmml.tree.BranchNode;
import org.dmg.pmml.tree.LeafNode;
import org.dmg.pmml.tree.Node;
import org.dmg.pmml.tree.TreeModel;
import org.jpmml.model.ReflectionUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AttributeCleanerTest {

	@Test
	public void clean(){
		MiningField miningField = new MiningField()
			.setUsageType(MiningField.UsageType.ACTIVE)
			.setOutlierTreatment(OutlierTreatmentMethod.AS_MISSING_VALUES);

		assertEquals(MiningField.UsageType.ACTIVE, ReflectionUtil.getFieldValue(PMMLAttributes.MININGFIELD_USAGETYPE, miningField));
		assertEquals(OutlierTreatmentMethod.AS_MISSING_VALUES, ReflectionUtil.getFieldValue(PMMLAttributes.MININGFIELD_OUTLIERTREATMENT, miningField));

		MiningSchema miningSchema = new MiningSchema()
			.addMiningFields(miningField);

		OutputField outputField = new OutputField()
			.setFinalResult(Boolean.TRUE)
			.setIsMultiValued("0");

		assertEquals(Boolean.TRUE, ReflectionUtil.getFieldValue(PMMLAttributes.OUTPUTFIELD_FINALRESULT, outputField));
		assertEquals("0", ReflectionUtil.getFieldValue(PMMLAttributes.OUTPUTFIELD_ISMULTIVALUED, outputField));

		Output output = new Output()
			.addOutputFields(outputField);

		Node root = new BranchNode(null, True.INSTANCE);

		SimplePredicate leftPredicate = new SimplePredicate(FieldName.create("x"), SimplePredicate.Operator.LESS_THAN, 0);
		SimplePredicate rightPredicate = new SimplePredicate(FieldName.create("x"), SimplePredicate.Operator.GREATER_OR_EQUAL, 0);

		assertEquals((Integer)0, ReflectionUtil.getFieldValue(PMMLAttributes.SIMPLEPREDICATE_VALUE, leftPredicate));
		assertEquals((Integer)0, ReflectionUtil.getFieldValue(PMMLAttributes.SIMPLEPREDICATE_VALUE, rightPredicate));

		root.addNodes(
			new LeafNode("negative", leftPredicate),
			new LeafNode("nonNegative", rightPredicate)
		);

		TreeModel treeModel = new TreeModel(MiningFunction.CLASSIFICATION, miningSchema, root)
			.setScorable(Boolean.FALSE)
			.setOutput(output);

		assertEquals(Boolean.FALSE, ReflectionUtil.getFieldValue(org.dmg.pmml.tree.PMMLAttributes.TREEMODEL_SCORABLE, treeModel));

		AttributeCleaner cleaner = new AttributeCleaner();
		cleaner.applyTo(treeModel);

		assertEquals((Enum<?>)null, ReflectionUtil.getFieldValue(PMMLAttributes.MININGFIELD_USAGETYPE, miningField));
		assertEquals(OutlierTreatmentMethod.AS_MISSING_VALUES, ReflectionUtil.getFieldValue(PMMLAttributes.MININGFIELD_OUTLIERTREATMENT, miningField));

		assertEquals(MiningField.UsageType.ACTIVE, miningField.getUsageType());
		assertEquals(OutlierTreatmentMethod.AS_MISSING_VALUES, miningField.getOutlierTreatment());

		assertEquals((Boolean)null, ReflectionUtil.getFieldValue(PMMLAttributes.OUTPUTFIELD_FINALRESULT, outputField));
		assertEquals((String)null, ReflectionUtil.getFieldValue(PMMLAttributes.OUTPUTFIELD_ISMULTIVALUED, outputField));

		assertEquals(Boolean.TRUE, outputField.isFinalResult());
		assertEquals("0", outputField.getIsMultiValued());

		assertEquals(Boolean.FALSE, ReflectionUtil.getFieldValue(org.dmg.pmml.tree.PMMLAttributes.TREEMODEL_SCORABLE, treeModel));

		assertEquals((Integer)0, ReflectionUtil.getFieldValue(PMMLAttributes.SIMPLEPREDICATE_VALUE, leftPredicate));
		assertEquals((Integer)0, ReflectionUtil.getFieldValue(PMMLAttributes.SIMPLEPREDICATE_VALUE, rightPredicate));
	}
}