package io.equiflux.node.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Objects;

/**
 * VRF输出
 * 
 * <p>封装VRF（可验证随机函数）的输出结果，包含：
 * <ul>
 *   <li>VRF输出值（32字节）</li>
 *   <li>VRF证明（64字节）</li>
 * </ul>
 * 
 * <p>VRF输出是确定性的伪随机值，具有以下特性：
 * <ul>
 *   <li>唯一性：相同输入产生相同输出</li>
 *   <li>随机性：输出看起来是随机的</li>
 *   <li>可验证性：可以通过证明验证输出的正确性</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class VRFOutput {
    
    private final byte[] output;
    private final VRFProof proof;
    
    /**
     * 构造VRF输出
     * 
     * @param output VRF输出值（32字节）
     * @param proof VRF证明
     */
    @JsonCreator
    public VRFOutput(@JsonProperty("output") byte[] output, 
                     @JsonProperty("proof") VRFProof proof) {
        this.output = Objects.requireNonNull(output, "VRF output cannot be null");
        this.proof = Objects.requireNonNull(proof, "VRF proof cannot be null");
        
        if (output.length != 32) {
            throw new IllegalArgumentException("VRF output must be 32 bytes");
        }
    }
    
    /**
     * 获取VRF输出值
     * 
     * @return 32字节的VRF输出值
     */
    public byte[] getOutput() {
        return output.clone();
    }
    
    /**
     * 获取VRF证明
     * 
     * @return VRF证明
     */
    public VRFProof getProof() {
        return proof;
    }
    
    /**
     * 将VRF输出转换为0-1之间的分数
     * 
     * <p>用于共识中的出块者选择，将256位输出转换为浮点数分数。
     * 
     * @return 0-1之间的分数
     */
    public double toScore() {
        // 将32字节输出转换为BigInteger，然后除以2^256
        // 使用前8个字节计算分数，确保结果在0-1范围内
        long high = ((long) (output[0] & 0xFF) << 56) |
                   ((long) (output[1] & 0xFF) << 48) |
                   ((long) (output[2] & 0xFF) << 40) |
                   ((long) (output[3] & 0xFF) << 32) |
                   ((long) (output[4] & 0xFF) << 24) |
                   ((long) (output[5] & 0xFF) << 16) |
                   ((long) (output[6] & 0xFF) << 8) |
                   (output[7] & 0xFF);
        
        // 使用高8字节计算分数，避免精度问题
        // 确保结果在0-1范围内
        return Math.abs(high) / (double) Long.MAX_VALUE;
    }
    
    /**
     * 获取VRF输出的十六进制字符串表示
     * 
     * @return 十六进制字符串
     */
    public String toHexString() {
        StringBuilder sb = new StringBuilder(output.length * 2);
        for (byte b : output) {
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
        VRFOutput vrfOutput = (VRFOutput) obj;
        return Arrays.equals(output, vrfOutput.output) &&
               Objects.equals(proof, vrfOutput.proof);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(output) * 31 + Objects.hashCode(proof);
    }
    
    @Override
    public String toString() {
        return "VRFOutput{" +
               "output=" + toHexString() +
               ", proof=" + proof +
               '}';
    }
}
