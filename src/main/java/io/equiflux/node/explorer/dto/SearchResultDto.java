package io.equiflux.node.explorer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 搜索结果DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class SearchResultDto {
    
    @JsonProperty("query")
    private String query;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("block_result")
    private BlockDetailDto blockResult;
    
    @JsonProperty("transaction_result")
    private TransactionDetailDto transactionResult;
    
    @JsonProperty("account_result")
    private AccountDetailDto accountResult;
    
    // Getters and Setters
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public BlockDetailDto getBlockResult() { return blockResult; }
    public void setBlockResult(BlockDetailDto blockResult) { this.blockResult = blockResult; }
    
    public TransactionDetailDto getTransactionResult() { return transactionResult; }
    public void setTransactionResult(TransactionDetailDto transactionResult) { this.transactionResult = transactionResult; }
    
    public AccountDetailDto getAccountResult() { return accountResult; }
    public void setAccountResult(AccountDetailDto accountResult) { this.accountResult = accountResult; }
}
