package com.company.permit.framework.security;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RsaService {
    private final RSA rsa = new RSA();

    public String publicKeyPem() {
        return "-----BEGIN PUBLIC KEY-----\n" + rsa.getPublicKeyBase64() + "\n-----END PUBLIC KEY-----";
    }

    public String decryptPassword(String cipherText) {
        try {
            return rsa.decryptStr(cipherText, KeyType.PrivateKey);
        } catch (Exception e) {
            log.warn("RSA 解密失败");
            return null;
        }
    }
}
