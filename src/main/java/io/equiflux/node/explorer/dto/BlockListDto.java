package io.equiflux.node.explorer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 区块列表DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class BlockListDto {
    
    @JsonProperty("blocks")
    private List<BlockSummaryDto> blocks;
    
    @JsonProperty("total_blocks")
    private long totalBlocks;
    
    @JsonProperty("page")
    private int page;
    
    @JsonProperty("size")
    private int size;
    
    @JsonProperty("total_pages")
    private int totalPages;
    
    // Getters and Setters
    public List<BlockSummaryDto> getBlocks() { return blocks; }
    public void setBlocks(List<BlockSummaryDto> blocks) { this.blocks = blocks; }
    
    public long getTotalBlocks() { return totalBlocks; }
    public void setTotalBlocks(long totalBlocks) { this.totalBlocks = totalBlocks; }
    
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
