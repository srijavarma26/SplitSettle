package com.splitsettle.settlementservice.dto;

import java.math.BigDecimal;
import java.util.Map;

public class GroupBalanceDTO {

    private Long groupId;
    private Map<Long, BigDecimal> netBalances;

    public GroupBalanceDTO() {
    }

    public GroupBalanceDTO(Long groupId, Map<Long, BigDecimal> netBalances) {
        this.groupId = groupId;
        this.netBalances = netBalances;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Map<Long, BigDecimal> getNetBalances() {
        return netBalances;
    }

    public void setNetBalances(Map<Long, BigDecimal> netBalances) {
        this.netBalances = netBalances;
    }
}
