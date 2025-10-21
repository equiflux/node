package io.equiflux.node.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Objects;

/**
 * VRF证明
 * 
 * <p>封装VRF（可验证随机函数）的证明，用于验证VRF输出的正确性。
 * 
 * <p>VRF证明包含足够的信息来验证：
 * <ul>
 *   <li>输出确实是由指定的私钥生成的</li>
 *   <li>输出对应特定的输入</li>
 *   <li>输出没有被篡改</li>
 * </ul>
 * 
 * <p>在Equiflux中，VRF证明使用EdDSA签名实现，大小为64字节。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class VRFProof {
    
    private final byte[] proof;
    
    /**
     * 构造VRF证明
     * 
     * @param proof 证明字节数组（64字节）
     */
    @JsonCreator
    public VRFProof(@JsonProperty("proof") byte[] proof) {
        this.proof = Objects.requireNonNull(proof, "VRF proof cannot be null");
        
        if (proof.length != 64) {
            throw new IllegalArgumentException("VRF proof must be 64 bytes");
        }
    }
    
    /**
     * 获取证明字节数组
     * 
     * @return 64字节的证明
     */
    public byte[] getProof() {
        return proof.clone();
    }
    
    /**
     * 获取证明的十六进制字符串表示
     * 
     * @return 十六进制字符串
     */
    public String toHexString() {
        StringBuilder sb = new StringBuilder(proof.length * 2);
        for (byte b : proof) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VRFProof vrfProof = (VRFProof) obj;
        return Arrays.equals(proof, vrfProof.proof);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(proof);
    }
    
    @Override
    public String toString() {
        return "VRFProof{" +
               "proof=" + toHexString() +
               '}';
    }
}