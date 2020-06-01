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
package com.orchsym.util;

import java.security.GeneralSecurityException;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author GU Guoqiang
 *
 */
public class SensitiveUtilTest {
    @Test
    public void test_encrypt_empty() throws GeneralSecurityException {
        String encrypt = SensitiveUtil.encrypt(null);
        Assert.assertEquals("", encrypt);

        encrypt = SensitiveUtil.encrypt("");
        Assert.assertEquals("", encrypt);
    }

    @Test
    public void test_decrypt_empty() throws GeneralSecurityException {
        String decrypt = SensitiveUtil.decrypt(null);
        Assert.assertEquals("", decrypt);

        decrypt = SensitiveUtil.decrypt("");
        Assert.assertEquals("", decrypt);
    }

    @Test
    public void test_encrypt_decrypt() throws GeneralSecurityException {
        doTest(" ", "W2Fqx+yXAEc="); // space
        doTest("  ", "r4FYbm20/cw="); // spaces
        doTest("12345678", "XpeoIJZu5CkmWjT04M9UKw=="); // number
        doTest("!@#$%^&*", "RjECLO/xHhFGDqU3SmhiEA=="); // special
        doTest("ASDFG", "QDTCgvyYS90="); // special
        doTest(";':\"", "hWIKMsxESUI=");
        doTest("<>[]", "GvzmgBNKD0o=");
    }

    private void doTest(String rawValue, String enValue) throws GeneralSecurityException {
        String encrypt = SensitiveUtil.encrypt(rawValue);
        Assert.assertEquals(enValue, encrypt);

        String decrypt = SensitiveUtil.decrypt(encrypt);
        Assert.assertEquals(rawValue, decrypt);
    }

}
