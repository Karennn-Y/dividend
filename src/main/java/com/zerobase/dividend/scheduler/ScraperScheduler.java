package com.zerobase.dividend.scheduler;

import com.zerobase.dividend.model.Company;
import com.zerobase.dividend.model.ScrapedResult;
import com.zerobase.dividend.model.constants.CacheKey;
import com.zerobase.dividend.persist.CompanyRepository;
import com.zerobase.dividend.persist.DividendRepository;
import com.zerobase.dividend.persist.entity.CompanyEntity;
import com.zerobase.dividend.persist.entity.DividendEntity;
import com.zerobase.dividend.scraper.Scraper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@EnableCaching
@Component
public class ScraperScheduler {

	private final CompanyRepository companyRepository;
	private final DividendRepository dividendRepository;
	private final Scraper yahooFinanceScraper;

	// 캐시 비우기 위한 어노테이션
	@CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
	// 일정 주기마다 수행
	@Scheduled(cron = "${scheduler.scrap.yahoo}")
	public void yahooFinanceScheduling() {
		log.info("scraping scheduler is started");
		// 저장된 회사 목록을 조회
		List<CompanyEntity> companies = this.companyRepository.findAll();
		// 회사마다 배당금 정보를 새로 스크래핑
		for (var company : companies) {
			log.info("scraping scheduler is started -> " + company.getName());
			ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(
								new Company(company.getName(), company.getTicker())
			);
			// 스크래핑한 배당금 정보 중 데이터베이스에 없는 값은 저장
			scrapedResult.getDividends().stream()
				// 배당금 모델을 베당금 엔티티로 매핑
				.map(e -> new DividendEntity(company.getId(), e))
				// 각각의 배당금들을 하나씩 배당금 레파지토리에 삽입
				.forEach(e -> {
					boolean exists = this.dividendRepository.existsByCompanyIdAndDate(
																e.getCompanyId(), e.getDate());
					if (!exists) {
						this.dividendRepository.save(e);
						log.info("insert new dividend -> " + e.toString());
					}
				});
			// 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지 (과부화 예방)
			try {
				Thread.sleep(3000); // 3초간 스레드 정지
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

	}
}
