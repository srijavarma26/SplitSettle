package com.splitsettle.expenseservice.controller;

import com.splitsettle.expenseservice.dto.AddMemberRequest;
import com.splitsettle.expenseservice.dto.CreateGroupRequest;
import com.splitsettle.expenseservice.dto.GroupBalanceDTO;
import com.splitsettle.expenseservice.dto.GroupResponse;
import com.splitsettle.expenseservice.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupController {

    private final ExpenseService expenseService;

    public GroupController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        GroupResponse response = expenseService.createGroup(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable Long id) {
        GroupResponse response = expenseService.getGroupById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<GroupResponse> addMember(@PathVariable Long id, @Valid @RequestBody AddMemberRequest request) {
        GroupResponse response = expenseService.addMemberToGroup(id, request.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/balances")
    public ResponseEntity<GroupBalanceDTO> getGroupBalances(@PathVariable Long id) {
        GroupBalanceDTO response = expenseService.getGroupBalances(id);
        return ResponseEntity.ok(response);
    }
}
