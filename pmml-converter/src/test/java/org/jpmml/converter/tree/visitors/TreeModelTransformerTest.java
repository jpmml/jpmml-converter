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
package org.jpmml.converter.tree.visitors;

import java.util.Arrays;

import org.dmg.pmml.MiningFunction;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.True;
import org.dmg.pmml.tree.BranchNode;
import org.dmg.pmml.tree.LeafNode;
import org.dmg.pmml.tree.Node;
import org.dmg.pmml.tree.TreeModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

abstract
public class TreeModelTransformerTest {

	static
	protected TreeModel createRegressionTreeModel(TreeModel.MissingValueStrategy missingValueStrategy, Node root){
		return createTreeModel(MiningFunction.REGRESSION, missingValueStrategy, root);
	}

	static
	protected TreeModel createClassificationTreeModel(TreeModel.MissingValueStrategy missingValueStrategy, Node root){
		return createTreeModel(MiningFunction.CLASSIFICATION, missingValueStrategy, root);
	}

	static
	protected TreeModel createTreeModel(MiningFunction miningFunction, TreeModel.MissingValueStrategy missingValueStrategy, Node root){
		TreeModel treeModel = new TreeModel(miningFunction, new MiningSchema(), root)
			.setMissingValueStrategy(missingValueStrategy)
			.setSplitCharacteristic(TreeModel.SplitCharacteristic.BINARY_SPLIT);

		return treeModel;
	}

	static
	protected Node createRoot(Object id, Object score, Object defaultChild, Node... children){
		return createBranch(id, score, True.INSTANCE, defaultChild, children);
	}

	static
	protected Node createBranch(Object id, Object score, Predicate predicate, Object defaultChild, Node... children){
		return new BranchNode(score, predicate)
			.setId(id)
			.setDefaultChild(defaultChild)
			.addNodes(children);
	}

	static
	protected Node createLeaf(Object id, Object score, Predicate predicate){
		return new LeafNode(score, predicate)
			.setId(id);
	}

	static
	protected void checkTreeModel(TreeModel treeModel, TreeModel.MissingValueStrategy missingValueStrategy, TreeModel.NoTrueChildStrategy noTrueChildStrategy){
		assertEquals(missingValueStrategy, treeModel.getMissingValueStrategy());
		assertEquals(noTrueChildStrategy, treeModel.getNoTrueChildStrategy());
		assertEquals(TreeModel.SplitCharacteristic.MULTI_SPLIT, treeModel.getSplitCharacteristic());
	}

	static
	protected void checkRoot(Node root, Object score, Node... children){
		assertEquals(True.INSTANCE, root.getPredicate());

		checkBranch(root, score, children);
	}

	static
	protected void checkBranch(Node node, Object score, Node... children){
		assertNull(node.getId());
		assertNull(node.getDefaultChild());
		assertEquals(score, node.getScore());
		assertEquals(Arrays.asList(children), node.getNodes());
	}

	static
	protected void checkLeaf(Node node, Object score){
		assertNull(node.getId());
		assertEquals(score, node.getScore());
		assertFalse(node.hasNodes());
	}
}