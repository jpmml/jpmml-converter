/*
 * Copyright (c) 2019 Villu Ruusmann
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

import org.dmg.pmml.LocalTransformations;
import org.dmg.pmml.Model;
import org.dmg.pmml.Output;
import org.dmg.pmml.Targets;
import org.dmg.pmml.VisitorAction;
import org.jpmml.model.visitors.AbstractVisitor;

public class ModelCleaner extends AbstractVisitor {

	@Override
	public VisitorAction visit(Model model){
		LocalTransformations localTransformations = model.getLocalTransformations();
		Targets targets = model.getTargets();
		Output output = model.getOutput();

		if(localTransformations != null && !localTransformations.hasDerivedFields()){
			model.setLocalTransformations(null);
		} // End if

		if(targets != null && !targets.hasTargets()){
			model.setTargets(null);
		} // End if

		if(output != null && !output.hasOutputFields()){
			model.setOutput(null);
		}

		return super.visit(model);
	}
}