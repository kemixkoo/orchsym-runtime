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
package org.apache.nifi.web.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

import net.sourceforge.pinyin4j.PinyinHelper;

/**
 * @author GU Guoqiang
 *
 */
public class ChinesePinyinUtil {
    /**
     * 根据Unicode编码判断中文汉字和符号
     */
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS //
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS //
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A//
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B //
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION//
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS //
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {//
            return true;
        }
        return false;
    }

    public static Comparator<String> zhComparator = new Comparator<String>() {
        @Override
        public int compare(String name1, String name2) {
            // check the first
            final int first = compare(name1, name2, 0);
            if (0 != first) {
                return first;
            }
            // check the second
            final int second = compare(name1, name2, 1);
            if (0 != second) {
                return second;
            }
            // check the third
            final int third = compare(name1, name2, 2);
            if (0 != third) {
                return third;
            }
            // others, check name directly
            return name1.compareToIgnoreCase(name2);
        }

        int compare(String name1, String name2, int charIndex) {
            if (name1.length() > charIndex && name2.length() > charIndex) {
                final char char1 = name1.charAt(charIndex);
                final char char2 = name2.charAt(charIndex);
                int first = compare(char1, char2);
                if (first != 0) {
                    return first;
                }
            }
            return 0; // for next
        }

        int compare(char c1, char c2) {
            String c1Str = String.valueOf(c1);
            if (isChinese(c1)) {
                final String[] c1Py = PinyinHelper.toHanyuPinyinStringArray(c1);
                if (!Objects.isNull(c1Py) && c1Py.length > 0) {
                    c1Str = Arrays.stream(c1Py).collect(Collectors.joining(""));
                }
            }
            String c2Str = String.valueOf(c2);
            if (isChinese(c2)) {
                final String[] c2Py = PinyinHelper.toHanyuPinyinStringArray(c2);
                if (!Objects.isNull(c2Py) && c2Py.length > 0) {
                    c2Str = Arrays.stream(c2Py).collect(Collectors.joining(""));
                }
            }
            return c1Str.compareToIgnoreCase(c2Str);
        }
    };
}
