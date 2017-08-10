package com.liting;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.*;

import static com.liting.Utility.cleanedTokenize;
import static com.liting.Utility.errInfo;

/**
 * Created by wangltlily311 on 8/9/17.
 */
public class ProductInfoCrawler {private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36";
    private static final String AMAZON_QUERY_URL = "https://www.amazon.com/s/ref=nb_sb_noss?field-keywords=";
    private final String authUser = "bittiger";
    private final String authPassword = "cs504";
    private static int adId = 0;
    final static Logger logger = Logger.getLogger(ProductInfoCrawler.class);
    private static Set<String> queriesSet = new HashSet<String>();

    public void initProxy() {
//        System.setProperty("socksProxyHost", "199.101.97.161"); // set socks proxy server
//        System.setProperty("socksProxyPort", "61336"); // set socks proxy port

        System.setProperty("http.proxyHost", "199.101.97.161"); // set proxy server
        System.setProperty("http.proxyPort", "60099"); // set proxy port
        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword);
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                authUser, authPassword.toCharArray());
                    }
                }
        );
    }

    public static List<Ad> getProds(String query, double bidPrice, int campaignId, int queryGroupId, int pageNum) {
        List<Ad> adsForThisQuery = new ArrayList<Ad>();
        String[] queryTerms = query.split(" ");
        String queryString = new String();
        for (String queryTerm : queryTerms) {
            queryString += queryTerm + "+";
        }
        queryString = queryString.substring(0, (queryString.length() - 1));
        System.out.println("queryString: " + queryString);
        //String urlbase = AMAZON_QUERY_URL + query + "&page=";
        //implements paging
        //https://www.amazon.com/s/ref=nb_sb_ss_c_1_6?field- keywords=nikon+d3400&page=2
        //String pageString = "&page=";
        //System.out.println("url: " + urlbase);
        //String url2 = "https://www.amazon.com/Garden-Life-Prenatal-Omega-Supplement/dp/B001U9I0M0/ref=sr_1_1?ie=UTF8&qid=1498791161&sr=8-1-spons&keywords=Prenatal%2BDHA&th=1";

        try {
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            //headers.put("Accept-Encoding", "gzip, deflate, br");
            //headers.put("Accept-Encoding", "gzip, deflate");

            for (int page = 1; page <= pageNum; page++) {
                String url = AMAZON_QUERY_URL + queryString + "&page=" + Integer.toString(page);

                System.out.println("this is page: " + page);
                System.out.println("url: " + url);
                if (!queriesSet.contains(url)) {

                    Document document = Jsoup.connect(url).maxBodySize(0).headers(headers).userAgent(USER_AGENT).timeout(10000).get();
                    //Document document = Jsoup.connect(url).maxBodySize(0).userAgent(USER_AGENT).timeout(10000).get();
                    //System.out.println(document.body().text());

                    //Elements results = document.select("li[data-asin]");
                    //Elements results = document.select("#s-results-list-atf");
                    Elements results = document.getElementsByClass("s-result-item celwidget ");
                    //System.out.println("results: " + results);
                    //#s-results-list-atf
                    //#s-results-list-atf
                    //#s-results-list-atf
                    //#s-results-list-atf
                    System.out.println("results size(): " + results.size());
                    //System.out.println("results: " + results);
                    int resultsLen = results.size();

                    for (int i = 0; i < resultsLen; i++) {
                        //System.out.println(results.get(i));
                        int index = getResultIndex(results.get(i));
                        System.out.println("the index of result is: " + index);
                        //System.out.println("index: " + index);
                        //List<String> keywords = crawlKeywords(document, index);
                        //System.out.println(keywords.toString());
                        //String category = crawlCategory(document);
                        //System.out.println("category: " + category);
                        //String titleString = crawlTitle(document, index);
                        //System.out.println("title is: " + titleString);
                        //double price = crawlPrice(document, index);
                        //double price = crawlPrice(document);
                        //System.out.println("price: " + price);
                        //String brandString = crawlBrand(document, index);
                        //System.out.println("brand: " + brandString);
                        //String thumbnailString = crawlThumbnail(document, index);
                        //System.out.println("thumbnail: " + thumbnailString);
                        //String detailUrl = crawlDetailUrl(document, index);
                        //System.out.println("detailUrl: " + detailUrl);
                        Ad ad = updateAd(document, results.get(i), index, query, bidPrice, campaignId, queryGroupId);
                        //ad.adId
                        System.out.println("Ad :" + ad);
                        adsForThisQuery.add(ad);
                        //System.out.println("List<Ad> size: " + adsForThisQuery.size());
                        queriesSet.add(url);
                    }
                }
                else {
                    continue;
                }

            }



        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Sorry, something wrong!", e);
            errInfo(e);
        }
        System.out.println("adsForThisQuery: " + adsForThisQuery.toString());
        System.out.println("List<Ad> size: " + adsForThisQuery.size());
        return adsForThisQuery;
    }

    private static Ad updateAd(Document document, Element result, int index, String query, double bidPrice, int campaignId, int queryGroupId) throws IOException {
        Ad ad = new Ad();
        ad.query = query;
        ad.bidPrice = bidPrice;
        ad.campaignId = campaignId;
        ad.query_group_id = queryGroupId;
        ad.brand = crawlBrand(document, index);
        ad.category = crawlCategory(document);
        ad.costPerClick = 0.0;
        ad.description = null;
        ad.title = crawlTitle(document, index);
        ad.detail_url = crawlDetailUrl(document, index);
        ad.keyWords = crawlKeywords(document, index);
        ad.prodId = crawlProdId(result);
        ad.adId = ProductInfoCrawler.adId++;
        ad.pClick = 0.0;
        ad.position = 0;
        ad.relevanceScore = 0.0;
        ad.rankScore = 0.0;
        ad.qualityScore = 0.0;
        ad.thumbnail = crawlThumbnail(document, index);
        //ad.price = 0.0;
        ad.price = crawlPrice(document, index);
        //System.out.println("price: " + ad.price);
        return ad;
    }

    //get each ad's result index for further crawl
    private static int getResultIndex(Element result) {
        //String adId = result.attr("data-asin");
        //System.out.println("adId: " + adId);
        String idString = result.attr("id");
        System.out.println("in getResultIndex resultId : " + idString);
        if (idString == null || idString.isEmpty()) {
            return -1;
        }
        try {
            String parts[] = idString.split("_");
            System.out.println("the result num in #result is: " + parts[1]);
            return Integer.parseInt(parts[1]);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            logger.error("Sorry, number format wrong!", e);
            return -1;
        }
    }

    private static String crawlProdId(Element result) {
        String adIdString = result.attr("data-asin");
        return adIdString;
    }

    private static String crawlCategory(Document document) {
        Element categoryEle = document.select("#leftNavContainer > ul:nth-child(2) > div > li:nth-child(1) > span > a > h4").first();
        return categoryEle.text();
    }

    private static String crawlTitle(Document document, int index) {
        String titleString = new String();
        List<String> titleList = new ArrayList<String>();
        titleList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div.a-row.a-spacing-none.scx-truncate-medium.sx-line-clamp-2 > a > h2");
        titleList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a > h2");
        try {
            for (String title : titleList) {
                //String titlePath = "#result_" + Integer.toString(index) + title;
                Element titleEle = document.select("#result_" + Integer.toString(index) + title).first();
                if (titleEle != null) {
                    //System.out.println("titleEle: " + titleEle.text());
                    titleString = titleEle.text();
                    break;
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Sorry, error occurred in function crawlTitle", e);
        }

        return titleString;
    }

    private static List<String > crawlKeywords(Document document, int index) {
        //#result_4 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div.a-row.a-spacing-none.scx-truncate-medium.sx-line-clamp-2 > a > h2
        //#result_0 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div.a-row.a-spacing-none.scx-truncate-medium.sx-line-clamp-2 > a > h2
        List<String> titleList = new ArrayList<String>();
        titleList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div.a-row.a-spacing-none.scx-truncate-medium.sx-line-clamp-2 > a > h2");
        titleList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a > h2");
        List<String> keywords = new ArrayList<String>();
        try {
            for (String title : titleList) {
                //String titlePath = "#result_" + Integer.toString(index) + title;
                Element titleEle = document.select("#result_" + Integer.toString(index) + title).first();
                if (titleEle != null) {
                    //System.out.println("titleEle: " + titleEle.text());
                    keywords = cleanedTokenize(titleEle.text());
                    //System.out.println("keywords: " + keywords.toString());
                    break;
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Sorry, error occurred in function crawlKeywords", e);
        }
        return keywords;
    }

    private static double crawlPrice(Document document, int index) {
        //#result_0 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(4) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing
        //#result_1 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(4) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing
        //#result_2 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span
        //#result_3 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(2) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing
        //#result_4 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing
        //#result_5 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(2) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span

//        String PricePath = " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span";
//        Element prodPrice = document.select("#result_" + Integer.toString(index) + PricePath).first();
//        //System.out.println("prodPrice: " + prodPrice.text());
//        if (prodPrice != null) {
//            String prodPriceString = prodPrice.attr("aria-label").replace("$", "");
//            System.out.println("prodPriceString: " + prodPriceString);
//            return Double.parseDouble(prodPriceString);
//        }
//        else {
//            return 0.0;
//        }

        //#result_0 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(4) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > span
        //#result_0 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(4) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > sup.sx-price-fractional

        //#result_1 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(4) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > span
        //#result_1 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(4) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > sup.sx-price-fractional

        //#result_2 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span > span > span
        //#result_2 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span > span > sup.sx-price-fractional

        //#result_3 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(2) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > span
        //#result_3 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(2) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > sup.sx-price-fractional

        //#result_4 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > span
        //#result_4 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > sup.sx-price-fractional

        //#result_5 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(2) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span > span > span
        //#result_5 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(2) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span > span > sup.sx-price-fractional

        //#result_6 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(2) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span > span > span
        //#result_6 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(2) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span > span > sup.sx-price-fractional

        //#result_10 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(2) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span > span > span

        Map<String, String> wholeFractMap = new HashMap<String, String>();
        wholeFractMap.put(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(4) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > span",
                " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(4) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > sup.sx-price-fractional");
        wholeFractMap.put(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span > span > span",
                " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span > span > sup.sx-price-fractional");
        wholeFractMap.put(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(2) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span > span > span",
                " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(2) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span > span > sup.sx-price-fractional");
        wholeFractMap.put(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(2) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > span",
                " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(2) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > sup.sx-price-fractional");
        wholeFractMap.put(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > span",
                " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > sup.sx-price-fractional");
        wholeFractMap.put(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(4) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > span",
                " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(4) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > sup.sx-price-fractional");

        try {
            for (String mapEle : wholeFractMap.keySet()) {
                String priceWholePath = "#result_" + Integer.toString(index) + mapEle;
                String priceFracPath = "#result_" + Integer.toString(index) + wholeFractMap.get(mapEle);
                Element wholePriceEle = document.select(priceWholePath).first();
                Element fracPriceEle = document.select(priceFracPath).first();
                if (wholePriceEle != null && fracPriceEle != null) {
                    String wholePriceString = wholePriceEle.text();
                    String fracPriceString = fracPriceEle.text();
                    if (wholePriceString.contains(",")) {
                        wholePriceString = wholePriceString.replace(",", "");
                        return Double.parseDouble(wholePriceString) + Double.parseDouble(fracPriceString) / 100.0;
                    }
                    else {
                        return Double.parseDouble(wholePriceString) + Double.parseDouble(fracPriceString) / 100.0;
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Sorry, something wrong!", e );
        }

        return 0.0;
    }

    private static String crawlBrand(Document document, int index) {
        String brand = " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(2) > span:nth-child(2)";
        String brandPath = "#result_" + Integer.toString(index) + brand;
        Element brandString = document.select(brandPath).first();
        if (brandString != null) {
            return brandString.text();
        }
        else {
            return null;
        }
    }

    private static String crawlThumbnail(Document document, int index) {
        //#result_7 > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a > img
        //#result_8 > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a > img
        //#result_4 > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a > img
        //#result_3 > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a > img
        //#result_0 > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a > img
        //#result_19 > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a > img
        String imgPart = " > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a > img";
        String thumbnailPath = "#result_" + Integer.toString(index) + imgPart;
        Element thumbnail = document.select(thumbnailPath).first();
        if (thumbnail != null) {
            return thumbnail.attr("src");
        }
        else {
            return null;
        }
    }

    private static String crawlDetailUrl(Document document, int index) {
        //#result_1 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a
        //#result_0 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div.a-row.a-spacing-none.scx-truncate-medium.sx-line-clamp-2 > a
        //#result_2 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div.a-row.a-spacing-none.scx-truncate-medium.sx-line-clamp-2 > a
        List<String> DetailList = new ArrayList<String>();
        DetailList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a");
        DetailList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div.a-row.a-spacing-none.scx-truncate-medium.sx-line-clamp-2 > a");
        for (String detail : DetailList) {
            String detailPath = "#result_" + Integer.toString(index) + detail;
            Element detailUrlEle = document.select(detailPath).first();
            if (detailUrlEle != null) {
                if (detailUrlEle.text().contains("https://www.amazon.com/")) {
                    String detailUrl = detailUrlEle.attr("href");
                    return detailUrl;
                }
                else {
                    return "https://www.amazon.com" + detailUrlEle.attr("href");
                }
            }
        }
        return null;

    }

//    private static List<String> cleanData(String string) throws IOException{
//        String[] stopWords = new String[] {".", ",", "\"", "'", "?", "!", ":", ";", "(", ")", "[", "]", "{", "}", "&", "/", "...", "-", "+", "*", "|", "),"};
//        String stopWordsString = "a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your";
//        StandardAnalyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_40);
//        TokenStream tokenStream = new StandardTokenizer(Version.LUCENE_40, new StringReader(string));
//        List<String> res = new ArrayList<>();
//        tokenStream = new StopFilter(Version.LUCENE_40, tokenStream, standardAnalyzer.STOP_WORDS_SET);
//        tokenStream = new StopFilter(Version.LUCENE_40, tokenStream, StopFilter.makeStopSet(Version.LUCENE_40, stopWords, true));
//        CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
//        while (tokenStream.incrementToken()) {
//            res.add(token.toString().toLowerCase());
//        }
//        return res;
//    }
}

