package com.zerobase.dividend;

import com.zerobase.dividend.model.Company;
import com.zerobase.dividend.scraper.Scraper;
import com.zerobase.dividend.scraper.YahooFinanceScraper;
import java.io.IOException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DividendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DividendApplication.class, args);

	}
}
