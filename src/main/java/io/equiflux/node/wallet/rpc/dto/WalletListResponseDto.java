package io.equiflux.node.wallet.rpc.dto;

import java.util.List;

/**
 * 钱包列表响应DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class WalletListResponseDto {
    
    private boolean success;
    private String message;
    private List<WalletResponseDto> wallets;
    private int count;
    
    public WalletListResponseDto() {
        this.success = true;
    }
    
    public static WalletListResponseDto error(String message) {
        WalletListResponseDto dto = new WalletListResponseDto();
        dto.success = false;
        dto.message = message;
        return dto;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<WalletResponseDto> getWallets() {
        return wallets;
    }
    
    public void setWallets(List<WalletResponseDto> wallets) {
        this.wallets = wallets;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
}
