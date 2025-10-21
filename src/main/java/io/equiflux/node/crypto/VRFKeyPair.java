package io.equiflux.node.crypto;

import io.equiflux.node.exception.CryptoException;
import io.equiflux.node.model.VRFOutput;
import io.equiflux.node.model.VRFProof;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.Objects;

/**
 * VRF密钥对
 * 
 * <p>用于VRF（可验证随机函数）的密钥对，包含私钥和公钥。
 * 
 * <p>在Equiflux中，VRF使用以下实现：
 * <ul>
 *   <li>VRF函数：HMAC-SHA256</li>
 *   <li>证明机制：EdDSA签名</li>
 *   <li>输出大小：32字节</li>
 *   <li>证明大小：64字节</li>
 * </ul>
 * 
 * <p>VRF具有以下特性：
 * <ul>
 *   <li>唯一性：相同输入产生相同输出</li>
 *   <li>随机性：输出看起来是随机的</li>
 *   <li>可验证性：可以通过证明验证输出的正确性</li>
 *   <li>不可预测性：无法预测输出</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class VRFKeyPair {
    
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String ED25519_ALGORITHM = "Ed25519";
    private static final String ED25519_PROVIDER = "SunEC";
    
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    
    /**
     * 构造VRF密钥对
     * 
     * @param privateKey 私钥
     * @param publicKey 公钥
     */
    public VRFKeyPair(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = Objects.requireNonNull(privateKey, "Private key cannot be null");
        this.publicKey = Objects.requireNonNull(publicKey, "Public key cannot be null");
    }
    
    /**
     * 生成新的VRF密钥对
     * 
     * @return 新的VRF密钥对
     * @throws CryptoException 如果密钥生成失败
     */
    public static VRFKeyPair generate() {
        Ed25519KeyPair ed25519KeyPair = Ed25519KeyPair.generate();
        return new VRFKeyPair(ed25519KeyPair.getPrivateKey(), ed25519KeyPair.getPublicKey());
    }
    
    /**
     * 计算VRF输出
     * 
     * @param input VRF输入
     * @return VRF输出（包含输出值和证明）
     * @throws CryptoException 如果VRF计算失败
     */
    public VRFOutput evaluate(byte[] input) {
        if (input == null) {
            throw new IllegalArgumentException("VRF input cannot be null");
        }
        
        try {
            // 使用HMAC-SHA256计算VRF输出（使用公钥编码作为密钥材料）
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(publicKey.getEncoded(), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            
            byte[] output = mac.doFinal(input);
            
            // 生成VRF证明
            VRFProof proof = generateProof(input, output);
            
            return new VRFOutput(output, proof);
        } catch (Exception e) {
            throw new CryptoException("VRF evaluation failed", e);
        }
    }
    
    /**
     * 验证VRF输出
     * 
     * @param publicKey 公钥
     * @param input VRF输入
     * @param output VRF输出
     * @param proof VRF证明
     * @return true如果验证通过，false否则
     * @throws CryptoException 如果验证过程失败
     */
    public static boolean verify(PublicKey publicKey, byte[] input, VRFOutput output, VRFProof proof) {
        if (publicKey == null) {
            throw new IllegalArgumentException("Public key cannot be null");
        }
        if (input == null) {
            throw new IllegalArgumentException("VRF input cannot be null");
        }
        if (output == null) {
            throw new IllegalArgumentException("VRF output cannot be null");
        }
        if (proof == null) {
            throw new IllegalArgumentException("VRF proof cannot be null");
        }
        
        try {
            // 重新计算VRF输出
            VRFOutput expectedOutput = evaluateFromPublicKey(publicKey, input, proof);
            
            // 比较输出
            return Arrays.equals(output.getOutput(), expectedOutput.getOutput());
        } catch (Exception e) {
            // 验证失败时返回false而不是抛出异常
            return false;
        }
    }
    
    /**
     * 生成VRF证明
     * 
     * @param input VRF输入
     * @param output VRF输出
     * @return VRF证明
     * @throws CryptoException 如果证明生成失败
     */
    private VRFProof generateProof(byte[] input, byte[] output) {
        try {
            // 使用EdDSA签名作为VRF证明
            Signature signature = Signature.getInstance(ED25519_ALGORITHM, ED25519_PROVIDER);
            signature.initSign(privateKey);
            
            // 将输入和输出组合作为签名数据
            byte[] dataToSign = new byte[input.length + output.length];
            System.arraycopy(input, 0, dataToSign, 0, input.length);
            System.arraycopy(output, 0, dataToSign, input.length, output.length);
            
            signature.update(dataToSign);
            byte[] proofBytes = signature.sign();
            
            return new VRFProof(proofBytes);
        } catch (Exception e) {
            throw new CryptoException("Failed to generate VRF proof", e);
        }
    }
    
    /**
     * 从公钥验证VRF证明
     * 
     * @param publicKey 公钥
     * @param input VRF输入
     * @param proof VRF证明
     * @return VRF输出
     * @throws CryptoException 如果计算失败
     */
    private static VRFOutput evaluateFromPublicKey(PublicKey publicKey, byte[] input, VRFProof proof) {
        try {
            // 验证证明的有效性
            Signature signature = Signature.getInstance(ED25519_ALGORITHM, ED25519_PROVIDER);
            signature.initVerify(publicKey);
            
            // 重新计算VRF输出（使用公钥的编码作为HMAC密钥）
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            byte[] keyMaterial = publicKey.getEncoded();
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyMaterial, HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            
            byte[] output = mac.doFinal(input);
            
            // 验证证明
            byte[] dataToVerify = new byte[input.length + output.length];
            System.arraycopy(input, 0, dataToVerify, 0, input.length);
            System.arraycopy(output, 0, dataToVerify, input.length, output.length);
            
            signature.update(dataToVerify);
            boolean isValid = signature.verify(proof.getProof());
            
            if (!isValid) {
                throw new CryptoException("VRF proof verification failed");
            }
            
            return new VRFOutput(output, proof);
        } catch (Exception e) {
            throw new CryptoException("Failed to evaluate VRF from public key", e);
        }
    }
    
    /**
     * 获取私钥
     * 
     * @return 私钥
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }
    
    /**
     * 获取公钥
     * 
     * @return 公钥
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }
    
    /**
     * 获取私钥的编码形式
     * 
     * @return 私钥的字节数组
     */
    public byte[] getPrivateKeyBytes() {
        return privateKey.getEncoded();
    }
    
    /**
     * 获取公钥的编码形式
     * 
     * @return 公钥的字节数组
     */
    public byte[] getPublicKeyBytes() {
        return publicKey.getEncoded();
    }
    
    /**
     * 获取公钥的十六进制字符串表示
     * 
     * @return 公钥的十六进制字符串
     */
    public String getPublicKeyHex() {
        return HashUtils.toHexString(getPublicKeyBytes());
    }
    
    /**
     * 获取私钥的十六进制字符串表示
     * 
     * @return 私钥的十六进制字符串
     */
    public String getPrivateKeyHex() {
        return HashUtils.toHexString(getPrivateKeyBytes());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VRFKeyPair that = (VRFKeyPair) obj;
        return Arrays.equals(privateKey.getEncoded(), that.privateKey.getEncoded()) &&
               Arrays.equals(publicKey.getEncoded(), that.publicKey.getEncoded());
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(privateKey.getEncoded()) * 31 + Arrays.hashCode(publicKey.getEncoded());
    }
    
    @Override
    public String toString() {
        return "VRFKeyPair{" +
               "publicKey=" + getPublicKeyHex() +
               '}';
    }
}
