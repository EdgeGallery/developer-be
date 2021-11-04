/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.security;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeyTemplates;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class TestCrypto {

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        AeadConfig.register();
        KeysetHandle keysetHandle = KeysetHandle.generateNew(KeyTemplates.get("AES128_GCM"));

        String keysetFilename = "my_keyset.json";
        CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withFile(new File(keysetFilename)));
        Aead aead = keysetHandle.getPrimitive(Aead.class);
        String text = "qw123";
        byte[] ciphertext = aead.encrypt(text.getBytes(), "123".getBytes());
        System.out.println(new String(ciphertext));

        byte[] decrypted = aead.decrypt(ciphertext, "123".getBytes());
        System.out.println(new String(decrypted));
    }

}
