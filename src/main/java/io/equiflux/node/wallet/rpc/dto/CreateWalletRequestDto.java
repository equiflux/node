package io.equiflux.node.wallet.rpc.dto;

/**
 * 创建钱包请求DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class CreateWalletRequestDto {
    
    private String password;
    
    public CreateWalletRequestDto() {}
    
    public CreateWalletRequestDto(String password) {
        this.password = password;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
