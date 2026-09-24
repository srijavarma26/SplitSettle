package com.splitsettle.expenseservice.service;

import com.splitsettle.expenseservice.client.UserClient;
import com.splitsettle.expenseservice.dto.*;
import com.splitsettle.expenseservice.entity.*;
import com.splitsettle.expenseservice.exception.GroupNotFoundException;
import com.splitsettle.expenseservice.exception.InvalidExpenseException;
import com.splitsettle.expenseservice.exception.UserNotFoundException;
import com.splitsettle.expenseservice.repository.ExpenseRepository;
import com.splitsettle.expenseservice.repository.ExpenseSplitRepository;
import com.splitsettle.expenseservice.repository.GroupMemberRepository;
import com.splitsettle.expenseservice.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final UserClient userClient;

    public ExpenseService(GroupRepository groupRepository,
                          GroupMemberRepository groupMemberRepository,
                          ExpenseRepository expenseRepository,
                          ExpenseSplitRepository expenseSplitRepository,
                          UserClient userClient) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.expenseRepository = expenseRepository;
        this.expenseSplitRepository = expenseSplitRepository;
        this.userClient = userClient;
    }

    private void validateUserExists(Long userId) {
        try {
            Boolean exists = userClient.checkUserExists(userId);
            if (exists == null || !exists) {
                throw new UserNotFoundException("User not found with id: " + userId);
            }
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            // Fallback or warning if user-service is temporarily unreachable during test/dev
        }
    }

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request) {
        validateUserExists(request.getCreatedBy());

        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setCreatedBy(request.getCreatedBy());

        Group savedGroup = groupRepository.save(group);

        // Add creator as member
        GroupMember creatorMember = new GroupMember(savedGroup.getId(), request.getCreatedBy());
        groupMemberRepository.save(creatorMember);

        return mapToGroupResponse(savedGroup, List.of(request.getCreatedBy()));
    }

    @Transactional
    public GroupResponse addMemberToGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with id: " + groupId));

        validateUserExists(userId);

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            GroupMember member = new GroupMember(groupId, userId);
            groupMemberRepository.save(member);
        }

        List<Long> memberIds = groupMemberRepository.findByGroupId(groupId).stream()
                .map(GroupMember::getUserId)
                .collect(Collectors.toList());

        return mapToGroupResponse(group, memberIds);
    }

    public GroupResponse getGroupById(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with id: " + groupId));

        List<Long> memberIds = groupMemberRepository.findByGroupId(groupId).stream()
                .map(GroupMember::getUserId)
                .collect(Collectors.toList());

        return mapToGroupResponse(group, memberIds);
    }

    @Transactional
    public ExpenseResponse addExpense(Long groupId, AddExpenseRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with id: " + groupId));

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, request.getPaidByUserId())) {
            throw new InvalidExpenseException("Payer with ID " + request.getPaidByUserId() + " is not a member of group " + groupId);
        }

        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        Set<Long> memberUserIds = members.stream().map(GroupMember::getUserId).collect(Collectors.toSet());

        Expense expense = new Expense(
                groupId,
                request.getPaidByUserId(),
                request.getAmount(),
                request.getDescription(),
                request.getSplitType()
        );
        Expense savedExpense = expenseRepository.save(expense);

        List<ExpenseSplit> calculatedSplits = processSplits(savedExpense.getId(), request, memberUserIds);
        expenseSplitRepository.saveAll(calculatedSplits);

        return mapToExpenseResponse(savedExpense, calculatedSplits);
    }

    private List<ExpenseSplit> processSplits(Long expenseId, AddExpenseRequest request, Set<Long> memberUserIds) {
        BigDecimal totalAmount = request.getAmount();
        List<ExpenseSplit> splits = new ArrayList<>();

        if (request.getSplitType() == SplitType.EQUAL) {
            List<Long> targetUserIds;
            if (request.getSplits() != null && !request.getSplits().isEmpty()) {
                targetUserIds = request.getSplits().stream().map(ExpenseSplitRequest::getUserId).collect(Collectors.toList());
            } else {
                targetUserIds = new ArrayList<>(memberUserIds);
            }

            for (Long uid : targetUserIds) {
                if (!memberUserIds.contains(uid)) {
                    throw new InvalidExpenseException("User " + uid + " is not a member of the group");
                }
            }

            int count = targetUserIds.size();
            if (count == 0) {
                throw new InvalidExpenseException("Cannot split expense among 0 members");
            }

            BigDecimal perPerson = totalAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
            BigDecimal remainder = totalAmount.subtract(perPerson.multiply(BigDecimal.valueOf(count)));

            for (int i = 0; i < targetUserIds.size(); i++) {
                Long uid = targetUserIds.get(i);
                BigDecimal amount = perPerson;
                if (i == 0) {
                    amount = amount.add(remainder); // adjust pennies on first member
                }
                splits.add(new ExpenseSplit(expenseId, uid, amount, null));
            }

        } else if (request.getSplitType() == SplitType.PERCENTAGE) {
            if (request.getSplits() == null || request.getSplits().isEmpty()) {
                throw new InvalidExpenseException("Percentage split details must be provided");
            }

            BigDecimal totalPercentage = BigDecimal.ZERO;
            for (ExpenseSplitRequest req : request.getSplits()) {
                if (!memberUserIds.contains(req.getUserId())) {
                    throw new InvalidExpenseException("User " + req.getUserId() + " is not a member of the group");
                }
                if (req.getPercentage() == null) {
                    throw new InvalidExpenseException("Percentage is required for user " + req.getUserId());
                }
                totalPercentage = totalPercentage.add(req.getPercentage());
            }

            if (totalPercentage.compareTo(new BigDecimal("100.00")) != 0 && totalPercentage.compareTo(new BigDecimal("100")) != 0) {
                throw new InvalidExpenseException("Total split percentage must equal 100%. Given: " + totalPercentage);
            }

            for (ExpenseSplitRequest req : request.getSplits()) {
                BigDecimal amount = totalAmount.multiply(req.getPercentage()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                splits.add(new ExpenseSplit(expenseId, req.getUserId(), amount, req.getPercentage()));
            }

        } else if (request.getSplitType() == SplitType.EXACT_AMOUNT) {
            if (request.getSplits() == null || request.getSplits().isEmpty()) {
                throw new InvalidExpenseException("Exact amount split details must be provided");
            }

            BigDecimal totalExact = BigDecimal.ZERO;
            for (ExpenseSplitRequest req : request.getSplits()) {
                if (!memberUserIds.contains(req.getUserId())) {
                    throw new InvalidExpenseException("User " + req.getUserId() + " is not a member of the group");
                }
                if (req.getAmount() == null) {
                    throw new InvalidExpenseException("Amount is required for user " + req.getUserId());
                }
                totalExact = totalExact.add(req.getAmount());
            }

            if (totalExact.compareTo(totalAmount) != 0) {
                throw new InvalidExpenseException("Sum of exact split amounts (" + totalExact + ") does not equal total expense amount (" + totalAmount + ")");
            }

            for (ExpenseSplitRequest req : request.getSplits()) {
                splits.add(new ExpenseSplit(expenseId, req.getUserId(), req.getAmount(), null));
            }
        }

        return splits;
    }

    public List<ExpenseResponse> getGroupExpenses(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new GroupNotFoundException("Group not found with id: " + groupId);
        }

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        return expenses.stream().map(e -> {
            List<ExpenseSplit> splits = expenseSplitRepository.findByExpenseId(e.getId());
            return mapToExpenseResponse(e, splits);
        }).collect(Collectors.toList());
    }

    public GroupBalanceDTO getGroupBalances(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new GroupNotFoundException("Group not found with id: " + groupId);
        }

        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        Map<Long, BigDecimal> netBalances = new HashMap<>();
        for (GroupMember member : members) {
            netBalances.put(member.getUserId(), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        for (Expense expense : expenses) {
            Long paidBy = expense.getPaidByUserId();
            BigDecimal amountPaid = expense.getAmount();

            // Payer balance increases (credited)
            netBalances.put(paidBy, netBalances.getOrDefault(paidBy, BigDecimal.ZERO).add(amountPaid));

            List<ExpenseSplit> splits = expenseSplitRepository.findByExpenseId(expense.getId());
            for (ExpenseSplit split : splits) {
                Long userId = split.getUserId();
                BigDecimal splitAmount = split.getAmount();

                // Split participant balance decreases (debited)
                netBalances.put(userId, netBalances.getOrDefault(userId, BigDecimal.ZERO).subtract(splitAmount));
            }
        }

        return new GroupBalanceDTO(groupId, netBalances);
    }

    public UserBalanceSummaryResponse getUserBalanceSummary(Long userId) {
        validateUserExists(userId);

        // Find all expenses paid by user
        List<Expense> paidExpenses = expenseRepository.findByPaidByUserId(userId);
        // Find all splits where user is involved
        List<ExpenseSplit> userSplits = expenseSplitRepository.findByUserId(userId);

        BigDecimal totalPaid = paidExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalShareInPaid = BigDecimal.ZERO;
        for (Expense e : paidExpenses) {
            List<ExpenseSplit> splits = expenseSplitRepository.findByExpenseId(e.getId());
            for (ExpenseSplit split : splits) {
                if (split.getUserId().equals(userId)) {
                    totalShareInPaid = totalShareInPaid.add(split.getAmount());
                }
            }
        }

        // Amount owed to user by others = total paid by user - user's own share in those expenses
        BigDecimal totalOwedTo = totalPaid.subtract(totalShareInPaid);

        // Total amount user owes to others = sum of user's splits in expenses paid by OTHER users
        BigDecimal totalOwed = BigDecimal.ZERO;
        for (ExpenseSplit split : userSplits) {
            Expense expense = expenseRepository.findById(split.getExpenseId()).orElse(null);
            if (expense != null && !expense.getPaidByUserId().equals(userId)) {
                totalOwed = totalOwed.add(split.getAmount());
            }
        }

        BigDecimal netBalance = totalOwedTo.subtract(totalOwed);

        return new UserBalanceSummaryResponse(
                userId,
                totalOwed.setScale(2, RoundingMode.HALF_UP),
                totalOwedTo.setScale(2, RoundingMode.HALF_UP),
                netBalance.setScale(2, RoundingMode.HALF_UP)
        );
    }

    private GroupResponse mapToGroupResponse(Group group, List<Long> memberIds) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedBy(),
                memberIds,
                group.getCreatedAt()
        );
    }

    private ExpenseResponse mapToExpenseResponse(Expense expense, List<ExpenseSplit> splits) {
        List<ExpenseSplitResponse> splitResponses = splits.stream()
                .map(s -> new ExpenseSplitResponse(s.getUserId(), s.getAmount(), s.getPercentage()))
                .collect(Collectors.toList());

        return new ExpenseResponse(
                expense.getId(),
                expense.getGroupId(),
                expense.getPaidByUserId(),
                expense.getAmount(),
                expense.getDescription(),
                expense.getSplitType(),
                splitResponses,
                expense.getCreatedAt()
        );
    }
}
