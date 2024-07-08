package zerobase.dividend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ScrapedResult {
    private Company company; // 스크래핑한 회사 정보

    private List<Dividend> dividends;

    public ScrapedResult(){
        this.dividends = new ArrayList<>();
    }
}
