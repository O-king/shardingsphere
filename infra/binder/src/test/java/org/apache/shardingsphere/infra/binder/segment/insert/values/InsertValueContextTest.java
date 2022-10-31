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

package org.apache.shardingsphere.infra.binder.segment.insert.values;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class InsertValueContextTest {
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertInstanceConstructedOk() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Collection<ExpressionSegment> assignments = Collections.emptyList();
        List<Object> parameters = Collections.emptyList();
        int parametersOffset = 0;
        InsertValueContext insertValueContext = new InsertValueContext(assignments, parameters, parametersOffset);
        Method getValueExpressionsMethod = InsertValueContext.class.getDeclaredMethod("getValueExpressions", Collection.class);
        getValueExpressionsMethod.setAccessible(true);
        List<ExpressionSegment> getValueExpressionsResult = (List<ExpressionSegment>) getValueExpressionsMethod.invoke(insertValueContext, new Object[]{assignments});
        assertThat(insertValueContext.getValueExpressions(), is(getValueExpressionsResult));
        Method getParametersMethod = InsertValueContext.class.getDeclaredMethod("getParameters", List.class, int.class);
        getParametersMethod.setAccessible(true);
        List<Object> getParametersResult = (List<Object>) getParametersMethod.invoke(insertValueContext, new Object[]{parameters, parametersOffset});
        assertThat(insertValueContext.getParameters(), is(getParametersResult));
    }
    
    @Test
    public void assertGetLiteralValueWhenParameterMarker() {
        Collection<ExpressionSegment> assignments = makeParameterMarkerExpressionSegment();
        String parameterValue = "test";
        List<Object> parameters = Collections.singletonList(parameterValue);
        int parametersOffset = 0;
        InsertValueContext insertValueContext = new InsertValueContext(assignments, parameters, parametersOffset);
        Object valueFromInsertValueContext = insertValueContext.getLiteralValue(0).get();
        assertThat(valueFromInsertValueContext, is(parameterValue));
    }

    @Test
    public void assertGetLiteralValueWhenParameterisNull() {
        Collection<ExpressionSegment> assignments = makeParameterMarkerExpressionSegment();
        String parameterValue = null;
        List<Object> parameters = Collections.singletonList(parameterValue);
        int parametersOffset = 0;
        InsertValueContext insertValueContext = new InsertValueContext(assignments, parameters, parametersOffset);
        Optional<Object> literalValue = insertValueContext.getLiteralValue(0);
        assertThat(false, is(literalValue.isPresent()));
    }
    
    private Collection<ExpressionSegment> makeParameterMarkerExpressionSegment() {
        return Collections.singleton(new ParameterMarkerExpressionSegment(0, 10, 5));
    }
    
    @Test
    public void assertGetLiteralValueWhenLiteralExpressionSegment() {
        Object literalObject = new Object();
        Collection<ExpressionSegment> assignments = makeLiteralExpressionSegment(literalObject);
        List<Object> parameters = Collections.emptyList();
        InsertValueContext insertValueContext = new InsertValueContext(assignments, parameters, 0);
        Object valueFromInsertValueContext = insertValueContext.getLiteralValue(0).get();
        assertThat(valueFromInsertValueContext, is(literalObject));
    }
    
    private Collection<ExpressionSegment> makeLiteralExpressionSegment(final Object literalObject) {
        return Collections.singleton(new LiteralExpressionSegment(0, 10, literalObject));
    }
    
    @Test
    public void assertGetParameterCount() {
        Collection<ExpressionSegment> expressions = Arrays.asList(
                new LiteralExpressionSegment(0, 10, null),
                new ExpressionProjectionSegment(0, 10, ""),
                new ParameterMarkerExpressionSegment(0, 10, 5),
                new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("")), new ParameterMarkerExpressionSegment(0, 10, 5), "=", ""));
        List<Object> parameters = Arrays.asList("", "");
        InsertValueContext insertValueContext = new InsertValueContext(expressions, parameters, 0);
        assertThat(insertValueContext.getParameterCount(), is(2));
    }
}
