package io.equiflux.node.wallet.rpc.dto;

/**
 * 导入钱包请求DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class ImportWalletRequestDto {
    
    private String privateKeyHex;
    private String password;
    
    public ImportWalletRequestDto() {}
    
    public ImportWalletRequestDto(String privateKeyHex, String password) {
        this.privateKeyHex = privateKeyHex;
        this.password = password;
    }
    
    public String getPrivateKeyHex() {
        return privateKeyHex;
    }
    
    public void setPrivateKeyHex(String privateKeyHex) {
        this.privateKeyHex = privateKeyHex;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
