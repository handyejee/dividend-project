package zerobase.dividend.scraper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.Dividend;
import zerobase.dividend.model.ScrapedResult;
import zerobase.dividend.model.constants.Month;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper{

    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history/?frequency=1mo&period1=%d&period2=%d";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s/?p=%s";
    //https://finance.yahoo.com/quote/MMM/?p=MMM

    private static final long START_TIME = 86400; // 1일 60(초) * 60(분) * 24(시간)

    @Override
    public ScrapedResult scrap(Company company) {
        var scrapResult = new ScrapedResult();
        scrapResult.setCompany(company);

        try {
            long now = System.currentTimeMillis() / 1000; //초 단위로 바꿈

            String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);
            System.out.println("url " + url);
            Connection connection = Jsoup.connect(url)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0");
            Document document = connection.get();

            Elements parsingDivs = document.getElementsByClass("table svelte-ewueuo");

            if (!parsingDivs.isEmpty()) {
                Element table = parsingDivs.first();

                assert table != null;
                Element tbody = table.selectFirst("tbody");

                assert tbody != null;
                List<Dividend> dividends = new ArrayList<>();

                for(Element e : tbody.children()){
                    String txt = e.text();
                    if (!txt.endsWith("Dividend")){
                        continue;
                    }
                    String[] splits = txt.split(" ");
                    int month = Month.strToNumber(splits[0]);
                    int day = Integer.parseInt(splits[1].replace(",", ""));
                    int year = Integer.parseInt(splits[2]);
                    String dividend = splits[3];

                    if(month < 0) {
                        throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
                    }

                    dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));

                }
                scrapResult.setDividends(dividends);

            } else {
                System.out.println("No table found with the specified class.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scrapResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker){ // ticker 받아서 company 돌려주기
        String url = String.format(SUMMARY_URL, ticker, ticker);

        try {
            Document document = Jsoup.connect(url).get();
            System.out.println(url);
            Element titleEle = document.getElementsByTag("h1").get(1);
            System.out.println(titleEle);

            String fullTitle = titleEle.text();
            String title = fullTitle.split(" \\(")[0];

            System.out.println("Company Name: " + title);

            return new Company(ticker, title);

        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
