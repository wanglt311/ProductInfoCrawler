package com.liting;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.List;

import static com.liting.Utility.errInfo;

/**
 * Created by wangltlily311 on 8/9/17.
 */
public class Main {

    public static void main(String[] args) {
        // write your code here

        System.out.println("*****************************************************************************");

        ProductInfoCrawler crawlerTest = new ProductInfoCrawler();
        crawlerTest.initProxy();
        //crawlerTest.getProds("Prenatal DHA", 3.4, 8040, 10, 1);

        //amazonCrawler.getAmazonProds("LED TV");
        ObjectMapper mapper = new ObjectMapper();
        String rawQueryPath = "/Users/wangltlily311/TigerTech/ProductInfoCrawler/src/resource/rawQuery3.txt";
        //System.out.println("please enter the path for adsDataString: ");
        String adsDataPath = "/Users/wangltlily311/TigerTech/ProductInfoCrawler/src/resource/AdsDataDetail.json";

        File fileInput = new File(rawQueryPath);
        File fileOutput = new File(adsDataPath);
        BufferedReader reader = null;
        try {
            if (!fileOutput.exists()) {
                fileOutput.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(fileOutput.getAbsoluteFile());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            System.out.println("read file by line");
            reader = new BufferedReader(new FileReader(fileInput));
            String line = null;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                System.out.println("line " + line + ": " + line);
                lineNum++;
                if (line.isEmpty()) {
                    continue;
                }
                System.out.println("this line is: " + line);
                String[] fields = line.split(",");
                String query = fields[0].trim();
                double bidPrice = Double.parseDouble(fields[1].trim());
                int campaignId = Integer.parseInt(fields[2].trim());
                int queryGroupId = Integer.parseInt(fields[3].trim());

                List<Ad> ads= crawlerTest.getProds(query, bidPrice, campaignId, queryGroupId, 3);
                for (Ad ad : ads) {
                    String jsonInString = mapper.writeValueAsString(ad);
                    bufferedWriter.write(jsonInString);
                    bufferedWriter.newLine();
                }
                Thread.sleep(3000);

            }
            reader.close();
            bufferedWriter.close();
        }
//        catch (IOException e) {
//            e.printStackTrace();
//            errInfo(e);
//        }
        catch (EOFException e) {
            //the end of file have been reached
            e.printStackTrace();
        }
        catch (FileNotFoundException e) {
            //the specified file not exist
            e.printStackTrace();
        }
        catch (ObjectStreamException e) {
            //the file is corrupted
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            errInfo(e);
        }
        catch (IOException e) {
            //some other I/O error occurred
            e.printStackTrace();
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    errInfo(e);
                }
            }
        }
    }
}

