package com.zerobase.dividend.persist;

import com.zerobase.dividend.persist.entity.CompanyEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {

	// 이미 저장된 회사인지 check
	boolean existsByTicker(String ticker);

	// 회사 명으로 조회
	Optional<CompanyEntity> findByName(String name);

	// ticker로 회사 조회
	Optional<CompanyEntity> findByTicker(String ticker);

	// like 연산자
	Page<CompanyEntity> findByNameStartingWithIgnoreCase(String s, Pageable pageable);
}
