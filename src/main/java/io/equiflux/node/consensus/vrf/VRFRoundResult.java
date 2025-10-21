package io.equiflux.node.consensus.vrf;

import io.equiflux.node.model.VRFAnnouncement;

import java.security.PublicKey;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * VRF轮次结果
 * 
 * <p>封装一轮VRF收集的结果，包含：
 * <ul>
 *   <li>出块者（分数最高的节点）</li>
 *   <li>前X名奖励节点</li>
 *   <li>所有有效的VRF公告</li>
 * </ul>
 * 
 * <p>用于区块构造和验证过程中的出块者选择和奖励分配。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class VRFRoundResult {
    
    private final VRFAnnouncement winner;
    private final List<VRFAnnouncement> topX;
    private final List<VRFAnnouncement> allValid;
    
    /**
     * 构造VRF轮次结果
     * 
     * @param winner 出块者（分数最高的节点）
     * @param topX 前X名奖励节点
     * @param allValid 所有有效的VRF公告
     */
    public VRFRoundResult(VRFAnnouncement winner, List<VRFAnnouncement> topX, List<VRFAnnouncement> allValid) {
        this.winner = Objects.requireNonNull(winner, "Winner cannot be null");
        this.topX = Objects.requireNonNull(topX, "Top X cannot be null");
        this.allValid = Objects.requireNonNull(allValid, "All valid cannot be null");
        
        if (topX.isEmpty()) {
            throw new IllegalArgumentException("Top X cannot be empty");
        }
        if (allValid.isEmpty()) {
            throw new IllegalArgumentException("All valid cannot be empty");
        }
        
        // 验证出块者是否在前X名中
        if (!topX.contains(winner)) {
            throw new IllegalArgumentException("Winner must be in top X");
        }
        
        // 验证前X名是否在所有有效公告中
        if (!allValid.containsAll(topX)) {
            throw new IllegalArgumentException("Top X must be subset of all valid");
        }
    }
    
    /**
     * 获取出块者
     * 
     * @return 出块者VRF公告
     */
    public VRFAnnouncement getWinner() {
        return winner;
    }
    
    /**
     * 获取前X名奖励节点
     * 
     * @return 前X名VRF公告列表
     */
    public List<VRFAnnouncement> getTopX() {
        return Collections.unmodifiableList(topX);
    }
    
    /**
     * 获取所有有效的VRF公告
     * 
     * @return 所有有效的VRF公告列表
     */
    public List<VRFAnnouncement> getAllValid() {
        return Collections.unmodifiableList(allValid);
    }
    
    /**
     * 获取前X名公钥列表
     * 
     * @return 前X名公钥列表
     */
    public List<PublicKey> getTopXPublicKeys() {
        return topX.stream()
                .map(VRFAnnouncement::getPublicKey)
                .toList();
    }
    
    /**
     * 获取所有有效公钥列表
     * 
     * @return 所有有效公钥列表
     */
    public List<PublicKey> getAllValidPublicKeys() {
        return allValid.stream()
                .map(VRFAnnouncement::getPublicKey)
                .toList();
    }
    
    /**
     * 获取出块者公钥
     * 
     * @return 出块者公钥
     */
    public PublicKey getWinnerPublicKey() {
        return winner.getPublicKey();
    }
    
    /**
     * 获取出块者公钥的十六进制字符串
     * 
     * @return 出块者公钥的十六进制字符串
     */
    public String getWinnerPublicKeyHex() {
        return winner.getPublicKeyHex();
    }
    
    /**
     * 获取出块者分数
     * 
     * @return 出块者分数
     */
    public double getWinnerScore() {
        return winner.getScore();
    }
    
    /**
     * 获取前X名数量
     * 
     * @return 前X名数量
     */
    public int getTopXCount() {
        return topX.size();
    }
    
    /**
     * 获取所有有效公告数量
     * 
     * @return 所有有效公告数量
     */
    public int getAllValidCount() {
        return allValid.size();
    }
    
    /**
     * 检查是否有足够的VRF公告
     * 
     * @param minRequired 最小要求数量
     * @return true如果有足够数量，false否则
     */
    public boolean hasEnoughVRFs(int minRequired) {
        return allValid.size() >= minRequired;
    }
    
    /**
     * 检查出块者是否合法
     * 
     * @return true如果出块者合法，false否则
     */
    public boolean isWinnerValid() {
        // 检查出块者是否在所有有效公告中
        if (!allValid.contains(winner)) {
            return false;
        }
        
        // 检查出块者是否确实是分数最高的
        return allValid.stream()
                .allMatch(announcement -> announcement.getScore() <= winner.getScore());
    }
    
    /**
     * 检查前X名是否合法
     * 
     * @return true如果前X名合法，false否则
     */
    public boolean isTopXValid() {
        // 检查前X名是否按分数降序排列
        for (int i = 0; i < topX.size() - 1; i++) {
            if (topX.get(i).getScore() < topX.get(i + 1).getScore()) {
                return false;
            }
        }
        
        // 检查前X名是否确实是分数最高的X个
        List<VRFAnnouncement> sortedAll = allValid.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .toList();
        
        for (int i = 0; i < Math.min(topX.size(), sortedAll.size()); i++) {
            if (!topX.get(i).equals(sortedAll.get(i))) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 获取轮次号
     * 
     * @return 轮次号
     */
    public long getRound() {
        return winner.getRound();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VRFRoundResult that = (VRFRoundResult) obj;
        return Objects.equals(winner, that.winner) &&
               Objects.equals(topX, that.topX) &&
               Objects.equals(allValid, that.allValid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(winner, topX, allValid);
    }
    
    @Override
    public String toString() {
        return "VRFRoundResult{" +
               "round=" + getRound() +
               ", winner=" + winner.getPublicKeyHex() +
               ", winnerScore=" + getWinnerScore() +
               ", topXCount=" + getTopXCount() +
               ", allValidCount=" + getAllValidCount() +
               '}';
    }
}
