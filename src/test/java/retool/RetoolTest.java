package retool;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

import io.restassured.response.Response;

import static io.restassured.RestAssured.*;

public class RetoolTest extends LaunchBrowser {

    int seconds = 10;
    String apiName = "order";
    int totalNumOfRows = 5;

    @Test
    public void runRetoolApiGenerationTest() throws InterruptedException {
        String column1Title = "Name";
        String column2Title = "OrderCount";
        String column3Title = "Email";
        String column4Title = "ProductId";
        Map<String, String> column1 = new HashMap<>();
        column1.put("name", column1Title);
        column1.put("type", "People / Full Name");
        Map<String, String> column2 = new HashMap<>();
        column2.put("name", column2Title);
        column2.put("type", "Numbers / Random");
        Map<String, String> column3 = new HashMap<>();
        column3.put("name", column3Title);
        column3.put("type", "People / Email Address");
        Map<String, String> column4 = new HashMap<>();
        column4.put("name", column4Title);
        column4.put("type", "Numbers / Product ID");
        List<Map<String, String>> rowsList = new ArrayList<>();
        List<String> headerNames = new ArrayList<>();
        rowsList.add(column1);
        rowsList.add(column2);
        rowsList.add(column3);
        rowsList.add(column4);
        headerNames.add("id");
        headerNames.add(column1Title);
        headerNames.add(column2Title);
        headerNames.add(column3Title);
        headerNames.add(column4Title);
        driver.get(url);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        driver.switchTo().frame(driver.findElement(By.xpath("//iframe[@title=\"REST API Generator\"]")));
        for (int i = 0; i < rowsList.size(); i++) {
            addColumns(rowsList.get(i), i);
        }
        updateApiName(apiName);
        updateRowsValue(totalNumOfRows);
        generateApi();
        String apiUrl = getGeneratedApiUrl();
        verifyAPI(apiUrl);
        verifyPreview(headerNames, totalNumOfRows);
        Thread.sleep(500);
    }

    public void addColumns(Map<String, String> row, int index) throws InterruptedException {
        String ulList = "//div[(contains(@class, 'ant-cascader-menus')) and not(contains(@class, 'ant-cascader-menus-hidden'))]//ul[@class='ant-cascader-menu']";
        if (index != 0) {
            WebElement addCol = driver.findElement(By.xpath("//div[@data-testid=\"addColumn--0\"]"));
            addCol.click();
            Thread.sleep(500);
        }
        WebElement columnTitle = new WebDriverWait(driver, Duration.ofSeconds(seconds))
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='input_name--" + index + "']")));
        while (!columnTitle.getAttribute("value").equals("")) {
            columnTitle.sendKeys(Keys.BACK_SPACE);
        }
        columnTitle.sendKeys(row.get("name"));
        String indexValue = index == 0 ? "" : "[" + index + "]";
        WebElement selection = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@data-testid=\"RetoolGrid:input_type" + indexValue + "\"]//span[contains(@class, 'ant-cascader-picker')]//input")));
        selection.click();
        Thread.sleep(500);
        String[] types = row.get("type").split("/");

        for (Integer i = 0; i < types.length; i++) {
            WebElement item = driver.findElement(By.xpath("" + ulList + "//li[contains(@title, '" + types[i].trim() + "')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", item);
        }
    }

    public void updateApiName(String apiName) {
        WebElement apiInput = driver.findElement(By.xpath("//input[@id=\"input_api_name--0\"]"));
        while (!apiInput.getAttribute("value").equals("")) {
            apiInput.sendKeys(Keys.BACK_SPACE);
        }
        apiInput.sendKeys(apiName);
    }

    public void updateRowsValue(int numberOfRows) {
        WebElement row = driver.findElement(By.xpath("//input[@id=\"input_rows--0\"]"));
        while (!row.getAttribute("value").equals("")) {
            row.sendKeys(Keys.BACK_SPACE);
        }
        row.sendKeys("" + numberOfRows + "");
    }

    public void generateApi() throws InterruptedException {
        WebElement generateButton = driver.findElement(By.xpath("//div[@data-testid=\"RetoolGrid:button_generate\"]//div[@id=\"button_generate--0\"]"));
        generateButton.click();
        Thread.sleep(2000);
    }

    public void verifyPreview(List<String> headerValues, int numOfRows) {
        // Verify Header names
        List<WebElement> headerColumns = driver.findElements(By.xpath("//div[@data-testid=\"table-generated_table\"]//div[@class=\"rt-thead -header\"]//div[contains(@class, 'rt-th')]"));
        Assert.assertTrue(headerColumns.size() == headerValues.size());
        for (int i = 0; i < headerColumns.size(); i++) {
            String headerText = headerColumns.get(i).getText().trim();
            Assert.assertTrue(headerValues.contains(headerText));
        }

        // Verify Number of rows
        List<WebElement> rows = driver.findElements(By.xpath("//div[@data-testid=\"table-generated_table\"]//div[contains(@class, 'rt-tr-group')]"));
        Assert.assertTrue(rows.size() == numOfRows);

        // Verify table data with orders.json file
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("src/test/orders.json")) {
            Object obj = jsonParser.parse(reader);
            org.json.simple.JSONArray list = (org.json.simple.JSONArray) obj;
            JSONArray ordersList = new JSONArray(list);
            Assert.assertTrue(ordersList.length() == numOfRows);
            Map<Number, String> columnPositions = new HashMap();
            List<WebElement> columns = driver.findElements(By.xpath("//div[@class=\"ReactTable\"]//span[@class=\"column-title\"]"));
            for (int i = 0; i < columns.size(); i++) {
                columnPositions.put(i, columns.get(i).getText());
            }

            for (int i = 0; i < columnPositions.size(); i++) {
                for (int j = 0; j < ordersList.length(); j++) {
                    String columnName = columnPositions.get(i);
                    new JSONObject();
                    JSONObject rowEntryDetails;
                    rowEntryDetails = (JSONObject) ordersList.get(j);
                    String entryText = driver.findElement(By.xpath("//div[@data-testid=\"generated_table-" + columnName + "-" + j + "\"]")).getText();
                    if (rowEntryDetails.get(columnName) instanceof String) {
                        Assert.assertTrue(entryText.equals(rowEntryDetails.get(columnName)));
                    } else if (rowEntryDetails.get(columnName) instanceof Number) {
                        Assert.assertTrue(entryText.equals(rowEntryDetails.get(columnName).toString()));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getGeneratedApiUrl() {
        WebElement apiHyperlink = driver.findElement(By.xpath("//div[@data-testid=\"RetoolGrid:endpoint_text\"]//a"));
        String api = apiHyperlink.getAttribute("href");
        return api;
    }

    public void verifyAPI(String apiName) {
        int expectedStatusCode = 200;
        Response response = given().
                when().get(apiName);
        response.then().statusCode(200);
        Assert.assertEquals(expectedStatusCode, response.statusCode());
        String body = response.asPrettyString();
        JSONArray jsonArray = new JSONArray(body);

        //Write to external file
        try {
            FileWriter file = new FileWriter("src/test/orders.json");
            file.write(jsonArray.toString());
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
