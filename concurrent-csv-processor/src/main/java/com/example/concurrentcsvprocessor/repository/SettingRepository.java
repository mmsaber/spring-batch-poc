package com.example.concurrentcsvprocessor.repository;

import com.example.concurrentcsvprocessor.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingRepository extends JpaRepository<Setting, String> {
}
