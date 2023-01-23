package com.zerobase.dividend.scraper;

import com.zerobase.dividend.model.Company;
import com.zerobase.dividend.model.Dividend;
import com.zerobase.dividend.model.ScrapedResult;
import com.zerobase.dividend.model.constants.Month;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class YahooFinanceScraper implements Scraper{

	private static final String STATISTIC_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
	private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s"; // 회사명 가져오기 위한 url
	private static final long START_TIME = 86400; // 60초 * 60분 * 24시간

	@Override
	public ScrapedResult scrap (Company company) {
		var scrapResult = new ScrapedResult();
		scrapResult.setCompany(company);

		try {
			long now = System.currentTimeMillis() / 1000;
			String url = String.format(STATISTIC_URL, company.getTicker(), START_TIME, now);
			Connection connection = Jsoup.connect(url);
			Document document = connection.get();

			Elements parsingDivs = document.getElementsByAttributeValue("data-test",
				"historical-prices");
			Element tableElements = parsingDivs.get(0);

			Element tbody = tableElements.children().get(1);

			List<Dividend> dividends = new ArrayList<>();
			for (Element e : tbody.children()) {
				String txt = e.text();
				if (!txt.endsWith("Dividend")) {
					continue;
				}
				String[] splits = txt.split(" ");
				int month = Month.strToNumber(splits[0]);
				int day = Integer.valueOf(splits[1].replace(",", ""));
				int year = Integer.valueOf(splits[2]);
				String dividend = splits[3];

				if (month < 0) {
					throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
				}

				dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));
			}

			scrapResult.setDividends(dividends);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return scrapResult;
	}

	@Override
	public Company scrapCompanyByTicker(String ticker) {

		String url = String.format(SUMMARY_URL, ticker, ticker);

		try {
			Document document = Jsoup.connect(url).get();
			Element titleElement = document.getElementsByTag("h1").get(0);
			// 데이터의 특성에 따라 split 조건이 달라질 수 있음
			String title = titleElement.text().split(" - ")[1].trim();

			return new Company(ticker, title);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}
