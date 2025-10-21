package io.equiflux.node.crypto;

import io.equiflux.node.exception.CryptoException;
import io.equiflux.node.model.Block;
import io.equiflux.node.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.security.Signature;
import java.util.Objects;

/**
 * 签名验证器
 * 
 * <p>提供区块和交易签名的验证功能，使用Ed25519签名算法。
 * 
 * <p>主要功能：
 * <ul>
 *   <li>区块签名验证</li>
 *   <li>交易签名验证</li>
 *   <li>批量签名验证</li>
 * </ul>
 * 
 * <p>签名验证过程：
 * <ol>
 *   <li>构造待签名数据</li>
 *   <li>使用公钥初始化验证器</li>
 *   <li>更新待验证数据</li>
 *   <li>验证签名</li>
 * </ol>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Component
public class SignatureVerifier {
    
    private static final Logger logger = LoggerFactory.getLogger(SignatureVerifier.class);
    
    private static final String ED25519_ALGORITHM = "Ed25519";
    private static final String ED25519_PROVIDER = "SunEC";
    
    /**
     * 验证区块签名
     * 
     * @param block 区块
     * @param publicKeyBytes 公钥字节数组
     * @param signature 签名
     * @return true如果签名有效，false否则
     * @throws CryptoException 如果验证过程失败
     */
    public boolean verifyBlockSignature(Block block, byte[] publicKeyBytes, byte[] signature) {
        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null");
        }
        if (publicKeyBytes == null) {
            throw new IllegalArgumentException("Public key bytes cannot be null");
        }
        if (signature == null) {
            throw new IllegalArgumentException("Signature cannot be null");
        }
        
        try {
            byte[] dataToVerify = block.serializeForSigning();
            boolean isValid = Ed25519KeyPair.verify(publicKeyBytes, dataToVerify, signature);
            
            logger.debug("Block signature verification: block={}, publicKey={}, valid={}", 
                        block.getHeight(), HashUtils.toHexString(publicKeyBytes), isValid);
            
            return isValid;
        } catch (Exception e) {
            logger.error("Block signature verification failed", e);
            throw new CryptoException("Block signature verification failed", e);
        }
    }
    
    /**
     * 验证交易签名
     * 
     * @param transaction 交易
     * @return true如果签名有效，false否则
     * @throws CryptoException 如果验证过程失败
     */
    public boolean verifyTransactionSignature(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        
        try {
            byte[] dataToVerify = transaction.serializeForSigning();
            boolean isValid = Ed25519KeyPair.verify(transaction.getSenderPublicKey(), dataToVerify, transaction.getSignature());
            
            logger.debug("Transaction signature verification: tx={}, valid={}", 
                        transaction.getHash(), isValid);
            
            return isValid;
        } catch (Exception e) {
            logger.error("Transaction signature verification failed", e);
            throw new CryptoException("Transaction signature verification failed", e);
        }
    }
    
    /**
     * 验证交易签名（使用指定公钥）
     * 
     * @param transaction 交易
     * @param publicKey 公钥
     * @return true如果签名有效，false否则
     * @throws CryptoException 如果验证过程失败
     */
    public boolean verifyTransactionSignature(Transaction transaction, PublicKey publicKey) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        if (publicKey == null) {
            throw new IllegalArgumentException("Public key cannot be null");
        }
        
        try {
            Signature sig = Signature.getInstance(ED25519_ALGORITHM, ED25519_PROVIDER);
            sig.initVerify(publicKey);
            
            byte[] dataToVerify = transaction.serializeForSigning();
            sig.update(dataToVerify);
            
            boolean isValid = sig.verify(transaction.getSignature());
            
            logger.debug("Transaction signature verification: tx={}, publicKey={}, valid={}", 
                        transaction.getHash(), HashUtils.toHexString(publicKey.getEncoded()), isValid);
            
            return isValid;
        } catch (Exception e) {
            logger.error("Transaction signature verification failed", e);
            throw new CryptoException("Transaction signature verification failed", e);
        }
    }
    
    /**
     * 验证签名（通用方法）
     * 
     * @param publicKey 公钥
     * @param data 待验证数据
     * @param signature 签名
     * @return true如果签名有效，false否则
     * @throws CryptoException 如果验证过程失败
     */
    public boolean verify(PublicKey publicKey, byte[] data, byte[] signature) {
        if (publicKey == null) {
            throw new IllegalArgumentException("Public key cannot be null");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        if (signature == null) {
            throw new IllegalArgumentException("Signature cannot be null");
        }
        
        try {
            Signature sig = Signature.getInstance(ED25519_ALGORITHM, ED25519_PROVIDER);
            sig.initVerify(publicKey);
            sig.update(data);
            
            return sig.verify(signature);
        } catch (Exception e) {
            logger.error("Signature verification failed", e);
            throw new CryptoException("Signature verification failed", e);
        }
    }
    
    /**
     * 批量验证交易签名
     * 
     * @param transactions 交易列表
     * @return 验证结果数组，true表示签名有效
     * @throws CryptoException 如果验证过程失败
     */
    public boolean[] verifyTransactionSignatures(Transaction[] transactions) {
        if (transactions == null) {
            throw new IllegalArgumentException("Transactions cannot be null");
        }
        
        boolean[] results = new boolean[transactions.length];
        
        for (int i = 0; i < transactions.length; i++) {
            try {
                results[i] = verifyTransactionSignature(transactions[i]);
            } catch (Exception e) {
                logger.warn("Failed to verify transaction signature at index {}", i, e);
                results[i] = false;
            }
        }
        
        logger.debug("Batch transaction signature verification: total={}, valid={}", 
                    transactions.length, countValid(results));
        
        return results;
    }
    
    /**
     * 验证区块中的所有签名
     * 
     * @param block 区块
     * @return true如果所有签名都有效，false否则
     * @throws CryptoException 如果验证过程失败
     */
    public boolean verifyAllBlockSignatures(Block block) {
        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null");
        }
        
        if (block.getSignatures().isEmpty()) {
            logger.warn("Block {} has no signatures", block.getHeight());
            return false;
        }
        
        boolean allValid = true;
        for (var entry : block.getSignatures().entrySet()) {
            byte[] publicKeyBytes = entry.getKey();
            byte[] signature = entry.getValue();
            
            if (!verifyBlockSignature(block, publicKeyBytes, signature)) {
                logger.warn("Invalid signature for public key {} in block {}", 
                           HashUtils.toHexString(publicKeyBytes), block.getHeight());
                allValid = false;
            }
        }
        
        logger.debug("All block signatures verification: block={}, valid={}", 
                    block.getHeight(), allValid);
        
        return allValid;
    }
    
    /**
     * 验证区块中的所有交易签名
     * 
     * @param block 区块
     * @return true如果所有交易签名都有效，false否则
     * @throws CryptoException 如果验证过程失败
     */
    public boolean verifyAllTransactionSignatures(Block block) {
        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null");
        }
        
        if (block.getTransactions().isEmpty()) {
            logger.debug("Block {} has no transactions", block.getHeight());
            return true;
        }
        
        boolean allValid = true;
        for (Transaction transaction : block.getTransactions()) {
            if (!verifyTransactionSignature(transaction)) {
                logger.warn("Invalid signature for transaction {} in block {}", 
                           transaction.getHash(), block.getHeight());
                allValid = false;
            }
        }
        
        logger.debug("All transaction signatures verification: block={}, valid={}", 
                    block.getHeight(), allValid);
        
        return allValid;
    }
    
    /**
     * 统计有效签名数量
     * 
     * @param results 验证结果数组
     * @return 有效签名数量
     */
    private int countValid(boolean[] results) {
        int count = 0;
        for (boolean result : results) {
            if (result) {
                count++;
            }
        }
        return count;
    }
}
