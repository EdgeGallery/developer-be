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

package org.edgegallery.developer.service.apppackage.csar.signature;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Signature {
    public Optional<byte[]> signMessage(String srcMsg, String charSet, String certPath, String certPwd) {
        String privateKeyName = null;
        char[] passphrase = certPwd.toCharArray();
        String ksType = "PKCS12";
        try (FileInputStream fileInputStream = new FileInputStream(certPath)) {
            Provider provider = new BouncyCastleProvider();
            Security.addProvider(provider);
            KeyStore keyStore = KeyStore.getInstance(ksType);
            keyStore.load(fileInputStream, passphrase);
            if (keyStore.aliases().hasMoreElements()) {
                privateKeyName = keyStore.aliases().nextElement();
            }
            Certificate cert = keyStore.getCertificate(privateKeyName);
            if (keyStore.getKey(privateKeyName, passphrase) instanceof PrivateKey) {
                PrivateKey privateKey = (PrivateKey) keyStore.getKey(privateKeyName, passphrase);
                if (cert instanceof X509Certificate) {
                    X509Certificate cerx509 = (X509Certificate) cert;
                    List<Certificate> certList = new ArrayList<>();
                    certList.add(cerx509);
                    CMSTypedData msg = new CMSProcessableByteArray(srcMsg.getBytes(charSet));
                    Store certs = new JcaCertStore(certList);
                    CMSSignedDataGenerator cmsSignedDataGenerator = new CMSSignedDataGenerator();
                    ContentSigner sha1Signer = (new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(privateKey));
                    cmsSignedDataGenerator.addSignerInfoGenerator((new JcaSignerInfoGeneratorBuilder((new JcaDigestCalculatorProviderBuilder()).setProvider("BC").build())).build(sha1Signer, cerx509));
                    cmsSignedDataGenerator.addCertificates(certs);
                    CMSSignedData sigData = cmsSignedDataGenerator.generate(msg,true);
                    fileInputStream.close();
                    return Optional.of(Base64.encode(sigData.getEncoded()));
                }
            }
            return Optional.empty();
        } catch (KeyStoreException
                | UnrecoverableKeyException
                | NoSuchAlgorithmException
                | IOException
                | CertificateException
                | OperatorCreationException
                | CMSException e) {
            return Optional.empty();
        }
    }
    public Signature(){
        Security.addProvider(new BouncyCastleProvider());
    }
}

