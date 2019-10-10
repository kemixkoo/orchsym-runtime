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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * @author Kemix Koo
 *
 */
public final class SensitiveUtil {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private static final String ALGO_JSON = "PBEWithMD5AndDES";

    private static final int ITER_COUNT = 100;

    private static final String SALT_STR = "Orchsym!";
    private static final byte[] SALT = SALT_STR.getBytes(UTF_8);

    private static final String PASS_STR = "https://www.baishan.com";
    private static final char[] PASSPHASE = PASS_STR.toCharArray();

    public static String encrypt(String rawValue) throws GeneralSecurityException {
        if (null == rawValue || rawValue.isEmpty()) {
            return ""; // no need for empty
        }
        final SecretKey secretKey = createKey();
        final Cipher ecipher = Cipher.getInstance(ALGO_JSON);

        ecipher.init(Cipher.ENCRYPT_MODE, secretKey, new PBEParameterSpec(SALT, ITER_COUNT));

        final byte[] enValue = ecipher.doFinal(rawValue.getBytes(UTF_8));

        final byte[] encode = Base64.getEncoder().encode(enValue);
        return new String(encode, UTF_8);
    }

    private static SecretKey createKey() throws GeneralSecurityException {
        final SecretKey secretKey = SecretKeyFactory.getInstance(ALGO_JSON).generateSecret(new PBEKeySpec(PASSPHASE, SALT, ITER_COUNT));
        return secretKey;
    }

    public static String decrypt(String enValue) throws GeneralSecurityException {
        if (null == enValue || enValue.isEmpty()) {
            return ""; // no need for empty
        }
        final SecretKey secretKey = createKey();
        final Cipher dcipher = Cipher.getInstance(ALGO_JSON);

        dcipher.init(Cipher.DECRYPT_MODE, secretKey, new PBEParameterSpec(SALT, ITER_COUNT));

        final byte[] decode = Base64.getDecoder().decode(enValue.getBytes(UTF_8));
        final byte[] deValue = dcipher.doFinal(decode);

        return new String(deValue, UTF_8);
    }
}
