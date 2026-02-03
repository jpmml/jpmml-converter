/*
 * Copyright (c) 2016 Villu Ruusmann
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

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import org.dmg.pmml.Apply;
import org.dmg.pmml.Constant;
import org.dmg.pmml.Expression;
import org.dmg.pmml.PMMLFunctions;
import org.dmg.pmml.VisitorAction;
import org.jpmml.model.InvalidElementException;
import org.jpmml.model.visitors.AbstractVisitor;

public class ExpressionCompactor extends AbstractVisitor {

	@Override
	public VisitorAction visit(Apply apply){
		String function = apply.requireFunction();

		switch(function){
			case PMMLFunctions.EQUAL:
			case PMMLFunctions.NOTEQUAL:
				simplifyComparisonExpression(apply);
				break;
			case PMMLFunctions.AND:
			case PMMLFunctions.OR:
				inlineNestedExpressions(apply);
				break;
			case PMMLFunctions.NOT:
				negateExpression(apply);
				break;
			case PMMLFunctions.CONCAT:
				inlineNestedExpressions(apply);
				break;
			default:
				break;
		}

		return super.visit(apply);
	}

	static
	private void simplifyComparisonExpression(Apply apply){
		String function = apply.requireFunction();
		List<Expression> expressions = apply.getExpressions();

		if(expressions.size() != 2){
			throw new InvalidElementException(apply);
		}

		ListIterator<Expression> expressionIt = expressions.listIterator();
		while(expressionIt.hasNext()){
			Expression expression = expressionIt.next();

			if(isMissingConstant(expression)){
				expressionIt.remove();
			}
		}

		if(expressions.size() == 0){
			throw new InvalidElementException(apply);
		} else

		if(expressions.size() == 1){

			switch(function){
				case PMMLFunctions.EQUAL:
					apply.setFunction(PMMLFunctions.ISMISSING);
					break;
				case PMMLFunctions.NOTEQUAL:
					apply.setFunction(PMMLFunctions.ISNOTMISSING);
					break;
				default:
					throw new InvalidElementException(apply);
			}
		}
	}

	static
	private void inlineNestedExpressions(Apply apply){
		String function = apply.requireFunction();
		List<Expression> expressions = apply.getExpressions();

		if(expressions.size() < 2){
			throw new InvalidElementException(apply);
		}

		ListIterator<Expression> expressionIt = expressions.listIterator();
		while(expressionIt.hasNext()){
			Expression expression = expressionIt.next();

			if(expression instanceof Apply){
				Apply nestedApply = (Apply)expression;

				if(Objects.equals(function, nestedApply.requireFunction())){
					expressionIt.remove();

					// Depth first, breadth second
					inlineNestedExpressions(nestedApply);

					List<Expression> nestedExpressions = nestedApply.getExpressions();
					for(Expression nestedExpression : nestedExpressions){
						expressionIt.add(nestedExpression);
					}
				}
			}
		}
	}

	static
	private void negateExpression(Apply apply){
		List<Expression> expressions = apply.getExpressions();

		if(expressions.size() != 1){
			throw new InvalidElementException(apply);
		}

		ListIterator<Expression> expressionIt = expressions.listIterator();

		Expression expression = expressionIt.next();

		if(expression instanceof Apply){
			Apply nestedApply = (Apply)expression;

			String negatedFunction = negate(nestedApply.requireFunction());
			if(negatedFunction != null){
				expressionIt.remove();

				apply.setFunction(negatedFunction);

				List<Expression> nestedExpressions = nestedApply.getExpressions();
				for(Expression nestedExpression : nestedExpressions){
					expressionIt.add(nestedExpression);
				}
			}
		}
	}

	static
	private boolean isMissingConstant(Expression expression){

		if(expression instanceof Constant){
			Constant constant = (Constant)expression;

			return constant.isMissing();
		}

		return false;
	}

	static
	private String negate(String function){

		switch(function){
			case PMMLFunctions.EQUAL:
				return PMMLFunctions.NOTEQUAL;
			case PMMLFunctions.GREATEROREQUAL:
				return PMMLFunctions.LESSTHAN;
			case PMMLFunctions.GREATERTHAN:
				return PMMLFunctions.LESSOREQUAL;
			case PMMLFunctions.ISMISSING:
				return PMMLFunctions.ISNOTMISSING;
			case PMMLFunctions.ISNOTMISSING:
				return PMMLFunctions.ISMISSING;
			case PMMLFunctions.LESSOREQUAL:
				return PMMLFunctions.GREATERTHAN;
			case PMMLFunctions.LESSTHAN:
				return PMMLFunctions.GREATEROREQUAL;
			case PMMLFunctions.NOTEQUAL:
				return PMMLFunctions.EQUAL;
			default:
				return null;
		}
	}
}