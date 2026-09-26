/*
 * Copyright (c) 2026 Villu Ruusmann
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

import org.dmg.pmml.ComplexScoreDistribution;
import org.dmg.pmml.Score;
import org.dmg.pmml.SimplePredicate;
import org.dmg.pmml.True;
import org.dmg.pmml.tree.ComplexNode;
import org.dmg.pmml.tree.Node;
import org.dmg.pmml.tree.TreeModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TreeModelFlattenerTest extends TreeModelTransformerTest {

	@Test
	public void flattenRegression(){
		Node grandchild = createLeaf(null, 1d, new SimplePredicate("x", SimplePredicate.Operator.LESS_OR_EQUAL, 1));

		Node child = createBranch(null, 1.5d, new SimplePredicate("x", SimplePredicate.Operator.LESS_OR_EQUAL, 2), null, grandchild);

		Node root = createRoot(null, 4d, null, child);

		TreeModel treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.NONE, root);

		flatten(new ClassificationTreeModelFlattener(), treeModel);

		assertEquals(TreeModel.SplitCharacteristic.BINARY_SPLIT, treeModel.getSplitCharacteristic());

		TreeModelFlattener flattener = new RegressionTreeModelFlattener();

		flatten(flattener, treeModel);

		checkTreeModel(treeModel, TreeModel.MissingValueStrategy.NONE, TreeModel.NoTrueChildStrategy.RETURN_NULL_PREDICTION);
		checkRoot(root, 4d, grandchild, child);
		checkLeaf(grandchild, 1d);
		checkLeaf(child, 1.5d);

		grandchild = createLeaf(null, 1d, new SimplePredicate("x", SimplePredicate.Operator.LESS_THAN, 1));

		child = createBranch(null, 1.5d, new SimplePredicate("x", SimplePredicate.Operator.LESS_THAN, 2), null, grandchild);

		root = createRoot(null, 4d, null, child);

		treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.NONE, root);

		flatten(flattener, treeModel);

		checkRoot(root, 4d, grandchild, child);
		checkLeaf(grandchild, 1d);
		checkLeaf(child, 1.5d);

		grandchild = createLeaf(null, 1d, new SimplePredicate("x", SimplePredicate.Operator.EQUAL, "a"));

		child = createBranch(null, 1.5d, new SimplePredicate("x", SimplePredicate.Operator.EQUAL, "b"), null, grandchild);

		root = createRoot(null, 4d, null, child);

		treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.NONE, root);

		flatten(flattener, treeModel);

		checkRoot(root, 4d, child);
		checkBranch(child, 1.5d, grandchild);
		checkLeaf(grandchild, 1d);

		grandchild = createLeaf(null, 1d, new SimplePredicate("x", SimplePredicate.Operator.GREATER_THAN, 1));

		child = createBranch(null, 1.5d, new SimplePredicate("x", SimplePredicate.Operator.LESS_OR_EQUAL, 2), null, grandchild);

		root = createRoot(null, 4d, null, child);

		treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.NONE, root);

		flatten(flattener, treeModel);

		checkRoot(root, 4d, child);
		checkBranch(child, 1.5d, grandchild);
		checkLeaf(grandchild, 1d);

		grandchild = new ComplexNode(null, True.INSTANCE)
			.addScores(new Score("y1", 1d), new Score("y2", 10d));

		child = new ComplexNode(null, new SimplePredicate("x", SimplePredicate.Operator.LESS_OR_EQUAL, 2))
			.addScores(new Score("y1", 1.5d), new Score("y2", 15d))
			.addNodes(grandchild);

		root = createRoot(null, 4d, null, child);

		treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.NONE, root);

		flatten(flattener, treeModel);

		checkRoot(root, 4d, child);
		checkLeaf(child, null);

		assertEquals(grandchild.getScores(), child.getScores());
	}

	@Test
	public void flattenClassification(){
		Node firstGrandchild = createLeaf(null, "no", new SimplePredicate("x", SimplePredicate.Operator.LESS_OR_EQUAL, 1));

		Node secondGrandchild = new ComplexNode("yes", True.INSTANCE)
			.addScoreDistributions(new ComplexScoreDistribution("no", 1), new ComplexScoreDistribution("yes", 3));

		Node firstChild = new ComplexNode("no", new SimplePredicate("x", SimplePredicate.Operator.LESS_OR_EQUAL, 2))
			.addScoreDistributions(new ComplexScoreDistribution("no", 3), new ComplexScoreDistribution("yes", 3))
			.addNodes(firstGrandchild, secondGrandchild);

		Node secondChild = createLeaf(null, "no", True.INSTANCE);

		Node root = createRoot(null, null, null, firstChild, secondChild);

		TreeModel treeModel = createClassificationTreeModel(TreeModel.MissingValueStrategy.NONE, root);

		flatten(new RegressionTreeModelFlattener(), treeModel);

		assertEquals(TreeModel.SplitCharacteristic.BINARY_SPLIT, treeModel.getSplitCharacteristic());

		flatten(new ClassificationTreeModelFlattener(), treeModel);

		checkTreeModel(treeModel, TreeModel.MissingValueStrategy.NONE, TreeModel.NoTrueChildStrategy.RETURN_NULL_PREDICTION);
		checkRoot(root, null, firstGrandchild, firstChild, secondChild);
		checkLeaf(firstGrandchild, "no");
		checkLeaf(firstChild, "yes");
		checkLeaf(secondChild, "no");

		assertEquals(secondGrandchild.getScoreDistributions(), firstChild.getScoreDistributions());
	}

	static
	private void flatten(TreeModelFlattener flattener, TreeModel treeModel){
		flattener.applyTo(treeModel);
	}
}