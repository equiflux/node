package io.equiflux.node.explorer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 交易列表DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class TransactionListDto {
    
    @JsonProperty("transactions")
    private List<TransactionSummaryDto> transactions;
    
    @JsonProperty("total_transactions")
    private long totalTransactions;
    
    @JsonProperty("page")
    private int page;
    
    @JsonProperty("size")
    private int size;
    
    @JsonProperty("total_pages")
    private int totalPages;
    
    // Getters and Setters
    public List<TransactionSummaryDto> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionSummaryDto> transactions) { this.transactions = transactions; }
    
    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }
    
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
