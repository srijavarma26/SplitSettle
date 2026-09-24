package com.splitsettle.settlementservice.repository;

import com.splitsettle.settlementservice.entity.SettlementRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRecordRepository extends JpaRepository<SettlementRecord, Long> {
    List<SettlementRecord> findByGroupId(Long groupId);
}
