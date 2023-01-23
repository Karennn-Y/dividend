package com.zerobase.dividend.service;


import com.zerobase.dividend.exception.impl.AlreadyExistCompanyException;
import com.zerobase.dividend.exception.impl.NoCompanyException;
import com.zerobase.dividend.model.Company;
import com.zerobase.dividend.model.ScrapedResult;
import com.zerobase.dividend.persist.CompanyRepository;
import com.zerobase.dividend.persist.DividendRepository;
import com.zerobase.dividend.persist.entity.CompanyEntity;
import com.zerobase.dividend.persist.entity.DividendEntity;
import com.zerobase.dividend.scraper.Scraper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;


@Service
@AllArgsConstructor
public class CompanyService {

	private final Trie trie;
	private final Scraper yahooFinanceScraper;
	private final CompanyRepository companyRepository;
	private final DividendRepository dividendRepository;

	public Company save(String ticker) {
		boolean exists = this.companyRepository.existsByTicker(ticker);

		if (exists) {
			throw new AlreadyExistCompanyException();
		}
		return this.storeCompanyAndDividend(ticker);
	}

	public Page<CompanyEntity> getAllCompany(Pageable pageable) {
		return this.companyRepository.findAll(pageable);
	}

	private Company storeCompanyAndDividend(String ticker) {
		// ticker 를 기준으로 회사를 스크래핑
		Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);
		if (ObjectUtils.isEmpty(company)) {
			throw new RuntimeException("failed to scrap ticker -> " + ticker);
		}

		// 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
		ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

		// 스크래핑 결과
		CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
		List<DividendEntity> dividendEntityList= scrapedResult.getDividends().stream()
													.map(e -> new DividendEntity(companyEntity.getId(), e))
													.collect(Collectors.toList());

		this.dividendRepository.saveAll(dividendEntityList);
		return company;
	}

	public List<String> getCompanyNamesByKeyword(String keyword) {
		Pageable limit = PageRequest.of(0, 10);
		Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);
		return companyEntities.stream()
					.map(e -> e.getName())
					.collect(Collectors.toList());
	}

	// 자동완성
	public void addAutocompleteKeyword(String keyword) {
		this.trie.put(keyword, null);
	}
	// 자동완성으로 회사찾기
	public List<String> autocomplete(String keyword) {
		return (List<String>) this.trie.prefixMap(keyword).keySet()
			.stream()
//			.limit(10) // 데이터가 많은 경우
			.collect(Collectors.toList());
	}

	// 트라이에 저장된 키워드 삭제
	public void	deleteAutocompleteKeyword(String keyword) {
		this.trie.remove(keyword);
	}

	public String deleteCompany(String ticker) {
		var company = this.companyRepository.findByTicker(ticker)
											.orElseThrow(() -> new NoCompanyException());
		// 배당금정보 삭제
		this.dividendRepository.deleteAllByCompanyId(company.getId());
		// 회사 정보 삭제
		this.companyRepository.delete(company);
		// trie 안에있는 회사 이름 삭제
		this.deleteAutocompleteKeyword(company.getName());

		return company.getName();
	}
}
