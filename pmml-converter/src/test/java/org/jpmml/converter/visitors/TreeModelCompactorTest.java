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

import java.util.Arrays;

import org.dmg.pmml.CompoundPredicate;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.Score;
import org.dmg.pmml.SimplePredicate;
import org.dmg.pmml.SimpleSetPredicate;
import org.dmg.pmml.True;
import org.dmg.pmml.tree.ComplexNode;
import org.dmg.pmml.tree.CountingBranchNode;
import org.dmg.pmml.tree.CountingLeafNode;
import org.dmg.pmml.tree.Node;
import org.dmg.pmml.tree.TreeModel;
import org.jpmml.converter.PMMLUtil;
import org.jpmml.model.UnsupportedElementException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TreeModelCompactorTest extends TreeModelTransformerTest {

	@Test
	public void compactContinuous(){
		Node firstChild = new CountingLeafNode(1d, new SimplePredicate("x", SimplePredicate.Operator.LESS_OR_EQUAL, 0))
			.setId(1)
			.setRecordCount(10);

		Node secondChild = createLeaf(2, 2d, new SimplePredicate("x", SimplePredicate.Operator.GREATER_THAN, 0));

		Node root = createRoot(0, 1.5d, 2, firstChild, secondChild);

		TreeModel treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.DEFAULT_CHILD, root);

		TreeModelCompactor compactor = new RegressionTreeModelCompactor();

		compact(compactor, treeModel);

		checkTreeModel(treeModel, TreeModel.MissingValueStrategy.NONE, TreeModel.NoTrueChildStrategy.RETURN_LAST_PREDICTION);
		checkRoot(root, 2d, firstChild);
		checkLeaf(firstChild, 1d);

		root = createRoot(0, 1.5d, 2, createLeaf(1, 1d, new SimplePredicate("x", SimplePredicate.Operator.LESS_OR_EQUAL, 0)), createLeaf(2, 2d, new SimplePredicate("x", SimplePredicate.Operator.GREATER_THAN, 1)));

		assertEquals(10, firstChild.getRecordCount());

		treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.DEFAULT_CHILD, root);

		compactThrows(compactor, treeModel, IllegalArgumentException.class);

		firstChild = createLeaf(1, 1d, new SimplePredicate("x", SimplePredicate.Operator.LESS_THAN, 0));
		secondChild = createLeaf(2, 2d, new SimplePredicate("x", SimplePredicate.Operator.GREATER_OR_EQUAL, 0));

		root = createRoot(0, 1.5d, 1, firstChild, secondChild);

		treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.DEFAULT_CHILD, root);

		TreeModelCompactor lessThanCompactor = new RegressionTreeModelCompactor(){

			@Override
			protected SimplePredicate.Operator getSplit(){
				return SimplePredicate.Operator.LESS_THAN;
			}
		};

		compact(lessThanCompactor, treeModel);

		checkRoot(root, 1d, secondChild);
		checkLeaf(secondChild, 2d);

		firstChild = new ComplexNode(null, new SimplePredicate("x", SimplePredicate.Operator.LESS_OR_EQUAL, 0))
			.setId(1)
			.addScores(new Score("y1", 1d), new Score("y2", 10d));

		secondChild = new ComplexNode(null, new SimplePredicate("x", SimplePredicate.Operator.GREATER_THAN, 0))
			.setId(2)
			.addScores(new Score("y1", 2d), new Score("y2", 20d));

		root = new ComplexNode(null, True.INSTANCE)
			.setId(0)
			.setDefaultChild(2)
			.addScores(new Score("y1", 1.5d), new Score("y2", 15d))
			.addNodes(firstChild, secondChild);

		treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.DEFAULT_CHILD, root);

		compact(compactor, treeModel);

		checkRoot(root, null, firstChild);
		checkLeaf(firstChild, null);

		assertEquals(secondChild.getScores(), root.getScores());

		Node firstGrandchild = createLeaf(3, "no", new SimplePredicate("x", SimplePredicate.Operator.LESS_OR_EQUAL, 1));
		Node secondGrandchild = createLeaf(4, "yes", new SimplePredicate("x", SimplePredicate.Operator.GREATER_THAN, 1));

		firstChild = createLeaf(1, "no", new SimplePredicate("x", SimplePredicate.Operator.LESS_OR_EQUAL, 0));
		secondChild = createBranch(2, null, new SimplePredicate("x", SimplePredicate.Operator.GREATER_THAN, 0), 4, firstGrandchild, secondGrandchild);

		root = new CountingBranchNode(null, True.INSTANCE)
			.setId(0)
			.setRecordCount(10)
			.setDefaultChild(2)
			.addNodes(firstChild, secondChild);

		treeModel = createClassificationTreeModel(TreeModel.MissingValueStrategy.DEFAULT_CHILD, root);

		compact(compactor, treeModel);

		assertEquals(TreeModel.SplitCharacteristic.BINARY_SPLIT, treeModel.getSplitCharacteristic());

		assertEquals(0, root.getId());

		compact(new ClassificationTreeModelCompactor(), treeModel);

		checkTreeModel(treeModel, TreeModel.MissingValueStrategy.NONE, TreeModel.NoTrueChildStrategy.RETURN_NULL_PREDICTION);
		checkRoot(root, null, firstChild, firstGrandchild, secondGrandchild);
		checkLeaf(firstChild, "no");
		checkLeaf(firstGrandchild, "no");
		checkLeaf(secondGrandchild, "yes");

		assertEquals(10, root.getRecordCount());

		assertEquals(True.INSTANCE, secondGrandchild.getPredicate());
	}

	@Test
	public void compactBinary(){
		Node firstChild = createLeaf(1, 1d, new SimplePredicate("x", SimplePredicate.Operator.NOT_EQUAL, "a"));
		Node secondChild = createLeaf(2, 2d, new SimplePredicate("x", SimplePredicate.Operator.EQUAL, "a"));

		Node root = createRoot(0, 1.5d, null, firstChild, secondChild);

		TreeModel treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.NONE, root);

		TreeModelCompactor compactor = new RegressionTreeModelCompactor();

		compact(compactor, treeModel);

		checkTreeModel(treeModel, TreeModel.MissingValueStrategy.NULL_PREDICTION, TreeModel.NoTrueChildStrategy.RETURN_LAST_PREDICTION);
		checkRoot(root, 1d, secondChild);
		checkLeaf(secondChild, 2d);

		firstChild = createLeaf(null, 1d, new SimplePredicate("x", SimplePredicate.Operator.EQUAL, "a"));
		secondChild = createLeaf(null, 2d, new SimplePredicate("x", SimplePredicate.Operator.NOT_EQUAL, "a"));

		root = createRoot(0, 1.5d, null, firstChild, secondChild);

		treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.NONE, root);

		compact(compactor, treeModel);

		checkRoot(root, 2d, firstChild);
		checkLeaf(firstChild, 1d);
	}

	@Test
	public void compactMissing(){
		Node firstChild = createLeaf(1, 1d, new SimplePredicate("x", SimplePredicate.Operator.IS_NOT_MISSING, null));
		Node secondChild = createLeaf(2, 2d, new SimplePredicate("x", SimplePredicate.Operator.IS_MISSING, null));

		Node root = createRoot(0, 1.5d, 2, firstChild, secondChild);

		TreeModel treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.DEFAULT_CHILD, root);

		compact(new RegressionTreeModelCompactor(), treeModel);

		checkRoot(root, 2d, firstChild);
		checkLeaf(firstChild, 1d);
	}

	@Test
	public void compactCategorical(){
		Predicate firstPredicate = new SimplePredicate("x", SimplePredicate.Operator.EQUAL, "a");
		Predicate secondPredicate = new SimpleSetPredicate("x", SimpleSetPredicate.BooleanOperator.IS_IN, PMMLUtil.createStringArray(Arrays.asList("b", "c")));

		Node firstChild = createLeaf(1, 1d, firstPredicate);

		Node root = createRoot(0, 1.5d, 2, firstChild, createLeaf(2, 2d, secondPredicate));

		TreeModel treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.DEFAULT_CHILD, root);

		TreeModelCompactor compactor = new RegressionTreeModelCompactor();

		compact(compactor, treeModel);

		checkRoot(root, 2d, firstChild);
		checkLeaf(firstChild, 1d);

		Predicate surrogatePredicate = new CompoundPredicate(CompoundPredicate.BooleanOperator.SURROGATE, null)
			.addPredicates(secondPredicate, new SimplePredicate("x", SimplePredicate.Operator.IS_MISSING, null));

		firstChild = createLeaf(1, 1d, surrogatePredicate);

		root = createRoot(0, 1.5d, 2, firstChild, createLeaf(2, 2d, True.INSTANCE));

		treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.DEFAULT_CHILD, root);

		compact(compactor, treeModel);

		checkRoot(root, 2d, firstChild);
		checkLeaf(firstChild, 1d);

		root = createRoot(0, 1.5d, 1, createLeaf(1, 1d, surrogatePredicate), createLeaf(2, 2d, True.INSTANCE));

		treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.DEFAULT_CHILD, root);

		compactThrows(compactor, treeModel, UnsupportedElementException.class);

		Predicate overlappingPredicate = new SimpleSetPredicate("x", SimpleSetPredicate.BooleanOperator.IS_IN, PMMLUtil.createStringArray(Arrays.asList("a", "b")));

		root = createRoot(0, 1.5d, 2, createLeaf(1, 1d, firstPredicate), createLeaf(2, 2d, overlappingPredicate));

		treeModel = createRegressionTreeModel(TreeModel.MissingValueStrategy.DEFAULT_CHILD, root);

		compactThrows(compactor, treeModel, IllegalArgumentException.class);
	}

	static
	private void compact(TreeModelCompactor compactor, TreeModel treeModel){
		compactor.applyTo(treeModel);
	}

	static
	private <E extends Exception> E compactThrows(TreeModelCompactor compactor, TreeModel treeModel, Class<E> clazz){
		return assertThrows(clazz, () -> compact(compactor, treeModel));
	}
}