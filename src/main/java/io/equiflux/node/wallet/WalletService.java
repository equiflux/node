package io.equiflux.node.wallet;

import io.equiflux.node.model.Transaction;
import io.equiflux.node.storage.model.AccountState;
import io.equiflux.node.exception.WalletException;
import io.equiflux.node.wallet.model.*;

import java.util.List;
import java.util.Optional;

/**
 * 钱包服务接口
 * 
 * <p>提供完整的钱包功能，包括密钥管理、账户管理、交易签名和广播等。
 * 
 * <p>主要功能：
 * <ul>
 *   <li>密钥对生成和管理</li>
 *   <li>钱包账户创建和管理</li>
 *   <li>余额查询和状态管理</li>
 *   <li>交易构建和签名</li>
 *   <li>交易广播和状态跟踪</li>
 *   <li>钱包安全功能</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public interface WalletService {
    
    // ==================== 密钥管理 ====================
    
    /**
     * 生成新的密钥对
     * 
     * @return 新生成的密钥对
     * @throws WalletException 钱包异常
     */
    WalletKeyPair generateKeyPair() throws WalletException;
    
    /**
     * 从私钥导入密钥对
     * 
     * @param privateKeyHex 私钥十六进制字符串
     * @return 导入的密钥对
     * @throws WalletException 钱包异常
     */
    WalletKeyPair importKeyPair(String privateKeyHex) throws WalletException;
    
    /**
     * 导出私钥
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @param password 钱包密码
     * @return 私钥十六进制字符串
     * @throws WalletException 钱包异常
     */
    String exportPrivateKey(String publicKeyHex, String password) throws WalletException;
    
    /**
     * 验证密钥对
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @param privateKeyHex 私钥十六进制字符串
     * @return true如果密钥对有效，false否则
     * @throws WalletException 钱包异常
     */
    boolean validateKeyPair(String publicKeyHex, String privateKeyHex) throws WalletException;
    
    // ==================== 钱包账户管理 ====================
    
    /**
     * 创建新钱包
     * 
     * @param password 钱包密码
     * @return 新创建的钱包信息
     * @throws WalletException 钱包异常
     */
    WalletInfo createWallet(String password) throws WalletException;
    
    /**
     * 从私钥创建钱包
     * 
     * @param privateKeyHex 私钥十六进制字符串
     * @param password 钱包密码
     * @return 创建的钱包信息
     * @throws WalletException 钱包异常
     */
    WalletInfo createWalletFromPrivateKey(String privateKeyHex, String password) throws WalletException;
    
    /**
     * 解锁钱包
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @param password 钱包密码
     * @return true如果解锁成功，false否则
     * @throws WalletException 钱包异常
     */
    boolean unlockWallet(String publicKeyHex, String password) throws WalletException;
    
    /**
     * 锁定钱包
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @throws WalletException 钱包异常
     */
    void lockWallet(String publicKeyHex) throws WalletException;
    
    /**
     * 获取钱包信息
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 钱包信息
     * @throws WalletException 钱包异常
     */
    Optional<WalletInfo> getWalletInfo(String publicKeyHex) throws WalletException;
    
    /**
     * 获取所有钱包列表
     * 
     * @return 钱包信息列表
     * @throws WalletException 钱包异常
     */
    List<WalletInfo> getAllWallets() throws WalletException;
    
    /**
     * 删除钱包
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @param password 钱包密码
     * @return true如果删除成功，false否则
     * @throws WalletException 钱包异常
     */
    boolean deleteWallet(String publicKeyHex, String password) throws WalletException;
    
    // ==================== 账户状态管理 ====================
    
    /**
     * 获取账户余额
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 账户余额
     * @throws WalletException 钱包异常
     */
    long getBalance(String publicKeyHex) throws WalletException;
    
    /**
     * 获取账户状态
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 账户状态
     * @throws WalletException 钱包异常
     */
    Optional<AccountState> getAccountState(String publicKeyHex) throws WalletException;
    
    /**
     * 获取账户nonce
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 账户nonce
     * @throws WalletException 钱包异常
     */
    long getNonce(String publicKeyHex) throws WalletException;
    
    /**
     * 获取账户质押金额
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 质押金额
     * @throws WalletException 钱包异常
     */
    long getStake(String publicKeyHex) throws WalletException;
    
    // ==================== 交易管理 ====================
    
    /**
     * 构建转账交易
     * 
     * @param fromPublicKeyHex 发送方公钥十六进制字符串
     * @param toPublicKeyHex 接收方公钥十六进制字符串
     * @param amount 转账金额
     * @param fee 手续费
     * @param password 钱包密码
     * @return 构建的交易
     * @throws WalletException 钱包异常
     */
    Transaction buildTransferTransaction(String fromPublicKeyHex, String toPublicKeyHex, 
                                       long amount, long fee, String password) throws WalletException;
    
    /**
     * 构建质押交易
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @param stakeAmount 质押金额
     * @param fee 手续费
     * @param password 钱包密码
     * @return 构建的交易
     * @throws WalletException 钱包异常
     */
    Transaction buildStakeTransaction(String publicKeyHex, long stakeAmount, 
                                    long fee, String password) throws WalletException;
    
    /**
     * 构建解质押交易
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @param unstakeAmount 解质押金额
     * @param fee 手续费
     * @param password 钱包密码
     * @return 构建的交易
     * @throws WalletException 钱包异常
     */
    Transaction buildUnstakeTransaction(String publicKeyHex, long unstakeAmount, 
                                      long fee, String password) throws WalletException;
    
    /**
     * 签名交易
     * 
     * @param transaction 要签名的交易
     * @param publicKeyHex 公钥十六进制字符串
     * @param password 钱包密码
     * @return 签名后的交易
     * @throws WalletException 钱包异常
     */
    Transaction signTransaction(Transaction transaction, String publicKeyHex, String password) throws WalletException;
    
    /**
     * 验证交易签名
     * 
     * @param transaction 要验证的交易
     * @return true如果签名有效，false否则
     * @throws WalletException 钱包异常
     */
    boolean verifyTransactionSignature(Transaction transaction) throws WalletException;
    
    // ==================== 交易广播 ====================
    
    /**
     * 广播交易
     * 
     * @param transaction 要广播的交易
     * @return 交易哈希
     * @throws WalletException 钱包异常
     */
    String broadcastTransaction(Transaction transaction) throws WalletException;
    
    /**
     * 获取交易状态
     * 
     * @param transactionHash 交易哈希
     * @return 交易状态
     * @throws WalletException 钱包异常
     */
    Optional<TransactionStatus> getTransactionStatus(String transactionHash) throws WalletException;
    
    /**
     * 获取交易历史
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 交易历史列表
     * @throws WalletException 钱包异常
     */
    List<TransactionInfo> getTransactionHistory(String publicKeyHex, int limit, int offset) throws WalletException;
    
    // ==================== 钱包安全 ====================
    
    /**
     * 修改钱包密码
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return true如果修改成功，false否则
     * @throws WalletException 钱包异常
     */
    boolean changePassword(String publicKeyHex, String oldPassword, String newPassword) throws WalletException;
    
    /**
     * 备份钱包
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @param password 钱包密码
     * @return 钱包备份数据
     * @throws WalletException 钱包异常
     */
    WalletBackup backupWallet(String publicKeyHex, String password) throws WalletException;
    
    /**
     * 恢复钱包
     * 
     * @param backup 钱包备份数据
     * @param password 钱包密码
     * @return 恢复的钱包信息
     * @throws WalletException 钱包异常
     */
    WalletInfo restoreWallet(WalletBackup backup, String password) throws WalletException;
}
