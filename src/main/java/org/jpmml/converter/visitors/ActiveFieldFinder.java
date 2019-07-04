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

import java.util.HashSet;
import java.util.Set;

import org.dmg.pmml.Field;
import org.dmg.pmml.Model;
import org.dmg.pmml.PMMLObject;
import org.dmg.pmml.Visitable;
import org.dmg.pmml.mining.MiningModel;

abstract
public class ActiveFieldFinder extends DeepFieldResolver {

	private Set<Field<?>> activeFields = new HashSet<>();


	@Override
	public void applyTo(Visitable visitable){
		this.activeFields.clear();

		super.applyTo(visitable);
	}

	@Override
	public PMMLObject popParent(){
		PMMLObject parent = super.popParent();

		if(parent instanceof MiningModel){
			MiningModel miningModel = (MiningModel)parent;

			processMiningModel(miningModel);
		} else

		if(parent instanceof Model){
			Model model = (Model)parent;

			processModel(model);
		}

		return parent;
	}

	private void processMiningModel(MiningModel miningModel){
		Set<Field<?>> activeFields = getActiveFields();

		activeFields.addAll(DeepFieldResolverUtil.getActiveFields(this, miningModel));
	}

	private void processModel(Model model){
		Set<Field<?>> activeFields = getActiveFields();

		activeFields.addAll(DeepFieldResolverUtil.getActiveFields(this, model));
	}

	public Set<Field<?>> getActiveFields(){
		return this.activeFields;
	}
}