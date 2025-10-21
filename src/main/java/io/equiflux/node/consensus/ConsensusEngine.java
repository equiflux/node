package io.equiflux.node.consensus;

import io.equiflux.node.crypto.VRFKeyPair;
import io.equiflux.node.model.Block;
import io.equiflux.node.model.Transaction;

import java.util.List;

/**
 * 共识引擎接口
 * 
 * <p>定义共识引擎的核心接口，提供：
 * <ul>
 *   <li>区块生产</li>
 *   <li>区块验证</li>
 *   <li>状态查询</li>
 * </ul>
 * 
 * <p>实现类：EquifluxConsensus
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public interface ConsensusEngine {
    
    /**
     * 生产区块
     * 
     * @param myVrfKeyPair 本节点VRF密钥对
     * @param previousBlock 前一区块
     * @param availableTransactions 可用交易列表
     * @return 生产的区块，如果本节点不是出块者则返回null
     */
    Block produceBlock(VRFKeyPair myVrfKeyPair, Block previousBlock, List<Transaction> availableTransactions);
    
    /**
     * 验证区块
     * 
     * @param block 区块
     * @param previousBlock 前一区块
     * @return true如果验证通过，false否则
     */
    boolean verifyBlock(Block block, Block previousBlock);
    
    /**
     * 获取当前高度
     * 
     * @return 当前高度
     */
    long getCurrentHeight();
    
    /**
     * 获取当前轮次
     * 
     * @return 当前轮次
     */
    long getCurrentRound();
    
    /**
     * 获取当前纪元
     * 
     * @return 当前纪元
     */
    long getCurrentEpoch();
    
    /**
     * 检查共识是否正常运行
     * 
     * @return true如果正常运行，false否则
     */
    boolean isConsensusHealthy();
    
    /**
     * 获取共识状态
     * 
     * @return 共识状态信息
     */
    java.util.Map<String, Object> getConsensusStatus();
}
