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

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.dmg.pmml.False;
import org.dmg.pmml.MiningFunction;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.tree.Node;
import org.dmg.pmml.tree.TreeModel;
import org.jpmml.model.ReflectionUtil;
import org.jpmml.model.UnsupportedAttributeException;

public class TreeModelPruner extends AbstractTreeModelTransformer {

	private MiningFunction miningFunction = null;


	@Override
	public void enterNode(Node node){
		Object defaultChild = node.getDefaultChild();

		if(node.hasNodes()){
			List<Node> children = node.getNodes();

			for(Iterator<Node> it = children.iterator(); it.hasNext(); ){
				Node child = it.next();

				if(defaultChild != null && equalsNode(defaultChild, child)){
					continue;
				}

				Predicate predicate = child.requirePredicate();

				if(predicate instanceof False){
					it.remove();
				}
			}
		}
	}

	@Override
	public void exitNode(Node node){
		Object defaultChild = node.getDefaultChild();
		Predicate predicate = node.requirePredicate();
		Number recordCount = node.getRecordCount();

		if(node.hasNodes()){
			List<Node> children = node.getNodes();

			if(children.size() == 1){
				Node child = children.get(0);

				Predicate childPredicate = child.requirePredicate();
				Number childRecordCount = child.getRecordCount();

				if(ReflectionUtil.equals(predicate, childPredicate) && Objects.equals(recordCount, childRecordCount)){

					if(defaultChild != null){
						node.setDefaultChild(null);
					} // End if

					if(this.miningFunction == MiningFunction.REGRESSION){
						initScore(node, child);
					} else

					if(this.miningFunction == MiningFunction.CLASSIFICATION){
						initScoreDistribution(node, child);
					}

					initDefaultChild(node, child);
					replaceChildWithGrandchildren(node, child);
				}
			}
		}
	}

	@Override
	public void enterTreeModel(TreeModel treeModel){
		super.enterTreeModel(treeModel);

		MiningFunction miningFunction = treeModel.requireMiningFunction();
		switch(miningFunction){
			case REGRESSION:
			case CLASSIFICATION:
				break;
			default:
				throw new UnsupportedAttributeException(treeModel, miningFunction);
		}

		this.miningFunction = miningFunction;
	}

	@Override
	public void exitTreeModel(TreeModel treeModel){
		super.exitTreeModel(treeModel);

		this.miningFunction = null;
	}
}