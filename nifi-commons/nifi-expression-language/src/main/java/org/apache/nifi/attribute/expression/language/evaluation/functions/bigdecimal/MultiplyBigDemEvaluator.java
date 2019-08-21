/*
 * Licensed to the Orchsym Runtime under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * 
 * this file to You under the Orchsym License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://github.com/orchsym/runtime/blob/master/orchsym/LICENSE
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.nifi.attribute.expression.language.evaluation.BigDecimalEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.BigDecimalQueryResult;
import org.apache.nifi.attribute.expression.language.evaluation.Evaluator;
import org.apache.nifi.attribute.expression.language.evaluation.QueryResult;
/**
 * 
 * @author LiGuo
 * BigDecimal 乘法
 */
public class MultiplyBigDemEvaluator extends BigDecimalEvaluator {

    private final Evaluator<BigDecimal> subject;
    private final Evaluator<BigDecimal> plusValue;

    public MultiplyBigDemEvaluator(final Evaluator<BigDecimal> subject, final Evaluator<BigDecimal> plusValue) {
        this.subject = subject;
        this.plusValue = plusValue;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public QueryResult<BigDecimal> evaluate(final Map<String, String> attributes) {
        final BigDecimal subjectValue = subject.evaluate(attributes).getValue();
        if (subjectValue == null) {
            return new BigDecimalQueryResult(null);
        }

        final BigDecimal multiply = plusValue.evaluate(attributes).getValue();
        if (multiply == null) {
            return new BigDecimalQueryResult(null);
        }

        BigDecimal result  = (BigDecimal) subjectValue.multiply(multiply);
        
        return new BigDecimalQueryResult(result);
    }

    @Override
    public Evaluator<?> getSubjectEvaluator() {
        return subject;
    }

}
