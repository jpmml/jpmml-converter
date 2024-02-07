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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.base.Equivalence;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.EvaluatorBuilder;
import org.jpmml.evaluator.FieldNameSet;
import org.jpmml.evaluator.FunctionNameStack;
import org.jpmml.evaluator.ModelEvaluatorBuilder;
import org.jpmml.evaluator.ResultField;
import org.jpmml.evaluator.testing.ArchiveBatch;
import org.jpmml.evaluator.testing.CsvUtil;
import org.jpmml.evaluator.visitors.UnsupportedMarkupInspector;
import org.jpmml.model.visitors.InvalidMarkupInspector;
import org.jpmml.model.visitors.MissingMarkupInspector;
import org.jpmml.model.visitors.VisitorBattery;

abstract
public class ModelEncoderBatch extends ArchiveBatch {

	private Map<String, Object> options = new LinkedHashMap<>();


	public ModelEncoderBatch(String algorithm, String dataset, Predicate<ResultField> columnFilter, Equivalence<Object> equivalence){
		super(algorithm, dataset, columnFilter, equivalence);
	}

	abstract
	public ModelEncoderBatchTest getArchiveBatchTest();

	abstract
	public PMML getPMML() throws Exception;

	@Override
	public InputStream open(String path) throws IOException {
		ModelEncoderBatchTest batchTest = getArchiveBatchTest();

		Class<? extends ModelEncoderBatchTest> clazz = batchTest.getClass();

		InputStream result = clazz.getResourceAsStream(path);
		if(result == null){
			throw new IOException(path);
		}

		return result;
	}

	@Override
	protected List<Map<String, String>> loadRecords(String path) throws IOException {
		String separator = getSeparator();

		CsvUtil.Table table;

		try(InputStream is = open(path)){
			table = CsvUtil.readTable(is, separator);
		}

		Function<String, String> function = new Function<String, String>(){

			@Override
			public String apply(String string){

				if(("N/A").equals(string) || ("NA").equals(string)){
					return null;
				}

				return string;
			}
		};

		return CsvUtil.toRecords(table, function);
	}

	protected String getSeparator(){
		return ",";
	}

	/**
	 * @see #setOptions(Map)
	 */
	public List<Map<String, Object>> getOptionsMatrix(){
		return Collections.singletonList(Collections.emptyMap());
	}

	@Override
	public Evaluator getEvaluator() throws Exception {
		EvaluatorBuilder evaluatorBuilder = getEvaluatorBuilder();

		Evaluator evaluator = evaluatorBuilder.build();

		evaluator.verify();

		return evaluator;
	}

	public EvaluatorBuilder getEvaluatorBuilder() throws Exception {
		PMML pmml = getPMML();

		ModelEvaluatorBuilder evaluatorBuilder = new ModelEvaluatorBuilder(pmml);

		// XXX
		evaluatorBuilder
			.setDerivedFieldGuard(new FieldNameSet(8))
			.setFunctionGuard(new FunctionNameStack(4));

		return evaluatorBuilder;
	}

	public void validatePMML(PMML pmml) throws Exception {
		VisitorBattery visitorBattery = getValidators();

		if(visitorBattery != null && !visitorBattery.isEmpty()){
			visitorBattery.applyTo(pmml);
		}
	}

	public VisitorBattery getValidators(){
		VisitorBattery visitorBattery = new VisitorBattery();

		visitorBattery.add(MissingMarkupInspector.class);
		visitorBattery.add(InvalidMarkupInspector.class);

		// XXX
		visitorBattery.add(UnsupportedMarkupInspector.class);

		return visitorBattery;
	}

	public Map<String, Object> getOptions(){
		return this.options;
	}

	public void setOptions(Map<String, Object> options){
		this.options = options;
	}

	static
	protected String truncate(String string){

		for(int i = 0; i < string.length(); i++){
			char c = string.charAt(i);

			if(!Character.isLetterOrDigit(c)){
				return string.substring(0, i);
			}
		}

		return string;
	}
}
