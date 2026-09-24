package com.splitsettle.settlementservice.client;

import com.splitsettle.settlementservice.dto.GroupBalanceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "expense-service", url = "${expense-service.url}")
public interface ExpenseClient {

    @GetMapping("/api/v1/groups/{id}/balances")
    GroupBalanceDTO getGroupBalances(@PathVariable("id") Long groupId);
}
