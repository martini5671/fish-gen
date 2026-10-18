package com.martini.fish_gen.repository;

import com.martini.fish_gen.model.dao.TokenUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenUsageRepository extends JpaRepository<TokenUsage, Long> {
}
