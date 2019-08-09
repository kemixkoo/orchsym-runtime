/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nifi.attribute.expression.language.compile;

import static org.apache.nifi.attribute.expression.language.antlr.AttributeExpressionParser.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;
import org.apache.nifi.attribute.expression.language.CompiledExpression;
import org.apache.nifi.attribute.expression.language.Query;
import org.apache.nifi.attribute.expression.language.Query.Range;
import org.apache.nifi.attribute.expression.language.antlr.AttributeExpressionLexer;
import org.apache.nifi.attribute.expression.language.antlr.AttributeExpressionParser;
import org.apache.nifi.attribute.expression.language.evaluation.BigDecimalEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.BooleanEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.DateEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.Evaluator;
import org.apache.nifi.attribute.expression.language.evaluation.StringEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.cast.BigDecimalCastEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.cast.BooleanCastEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.cast.DateCastEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.cast.DecimalCastEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.cast.NumberCastEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.cast.StringCastEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.cast.WholeNumberCastEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.AndEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.AppendEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.Base64DecodeEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.Base64EncodeEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.CharSequenceTranslatorEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.ContainsEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.DivideEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.EndsWithEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.EqualsEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.EqualsIgnoreCaseEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.FindEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.FormatEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.FromRadixEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.GetDelimitedFieldEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.GetStateVariableEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.GreaterThanEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.GreaterThanOrEqualEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.HostnameEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.IPEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.IfElseEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.InEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.IndexOfEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.IsEmptyEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.IsNullEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.JsonPathEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.LastIndexOfEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.LengthEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.LessThanEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.LessThanOrEqualEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.MatchesEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.MathEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.MinusEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.ModEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.MultiplyEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.NotEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.NotNullEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.NowEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.NumberToDateEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.OneUpSequenceEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.OrEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.PlusEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.PrependEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.RandomNumberGeneratorEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.ReplaceAllEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.ReplaceEmptyEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.ReplaceEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.ReplaceFirstEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.ReplaceNullEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.StartsWithEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.StringToDateEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.SubstringAfterEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.SubstringAfterLastEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.SubstringBeforeEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.SubstringBeforeLastEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.SubstringEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.ToLowerEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.ToRadixEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.ToStringEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.ToUpperEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.TrimEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.UrlDecodeEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.UrlEncodeEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.UuidEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.AbsBigDecimalEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.CompareToBigDemEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.DivideBigDemEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.EqualsBigDemEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.FloatValueBigDecimalEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.GreateThanBigDemEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.GreateThanOrEqualBigDemEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.IntValueBigDecimalEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.LessThanBigDemEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.LessThanOrEqualBigDemEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.LongValueBigDecimalEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.MaxBigDemEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.MinBigDemEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.MinusBigDemEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.MovePointLeftBigDemEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.MovePointRightBigDemEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.MultiplyBigDemEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.NegateBigDecimalEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.PlusBigDemEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.PowBigDemEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.ScaleByPowerOfTenDemEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.SetScaleBigDemEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.ShortValueBigDecimalEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.ToBigDecimalEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.bigdecimal.ToPercentBigDecimalEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.AddDaysEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.AddHoursEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.AddMinsEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.AddMonthsEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.AddSecsEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.AddWeeksEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.AddYearsEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.DateAfterEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.DateBeforeEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.GetDateForWeekEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.GetDayOfMonthEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.GetDayOfWeekEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.GetDayOfWeekInMonthEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.GetDayOfYearEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.GetHourEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.GetHourOfDayEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.GetMilliSecondEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.GetMinuteEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.GetMonthEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.GetQuarterEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.GetSecondEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.GetWeekOfMonthEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.GetWeekOfYearEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.GetYearEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.functions.date.IsWeekendEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.literals.BooleanLiteralEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.literals.DecimalLiteralEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.literals.StringLiteralEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.literals.ToLiteralEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.literals.WholeNumberLiteralEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.reduce.CountEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.reduce.JoinEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.reduce.ReduceEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.selection.AllAttributesEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.selection.AnyAttributeEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.selection.AttributeEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.selection.DelineatedAttributeEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.selection.IteratingEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.selection.MappingEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.selection.MultiAttributeEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.selection.MultiMatchAttributeEvaluator;
import org.apache.nifi.attribute.expression.language.evaluation.selection.MultiNamedAttributeEvaluator;
import org.apache.nifi.attribute.expression.language.exception.AttributeExpressionLanguageException;
import org.apache.nifi.attribute.expression.language.exception.AttributeExpressionLanguageParsingException;
import org.apache.nifi.expression.AttributeExpression.ResultType;
import org.apache.nifi.flowfile.FlowFile;

public class ExpressionCompiler {
    private final Set<Evaluator<?>> evaluators = new HashSet<>();

    public CompiledExpression compile(final String expression) {
        try {
            final CharStream input = new ANTLRStringStream(expression);
            final AttributeExpressionLexer lexer = new AttributeExpressionLexer(input);
            final CommonTokenStream lexerTokenStream = new CommonTokenStream(lexer);

            final AttributeExpressionParser parser = new AttributeExpressionParser(lexerTokenStream);
            final Tree ast = (Tree) parser.query().getTree();
            final Tree tree = ast.getChild(0);

            final Evaluator<?> evaluator = buildEvaluator(tree);
            verifyMappingEvaluatorReduced(evaluator);

            final Set<Evaluator<?>> allEvaluators = new HashSet<>(evaluators);
            this.evaluators.clear();

            return new CompiledExpression(expression, evaluator, tree, allEvaluators);
        } catch (final AttributeExpressionLanguageParsingException e) {
            throw e;
        } catch (final Exception e) {
            throw new AttributeExpressionLanguageParsingException(e);
        }
    }

    private Tree compileTree(final String expression) throws AttributeExpressionLanguageParsingException {
        try {
            final CharStream input = new ANTLRStringStream(expression);
            final AttributeExpressionLexer lexer = new AttributeExpressionLexer(input);
            final CommonTokenStream lexerTokenStream = new CommonTokenStream(lexer);

            final AttributeExpressionParser parser = new AttributeExpressionParser(lexerTokenStream);
            final Tree ast = (Tree) parser.query().getTree();
            final Tree tree = ast.getChild(0);

            // ensure that we are able to build the evaluators, so that we validate syntax
            final Evaluator<?> evaluator = buildEvaluator(tree);
            verifyMappingEvaluatorReduced(evaluator);

            return tree;
        } catch (final AttributeExpressionLanguageParsingException e) {
            throw e;
        } catch (final Exception e) {
            throw new AttributeExpressionLanguageParsingException(e);
        }
    }

    private void verifyMappingEvaluatorReduced(final Evaluator<?> evaluator) {
        final Evaluator<?> rightMostEvaluator;
        if (evaluator instanceof IteratingEvaluator) {
            rightMostEvaluator = ((IteratingEvaluator<?>) evaluator).getLogicEvaluator();
        } else {
            rightMostEvaluator = evaluator;
        }

        Evaluator<?> eval = rightMostEvaluator.getSubjectEvaluator();
        Evaluator<?> lastEval = rightMostEvaluator;
        while (eval != null) {
            if (eval instanceof ReduceEvaluator) {
                throw new AttributeExpressionLanguageParsingException("Expression attempts to call function '" + lastEval.getToken() + "' on the result of '" + eval.getToken()
                        + "'. This is not allowed. Instead, use \"${literal( ${<embedded expression>} ):" + lastEval.getToken() + "(...)}\"");
            }

            lastEval = eval;
            eval = eval.getSubjectEvaluator();
        }

        // if the result type of the evaluator is BOOLEAN, then it will always
        // be reduced when evaluator.
        final ResultType resultType = evaluator.getResultType();
        if (resultType == ResultType.BOOLEAN) {
            return;
        }

        final Evaluator<?> rootEvaluator = getRootSubjectEvaluator(evaluator);
        if (rootEvaluator != null && rootEvaluator instanceof MultiAttributeEvaluator) {
            final MultiAttributeEvaluator multiAttrEval = (MultiAttributeEvaluator) rootEvaluator;
            switch (multiAttrEval.getEvaluationType()) {
            case ALL_ATTRIBUTES:
            case ALL_MATCHING_ATTRIBUTES:
            case ALL_DELINEATED_VALUES: {
                if (!(evaluator instanceof ReduceEvaluator)) {
                    throw new AttributeExpressionLanguageParsingException("Cannot evaluate expression because it attempts to reference multiple attributes but does not use a reducing function");
                }
                break;
            }
            default:
                throw new AttributeExpressionLanguageParsingException("Cannot evaluate expression because it attempts to reference multiple attributes but does not use a reducing function");
            }
        }
    }

    private Evaluator<?> getRootSubjectEvaluator(final Evaluator<?> evaluator) {
        if (evaluator == null) {
            return null;
        }

        final Evaluator<?> subject = evaluator.getSubjectEvaluator();
        if (subject == null) {
            return evaluator;
        }

        return getRootSubjectEvaluator(subject);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Evaluator<?> buildExpressionEvaluator(final Tree tree) {
        if (tree.getChildCount() == 0) {
            throw new AttributeExpressionLanguageParsingException("EXPRESSION tree node has no children");
        }

        final Evaluator<?> evaluator;
        if (tree.getChildCount() == 1) {
            evaluator = buildEvaluator(tree.getChild(0));
        } else {
            // we can chain together functions in the form of:
            // ${x:trim():substring(1,2):trim()}
            // in this case, the subject of the right-most function is the function to its left; its
            // subject is the function to its left (the first trim()), and its subject is the value of
            // the 'x' attribute. We accomplish this logic by iterating over all of the children of the
            // tree from the right-most child going left-ward.
            evaluator = buildFunctionExpressionEvaluator(tree, 0);
        }

        Evaluator<?> chosenEvaluator = evaluator;
        final Evaluator<?> rootEvaluator = getRootSubjectEvaluator(evaluator);
        if (rootEvaluator != null) {
            if (rootEvaluator instanceof MultiAttributeEvaluator) {
                final MultiAttributeEvaluator multiAttrEval = (MultiAttributeEvaluator) rootEvaluator;

                switch (multiAttrEval.getEvaluationType()) {
                case ANY_ATTRIBUTE:
                case ANY_MATCHING_ATTRIBUTE:
                case ANY_DELINEATED_VALUE:
                    chosenEvaluator = new AnyAttributeEvaluator((BooleanEvaluator) evaluator, multiAttrEval);
                    break;
                case ALL_ATTRIBUTES:
                case ALL_MATCHING_ATTRIBUTES:
                case ALL_DELINEATED_VALUES: {
                    final ResultType resultType = evaluator.getResultType();
                    if (resultType == ResultType.BOOLEAN) {
                        chosenEvaluator = new AllAttributesEvaluator((BooleanEvaluator) evaluator, multiAttrEval);
                    } else if (evaluator instanceof ReduceEvaluator) {
                        chosenEvaluator = new MappingEvaluator((ReduceEvaluator) evaluator, multiAttrEval);
                    } else {
                        throw new AttributeExpressionLanguageException("Cannot evaluate Expression because it attempts to reference multiple attributes but does not use a reducing function");
                    }
                    break;
                }
                }

                evaluators.add(chosenEvaluator);
                switch (multiAttrEval.getEvaluationType()) {
                case ANY_ATTRIBUTE:
                    chosenEvaluator.setToken("anyAttribute");
                    break;
                case ANY_MATCHING_ATTRIBUTE:
                    chosenEvaluator.setToken("anyMatchingAttribute");
                    break;
                case ANY_DELINEATED_VALUE:
                    chosenEvaluator.setToken("anyDelineatedValue");
                    break;
                case ALL_ATTRIBUTES:
                    chosenEvaluator.setToken("allAttributes");
                    break;
                case ALL_MATCHING_ATTRIBUTES:
                    chosenEvaluator.setToken("allMatchingAttributes");
                    break;
                case ALL_DELINEATED_VALUES:
                    chosenEvaluator.setToken("allDelineatedValues");
                    break;
                }
            }
        }

        return chosenEvaluator;
    }

    private Evaluator<?> buildFunctionExpressionEvaluator(final Tree tree, final int offset) {
        if (tree.getChildCount() == 0) {
            throw new AttributeExpressionLanguageParsingException("EXPRESSION tree node has no children");
        }
        final int firstChildIndex = tree.getChildCount() - offset - 1;
        if (firstChildIndex == 0) {
            return buildEvaluator(tree.getChild(0));
        }

        final Tree functionTree = tree.getChild(firstChildIndex);
        final Evaluator<?> subjectEvaluator = buildFunctionExpressionEvaluator(tree, offset + 1);

        final Tree functionNameTree = functionTree.getChild(0);
        final List<Evaluator<?>> argEvaluators = new ArrayList<>();
        for (int i = 1; i < functionTree.getChildCount(); i++) {
            argEvaluators.add(buildEvaluator(functionTree.getChild(i)));
        }
        return buildFunctionEvaluator(functionNameTree, subjectEvaluator, argEvaluators);
    }

    private List<Evaluator<?>> verifyArgCount(final List<Evaluator<?>> args, final int count, final String functionName) {
        if (args.size() != count) {
            throw new AttributeExpressionLanguageParsingException(functionName + "() function takes " + count + " arguments");
        }
        return args;
    }

    private Evaluator<String> toStringEvaluator(final Evaluator<?> evaluator) {
        return toStringEvaluator(evaluator, null);
    }

    private Evaluator<String> toStringEvaluator(final Evaluator<?> evaluator, final String location) {
        if (evaluator.getResultType() == ResultType.STRING) {
            return (StringEvaluator) evaluator;
        }

        return addToken(new StringCastEvaluator(evaluator), evaluator.getToken());
    }

    @SuppressWarnings("unchecked")
    private Evaluator<Boolean> toBooleanEvaluator(final Evaluator<?> evaluator, final String location) {
        switch (evaluator.getResultType()) {
        case BOOLEAN:
            return (Evaluator<Boolean>) evaluator;
        case STRING:
            return addToken(new BooleanCastEvaluator((StringEvaluator) evaluator), evaluator.getToken());
        default:
            throw new AttributeExpressionLanguageParsingException(
                    "Cannot implicitly convert Data Type " + evaluator.getResultType() + " to " + ResultType.BOOLEAN + (location == null ? "" : " at location [" + location + "]"));
        }

    }

    private Evaluator<Boolean> toBooleanEvaluator(final Evaluator<?> evaluator) {
        return toBooleanEvaluator(evaluator, null);
    }

    private Evaluator<Long> toWholeNumberEvaluator(final Evaluator<?> evaluator) {
        return toWholeNumberEvaluator(evaluator, null);
    }

    @SuppressWarnings("unchecked")
    private Evaluator<Long> toWholeNumberEvaluator(final Evaluator<?> evaluator, final String location) {
        switch (evaluator.getResultType()) {
        case WHOLE_NUMBER:
            return (Evaluator<Long>) evaluator;
        case STRING:
        case DATE:
        case DECIMAL:
        case NUMBER:
            return addToken(new WholeNumberCastEvaluator(evaluator), evaluator.getToken());
        default:
            throw new AttributeExpressionLanguageParsingException(
                    "Cannot implicitly convert Data Type " + evaluator.getResultType() + " to " + ResultType.WHOLE_NUMBER + (location == null ? "" : " at location [" + location + "]"));
        }
    }

    private Evaluator<Double> toDecimalEvaluator(final Evaluator<?> evaluator) {
        return toDecimalEvaluator(evaluator, null);
    }

    @SuppressWarnings("unchecked")
    private Evaluator<Double> toDecimalEvaluator(final Evaluator<?> evaluator, final String location) {
        switch (evaluator.getResultType()) {
        case DECIMAL:
            return (Evaluator<Double>) evaluator;
        case WHOLE_NUMBER:
        case STRING:
        case DATE:
        case NUMBER:
            return addToken(new DecimalCastEvaluator(evaluator), evaluator.getToken());
        default:
            throw new AttributeExpressionLanguageParsingException(
                    "Cannot implicitly convert Data Type " + evaluator.getResultType() + " to " + ResultType.DECIMAL + (location == null ? "" : " at location [" + location + "]"));
        }
    }

    private Evaluator<Number> toNumberEvaluator(final Evaluator<?> evaluator) {
        return toNumberEvaluator(evaluator, null);
    }

    @SuppressWarnings("unchecked")
    private Evaluator<Number> toNumberEvaluator(final Evaluator<?> evaluator, final String location) {
        switch (evaluator.getResultType()) {
        case NUMBER:
            return (Evaluator<Number>) evaluator;
        case STRING:
        case DATE:
        case DECIMAL:
        case WHOLE_NUMBER:
            return addToken(new NumberCastEvaluator(evaluator), evaluator.getToken());
        default:
            throw new AttributeExpressionLanguageParsingException(
                    "Cannot implicitly convert Data Type " + evaluator.getResultType() + " to " + ResultType.WHOLE_NUMBER + (location == null ? "" : " at location [" + location + "]"));
        }
    }

    private DateEvaluator toDateEvaluator(final Evaluator<?> evaluator) {
        return toDateEvaluator(evaluator, null);
    }

    private DateEvaluator toDateEvaluator(final Evaluator<?> evaluator, final String location) {
        if (evaluator.getResultType() == ResultType.DATE) {
            return (DateEvaluator) evaluator;
        }

        return new DateCastEvaluator(evaluator);
    }

    private BigDecimalEvaluator toBigDecimalEvaluator(final Evaluator<?> evaluator) {
        return toBigDecimalEvaluator(evaluator, null);
    }

    private BigDecimalEvaluator toBigDecimalEvaluator(final Evaluator<?> evaluator, final String location) {
        if (evaluator.getResultType() == ResultType.BIGDECIMAL) {
            return (BigDecimalEvaluator) evaluator;
        }

        return new BigDecimalCastEvaluator(evaluator);
    }

    private Evaluator<?> buildFunctionEvaluator(final Tree tree, final Evaluator<?> subjectEvaluator, final List<Evaluator<?>> argEvaluators) {
        switch (tree.getType()) {
        case TRIM: {
            verifyArgCount(argEvaluators, 0, "trim");
            return addToken(new TrimEvaluator(toStringEvaluator(subjectEvaluator)), "trim");
        }
        case TO_STRING: {
            verifyArgCount(argEvaluators, 0, "toString");
            return addToken(new ToStringEvaluator(subjectEvaluator), "toString");
        }
        case TO_LOWER: {
            verifyArgCount(argEvaluators, 0, "toLower");
            return addToken(new ToLowerEvaluator(toStringEvaluator(subjectEvaluator)), "toLower");
        }
        case TO_UPPER: {
            verifyArgCount(argEvaluators, 0, "toUpper");
            return addToken(new ToUpperEvaluator(toStringEvaluator(subjectEvaluator)), "toUpper");
        }
        case URL_ENCODE: {
            verifyArgCount(argEvaluators, 0, "urlEncode");
            return addToken(new UrlEncodeEvaluator(toStringEvaluator(subjectEvaluator)), "urlEncode");
        }
        case URL_DECODE: {
            verifyArgCount(argEvaluators, 0, "urlDecode");
            return addToken(new UrlDecodeEvaluator(toStringEvaluator(subjectEvaluator)), "urlDecode");
        }
        case BASE64_ENCODE: {
            verifyArgCount(argEvaluators, 0, "base64Encode");
            return addToken(new Base64EncodeEvaluator(toStringEvaluator(subjectEvaluator)), "base64Encode");
        }
        case BASE64_DECODE: {
            verifyArgCount(argEvaluators, 0, "base64Decode");
            return addToken(new Base64DecodeEvaluator(toStringEvaluator(subjectEvaluator)), "base64Decode");
        }
        case ESCAPE_CSV: {
            verifyArgCount(argEvaluators, 0, "escapeCsv");
            return addToken(CharSequenceTranslatorEvaluator.csvEscapeEvaluator(toStringEvaluator(subjectEvaluator)), "escapeJson");
        }
        case ESCAPE_HTML3: {
            verifyArgCount(argEvaluators, 0, "escapeHtml3");
            return addToken(CharSequenceTranslatorEvaluator.html3EscapeEvaluator(toStringEvaluator(subjectEvaluator)), "escapeJson");
        }
        case ESCAPE_HTML4: {
            verifyArgCount(argEvaluators, 0, "escapeHtml4");
            return addToken(CharSequenceTranslatorEvaluator.html4EscapeEvaluator(toStringEvaluator(subjectEvaluator)), "escapeJson");
        }
        case ESCAPE_JSON: {
            verifyArgCount(argEvaluators, 0, "escapeJson");
            return addToken(CharSequenceTranslatorEvaluator.jsonEscapeEvaluator(toStringEvaluator(subjectEvaluator)), "escapeJson");
        }
        case ESCAPE_XML: {
            verifyArgCount(argEvaluators, 0, "escapeXml");
            return addToken(CharSequenceTranslatorEvaluator.xmlEscapeEvaluator(toStringEvaluator(subjectEvaluator)), "escapeJson");
        }
        case UNESCAPE_CSV: {
            verifyArgCount(argEvaluators, 0, "unescapeCsv");
            return addToken(CharSequenceTranslatorEvaluator.csvUnescapeEvaluator(toStringEvaluator(subjectEvaluator)), "escapeJson");
        }
        case UNESCAPE_HTML3: {
            verifyArgCount(argEvaluators, 0, "unescapeHtml3");
            return addToken(CharSequenceTranslatorEvaluator.html3UnescapeEvaluator(toStringEvaluator(subjectEvaluator)), "escapeJson");
        }
        case UNESCAPE_HTML4: {
            verifyArgCount(argEvaluators, 0, "unescapeHtml4");
            return addToken(CharSequenceTranslatorEvaluator.html4UnescapeEvaluator(toStringEvaluator(subjectEvaluator)), "escapeJson");
        }
        case UNESCAPE_JSON: {
            verifyArgCount(argEvaluators, 0, "unescapeJson");
            return addToken(CharSequenceTranslatorEvaluator.jsonUnescapeEvaluator(toStringEvaluator(subjectEvaluator)), "escapeJson");
        }
        case UNESCAPE_XML: {
            verifyArgCount(argEvaluators, 0, "unescapeXml");
            return addToken(CharSequenceTranslatorEvaluator.xmlUnescapeEvaluator(toStringEvaluator(subjectEvaluator)), "escapeJson");
        }
        case SUBSTRING_BEFORE: {
            verifyArgCount(argEvaluators, 1, "substringBefore");
            return addToken(new SubstringBeforeEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to substringBefore")), "substringBefore");
        }
        case SUBSTRING_BEFORE_LAST: {
            verifyArgCount(argEvaluators, 1, "substringBeforeLast");
            return addToken(new SubstringBeforeLastEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to substringBeforeLast")),
                    "substringBeforeLast");
        }
        case SUBSTRING_AFTER: {
            verifyArgCount(argEvaluators, 1, "substringAfter");
            return addToken(new SubstringAfterEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to substringAfter")), "substringAfter");
        }
        case SUBSTRING_AFTER_LAST: {
            verifyArgCount(argEvaluators, 1, "substringAfterLast");
            return addToken(new SubstringAfterLastEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to substringAfterLast")),
                    "substringAfterLast");
        }
        case REPLACE_NULL: {
            verifyArgCount(argEvaluators, 1, "replaceNull");
            return addToken(new ReplaceNullEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to replaceNull")), "replaceNull");
        }
        case REPLACE_EMPTY: {
            verifyArgCount(argEvaluators, 1, "replaceEmtpy");
            return addToken(new ReplaceEmptyEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to replaceEmpty")), "replaceEmpty");
        }
        case REPLACE: {
            verifyArgCount(argEvaluators, 2, "replace");
            return addToken(new ReplaceEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to replace"),
                    toStringEvaluator(argEvaluators.get(1), "second argument to replace")), "replace");
        }
        case REPLACE_FIRST: {
            verifyArgCount(argEvaluators, 2, "replaceFirst");
            return addToken(new ReplaceFirstEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to replaceFirst"),
                    toStringEvaluator(argEvaluators.get(1), "second argument to replaceFirst")), "replaceFirst");
        }
        case REPLACE_ALL: {
            verifyArgCount(argEvaluators, 2, "replaceAll");
            return addToken(new ReplaceAllEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to replaceAll"),
                    toStringEvaluator(argEvaluators.get(1), "second argument to replaceAll")), "replaceAll");
        }
        case APPEND: {
            verifyArgCount(argEvaluators, 1, "append");
            return addToken(new AppendEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to append")), "append");
        }
        case PREPEND: {
            verifyArgCount(argEvaluators, 1, "prepend");
            return addToken(new PrependEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to prepend")), "prepend");
        }
        case SUBSTRING: {
            final int numArgs = argEvaluators.size();
            if (numArgs == 1) {
                return addToken(new SubstringEvaluator(toStringEvaluator(subjectEvaluator), toWholeNumberEvaluator(argEvaluators.get(0), "first argument to substring")), "substring");
            } else if (numArgs == 2) {
                return addToken(new SubstringEvaluator(toStringEvaluator(subjectEvaluator), toWholeNumberEvaluator(argEvaluators.get(0), "first argument to substring"),
                        toWholeNumberEvaluator(argEvaluators.get(1), "second argument to substring")), "substring");
            } else {
                throw new AttributeExpressionLanguageParsingException("substring() function can take either 1 or 2 arguments but cannot take " + numArgs + " arguments");
            }
        }
        case JOIN: {
            verifyArgCount(argEvaluators, 1, "join");
            return addToken(new JoinEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "join");
        }
        case COUNT: {
            verifyArgCount(argEvaluators, 0, "count");
            return addToken(new CountEvaluator(subjectEvaluator), "count");
        }
        case IS_NULL: {
            verifyArgCount(argEvaluators, 0, "isNull");
            return addToken(new IsNullEvaluator(toStringEvaluator(subjectEvaluator)), "isNull");
        }
        case IS_EMPTY: {
            verifyArgCount(argEvaluators, 0, "isEmpty");
            return addToken(new IsEmptyEvaluator(toStringEvaluator(subjectEvaluator)), "isEmpty");
        }
        case NOT_NULL: {
            verifyArgCount(argEvaluators, 0, "notNull");
            return addToken(new NotNullEvaluator(toStringEvaluator(subjectEvaluator)), "notNull");
        }
        case STARTS_WITH: {
            verifyArgCount(argEvaluators, 1, "startsWith");
            return addToken(new StartsWithEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to startsWith")), "startsWith");
        }
        case ENDS_WITH: {
            verifyArgCount(argEvaluators, 1, "endsWith");
            return addToken(new EndsWithEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to endsWith")), "endsWith");
        }
        case CONTAINS: {
            verifyArgCount(argEvaluators, 1, "contains");
            return addToken(new ContainsEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to contains")), "contains");
        }
        case IN: {
            List<Evaluator<String>> list = new ArrayList<Evaluator<String>>();
            for (int i = 0; i < argEvaluators.size(); i++) {
                list.add(toStringEvaluator(argEvaluators.get(i), i + "th argument to in"));
            }
            return addToken(new InEvaluator(toStringEvaluator(subjectEvaluator), list), "in");
        }
        case FIND: {
            verifyArgCount(argEvaluators, 1, "find");
            return addToken(new FindEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to find")), "find");
        }
        case MATCHES: {
            verifyArgCount(argEvaluators, 1, "matches");
            return addToken(new MatchesEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to matches")), "matches");
        }
        case EQUALS: {
            verifyArgCount(argEvaluators, 1, "equals");
            if (subjectEvaluator.getResultType() == ResultType.BIGDECIMAL) {
                return addToken(new EqualsBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toBigDecimalEvaluator(argEvaluators.get(0))), "equals");
            }
            return addToken(new EqualsEvaluator(subjectEvaluator, argEvaluators.get(0)), "equals");
        }
        case EQUALS_IGNORE_CASE: {
            verifyArgCount(argEvaluators, 1, "equalsIgnoreCase");
            return addToken(new EqualsIgnoreCaseEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to equalsIgnoreCase")), "equalsIgnoreCase");
        }
        case GREATER_THAN: {
            verifyArgCount(argEvaluators, 1, "gt");
            if (subjectEvaluator.getResultType() == ResultType.BIGDECIMAL) {
                return addToken(new GreateThanBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toBigDecimalEvaluator(argEvaluators.get(0))), "gt");
            }
            return addToken(new GreaterThanEvaluator(toNumberEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0), "first argument to gt")), "gt");
        }
        case GREATER_THAN_OR_EQUAL: {
            verifyArgCount(argEvaluators, 1, "ge");
            if (subjectEvaluator.getResultType() == ResultType.BIGDECIMAL) {
                return addToken(new GreateThanOrEqualBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toBigDecimalEvaluator(argEvaluators.get(0))), "ge");
            }
            return addToken(new GreaterThanOrEqualEvaluator(toNumberEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0), "first argument to ge")), "ge");
        }
        case LESS_THAN: {
            verifyArgCount(argEvaluators, 1, "lt");
            if (subjectEvaluator.getResultType() == ResultType.BIGDECIMAL) {
                return addToken(new LessThanBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toBigDecimalEvaluator(argEvaluators.get(0))), "lt");
            }
            return addToken(new LessThanEvaluator(toNumberEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0), "first argument to lt")), "lt");
        }
        case LESS_THAN_OR_EQUAL: {
            verifyArgCount(argEvaluators, 1, "le");
            if (subjectEvaluator.getResultType() == ResultType.BIGDECIMAL) {
                return addToken(new LessThanOrEqualBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toBigDecimalEvaluator(argEvaluators.get(0))), "le");
            }
            return addToken(new LessThanOrEqualEvaluator(toNumberEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0), "first argument to le")), "le");
        }
        case LENGTH: {
            verifyArgCount(argEvaluators, 0, "length");
            return addToken(new LengthEvaluator(toStringEvaluator(subjectEvaluator)), "length");
        }
        case TO_DATE: {
            if (argEvaluators.isEmpty()) {
                return addToken(new NumberToDateEvaluator(toWholeNumberEvaluator(subjectEvaluator)), "toDate");
            } else if (subjectEvaluator.getResultType() == ResultType.STRING && argEvaluators.size() == 1) {
                return addToken(new StringToDateEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0)), null), "toDate");
            } else if (subjectEvaluator.getResultType() == ResultType.STRING && argEvaluators.size() == 2) {
                return addToken(new StringToDateEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0)), toStringEvaluator(argEvaluators.get(1))), "toDate");
            } else {
                return addToken(new NumberToDateEvaluator(toWholeNumberEvaluator(subjectEvaluator)), "toDate");
            }
        }
        case TO_NUMBER: {
            verifyArgCount(argEvaluators, 0, "toNumber");
            switch (subjectEvaluator.getResultType()) {
            case STRING:
            case WHOLE_NUMBER:
            case DECIMAL:
            case NUMBER:
            case DATE:
                return addToken(toWholeNumberEvaluator(subjectEvaluator), "toNumber");
            default:
                throw new AttributeExpressionLanguageParsingException(
                        subjectEvaluator + " returns type " + subjectEvaluator.getResultType() + " but expected to get " + ResultType.STRING + ", " + ResultType.DECIMAL + ", or " + ResultType.DATE);
            }
        }
        case TO_DECIMAL: {
            verifyArgCount(argEvaluators, 0, "toDecimal");
            switch (subjectEvaluator.getResultType()) {
            case WHOLE_NUMBER:
            case DECIMAL:
            case STRING:
            case NUMBER:
            case DATE:
                return addToken(toDecimalEvaluator(subjectEvaluator), "toDecimal");
            default:
                throw new AttributeExpressionLanguageParsingException(subjectEvaluator + " returns type " + subjectEvaluator.getResultType() + " but expected to get " + ResultType.STRING + ", "
                        + ResultType.WHOLE_NUMBER + ", or " + ResultType.DATE);
            }
        }
        case TO_BIGDECIMAL: {
            verifyArgCount(argEvaluators, 0, "toBigDecimal");
            return addToken(new ToBigDecimalEvaluator(subjectEvaluator), "toBigDecimal");
        }
        case TO_PERCENT: {
            verifyArgCount(argEvaluators, 0, "toPercent");
            return addToken(new ToPercentBigDecimalEvaluator(subjectEvaluator), "toPercent");
        }
        case BIGDECIMAL_FLOATVALUE: {
            verifyArgCount(argEvaluators, 0, "floatValue");
            return addToken(new FloatValueBigDecimalEvaluator(subjectEvaluator), "floatValue");
        }
        case BIGDECIMAL_INTVALUE: {
            verifyArgCount(argEvaluators, 0, "intValue");
            return addToken(new IntValueBigDecimalEvaluator(subjectEvaluator), "intValue");
        }
        case BIGDECIMAL_LONGVALUE: {
            verifyArgCount(argEvaluators, 0, "longValue");
            return addToken(new LongValueBigDecimalEvaluator(subjectEvaluator), "longValue");
        }
        case BIGDECIMAL_SHORTVALUE: {
            verifyArgCount(argEvaluators, 0, "shortValue");
            return addToken(new ShortValueBigDecimalEvaluator(subjectEvaluator), "shortValue");
        }
        case BIGDECIMAL_ABS: {
            verifyArgCount(argEvaluators, 0, "abs");
            return addToken(new AbsBigDecimalEvaluator(subjectEvaluator), "abs");
        }
        case BIGDECIMAL_NEGATE: {
            verifyArgCount(argEvaluators, 0, "negate");
            return addToken(new NegateBigDecimalEvaluator(subjectEvaluator), "negate");
        }
        case TO_RADIX: {
            if (argEvaluators.size() == 1) {
                return addToken(new ToRadixEvaluator(toWholeNumberEvaluator(subjectEvaluator), toWholeNumberEvaluator(argEvaluators.get(0))), "toRadix");
            } else {
                return addToken(new ToRadixEvaluator(toWholeNumberEvaluator(subjectEvaluator), toWholeNumberEvaluator(argEvaluators.get(0)), toWholeNumberEvaluator(argEvaluators.get(1))), "toRadix");
            }
        }
        case FROM_RADIX: {
            return addToken(new FromRadixEvaluator(toStringEvaluator(subjectEvaluator), toWholeNumberEvaluator(argEvaluators.get(0))), "fromRadix");
        }
        case MOD: {
            return addToken(new ModEvaluator(toNumberEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0))), "mod");
        }
        case PLUS: {
            if (subjectEvaluator.getResultType() == ResultType.BIGDECIMAL) {
                return addToken(new PlusBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toBigDecimalEvaluator(argEvaluators.get(0))), "plus");
            }
            return addToken(new PlusEvaluator(toNumberEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0))), "plus");
        }
        case MINUS: {
            if (subjectEvaluator.getResultType() == ResultType.BIGDECIMAL) {
                return addToken(new MinusBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toBigDecimalEvaluator(argEvaluators.get(0))), "minus");
            }
            return addToken(new MinusEvaluator(toNumberEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0))), "minus");
        }
        case MULTIPLY: {
            if (subjectEvaluator.getResultType() == ResultType.BIGDECIMAL) {
                return addToken(new MultiplyBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toBigDecimalEvaluator(argEvaluators.get(0))), "multiply");
            }
            return addToken(new MultiplyEvaluator(toNumberEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0))), "multiply");
        }
        case DIVIDE: {
            if (subjectEvaluator.getResultType() == ResultType.BIGDECIMAL) {
                if (argEvaluators.size() == 1) {
                    return addToken(new DivideBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toBigDecimalEvaluator(argEvaluators.get(0))), "divide");
                } else if (argEvaluators.size() == 2) {
                    return addToken(new DivideBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toBigDecimalEvaluator(argEvaluators.get(0)), toNumberEvaluator(argEvaluators.get(1))), "divide");
                } else if (argEvaluators.size() == 3) {
                    return addToken(new DivideBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toBigDecimalEvaluator(argEvaluators.get(0)), toNumberEvaluator(argEvaluators.get(1)),
                            toStringEvaluator(argEvaluators.get(2))), "divide");
                }
            }
            return addToken(new DivideEvaluator(toNumberEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0))), "divide");
        }
        case BIGDECIMAL_SETSCALE: {
            if (argEvaluators.size() == 1) {
                return addToken(new SetScaleBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0))), "setScale");
            } else if (argEvaluators.size() == 2) {
                return addToken(new SetScaleBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0)), toStringEvaluator(argEvaluators.get(1))), "setScale");
            }

        }
        case BIGDECIMAL_COMPARETO: {
            return addToken(new CompareToBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toBigDecimalEvaluator(argEvaluators.get(0))), "compareTo");
        }
        case BIGDECIMAL_MIN: {
            return addToken(new MinBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toBigDecimalEvaluator(argEvaluators.get(0))), "min");
        }
        case BIGDECIMAL_MAX: {
            return addToken(new MaxBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toBigDecimalEvaluator(argEvaluators.get(0))), "max");
        }
        case BIGDECIMAL_MOVELEFT: {
            return addToken(new MovePointLeftBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "moveLeft");
        }
        case BIGDECIMAL_MOVERIGHT: {
            return addToken(new MovePointRightBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "moveRight");
        }
        case BIGDECIMAL_POW: {
            return addToken(new PowBigDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "pow");
        }
        case BIGDECIMAL_POWOFTEN: {
            return addToken(new ScaleByPowerOfTenDemEvaluator(toBigDecimalEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "powOfTen");
        }
        case MATH: {
            if (argEvaluators.size() == 1) {
                return addToken(new MathEvaluator(toNumberEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0)), null), "math");
            } else if (argEvaluators.size() == 2) {
                return addToken(new MathEvaluator(toNumberEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0)), toNumberEvaluator(argEvaluators.get(1))), "math");
            } else {
                throw new AttributeExpressionLanguageParsingException("math() function takes 1 or 2 arguments");
            }
        }
        case RANDOM: {
            return addToken(new RandomNumberGeneratorEvaluator(), "random");
        }
        case INDEX_OF: {
            verifyArgCount(argEvaluators, 1, "indexOf");
            return addToken(new IndexOfEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to indexOf")), "indexOf");
        }
        case LAST_INDEX_OF: {
            verifyArgCount(argEvaluators, 1, "lastIndexOf");
            return addToken(new LastIndexOfEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to lastIndexOf")), "lastIndexOf");
        }
        case FORMAT: {
            if (argEvaluators.size() == 1) {
                return addToken(new FormatEvaluator(toDateEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument of format"), null), "format");
            } else if (argEvaluators.size() == 2) {
                return addToken(new FormatEvaluator(toDateEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0)), toStringEvaluator(argEvaluators.get(1))), "format");
            } else {
                throw new AttributeExpressionLanguageParsingException("format() function takes 1 or 2 arguments");
            }
        }
        case OR: {
            return addToken(new OrEvaluator(toBooleanEvaluator(subjectEvaluator), toBooleanEvaluator(argEvaluators.get(0))), "or");
        }
        case AND: {
            return addToken(new AndEvaluator(toBooleanEvaluator(subjectEvaluator), toBooleanEvaluator(argEvaluators.get(0))), "and");
        }
        case NOT: {
            return addToken(new NotEvaluator(toBooleanEvaluator(subjectEvaluator)), "not");
        }
        case GET_DELIMITED_FIELD: {
            if (argEvaluators.size() == 1) {
                // Only a single argument - the index to return.
                return addToken(new GetDelimitedFieldEvaluator(toStringEvaluator(subjectEvaluator), toWholeNumberEvaluator(argEvaluators.get(0), "first argument of getDelimitedField")),
                        "getDelimitedField");
            } else if (argEvaluators.size() == 2) {
                // two arguments - index and delimiter.
                return addToken(new GetDelimitedFieldEvaluator(toStringEvaluator(subjectEvaluator), toWholeNumberEvaluator(argEvaluators.get(0), "first argument of getDelimitedField"),
                        toStringEvaluator(argEvaluators.get(1), "second argument of getDelimitedField")), "getDelimitedField");
            } else if (argEvaluators.size() == 3) {
                // 3 arguments - index, delimiter, quote char.
                return addToken(
                        new GetDelimitedFieldEvaluator(toStringEvaluator(subjectEvaluator), toWholeNumberEvaluator(argEvaluators.get(0), "first argument of getDelimitedField"),
                                toStringEvaluator(argEvaluators.get(1), "second argument of getDelimitedField"), toStringEvaluator(argEvaluators.get(2), "third argument of getDelimitedField")),
                        "getDelimitedField");
            } else if (argEvaluators.size() == 4) {
                // 4 arguments - index, delimiter, quote char, escape char
                return addToken(new GetDelimitedFieldEvaluator(toStringEvaluator(subjectEvaluator), toWholeNumberEvaluator(argEvaluators.get(0), "first argument of getDelimitedField"),
                        toStringEvaluator(argEvaluators.get(1), "second argument of getDelimitedField"), toStringEvaluator(argEvaluators.get(2), "third argument of getDelimitedField"),
                        toStringEvaluator(argEvaluators.get(3), "fourth argument of getDelimitedField")), "getDelimitedField");
            } else {
                // 5 arguments - index, delimiter, quote char, escape char, strip escape/quote chars flag
                return addToken(
                        new GetDelimitedFieldEvaluator(toStringEvaluator(subjectEvaluator), toWholeNumberEvaluator(argEvaluators.get(0), "first argument of getDelimitedField"),
                                toStringEvaluator(argEvaluators.get(1), "second argument of getDelimitedField"), toStringEvaluator(argEvaluators.get(2), "third argument of getDelimitedField"),
                                toStringEvaluator(argEvaluators.get(3), "fourth argument of getDelimitedField"), toBooleanEvaluator(argEvaluators.get(4), "fifth argument of getDelimitedField")),
                        "getDelimitedField");
            }
        }
        case JSON_PATH: {
            verifyArgCount(argEvaluators, 1, "jsonPath");
            return addToken(new JsonPathEvaluator(toStringEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "first argument to jsonPath")), "jsonPath");
        }
        case IF_ELSE: {
            verifyArgCount(argEvaluators, 2, "ifElse");
            return addToken(new IfElseEvaluator(toBooleanEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0), "argument to return if true"),
                    toStringEvaluator(argEvaluators.get(1), "argument to return if false")), "ifElse");
        }

        // date
        case ADD_YEARS: {
            verifyArgCount(argEvaluators, 1, "addYears");
            return addToken(new AddYearsEvaluator(toDateEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0))), "addYears");
        }
        case ADD_MONTHS: {
            verifyArgCount(argEvaluators, 1, "addMonths");
            return addToken(new AddMonthsEvaluator(toDateEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0))), "addMonths");
        }
        case ADD_WEEKS: {
            verifyArgCount(argEvaluators, 1, "addWeeks");
            return addToken(new AddWeeksEvaluator(toDateEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0))), "addWeeks");
        }
        case ADD_DAYS: {
            verifyArgCount(argEvaluators, 1, "addDays");
            return addToken(new AddDaysEvaluator(toDateEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0))), "addDays");
        }
        case ADD_HOURS: {
            verifyArgCount(argEvaluators, 1, "addHours");
            return addToken(new AddHoursEvaluator(toDateEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0))), "addHours");
        }
        case ADD_MINS: {
            verifyArgCount(argEvaluators, 1, "addMinutes");
            return addToken(new AddMinsEvaluator(toDateEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0))), "addMinutes");
        }
        case ADD_SECS: {
            verifyArgCount(argEvaluators, 1, "addSeconds");
            return addToken(new AddSecsEvaluator(toDateEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0))), "addSeconds");
        }
        case GET_YEAR: {
            if (argEvaluators.isEmpty()) {
                return addToken(new GetYearEvaluator(toDateEvaluator(subjectEvaluator), null), "getYear");
            }
            return addToken(new GetYearEvaluator(toDateEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "getYear");

        }
        case GET_MONTH: {
            if (argEvaluators.isEmpty()) {
                return addToken(new GetMonthEvaluator(toDateEvaluator(subjectEvaluator), null), "getMonth");
            }
            return addToken(new GetMonthEvaluator(toDateEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "getMonth");
        }
        case GET_DAY_OF_YEAR: {
            if (argEvaluators.isEmpty()) {
                return addToken(new GetDayOfYearEvaluator(toDateEvaluator(subjectEvaluator), null), "getDayOfYear");
            }
            return addToken(new GetDayOfYearEvaluator(toDateEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "getDayOfYear");
        }
        case GET_DAY_OF_MONTH: {
            if (argEvaluators.isEmpty()) {
                return addToken(new GetDayOfMonthEvaluator(toDateEvaluator(subjectEvaluator), null), "getDayOfMonth");
            }
            return addToken(new GetDayOfMonthEvaluator(toDateEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "getDayOfMonth");
        }
        case GET_DAY_OF_WEEK: {
            if (argEvaluators.isEmpty()) {
                return addToken(new GetDayOfWeekEvaluator(toDateEvaluator(subjectEvaluator), null), "getDayOfWeek");
            }
            return addToken(new GetDayOfWeekEvaluator(toDateEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "getDayOfWeek");
        }
        case GET_DAY_OF_WEEK_IN_MONTH: {
            if (argEvaluators.isEmpty()) {
                return addToken(new GetDayOfWeekInMonthEvaluator(toDateEvaluator(subjectEvaluator), null), "getDayOfWeekInMonth");
            }
            return addToken(new GetDayOfWeekInMonthEvaluator(toDateEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "getDayOfWeekInMonth");
        }
        case GET_WEEK_OF_MONTH: {
            if (argEvaluators.isEmpty()) {
                return addToken(new GetWeekOfMonthEvaluator(toDateEvaluator(subjectEvaluator), null), "getWeekOfMonth");
            }
            return addToken(new GetWeekOfMonthEvaluator(toDateEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "getWeekOfMonth");
        }
        case GET_WEEK_OF_YEAR: {
            if (argEvaluators.isEmpty()) {
                return addToken(new GetWeekOfYearEvaluator(toDateEvaluator(subjectEvaluator), null), "getWeekOfYear");
            }
            return addToken(new GetWeekOfYearEvaluator(toDateEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "getWeekOfYear");
        }
        case GET_HOUR: {
            if (argEvaluators.isEmpty()) {
                return addToken(new GetHourEvaluator(toDateEvaluator(subjectEvaluator), null), "getHour");
            }
            return addToken(new GetHourEvaluator(toDateEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "getHour");
        }
        case GET_HOUR_OF_DAY: {
            if (argEvaluators.isEmpty()) {
                return addToken(new GetHourOfDayEvaluator(toDateEvaluator(subjectEvaluator), null), "getHourOfDay");
            }
            return addToken(new GetHourOfDayEvaluator(toDateEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "getHourOfDay");
        }
        case GET_MINUTE: {
            if (argEvaluators.isEmpty()) {
                return addToken(new GetMinuteEvaluator(toDateEvaluator(subjectEvaluator), null), "getMinute");
            }
            return addToken(new GetMinuteEvaluator(toDateEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "getMinute");
        }
        case GET_SECOND: {
            if (argEvaluators.isEmpty()) {
                return addToken(new GetSecondEvaluator(toDateEvaluator(subjectEvaluator), null), "getSecond");
            }
            return addToken(new GetSecondEvaluator(toDateEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "getSecond");
        }
        case GET_MILLISECOND: {
            if (argEvaluators.isEmpty()) {
                return addToken(new GetMilliSecondEvaluator(toDateEvaluator(subjectEvaluator), null), "getMilliSecond");
            }
            return addToken(new GetMilliSecondEvaluator(toDateEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "getMilliSecond");
        }
        case AFTER: {
            verifyArgCount(argEvaluators, 1, "after");
            return addToken(new DateAfterEvaluator(toDateEvaluator(subjectEvaluator), toDateEvaluator(argEvaluators.get(0))), "after");
        }
        case BEFORE: {
            verifyArgCount(argEvaluators, 1, "before");
            return addToken(new DateBeforeEvaluator(toDateEvaluator(subjectEvaluator), toDateEvaluator(argEvaluators.get(0))), "before");
        }
        case GET_DATE_FOR_WEEK: {
            verifyArgCount(argEvaluators, 2, "getDateForWeek");
            return addToken(new GetDateForWeekEvaluator(toDateEvaluator(subjectEvaluator), toNumberEvaluator(argEvaluators.get(0)), toNumberEvaluator(argEvaluators.get(1))), "getDateForWeek");
        }
        case IS_WEEKEND: {
            verifyArgCount(argEvaluators, 0, "isWeekend");
            return addToken(new IsWeekendEvaluator(toDateEvaluator(subjectEvaluator)), "isWeekend");
        }
        case GET_QUARTER: {
            if (argEvaluators.isEmpty()) {
                return addToken(new GetQuarterEvaluator(toDateEvaluator(subjectEvaluator), null), "getQuarter");
            }
            return addToken(new GetQuarterEvaluator(toDateEvaluator(subjectEvaluator), toStringEvaluator(argEvaluators.get(0))), "getQuarter");
        }
        default:
            throw new AttributeExpressionLanguageParsingException("Expected a Function-type expression but got " + tree.toString());
        }
    }

    public Evaluator<?> buildEvaluator(final Tree tree) {
        switch (tree.getType()) {
        case EXPRESSION: {
            return buildExpressionEvaluator(tree);
        }
        case ATTRIBUTE_REFERENCE: {
            final Evaluator<?> childEvaluator = buildEvaluator(tree.getChild(0));
            if (childEvaluator instanceof MultiAttributeEvaluator) {
                return childEvaluator;
            }
            final AttributeEvaluator eval = new AttributeEvaluator(toStringEvaluator(childEvaluator));
            evaluators.add(eval);
            return eval;
        }
        case MULTI_ATTRIBUTE_REFERENCE: {

            final Tree functionTypeTree = tree.getChild(0);
            final int multiAttrType = functionTypeTree.getType();
            if (multiAttrType == ANY_DELINEATED_VALUE || multiAttrType == ALL_DELINEATED_VALUES) {
                final Evaluator<String> delineatedValueEvaluator = toStringEvaluator(buildEvaluator(tree.getChild(1)));
                final Evaluator<String> delimiterEvaluator = toStringEvaluator(buildEvaluator(tree.getChild(2)));

                final String token = (multiAttrType == ANY_DELINEATED_VALUE) ? "anyDelineatedValue" : "allDelineatedValues";
                return addToken(new DelineatedAttributeEvaluator(delineatedValueEvaluator, delimiterEvaluator, multiAttrType), token);
            }

            final List<String> attributeNames = new ArrayList<>();
            for (int i = 1; i < tree.getChildCount(); i++) { // skip the first child because that's the name of the multi-attribute function
                attributeNames.add(newStringLiteralEvaluator(tree.getChild(i).getText()).evaluate(null).getValue());
            }

            switch (multiAttrType) {
            case ALL_ATTRIBUTES:
                for (final String attributeName : attributeNames) {
                    try {
                        FlowFile.KeyValidator.validateKey(attributeName);
                    } catch (final IllegalArgumentException iae) {
                        throw new AttributeExpressionLanguageParsingException("Invalid Attribute Name: " + attributeName + ". " + iae.getMessage());
                    }
                }

                return addToken(new MultiNamedAttributeEvaluator(attributeNames, ALL_ATTRIBUTES), "allAttributes");
            case ALL_MATCHING_ATTRIBUTES:
                return addToken(new MultiMatchAttributeEvaluator(attributeNames, ALL_MATCHING_ATTRIBUTES), "allMatchingAttributes");
            case ANY_ATTRIBUTE:
                for (final String attributeName : attributeNames) {
                    try {
                        FlowFile.KeyValidator.validateKey(attributeName);
                    } catch (final IllegalArgumentException iae) {
                        throw new AttributeExpressionLanguageParsingException("Invalid Attribute Name: " + attributeName + ". " + iae.getMessage());
                    }
                }

                return addToken(new MultiNamedAttributeEvaluator(attributeNames, ANY_ATTRIBUTE), "anyAttribute");
            case ANY_MATCHING_ATTRIBUTE:
                return addToken(new MultiMatchAttributeEvaluator(attributeNames, ANY_MATCHING_ATTRIBUTE), "anyMatchingAttribute");
            default:
                throw new AssertionError("Illegal Multi-Attribute Reference: " + functionTypeTree.toString());
            }
        }
        case ATTR_NAME: {
            return newStringLiteralEvaluator(tree.getChild(0).getText());
        }
        case WHOLE_NUMBER: {
            return addToken(new WholeNumberLiteralEvaluator(tree.getText()), "wholeNumber");
        }
        case STRING_LITERAL: {
            return newStringLiteralEvaluator(tree.getText());
        }
        case DECIMAL: {
            return addToken(new DecimalLiteralEvaluator(tree.getText()), "decimal");
        }
        case TRUE:
        case FALSE:
            return buildBooleanEvaluator(tree);
        case UUID: {
            return addToken(new UuidEvaluator(), "uuid");
        }
        case NOW: {
            return addToken(new NowEvaluator(), "now");
        }
        case TO_LITERAL: {
            final Evaluator<?> argEvaluator = buildEvaluator(tree.getChild(0));
            return addToken(new ToLiteralEvaluator(argEvaluator), "toLiteral");
        }
        case IP: {
            try {
                return addToken(new IPEvaluator(), "ip");
            } catch (final UnknownHostException e) {
                throw new AttributeExpressionLanguageException(e);
            }
        }
        case HOSTNAME: {
            if (tree.getChildCount() == 0) {
                try {
                    return addToken(new HostnameEvaluator(false), "hostname");
                } catch (final UnknownHostException e) {
                    throw new AttributeExpressionLanguageException(e);
                }
            } else if (tree.getChildCount() == 1) {
                final Tree childTree = tree.getChild(0);
                try {
                    switch (childTree.getType()) {
                    case TRUE:
                        return addToken(new HostnameEvaluator(true), "hostname");
                    case FALSE:
                        return addToken(new HostnameEvaluator(false), "hostname");
                    default:
                        throw new AttributeExpressionLanguageParsingException("Call to hostname() must take 0 or 1 (boolean) parameter");
                    }
                } catch (final UnknownHostException e) {
                    throw new AttributeExpressionLanguageException(e);
                }
            } else {
                throw new AttributeExpressionLanguageParsingException("Call to hostname() must take 0 or 1 (boolean) parameter");
            }
        }
        case NEXT_INT: {
            return addToken(new OneUpSequenceEvaluator(), "nextInt");
        }
        case RANDOM: {
            return addToken(new RandomNumberGeneratorEvaluator(), "random");
        }
        case MATH: {
            if (tree.getChildCount() == 1) {
                return addToken(new MathEvaluator(null, toStringEvaluator(buildEvaluator(tree.getChild(0))), null), "math");
            } else {
                throw new AttributeExpressionLanguageParsingException("Call to math() as the subject must take exactly 1 parameter");
            }
        }
        case GET_STATE_VALUE: {
            final Tree childTree = tree.getChild(0);
            final Evaluator<?> argEvaluator = buildEvaluator(childTree);
            final Evaluator<String> stringEvaluator = toStringEvaluator(argEvaluator);
            final GetStateVariableEvaluator eval = new GetStateVariableEvaluator(stringEvaluator);
            evaluators.add(eval);
            return eval;
        }
        default:
            throw new AttributeExpressionLanguageParsingException("Unexpected token: " + tree.toString());
        }
    }

    private <T> Evaluator<T> addToken(final Evaluator<T> evaluator, final String token) {
        evaluator.setToken(token);
        evaluators.add(evaluator);
        return evaluator;
    }

    private Evaluator<String> newStringLiteralEvaluator(final String literalValue) {
        if (literalValue == null || literalValue.length() < 2) {
            return addToken(new StringLiteralEvaluator(literalValue), literalValue);
        }

        final List<Range> ranges = Query.extractExpressionRanges(literalValue);
        if (ranges.isEmpty()) {
            return addToken(new StringLiteralEvaluator(literalValue), literalValue);
        }

        final List<Evaluator<?>> evaluators = new ArrayList<>();

        int lastIndex = 0;
        for (final Range range : ranges) {
            if (range.getStart() > lastIndex) {
                evaluators.add(newStringLiteralEvaluator(literalValue.substring(lastIndex, range.getStart())));
            }

            final String treeText = literalValue.substring(range.getStart(), range.getEnd() + 1);
            evaluators.add(buildEvaluator(compileTree(treeText)));
            lastIndex = range.getEnd() + 1;
        }

        final Range lastRange = ranges.get(ranges.size() - 1);
        if (lastRange.getEnd() + 1 < literalValue.length()) {
            final String treeText = literalValue.substring(lastRange.getEnd() + 1);
            evaluators.add(newStringLiteralEvaluator(treeText));
        }

        if (evaluators.size() == 1) {
            return toStringEvaluator(evaluators.get(0));
        }

        Evaluator<String> lastEvaluator = toStringEvaluator(evaluators.get(0));
        for (int i = 1; i < evaluators.size(); i++) {
            lastEvaluator = new AppendEvaluator(lastEvaluator, toStringEvaluator(evaluators.get(i)));
        }

        this.evaluators.addAll(evaluators);
        return lastEvaluator;
    }

    private Evaluator<Boolean> buildBooleanEvaluator(final Tree tree) {
        switch (tree.getType()) {
        case TRUE:
            return addToken(new BooleanLiteralEvaluator(true), "true");
        case FALSE:
            return addToken(new BooleanLiteralEvaluator(false), "true");
        }
        throw new AttributeExpressionLanguageParsingException("Cannot build Boolean evaluator from tree " + tree.toString());
    }

}
