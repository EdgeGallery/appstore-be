/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.appstore.infrastructure.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("Signature")
public class Signature {

    private static final String KEY_TYPE = "PKCS12";

    private static final Logger LOGGER = LoggerFactory.getLogger(Signature.class);

    /**
     * sign message byte[].
     * @param srcMsg the source msg
     * @param certPath the cert path
     * @param certPwd the cert password
     * @return signed message byte[]
     */
    public Optional<byte[]> signMessage(String srcMsg, String certPath, String certPwd) {
        String privateKeyName = null;
        char[] passPhrase = certPwd.toCharArray();
        try (FileInputStream fileInputStream = new FileInputStream(certPath)) {
            BouncyCastleProvider provider = new BouncyCastleProvider();
            Security.addProvider(provider);
            KeyStore keyStore = KeyStore.getInstance(KEY_TYPE);
            keyStore.load(fileInputStream, passPhrase);
            if (keyStore.aliases().hasMoreElements()) {
                privateKeyName = keyStore.aliases().nextElement();
            }

            Optional<byte[]> signedData = getBytes(srcMsg, privateKeyName, passPhrase, keyStore);
            if (signedData.isPresent()) {
                return signedData;
            }
        } catch (IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException
            | UnrecoverableKeyException | OperatorCreationException | CMSException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    private Optional<byte[]> getBytes(String srcMsg, String privateKeyName, char[] passPhrase, KeyStore keyStore)
        throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateEncodingException,
        OperatorCreationException, CMSException, IOException {
        Certificate cert = keyStore.getCertificate(privateKeyName);
        if (keyStore.getKey(privateKeyName, passPhrase) instanceof PrivateKey) {
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(privateKeyName, passPhrase);
            if (cert instanceof X509Certificate) {
                X509Certificate certX509 = (X509Certificate) cert;
                List<Certificate> certList = new ArrayList<>();
                certList.add(certX509);
                CMSTypedData msg = new CMSProcessableByteArray(srcMsg.getBytes(StandardCharsets.UTF_8));
                Store certs = new JcaCertStore(certList);
                CMSSignedDataGenerator cmsSignedDataGenerator = new CMSSignedDataGenerator();
                ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC")
                    .build(privateKey);
                cmsSignedDataGenerator.addSignerInfoGenerator(
                    new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder()
                        .setProvider("BC").build()).build(sha1Signer, certX509));
                cmsSignedDataGenerator.addCertificates(certs);
                CMSSignedData signedData = cmsSignedDataGenerator.generate(msg, true);
                return Optional.of(Base64.encode(signedData.getEncoded()));
            }
        }
        return Optional.empty();
    }

    /**
     * verify the package signatures.
     *
     * @param filePath app package file path
     * @return execute result
     */
    public Boolean signVerify(String filePath) {
        try (ZipFile zipFile = new ZipFile(filePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                // root directory and file is end of mf
                if (entry.getName().split("/").length == 1 && entry.getName().endsWith("mf")) {
                    return validateSignature(zipFile, entry);
                }
            }
        } catch (CMSException e) {
            LOGGER.error("CMS exception, {}", e);
        } catch (IOException e) {
            LOGGER.error("IO exception, {}", e);
        }

        return false;
    }

    /**
     * get signature value from mf file and verify it.
     *
     * @param zipFile zipFile
     * @param entry entry
     * @return if verify success
     * @throws CMSException CMSException
     */
    private Boolean validateSignature(ZipFile zipFile, ZipEntry entry) throws CMSException {
        StringBuilder signData = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8))) {
            String line = "";
            boolean flag = false;
            while ((line = br.readLine()) != null) {
                if (flag && !line.startsWith("---")) {
                    signData.append(line.trim());
                    continue;
                }
                if (line.startsWith("---")) {
                    flag = true;
                }
            }
        } catch (IOException e) {
            LOGGER.error("io exception, {}", e);
        }

        String signStr = new String(signData).trim();
        if (StringUtils.isEmpty(signStr)) {
            LOGGER.error("signature value is null.");
            return false;
        }
        return signedDataVerify(signStr.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * verify signature value.
     *
     * @param signedData signature value
     * @return if verify sucess
     * @throws CMSException CMSException
     */
    public boolean signedDataVerify(byte[] signedData) throws CMSException {
        CMSSignedData cmsData = new CMSSignedData(Base64.decode(signedData));
        Store<X509CertificateHolder> store = cmsData.getCertificates();
        SignerInformationStore signerInfo = cmsData.getSignerInfos();
        Collection<SignerInformation> signers = signerInfo.getSigners();
        Iterator<SignerInformation> iterator = signers.iterator();
        while (iterator.hasNext()) {
            SignerInformation signer = iterator.next();
            Collection<X509CertificateHolder> certs = store.getMatches(signer.getSID());
            Iterator<X509CertificateHolder> certIterator = certs.iterator();
            X509CertificateHolder certHolder = certIterator.next();
            X509Certificate cert = null;
            try {
                cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);
                if (signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(cert))) {
                    return true;
                }
            } catch (CertificateException e) {
                LOGGER.error("certificate exception, {}", e);
            } catch (OperatorCreationException e) {
                LOGGER.error("operator create exception, {}", e);
            }

        }
        return false;
    }
}
