package org.edgegallery.developer.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;

class Signature {
    Optional<byte[]> signMessage(String srcMsg, String charSet, String certPath, String certPwd) {
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
                    ContentSigner sha1Signer = (new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC")
                        .build(privateKey));
                    cmsSignedDataGenerator.addSignerInfoGenerator((new JcaSignerInfoGeneratorBuilder(
                        (new JcaDigestCalculatorProviderBuilder()).setProvider("BC").build()))
                        .build(sha1Signer, cerx509));
                    cmsSignedDataGenerator.addCertificates(certs);
                    CMSSignedData sigData = cmsSignedDataGenerator.generate(msg, true);
                    fileInputStream.close();
                    return Optional.of(Base64.encode(sigData.getEncoded()));
                }
            }
            return Optional.empty();
        } catch (KeyStoreException | UnrecoverableKeyException
            | NoSuchAlgorithmException | IOException
            | CertificateException | OperatorCreationException | CMSException e) {
            return Optional.empty();
        }
    }

    Signature() {
        Security.addProvider(new BouncyCastleProvider());
    }
}

