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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.dmg.pmml.Visitor;
import org.jpmml.converter.ConfigurationException;
import org.jpmml.converter.ModelTransformer;
import org.jpmml.converter.tree.visitors.RegressionTreeModelCompactor;
import org.jpmml.converter.tree.visitors.RegressionTreeModelFlattener;
import org.jpmml.converter.tree.visitors.RegressionTreeModelPruner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TreeModelTransformerBuilderTest {

	@Test
	public void build(){
		TreeModelTransformerBuilder builder = createBuilder(Collections.emptyMap());

		ModelTransformer modelTransformer = builder.build();

		assertTrue(modelTransformer.isEmpty());

		builder = createBuilder(Collections.emptyMap())
			.putOption(HasTreeModelOptions.OPTION_PRUNE, Boolean.TRUE)
			.putOption(HasTreeModelOptions.OPTION_FLAT, Boolean.TRUE)
			.putOption(HasTreeModelOptions.OPTION_COMPACT, Boolean.TRUE);

		modelTransformer = builder.build();

		assertEquals(3, modelTransformer.size());

		checkVisitor(RegressionTreeModelCompactor.class, modelTransformer.get(0));
		checkVisitor(RegressionTreeModelFlattener.class, modelTransformer.get(1));
		checkVisitor(RegressionTreeModelPruner.class, modelTransformer.get(2));
	}

	@Test
	public void buildDefaults(){
		Map<String, ?> defaults = Map.of(HasTreeModelOptions.OPTION_COMPACT, Boolean.TRUE, HasTreeModelOptions.OPTION_PRUNE, Boolean.TRUE);

		TreeModelTransformerBuilder builder = createBuilder(defaults);

		ModelTransformer modelTransformer = builder.build();

		assertEquals(2, modelTransformer.size());

		builder = createBuilder(defaults)
			.putOption(HasTreeModelOptions.OPTION_NODE_ID, Boolean.TRUE);

		modelTransformer = builder.build();

		assertTrue(modelTransformer.isEmpty());

		TreeModelTransformerBuilder conflictingBuilder = createBuilder(defaults)
			.putOption(HasTreeModelOptions.OPTION_NODE_ID, Boolean.TRUE)
			.putOption(HasTreeModelOptions.OPTION_PRUNE, Boolean.TRUE);

		assertThrows(ConfigurationException.class, () -> conflictingBuilder.build());
	}

	@Test
	public void buildNodeDefaults(){
		Map<String, ?> defaults = Map.of(HasTreeModelOptions.OPTION_COMPACT, Boolean.TRUE, HasTreeModelOptions.OPTION_NODE_ID, Boolean.TRUE);

		TreeModelTransformerBuilder builder = createBuilder(defaults);

		ModelTransformer modelTransformer = builder.build();

		assertTrue(modelTransformer.isEmpty());

		assertEquals(Boolean.TRUE, builder.getOption(HasTreeModelOptions.OPTION_NODE_ID));

		builder = createBuilder(defaults)
			.putOption(HasTreeModelOptions.OPTION_COMPACT, Boolean.TRUE);

		modelTransformer = builder.build();

		assertEquals(1, modelTransformer.size());

		assertNull(builder.getOption(HasTreeModelOptions.OPTION_NODE_ID));
	}

	@Test
	public void buildNodeOptions(){
		Map<String, ?> defaults = Map.of(HasTreeModelOptions.OPTION_COMPACT, Boolean.TRUE);

		TreeModelTransformerBuilder builder = createBuilder(defaults, TreeModelTransformerBuilderTest.OPTION_NODE_EXTENSIONS)
			.putOption(TreeModelTransformerBuilderTest.OPTION_NODE_EXTENSIONS, Map.of("impurity", Map.of()));

		ModelTransformer modelTransformer = builder.build();

		assertTrue(modelTransformer.isEmpty());

		assertNull(builder.getOption(HasTreeModelOptions.OPTION_COMPACT));

		builder = createBuilder(defaults, TreeModelTransformerBuilderTest.OPTION_NODE_EXTENSIONS)
			.putOption(TreeModelTransformerBuilderTest.OPTION_NODE_EXTENSIONS, null)
			.putOption(HasTreeModelOptions.OPTION_NODE_ID, Boolean.FALSE);

		modelTransformer = builder.build();

		assertEquals(1, modelTransformer.size());

		TreeModelTransformerBuilder conflictingBuilder = createBuilder(defaults, TreeModelTransformerBuilderTest.OPTION_NODE_EXTENSIONS)
			.putOption(TreeModelTransformerBuilderTest.OPTION_NODE_EXTENSIONS, Map.of("impurity", Map.of()))
			.putOption(HasTreeModelOptions.OPTION_COMPACT, Boolean.TRUE);

		assertThrows(ConfigurationException.class, () -> conflictingBuilder.build());
	}

	static
	private TreeModelTransformerBuilder createBuilder(Map<String, ?> defaults, String... extraNodeOptions){
		TreeModelTransformerBuilder result = new RegressionTreeModelTransformerBuilder(){

			@Override
			protected List<String> getNodeOptions(){
				List<String> result = new ArrayList<>(super.getNodeOptions());
				result.addAll(Arrays.asList(extraNodeOptions));

				return result;
			}

			@Override
			protected Map<String, ?> getDefaults(){
				return defaults;
			}
		};

		return result;
	}

	static
	private void checkVisitor(Class<? extends Visitor> clazz, Visitor visitor){
		assertEquals(clazz, visitor.getClass());
	}

	private static final String OPTION_NODE_EXTENSIONS = "node_extensions";
}