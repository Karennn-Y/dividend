package com.zerobase.dividend.persist;

import com.zerobase.dividend.persist.entity.DividendEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DividendRepository extends JpaRepository<DividendEntity, Long> {

	// 회사 아이디로 조회
	List<DividendEntity> findAllByCompanyId(Long companyId);

	// 배당금 삭제 회사ID로
	@Transactional
	void deleteAllByCompanyId(Long companyId);

	boolean existsByCompanyIdAndDate(Long companyId, LocalDateTime date);

}
