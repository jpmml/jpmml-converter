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

import org.dmg.pmml.ComplexScoreDistribution;
import org.dmg.pmml.False;
import org.dmg.pmml.Score;
import org.dmg.pmml.SimplePredicate;
import org.dmg.pmml.True;
import org.dmg.pmml.tree.ComplexNode;
import org.dmg.pmml.tree.Node;
import org.dmg.pmml.tree.TreeModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TreeModelPrunerTest extends TreeModelTransformerTest {

	@Test
	public void pruneFalse(){
		Node firstChild = createLeaf(1, null, False.INSTANCE);
		Node secondChild = createLeaf(2, null, True.INSTANCE);

		Node parent = createBranch(0, null, new SimplePredicate("x", SimplePredicate.Operator.IS_NOT_MISSING, null), firstChild.getId(), firstChild, secondChild);

		Node root = createRoot(null, null, null, parent);

		TreeModel treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.DEFAULT_CHILD, root);

		TreeModelPruner pruner = new RegressionTreeModelPruner();

		prune(pruner, treeModel);

		assertEquals(Arrays.asList(firstChild, secondChild), parent.getNodes());

		parent.setDefaultChild(secondChild);

		prune(pruner, treeModel);

		assertEquals(Collections.singletonList(secondChild), parent.getNodes());

		secondChild.setPredicate(False.INSTANCE);

		prune(pruner, treeModel);

		assertEquals(Collections.singletonList(secondChild), parent.getNodes());

		parent.setDefaultChild(null);

		prune(pruner, treeModel);

		assertEquals(Collections.emptyList(), parent.getNodes());
	}

	@Test
	public void pruneNoOp(){
		SimplePredicate parentPredicate = new SimplePredicate("x", SimplePredicate.Operator.NOT_EQUAL, 0);
		SimplePredicate childPredicate = new SimplePredicate("x", SimplePredicate.Operator.NOT_EQUAL, 1);

		Node firstGrandchild = createLeaf(null, null, new SimplePredicate("x", SimplePredicate.Operator.EQUAL, 1));
		Node secondGrandchild = createLeaf(null, null, new SimplePredicate("x", SimplePredicate.Operator.EQUAL, 2));

		Node child = createBranch(null, null, childPredicate, null, firstGrandchild, secondGrandchild);

		Node parent = createBranch(null, null, parentPredicate, null, child);

		Node root = createRoot(null, null, null, parent);

		TreeModel treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.NONE, root);

		TreeModelPruner pruner = new RegressionTreeModelPruner();

		prune(pruner, treeModel);

		assertEquals(Collections.singletonList(child), parent.getNodes());
		assertEquals(Arrays.asList(firstGrandchild, secondGrandchild), child.getNodes());

		childPredicate.setValue(0);

		prune(pruner, treeModel);

		assertEquals(Arrays.asList(firstGrandchild, secondGrandchild), parent.getNodes());
	}

	@Test
	public void pruneSamePredicate(){
		SimplePredicate parentPredicate = new SimplePredicate("x", SimplePredicate.Operator.LESS_OR_EQUAL, 0);
		SimplePredicate childPredicate = new SimplePredicate("x", SimplePredicate.Operator.LESS_OR_EQUAL, 0);

		Node firstGrandchild = createLeaf(null, 1d, new SimplePredicate("y", SimplePredicate.Operator.LESS_OR_EQUAL, 0));
		Node secondGrandchild = createLeaf(null, 2d, new SimplePredicate("y", SimplePredicate.Operator.GREATER_THAN, 0));

		Node child = new ComplexNode(2d, childPredicate)
			.addScores(new Score("y1", 2d), new Score("y2", 20d))
			.addNodes(firstGrandchild, secondGrandchild);

		Node parent = new ComplexNode(1d, parentPredicate)
			.addScores(new Score("y1", 1d), new Score("y2", 10d))
			.addNodes(child);

		Node root = createRoot(null, null, null, parent);

		TreeModel treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.NONE, root);

		TreeModelPruner classificationPruner = new ClassificationTreeModelPruner();

		prune(classificationPruner, treeModel);

		assertEquals(Collections.singletonList(child), parent.getNodes());

		TreeModelPruner regressionPruner = new RegressionTreeModelPruner();

		prune(regressionPruner, treeModel);

		checkBranch(parent, 2d, firstGrandchild, secondGrandchild);

		assertEquals(child.getScores(), parent.getScores());

		firstGrandchild = createLeaf(null, "no", new SimplePredicate("y", SimplePredicate.Operator.LESS_OR_EQUAL, 0));
		secondGrandchild = createLeaf(null, "yes", new SimplePredicate("y", SimplePredicate.Operator.GREATER_THAN, 0));

		child = new ComplexNode("yes", childPredicate)
			.setRecordCount(9)
			.addScoreDistributions(new ComplexScoreDistribution("no", 4), new ComplexScoreDistribution("yes", 6))
			.addNodes(firstGrandchild, secondGrandchild);

		parent = new ComplexNode("no", parentPredicate)
			.setRecordCount(10)
			.addScoreDistributions(new ComplexScoreDistribution("no", 6), new ComplexScoreDistribution("yes", 4))
			.addNodes(child);

		root = createRoot(null, null, null, parent);

		treeModel = createClassificationTreeModel(TreeModel.MissingValueStrategy.NONE, root);

		prune(regressionPruner, treeModel);

		assertEquals(Collections.singletonList(child), parent.getNodes());

		prune(classificationPruner, treeModel);

		assertEquals(Collections.singletonList(child), parent.getNodes());

		child.setRecordCount(10);

		prune(classificationPruner, treeModel);

		checkBranch(parent, "yes", firstGrandchild, secondGrandchild);

		assertEquals(10, parent.getRecordCount());
		assertEquals(child.getScoreDistributions(), parent.getScoreDistributions());

		firstGrandchild = createLeaf(3, 1d, new SimplePredicate("y", SimplePredicate.Operator.LESS_OR_EQUAL, 0));
		secondGrandchild = createLeaf(4, 2d, new SimplePredicate("y", SimplePredicate.Operator.GREATER_THAN, 0));

		child = createBranch(2, 2d, childPredicate, secondGrandchild.getId(), firstGrandchild, secondGrandchild);

		parent = createBranch(1, 1d, parentPredicate, child.getId(), child);

		root = createRoot(null, null, null, parent);

		treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.DEFAULT_CHILD, root);

		prune(regressionPruner, treeModel);

		assertEquals(secondGrandchild.getId(), parent.getDefaultChild());
		assertEquals(2d, parent.getScore());
		assertEquals(Arrays.asList(firstGrandchild, secondGrandchild), parent.getNodes());
	}

	static
	private void prune(TreeModelPruner pruner, TreeModel treeModel){
		pruner.applyTo(treeModel);
	}
}