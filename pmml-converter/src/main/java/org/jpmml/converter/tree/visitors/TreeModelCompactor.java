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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.dmg.pmml.Array;
import org.dmg.pmml.CompoundPredicate;
import org.dmg.pmml.False;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.SimplePredicate;
import org.dmg.pmml.SimpleSetPredicate;
import org.dmg.pmml.True;
import org.dmg.pmml.tree.Node;
import org.dmg.pmml.tree.TreeModel;
import org.jpmml.model.InvalidElementException;
import org.jpmml.model.UnsupportedAttributeException;
import org.jpmml.model.UnsupportedElementException;

abstract
public class TreeModelCompactor extends TreeModelTransformer {

	abstract
	protected void mergeNode(Node parentNode, Node node);

	@Override
	public void enterNode(Node node){
		Object defaultChild = node.getDefaultChild();

		if(node.hasNodes()){
			List<Node> children = node.getNodes();

			if(children.size() != 2){
				throw new UnsupportedElementException(node);
			}

			Node firstChild = children.get(0);
			Node secondChild = children.get(1);

			SplitType splitType;

			try {
				splitType = getSplitType(firstChild.requirePredicate(), secondChild.requirePredicate());
			} catch(IllegalArgumentException iae){
				throw new InvalidElementException(node)
					.initCause(iae);
			}

			TreeModel treeModel = getParentTreeModel();

			TreeModel.MissingValueStrategy missingValueStrategy = treeModel.getMissingValueStrategy();
			switch(missingValueStrategy){
				case DEFAULT_CHILD:
					{
						if(defaultChild == null){
							throw new UnsupportedElementException(node);
						} // End if

						if(equalsNode(defaultChild, firstChild)){
							children = swapChildren(node);
						} else

						if(equalsNode(defaultChild, secondChild)){
							// Ignored
						} else

						{
							throw new UnsupportedElementException(node);
						}

						node.setDefaultChild(null);
					}
					break;
				case NONE:
					{
						if(defaultChild != null){
							throw new UnsupportedElementException(node);
						}

						switch(splitType){
							case CONTINUOUS:
							case CATEGORICAL:
								break;
							case BINARY:
								{
									SimplePredicate firstPredicate = firstChild.requirePredicate(SimplePredicate.class);

									if(hasOperator(firstPredicate, SimplePredicate.Operator.NOT_EQUAL)){
										children = swapChildren(node);
									}
								}
								break;
							default:
								throw new UnsupportedElementException(node);
						}
					}
					break;
				default:
					throw new UnsupportedAttributeException(treeModel, missingValueStrategy);
			}

			firstChild = children.get(0);
			secondChild = children.get(1);

			if(firstChild.requirePredicate() instanceof True){
				throw new UnsupportedElementException(node);
			}

			secondChild.setPredicate(True.INSTANCE);
		} else

		{
			if(defaultChild != null){
				throw new UnsupportedElementException(node);
			} // End if

			if(!node.hasScore() && !node.hasPayloads()){
				throw new UnsupportedElementException(node);
			}
		}

		node.setId(null);
	}

	@Override
	public void exitNode(Node node){
		Predicate predicate = node.requirePredicate();

		if(predicate instanceof True){
			Node parentNode = getParentNode();

			if(parentNode == null){
				return;
			}

			mergeNode(parentNode, node);
		}
	}

	@Override
	public void enterTreeModel(TreeModel treeModel){
		super.enterTreeModel(treeModel);

		TreeModel.MissingValueStrategy missingValueStrategy = treeModel.getMissingValueStrategy();
		switch(missingValueStrategy){
			case NONE:
			case DEFAULT_CHILD:
				break;
			default:
				throw new UnsupportedAttributeException(treeModel, missingValueStrategy);
		}

		TreeModel.NoTrueChildStrategy noTrueChildStrategy = treeModel.getNoTrueChildStrategy();
		if(noTrueChildStrategy != TreeModel.NoTrueChildStrategy.RETURN_NULL_PREDICTION){
			throw new UnsupportedAttributeException(treeModel, noTrueChildStrategy);
		}

		TreeModel.SplitCharacteristic splitCharacteristic = treeModel.getSplitCharacteristic();
		if(splitCharacteristic != TreeModel.SplitCharacteristic.BINARY_SPLIT){
			throw new UnsupportedAttributeException(treeModel, splitCharacteristic);
		}
	}

	@Override
	public void exitTreeModel(TreeModel treeModel){
		TreeModel.MissingValueStrategy missingValueStrategy = treeModel.getMissingValueStrategy();
		switch(missingValueStrategy){
			case NONE:
				// Missing values are not allowed
				treeModel.setMissingValueStrategy(TreeModel.MissingValueStrategy.NULL_PREDICTION);
				break;
			case DEFAULT_CHILD:
				// Missing values are allowed
				treeModel.setMissingValueStrategy(TreeModel.MissingValueStrategy.NONE);
				break;
			default:
				throw new UnsupportedAttributeException(treeModel, missingValueStrategy);
		}

		treeModel.setSplitCharacteristic(TreeModel.SplitCharacteristic.MULTI_SPLIT);

		super.exitTreeModel(treeModel);
	}

	protected SimplePredicate.Operator getSplit(){
		return SimplePredicate.Operator.LESS_OR_EQUAL;
	}

	protected SimplePredicate.Operator getLeftSplit(){
		return getSplit();
	}

	protected SimplePredicate.Operator getRightSplit(){
		return negate(getSplit());
	}

	private SplitType getSplitType(Predicate left, Predicate right){

		if((left instanceof SimplePredicate) && (right instanceof SimplePredicate)){
			SimplePredicate leftSimplePredicate = (SimplePredicate)left;
			SimplePredicate rightSimplePredicate = (SimplePredicate)right;

			checkFieldReference(left, right);

			SimplePredicate.Operator leftOperator = leftSimplePredicate.requireOperator();
			SimplePredicate.Operator rightOperator = rightSimplePredicate.requireOperator();

			if(leftOperator == getLeftSplit() && rightOperator == getRightSplit()){
				checkValue(left, right);

				return SplitType.CONTINUOUS;
			}

			switch(leftOperator){
				case EQUAL:
				case NOT_EQUAL:
					{
						if(rightOperator == negate(leftOperator)){
							checkValue(left, right);

							return SplitType.BINARY;
						}
					}
					break;
				case IS_NOT_MISSING:
					{
						if(rightOperator == SimplePredicate.Operator.IS_MISSING){
							return SplitType.MISSING;
						}
					}
					break;
				default:
					break;
			}
		}

		checkCategoricalSplit(left, right);

		return SplitType.CATEGORICAL;
	}

	static
	private void checkCategoricalSplit(Predicate left, Predicate right){
		Categories leftCategories = Categories.parse(left, false);
		Categories rightCategories = Categories.parse(right, true);

		String leftFieldName = leftCategories.getFieldName();
		String rightFieldName = rightCategories.getFieldName();

		if(leftFieldName != null && rightFieldName != null && !Objects.equals(leftFieldName, rightFieldName)){
			throw new IllegalArgumentException("Field names " + leftFieldName + " and " + rightFieldName + " are not the same");
		} // End if

		if(leftCategories.isMissing() && rightCategories.isMissing()){
			throw new IllegalArgumentException("Missing values are routed to both branches");
		}

		Set<?> leftValues = leftCategories.getValues();
		Set<?> rightValues = rightCategories.getValues();

		if(rightValues != null && !Collections.disjoint(leftValues, rightValues)){
			throw new IllegalArgumentException("Field value sets " + leftValues + " and " + rightValues + " are not disjoint");
		}
	}

	static
	private SimplePredicate.Operator negate(SimplePredicate.Operator operator){

		switch(operator){
			case EQUAL:
				return SimplePredicate.Operator.NOT_EQUAL;
			case NOT_EQUAL:
				return SimplePredicate.Operator.EQUAL;
			case LESS_THAN:
				return SimplePredicate.Operator.GREATER_OR_EQUAL;
			case LESS_OR_EQUAL:
				return SimplePredicate.Operator.GREATER_THAN;
			case GREATER_OR_EQUAL:
				return SimplePredicate.Operator.LESS_THAN;
			case GREATER_THAN:
				return SimplePredicate.Operator.LESS_OR_EQUAL;
			case IS_MISSING:
				return SimplePredicate.Operator.IS_NOT_MISSING;
			case IS_NOT_MISSING:
				return SimplePredicate.Operator.IS_MISSING;
			default:
				throw new IllegalArgumentException(String.valueOf(operator));
		}
	}

	static
	private enum SplitType {
		CONTINUOUS,
		BINARY,
		MISSING,
		CATEGORICAL,
		;
	}

	static
	private class Categories {

		private String fieldName = null;

		private Set<Object> values = null;

		private boolean missing = false;


		private Categories(String fieldName, Set<Object> values, boolean missing){
			this.fieldName = fieldName;
			this.values = values;
			this.missing = missing;
		}

		public String getFieldName(){
			return this.fieldName;
		}

		public Set<Object> getValues(){
			return this.values;
		}

		public boolean isEmpty(){

			if(this.values != null){
				return this.values.isEmpty();
			}

			return false;
		}

		public boolean isMissing(){
			return this.missing;
		}

		static
		public Categories parse(Predicate predicate, boolean right){

			if(predicate instanceof SimplePredicate){
				SimplePredicate simplePredicate = (SimplePredicate)predicate;

				SimplePredicate.Operator operator = simplePredicate.requireOperator();
				switch(operator){
					case EQUAL:
						return new Categories(simplePredicate.requireField(), Collections.singleton(simplePredicate.requireValue()), false);
					case IS_MISSING:
						return new Categories(simplePredicate.requireField(), Collections.emptySet(), true);
					default:
						throw new UnsupportedElementException(predicate);
				}
			} else

			if(predicate instanceof SimpleSetPredicate){
				SimpleSetPredicate simpleSetPredicate = (SimpleSetPredicate)predicate;

				SimpleSetPredicate.BooleanOperator booleanOperator = simpleSetPredicate.requireBooleanOperator();
				switch(booleanOperator){
					case IS_IN:
						return new Categories(simpleSetPredicate.requireField(), parseValues(simpleSetPredicate), false);
					default:
						throw new UnsupportedElementException(predicate);
				}
			} else

			if(predicate instanceof CompoundPredicate){
				CompoundPredicate compoundPredicate = (CompoundPredicate)predicate;

				CompoundPredicate.BooleanOperator booleanOperator = compoundPredicate.requireBooleanOperator();
				switch(booleanOperator){
					case SURROGATE:
						{
							List<Predicate> predicates = compoundPredicate.getPredicates();

							if(predicates.size() != 2){
								throw new UnsupportedElementException(predicate);
							}

							Categories valueCategories = parse(predicates.get(0), false);
							Categories missingCategories = parse(predicates.get(1), false);

							if(valueCategories.isMissing() || valueCategories.isEmpty() || !missingCategories.isMissing()){
								throw new UnsupportedElementException(predicate);
							} // End if

							if(!Objects.equals(valueCategories.getFieldName(), missingCategories.getFieldName())){
								throw new UnsupportedElementException(predicate);
							}

							return new Categories(valueCategories.getFieldName(), valueCategories.getValues(), true);
						}
					default:
						throw new UnsupportedElementException(predicate);
				}
			} else

			if(predicate instanceof False){
				return new Categories(null, Collections.emptySet(), false);
			} else

			if((predicate instanceof True) && right){
				return new Categories(null, null, false);
			} else

			{
				throw new UnsupportedElementException(predicate);
			}
		}

		static
		private Set<Object> parseValues(SimpleSetPredicate simpleSetPredicate){
			Array array = simpleSetPredicate.requireArray();

			Object value = array.getValue();

			if(value instanceof Collection){
				Collection<?> values = (Collection<?>)value;

				return new LinkedHashSet<>(values);
			} else

			{
				throw new UnsupportedElementException(array);
			}
		}
	}
}