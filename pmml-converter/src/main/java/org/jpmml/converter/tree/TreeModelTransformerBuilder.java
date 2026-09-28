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
package org.jpmml.converter.tree;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.dmg.pmml.MiningFunction;
import org.jpmml.converter.ConfigurationException;
import org.jpmml.converter.ExceptionUtil;
import org.jpmml.converter.ModelTransformer;
import org.jpmml.converter.ModelTransformerBuilder;
import org.jpmml.converter.tree.visitors.ClassificationTreeModelCompactor;
import org.jpmml.converter.tree.visitors.ClassificationTreeModelFlattener;
import org.jpmml.converter.tree.visitors.ClassificationTreeModelPruner;
import org.jpmml.converter.tree.visitors.NodeIdCleaner;
import org.jpmml.converter.tree.visitors.NodeRecordCountCleaner;
import org.jpmml.converter.tree.visitors.RegressionTreeModelCompactor;
import org.jpmml.converter.tree.visitors.RegressionTreeModelFlattener;
import org.jpmml.converter.tree.visitors.RegressionTreeModelPruner;
import org.jpmml.converter.tree.visitors.TreeModelCompactor;
import org.jpmml.converter.tree.visitors.TreeModelFlattener;
import org.jpmml.converter.tree.visitors.TreeModelPruner;

/**
 * @see HasTreeModelOptions
 */
public class TreeModelTransformerBuilder extends ModelTransformerBuilder {

	private MiningFunction miningFunction = null;


	public TreeModelTransformerBuilder(MiningFunction miningFunction){
		setMiningFunction(miningFunction);
	}

	protected TreeModelCompactor createCompactor(){
		MiningFunction miningFunction = getMiningFunction();

		switch(miningFunction){
			case CLASSIFICATION:
				return new ClassificationTreeModelCompactor();
			case REGRESSION:
				return new RegressionTreeModelCompactor();
			default:
				throw new IllegalArgumentException();
		}
	}

	protected TreeModelFlattener createFlattener(){
		MiningFunction miningFunction = getMiningFunction();

		switch(miningFunction){
			case CLASSIFICATION:
				return new ClassificationTreeModelFlattener();
			case REGRESSION:
				return new RegressionTreeModelFlattener();
			default:
				throw new IllegalArgumentException();
		}
	}

	protected TreeModelPruner createPruner(){
		MiningFunction miningFunction = getMiningFunction();

		switch(miningFunction){
			case CLASSIFICATION:
				return new ClassificationTreeModelPruner();
			case REGRESSION:
				return new RegressionTreeModelPruner();
			default:
				throw new IllegalArgumentException();
		}
	}

	@Override
	public ModelTransformer build(){
		checkConflicts();

		ModelTransformer result = new ModelTransformer(){
		};

		if(isEnabled(HasTreeModelOptions.OPTION_COMPACT)){
			result.add(createCompactor());
		} // End if

		if(isEnabled(HasTreeModelOptions.OPTION_FLAT)){
			result.add(createFlattener());
		} // End if

		if(isEnabled(HasTreeModelOptions.OPTION_PRUNE)){
			result.add(createPruner());
		} // End if

		if(isDisabled(HasTreeModelOptions.OPTION_NODE_ID)){
			result.add(new NodeIdCleaner());
		} // End if

		if(isDisabled(HasTreeModelOptions.OPTION_NODE_RECORDCOUNT)){
			result.add(new NodeRecordCountCleaner());
		}

		return result;
	}

	@Override
	public TreeModelTransformerBuilder putOption(String name, Object value){
		super.putOption(name, value);

		return this;
	}

	@Override
	public TreeModelTransformerBuilder putOptions(Map<String, ?> options){
		super.putOptions(options);

		return this;
	}

	@Override
	public Object getOption(String name){

		if(!hasOption(name)){
			List<String> nodeOptions = getNodeOptions();
			List<String> treeModelTransformationOptions = HasTreeModelOptions.OPTIONS_TREEMODEL_TRANSFORMATIONS;

			// First pass - explicit options take precedence over the default options of the other group
			if(isSuppressed(name, nodeOptions, treeModelTransformationOptions, this::isExplicitlyEnabled) || isSuppressed(name, treeModelTransformationOptions, nodeOptions, this::isExplicitlyEnabled)){
				return null;
			} // End if

			// Second pass - between default options, node options take precedence over tree model transformation options,
			// because tree model transformations are information-destructive
			if(isSuppressed(name, treeModelTransformationOptions, nodeOptions, this::isEnabled)){
				return null;
			}
		}

		return super.getOption(name);
	}

	protected void checkConflicts(){
		List<String> nodeOptions = getNodeOptions();
		List<String> treeModelTransformationOptions = HasTreeModelOptions.OPTIONS_TREEMODEL_TRANSFORMATIONS;

		for(String nodeOption : nodeOptions){

			if(!isExplicitlyEnabled(nodeOption)){
				continue;
			}

			for(String treeModelTransformationOption : treeModelTransformationOptions){

				if(isExplicitlyEnabled(treeModelTransformationOption)){
					throw new ConfigurationException("Conflicting tree model options " + ExceptionUtil.formatName(nodeOption) + " and " + ExceptionUtil.formatName(treeModelTransformationOption));
				}
			}
		}
	}

	protected List<String> getNodeOptions(){
		return HasTreeModelOptions.OPTIONS_NODE;
	}

	public MiningFunction getMiningFunction(){
		return this.miningFunction;
	}

	private void setMiningFunction(MiningFunction miningFunction){
		this.miningFunction = Objects.requireNonNull(miningFunction);
	}

	static
	private boolean isSuppressed(String name, List<String> options, List<String> suppressingOptions, Predicate<String> predicate){
		return options.contains(name) && (suppressingOptions.stream()).anyMatch(predicate);
	}
}