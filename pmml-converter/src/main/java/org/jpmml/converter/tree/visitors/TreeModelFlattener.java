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

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.dmg.pmml.MiningFunction;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.SimplePredicate;
import org.dmg.pmml.True;
import org.dmg.pmml.tree.Node;
import org.dmg.pmml.tree.TreeModel;

abstract
public class TreeModelFlattener extends TreeModelTransformer {

	public TreeModelFlattener(){
	}

	public TreeModelFlattener(MiningFunction miningFunction){
		super(miningFunction);
	}

	abstract
	protected void mergeNode(Node parentNode, Node node);

	@Override
	public void enterNode(Node node){

		if(node.hasNodes()){
			List<Node> children = node.getNodes();

			children:
			while(true){
				ListIterator<Node> childIt = children.listIterator();

				grandchildren:
				while(childIt.hasNext()){
					Node child = childIt.next();

					Iterator<Node> grandchildIt = getChildren(child);
					if(grandchildIt == null){
						continue grandchildren;
					}

					childIt.remove();

					while(grandchildIt.hasNext()){
						Node grandchild = grandchildIt.next();

						grandchildIt.remove();

						childIt.add(grandchild);
					}

					childIt.add(child);

					continue children;
				}

				break;
			}
		}
	}

	@Override
	public void exitNode(Node node){
		Predicate predicate = node.requirePredicate();

		if(predicate instanceof True){
			Node parentNode = getParentNode();

			if(parentNode == null){
				return;
			}

			List<Node> parentChildren = parentNode.getNodes();
			if(parentChildren.size() != 1){
				return;
			}

			mergeNode(parentNode, node);
		}
	}

	@Override
	public void enterTreeModel(TreeModel treeModel){
		super.enterTreeModel(treeModel);

		treeModel.setSplitCharacteristic(TreeModel.SplitCharacteristic.MULTI_SPLIT);
	}

	@Override
	public void exitTreeModel(TreeModel treeModel){
		super.exitTreeModel(treeModel);
	}

	static
	private Iterator<Node> getChildren(Node node){
		Predicate predicate = node.requirePredicate();

		if(!(predicate instanceof SimplePredicate)){
			return null;
		}

		SimplePredicate simplePredicate = (SimplePredicate)predicate;

		SimplePredicate.Operator operator = simplePredicate.requireOperator();
		switch(operator){
			// Upper bounds are nested, so the conditions of grandchildren imply the condition of the child
			case LESS_THAN:
			case LESS_OR_EQUAL:
				break;
			default:
				return null;
		}

		if(node.hasNodes()){
			List<Node> children = node.getNodes();

			int endPos = 0;

			for(Node child : children){
				Predicate childPredicate = child.requirePredicate();

				if(!hasFieldReference(childPredicate, simplePredicate.requireField()) || !hasOperator(childPredicate, operator)){
					break;
				}

				endPos++;
			}

			if(endPos > 0){
				return (children.subList(0, endPos)).iterator();
			}

			return null;
		}

		return null;
	}
}