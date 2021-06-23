/*
 * Copyright (c) 2021 Villu Ruusmann
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

import java.util.Arrays;
import java.util.Collections;

import org.dmg.pmml.False;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningFunction;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.SimplePredicate;
import org.dmg.pmml.True;
import org.dmg.pmml.tree.BranchNode;
import org.dmg.pmml.tree.ComplexNode;
import org.dmg.pmml.tree.LeafNode;
import org.dmg.pmml.tree.Node;
import org.dmg.pmml.tree.TreeModel;
import org.jpmml.converter.InternableSimplePredicate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TreeModelPrunerTest {

	@Test
	public void pruneFalse(){
		Node parent = new BranchNode()
			.setId(0)
			.setPredicate(new SimplePredicate(FieldName.create("x"), SimplePredicate.Operator.IS_NOT_MISSING, null));

		Node firstChild = new LeafNode()
			.setId(1)
			.setPredicate(False.INSTANCE);

		Node secondChild = new LeafNode()
			.setId(2)
			.setPredicate(True.INSTANCE);

		parent
			.setDefaultChild(firstChild.getId())
			.addNodes(firstChild, secondChild);

		prune(parent);

		assertEquals(Arrays.asList(firstChild, secondChild), parent.getNodes());

		parent.setDefaultChild(secondChild);

		prune(parent);

		assertEquals(Collections.singletonList(secondChild), parent.getNodes());

		secondChild.setPredicate(False.INSTANCE);

		prune(parent);

		assertEquals(Collections.singletonList(secondChild), parent.getNodes());

		parent.setDefaultChild(null);

		prune(parent);

		assertEquals(Collections.emptyList(), parent.getNodes());
	}

	@Test
	public void pruneNoOp(){
		SimplePredicate parentPredicate = new InternableSimplePredicate(FieldName.create("x"), SimplePredicate.Operator.NOT_EQUAL, 0);

		Node parent = new BranchNode()
			.setPredicate(parentPredicate);

		SimplePredicate childPredicate = new InternableSimplePredicate(FieldName.create("x"), SimplePredicate.Operator.NOT_EQUAL, 1);

		Node child = new BranchNode()
			.setPredicate(childPredicate);

		parent.addNodes(child);

		Node firstGrandchild = new LeafNode()
			.setPredicate(new SimplePredicate(FieldName.create("x"), SimplePredicate.Operator.EQUAL, 1));

		Node secondGrandchild = new LeafNode()
			.setPredicate(new SimplePredicate(FieldName.create("x"), SimplePredicate.Operator.EQUAL, 2));

		child.addNodes(firstGrandchild, secondGrandchild);

		prune(parent);

		assertEquals(Collections.singletonList(child), parent.getNodes());
		assertEquals(Arrays.asList(firstGrandchild, secondGrandchild), child.getNodes());

		childPredicate.setValue(0);

		prune(parent);

		assertEquals(Arrays.asList(firstGrandchild, secondGrandchild), parent.getNodes());
	}

	static
	private void prune(Node node){
		Node root = new ComplexNode()
			.setPredicate(True.INSTANCE)
			.addNodes(node);

		MiningSchema miningSchema = new MiningSchema()
			.addMiningFields(new MiningField(FieldName.create("x")));

		TreeModel treeModel = new TreeModel(MiningFunction.REGRESSION, miningSchema, root);

		TreeModelPruner pruner = new TreeModelPruner();
		pruner.applyTo(treeModel);
	}
}