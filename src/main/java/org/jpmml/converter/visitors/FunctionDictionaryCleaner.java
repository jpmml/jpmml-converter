/*
 * Copyright (c) 2020 Villu Ruusmann
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dmg.pmml.Apply;
import org.dmg.pmml.DefineFunction;
import org.dmg.pmml.PMML;
import org.dmg.pmml.PMMLObject;
import org.dmg.pmml.TransformationDictionary;
import org.dmg.pmml.VisitorAction;
import org.jpmml.model.visitors.AbstractVisitor;
import org.jpmml.model.visitors.Resettable;

public class FunctionDictionaryCleaner extends AbstractVisitor implements Resettable {

	public Set<String> functions = new HashSet<>();


	@Override
	public void reset(){
		this.functions.clear();
	}

	@Override
	public void pushParent(PMMLObject parent){
		super.pushParent(parent);
	}

	@Override
	public PMMLObject popParent(){
		PMMLObject parent = super.popParent();

		if(parent instanceof PMML){
			PMML pmml = (PMML)parent;

			TransformationDictionary transformationDictionary = pmml.getTransformationDictionary();
			if(transformationDictionary != null && transformationDictionary.hasDefineFunctions()){
				processDefineFunctions(transformationDictionary);

				if(!transformationDictionary.hasDefineFunctions() && !transformationDictionary.hasDerivedFields()){
					pmml.setTransformationDictionary(null);
				}
			}
		}

		return parent;
	}

	@Override
	public VisitorAction visit(Apply apply){
		processApply(apply);

		return super.visit(apply);
	}

	private void processDefineFunctions(TransformationDictionary transformationDictionary){

		if(transformationDictionary.hasDefineFunctions()){
			List<DefineFunction> defineFunctions = transformationDictionary.getDefineFunctions();

			for(Iterator<DefineFunction> it = defineFunctions.iterator(); it.hasNext(); ){
				DefineFunction defineFunction = it.next();

				boolean retain = this.functions.contains(defineFunction.getName());
				if(!retain){
					it.remove();
				}
			}
		}
	}

	private void processApply(Apply apply){
		String function = apply.getFunction();

		this.functions.add(function);
	}
}