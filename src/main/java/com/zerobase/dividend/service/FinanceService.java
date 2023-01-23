package com.zerobase.dividend.service;

import com.zerobase.dividend.exception.impl.NoCompanyException;
import com.zerobase.dividend.model.Company;
import com.zerobase.dividend.model.Dividend;
import com.zerobase.dividend.model.ScrapedResult;
import com.zerobase.dividend.model.constants.CacheKey;
import com.zerobase.dividend.persist.CompanyRepository;
import com.zerobase.dividend.persist.DividendRepository;
import com.zerobase.dividend.persist.entity.CompanyEntity;
import com.zerobase.dividend.persist.entity.DividendEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

	private final CompanyRepository companyRepository;
	private final DividendRepository dividendRepository;

	// 캐싱전 생각해야 되는 요소
	// 1. 요청이 빈번한가?
	// 2. 자주 변경이 되는 데이터인가?

	@Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
	public ScrapedResult getDividendByCompanyName(String companyName){
		log.info("search company: " + companyName);

		// 회사명을 기준으로 회사 정보를 조회
		CompanyEntity company = this.companyRepository.findByName(companyName)
												.orElseThrow(() -> new NoCompanyException());

		// 조회된 회사 ID 로 배당금 정보 조회
		List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(company.getId());

		// 조회된 배당금 정보 조합 후 반환
		List<Dividend> dividends = dividendEntities.stream()
													.map(e -> new Dividend(e.getDate(), e.getDividend()))
													.collect(Collectors.toList());

		//for 문 사용
//		List<Dividend> dividends = new ArrayList<>();
//		for (var entity : dividendEntities) {
//			dividends.add(new Dividend.(entity.getDate(), entity.getDividend()));
//		}



		return new ScrapedResult(new Company(company.getTicker(), company.getName()), dividends);
	}
}
