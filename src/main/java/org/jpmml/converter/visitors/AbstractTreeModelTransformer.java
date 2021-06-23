/*
 * Copyright (c) 2018 Villu Ruusmann
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

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.dmg.pmml.Array;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.HasFieldReference;
import org.dmg.pmml.HasValue;
import org.dmg.pmml.HasValueSet;
import org.dmg.pmml.PMMLObject;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.ScoreDistribution;
import org.dmg.pmml.SimplePredicate;
import org.dmg.pmml.SimpleSetPredicate;
import org.dmg.pmml.tree.Node;
import org.dmg.pmml.tree.TreeModel;
import org.jpmml.model.visitors.AbstractVisitor;

abstract
public class AbstractTreeModelTransformer extends AbstractVisitor {

	@Override
	public void pushParent(PMMLObject object){
		super.pushParent(object);

		if(object instanceof Node){
			enterNode((Node)object);
		} else

		if(object instanceof TreeModel){
			enterTreeModel((TreeModel)object);
		}
	}

	@Override
	public PMMLObject popParent(){
		PMMLObject object = super.popParent();

		if(object instanceof Node){
			exitNode((Node)object);
		} else

		if(object instanceof TreeModel){
			exitTreeModel((TreeModel)object);
		}

		return object;
	}

	public void enterNode(Node node){
	}

	public void exitNode(Node node){
	}

	public void enterTreeModel(TreeModel treeModel){
	}

	public void exitTreeModel(TreeModel treeModel){
	}

	protected Node getParentNode(){
		Deque<PMMLObject> parents = getParents();

		PMMLObject parent = parents.peekFirst();

		if(parent instanceof Node){
			return (Node)parent;
		} else

		if(parent instanceof TreeModel){
			return null;
		} else

		{
			throw new IllegalStateException();
		}
	}

	public Node getAncestorNode(java.util.function.Predicate<Node> predicate){
		Deque<PMMLObject> parents = getParents();

		Iterator<PMMLObject> parentIt = parents.iterator();

		while(parentIt.hasNext()){
			PMMLObject parent = parentIt.next();

			if(parent instanceof Node){
				Node node = (Node)parent;

				if(predicate.test(node)){
					return node;
				}
			} else

			if(parent instanceof TreeModel){
				return null;
			} else

			{
				throw new IllegalStateException();
			}
		}

		return null;
	}

	protected TreeModel getParentTreeModel(){
		Deque<PMMLObject> parents = getParents();

		Iterator<PMMLObject> parentIt = parents.iterator();

		while(parentIt.hasNext()){
			PMMLObject parent = parentIt.next();

			if(parent instanceof Node){
				continue;
			} else

			if(parent instanceof TreeModel){
				return (TreeModel)parent;
			} else

			{
				throw new IllegalStateException();
			}
		}

		throw new IllegalStateException();
	}

	static
	protected List<Node> swapChildren(Node node){
		List<Node> children = node.getNodes();

		if(children.size() != 2){
			throw new IllegalArgumentException();
		}

		Node firstChild = children.remove(0);

		children.add(1, firstChild);

		return children;
	}

	static
	protected void initScore(Node parentNode, Node node){
		Object score = node.getScore();

		if(parentNode.hasScore()){
			throw new IllegalArgumentException();
		}

		parentNode.setScore(score);
	}

	static
	protected void initScoreDistribution(Node parentNode, Node node){
		Object score = node.getScore();
		Number recordCount = node.getRecordCount();

		if(parentNode.hasScore()){
			throw new IllegalArgumentException();
		}

		parentNode.setScore(score);

		Number parentRecordCount = parentNode.getRecordCount();
		if(parentRecordCount != null){
			throw new IllegalArgumentException();
		}

		parentNode.setRecordCount(recordCount);

		if(parentNode.hasScoreDistributions()){
			throw new IllegalArgumentException();
		} // End if

		if(node.hasScoreDistributions()){
			List<ScoreDistribution> scoreDistributions = node.getScoreDistributions();

			List<ScoreDistribution> parentScoreDistributions = parentNode.getScoreDistributions();
			if(parentScoreDistributions.size() != 0){
				throw new IllegalArgumentException();
			}

			parentScoreDistributions.addAll(scoreDistributions);
		}
	}

	static
	protected void initDefaultChild(Node parentNode, Node node){
		Object defaultChild = node.getDefaultChild();

		Object parentDefaultChild = parentNode.getDefaultChild();
		if(parentDefaultChild != null){
			throw new IllegalArgumentException();
		}

		parentNode.setDefaultChild(defaultChild);
	}

	static
	protected void replaceChildWithGrandchildren(Node parentNode, Node node){
		List<Node> parentChildren = parentNode.getNodes();

		int index = parentChildren.indexOf(node);
		if(index < 0 || index != (parentChildren.size() - 1)){
			throw new IllegalArgumentException();
		}

		parentChildren.remove(index);

		if(node.hasNodes()){
			List<Node> children = node.getNodes();

			parentChildren.addAll(index, children);
		}
	}

	static
	protected boolean equalsNode(Object defaultChild, Node node){

		if(defaultChild instanceof Node){
			return Objects.equals(defaultChild, node);
		}

		return Objects.equals(defaultChild, node.getId());
	}

	static
	protected boolean hasFieldReference(Predicate predicate, FieldName name){

		if(predicate instanceof HasFieldReference){
			HasFieldReference<?> hasFieldReference = (HasFieldReference<?>)predicate;

			return Objects.equals(hasFieldReference.getField(), name);
		}

		return false;
	}

	static
	protected boolean hasValue(Predicate predicate, String value){

		if(predicate instanceof HasValue){
			HasValue<?> hasValue = (HasValue<?>)predicate;

			return Objects.equals(hasValue.getValue(), value);
		}

		return false;
	}

	static
	protected boolean hasOperator(Predicate predicate, SimplePredicate.Operator operator){

		if(predicate instanceof SimplePredicate){
			SimplePredicate simplePredicate = (SimplePredicate)predicate;

			return (simplePredicate.getOperator()).equals(operator);
		}

		return false;
	}

	static
	protected boolean hasBooleanOperator(Predicate predicate, SimpleSetPredicate.BooleanOperator booleanOperator){

		if(predicate instanceof SimpleSetPredicate){
			SimpleSetPredicate simpleSetPredicate = (SimpleSetPredicate)predicate;

			return (simpleSetPredicate.getBooleanOperator()).equals(booleanOperator);
		}

		return false;
	}

	static
	protected void checkFieldReference(Predicate left, Predicate right){
		checkFieldReference((HasFieldReference<?>)left, (HasFieldReference<?>)right);
	}

	static
	protected void checkFieldReference(HasFieldReference<?> left, HasFieldReference<?> right){
		FieldName leftName = left.getField();
		FieldName rightName = right.getField();

		if(!Objects.equals(leftName, rightName)){
			throw new IllegalArgumentException("Field names " + leftName + " and " + rightName + " are not the same");
		}
	}

	static
	protected void checkValue(Predicate left, Predicate right){
		checkValue((HasValue<?>)left, (HasValue<?>)right);
	}

	static
	protected void checkValue(HasValue<?> left, HasValue<?> right){
		Object leftValue = left.getValue();
		Object rightValue = right.getValue();

		if(!Objects.equals(leftValue, rightValue)){
			throw new IllegalArgumentException("Field values " + leftValue + " and " + rightValue + " are not the same");
		}
	}

	static
	protected void checkValueSet(Predicate left, Predicate right){
		checkValueSet((HasValueSet<?>)left, (HasValueSet<?>)right);
	}

	static
	protected void checkValueSet(HasValueSet<?> left, HasValueSet<?> right){
		Array leftArray = left.getArray();
		Array rightArray = right.getArray();

		if(!Objects.equals(leftArray.getValue(), rightArray.getValue())){
			throw new IllegalArgumentException("Field value sets " + leftArray.getValue() + " and " + rightArray.getValue() + " are not the same");
		}
	}
}