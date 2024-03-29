/*
 * Copyright (c) 2022 Villu Ruusmann
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
package org.jpmml.converter.testing;

import java.util.List;
import java.util.Map;

import com.google.common.base.Equivalence;
import org.jpmml.evaluator.testing.ArchiveBatchTest;
import org.jpmml.evaluator.testing.Batch;

abstract
public class ModelEncoderBatchTest extends ArchiveBatchTest {

	public ModelEncoderBatchTest(Equivalence<Object> equivalence){
		super(equivalence);
	}

	@Override
	public void evaluate(Batch batch) throws Exception {
		evaluate((ModelEncoderBatch)batch);
	}

	public void evaluate(ModelEncoderBatch modelEncoderBatch) throws Exception {
		List<Map<String, Object>> optionsMatrix = modelEncoderBatch.getOptionsMatrix();

		if(optionsMatrix.isEmpty()){
			throw new IllegalArgumentException();
		}

		for(int i = 0; i < optionsMatrix.size(); i++){
			Map<String, Object> options = optionsMatrix.get(i);

			modelEncoderBatch.setOptions(options);

			super.evaluate(modelEncoderBatch);
		}
	}
}