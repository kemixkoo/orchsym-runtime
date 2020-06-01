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
package org.apache.nifi.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.nifi.connectable.Position;
import org.junit.Assert;
import org.junit.Test;

public class PositionCalcUtilTest {

    @Test
    public void test_first() {
        final Position availablePosition = PositionCalcUtil.newAvailablePosition(Collections.emptyList());
        Assert.assertEquals(0, new Double(availablePosition.getX()).intValue());
        Assert.assertEquals(0, new Double(availablePosition.getY()).intValue());
    }

    @Test
    public void test_firstLine() {
        final List<Position> list = new ArrayList<>();

        Position next = null;
        for (int i = 0; i < 5; i++) {
            next = PositionCalcUtil.nextRight(next);
            list.add(next);
        }

        final Position availablePosition = PositionCalcUtil.newAvailablePosition(list);

        // (6*STEP_X, 0)
        Assert.assertEquals(5 * PositionCalcUtil.STEP_X, new Double(availablePosition.getX()).intValue());
        Assert.assertEquals(0, new Double(availablePosition.getY()).intValue());
    }

    @Test
    public void test_lastOneInLine() {
        final List<Position> list = new ArrayList<>();

        Position next = null;
        for (int i = 0; i < PositionCalcUtil.SIZE_IN_LINE - 1; i++) {
            next = PositionCalcUtil.nextRight(next);
            list.add(next);
        }

        final Position availablePosition = PositionCalcUtil.newAvailablePosition(list);

        // (SIZE_IN_LINE*STEP_X, 0) //in line 1
        Assert.assertEquals((PositionCalcUtil.SIZE_IN_LINE - 1) * PositionCalcUtil.STEP_X, new Double(availablePosition.getX()).intValue());
        Assert.assertEquals(0, new Double(availablePosition.getY()).intValue());
    }

    @Test
    public void test_newLine() {
        final List<Position> list = new ArrayList<>();

        Position next = null;
        for (int i = 0; i < PositionCalcUtil.SIZE_IN_LINE; i++) {
            next = PositionCalcUtil.nextRight(next);
            list.add(next);
        }

        final Position availablePosition = PositionCalcUtil.newAvailablePosition(list);

        // (0, STEP_Y) //in line 2
        Assert.assertEquals(0, new Double(availablePosition.getX()).intValue());
        Assert.assertEquals(1 * PositionCalcUtil.STEP_Y, new Double(availablePosition.getY()).intValue()); // new line
    }

    @Test
    public void test_newLineWithMulti() {
        final List<Position> list = new ArrayList<>();

        Position next = new Position(30.0, PositionCalcUtil.STEP_Y.doubleValue() + 40);

        // line 2
        for (int i = 0; i < PositionCalcUtil.SIZE_IN_LINE; i++) {
            next = PositionCalcUtil.nextRight(next);
            list.add(next);
        }

        // line 1
        next = null;
        for (int i = 0; i < PositionCalcUtil.SIZE_IN_LINE; i++) {
            next = PositionCalcUtil.nextRight(next);
            list.add(next);
        }

        final Position availablePosition = PositionCalcUtil.newAvailablePosition(list);

        // (0, 2*STEP_Y) in line 3
        Assert.assertEquals(0, new Double(availablePosition.getX()).intValue());
        Assert.assertEquals(2 * PositionCalcUtil.STEP_Y, new Double(availablePosition.getY()).intValue()); // new line
    }

    @Test
    public void test_anyWithMulti() {
        final List<Position> list = new ArrayList<>();

        Position next = new Position(0.0, PositionCalcUtil.STEP_Y.doubleValue() + 40);

        // line 2
        list.add(next);

        for (int i = 0; i < 7; i++) {
            next = PositionCalcUtil.nextRight(next);
            list.add(next);
        }

        // line 1
        next = null;
        for (int i = 0; i < PositionCalcUtil.SIZE_IN_LINE; i++) {
            next = PositionCalcUtil.nextRight(next);
            list.add(next);
        }

        final Position availablePosition = PositionCalcUtil.newAvailablePosition(list);

        // (8*STEP_X, STEP_Y) in line 2
        Assert.assertEquals((7 + 1) * PositionCalcUtil.STEP_X, new Double(availablePosition.getX()).intValue());
        Assert.assertEquals(1 * PositionCalcUtil.STEP_Y, new Double(availablePosition.getY()).intValue()); // new line
    }
}
