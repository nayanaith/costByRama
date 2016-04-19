package com.cost.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.cost.dto.CostRecaptureDTO;

/**
 * Cost recapture automated process
 *
 * @author Rameshkumar.Venkatachalam
 */
public class CostRecapture {

    private static List<CostRecaptureDTO> costRecaptureErrorList;

    private static List<CostRecaptureDTO> costRecaptureList;

    private static String ENV = "SIT";

    private static String PRD_ERROR_FILE = "Cost_Recapture_PRD_Error.xlsx";

    private static String PRD_INPUT_FILENAME = "Cost_Recapture_PRD.xlsx";

    private static String SIT_ERROR_FILE = "Cost_Recapture_SIT_Error.xlsx";

    private static String SIT_INPUT_FILENAME = "Cost_Recapture_SIT.xlsx";

    /**
     * @param row
     * @param offeringCode
     * @param orderCenter
     * @param scheduleGroup
     * @param effectiveDate
     * @param message
     */
    private static void createCells(final XSSFRow row,
                                    final String offeringCode,
                                    final String orderCenter,
                                    final String scheduleGroup,
                                    final String effectiveDate,
                                    final String profileId,
                                    final String costZeroFlg,
                                    final String message) {
        final XSSFCell cell1 = row.createCell(0);
        final XSSFCell cell2 = row.createCell(1);
        final XSSFCell cell3 = row.createCell(2);
        final XSSFCell cell4 = row.createCell(3);
        final XSSFCell cell5 = row.createCell(4);
        final XSSFCell cell6 = row.createCell(5);
        final XSSFCell cell7 = row.createCell(6);
        cell1.setCellValue(offeringCode == null ? "" : offeringCode);
        cell2.setCellValue(orderCenter == null ? "" : orderCenter);
        cell3.setCellValue(scheduleGroup == null ? "" : scheduleGroup);
        cell4.setCellValue(effectiveDate == null ? "" : effectiveDate);
        cell5.setCellValue(profileId == null ? "" : profileId);
        cell6.setCellValue(costZeroFlg == null ? "" : costZeroFlg);
        cell7.setCellValue(message);
    }

    private static boolean getBooleanValue(final String costZeroFlag) {

        boolean isCostZero = false;
        if (StringUtils.isNotEmpty(costZeroFlag)) {
            if ("Y".equalsIgnoreCase(costZeroFlag) || "YES".equalsIgnoreCase(costZeroFlag)) {
                isCostZero = true;
            }
        }

        return isCostZero;
    }

    public static boolean isAlertPresent(final WebDriver driver) {

        boolean presentFlag = false;
        try {
            // Check the presence of alert
            driver.switchTo().alert();
            // Alert present; set the flag
            presentFlag = true;
        } catch (final NoAlertPresentException ex) {
            // ignore the alert
        }
        return presentFlag;
    }

    public static void main(final String[] args) throws InterruptedException {

        try {
            readData();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        final File file = new File("chromedriver.exe");

        System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());

        final WebDriver driver = new ChromeDriver();
        // And now use this to find the ENV is SIT or PRD
        if ("PRD".equals(ENV)) {
            driver.get("http://my.gfs.com/cost/initialMaintainCapturedProfiledCosts");
        } else {
            driver.get("http://mysit.gfs.com/cost/initialMaintainCapturedProfiledCosts");
        }

        for (final CostRecaptureDTO costRecaptureDTO : costRecaptureList) {

            if (validateData(costRecaptureDTO)) {

                // Find the text input element by its name
                final WebElement itemElement = driver.findElement(By.name("itemCode"));

                // Enter something to search for
                itemElement.clear();

                // Enter something to search for
                itemElement.sendKeys(costRecaptureDTO.getOfferingCode());

                if (CollectionUtils.isNotEmpty(costRecaptureDTO.getOrderCenter())) {
                    // Find the text input element by its name
                    final WebElement allCheckBox = driver.findElement(By.name("checkAllOrderCentersCB"));
                    allCheckBox.click();

                    // Find the text input element by its name
                    final List<WebElement> orderCenterOptions = driver.findElements(By.name("orderCenter"));

                    for (final String orderCenter : costRecaptureDTO.getOrderCenter()) {
                        for (final WebElement option : orderCenterOptions) {
                            if (option.getAttribute("value").equals(orderCenter)) {
                                option.click();
                                break;
                            }
                        }
                    }
                }

                final WebElement select = driver.findElement(By.xpath("//select"));
                final List<WebElement> allOptions = select.findElements(By.tagName("option"));
                for (final WebElement option : allOptions) {
                    if (option.getAttribute("value").equals(costRecaptureDTO.getScheduleGroup())) {
                        option.click();
                        break;
                    }
                }

                // Find the text input element by its name
                final WebElement dateElement = driver.findElement(By.name("effectiveDate"));

                dateElement.clear();
                // Enter something to search for
                dateElement.sendKeys(costRecaptureDTO.getEffectiveDate());

                driver.findElement(By.name("Inquire")).click();

                if (isAlertPresent(driver)) {
                    final String message = driver.switchTo().alert().getText();
                    driver.switchTo().alert().accept();
                    costRecaptureDTO.setMessage(message);
                    costRecaptureErrorList.add(costRecaptureDTO);
                    revokeFlag(driver, costRecaptureDTO);
                    continue;
                }

                try {
                    driver.findElement(By.name("goBack")).click();
                    final String message = "Cost profile fields contained invalid data";
                    costRecaptureDTO.setMessage(message);
                    costRecaptureErrorList.add(costRecaptureDTO);
                    revokeFlag(driver, costRecaptureDTO);
                    continue;
                } catch (final NoSuchElementException e) {
                    // Ignore
                }

                try {
                    driver.findElement(By.name("create")).click();
                } catch (final NoSuchElementException e) {
                    // ignore
                }

                try {
                    final List<WebElement> recalcOptions = driver.findElements(By.name("ReCalculate"));
                    // Update cost value as 0 for the given profile id
                    if (costRecaptureDTO.isCostZero()) {
                        Map<Integer, String> profileIdPostionMap = new HashMap<Integer, String>();

                        int profileIdCount = 1;
                        for (final WebElement option : recalcOptions) {
                            profileIdPostionMap.put(profileIdCount, option.getAttribute("value"));
                            profileIdCount++;
                        }

                        int profileIdSize = recalcOptions.size();
                        final List<WebElement> newCostOptions = driver.findElements(By.name("newCost"));
                        int costZeroTotalCount = newCostOptions.size();
                        int countPerRow = costZeroTotalCount / profileIdSize;
                        for (final String profileId : costRecaptureDTO.getProfileIdList()) {
                            for (final Entry<Integer, String> entry : profileIdPostionMap.entrySet()) {
                                if (Objects.equals(profileId, entry.getValue())) {
                                    int startPosition = ((entry.getKey() - 1) * countPerRow);
                                    for (int i = startPosition; i < startPosition + countPerRow; i++) {
                                        newCostOptions.get(i).clear();
                                        newCostOptions.get(i).sendKeys("0");
                                    }
                                }
                            }
                        }
                        driver.findElement(By.name("UpdateTop")).click();
                    } else {

                        if (CollectionUtils.isEmpty(costRecaptureDTO.getProfileIdList())) {
                            driver.findElement(By.name("SelectAllButton")).click();
                        } else {
                            boolean isValidProfileId = false;
                            for (final String profileId : costRecaptureDTO.getProfileIdList()) {
                                for (final WebElement option : recalcOptions) {
                                    if (option.getAttribute("value").equals(profileId)) {
                                        option.click();
                                        isValidProfileId = true;
                                    }
                                }
                            }

                            if (!isValidProfileId) {
                                final String message = "Invalid Cost profile Id";
                                costRecaptureDTO.setMessage(message);
                                costRecaptureErrorList.add(costRecaptureDTO);
                                revokeFlag(driver, costRecaptureDTO);
                                continue;
                            }
                        }

                        driver.findElement(By.name("RecalcButtonTop")).click();

                        if (isAlertPresent(driver)) {
                            driver.switchTo().alert().accept();
                        }

                        driver.findElement(By.name("UpdateTop")).click();
                    }
                } catch (final NoSuchElementException e) {
                    costRecaptureDTO.setMessage("No captured cost data was found");
                    costRecaptureErrorList.add(costRecaptureDTO);
                    revokeFlag(driver, costRecaptureDTO);
                    continue;
                }

                if (isAlertPresent(driver)) {
                    driver.switchTo().alert().accept();
                }

                revokeFlag(driver, costRecaptureDTO);

                System.out.println("loop is done");
            }
        }

        if (!CollectionUtils.isEmpty(costRecaptureErrorList)) {
            String errorFileName = null;
            if ("PRD".equals(ENV)) {
                errorFileName = PRD_ERROR_FILE;
            } else {
                errorFileName = SIT_ERROR_FILE;
            }
            writeCostRecaptureErrorList(costRecaptureErrorList, errorFileName);
        }

        driver.quit();
    }

    /**
     * Read the input excel file
     *
     * @throws Exception
     */
    private static void readData() throws Exception {

        try {

            costRecaptureList = new ArrayList<CostRecaptureDTO>();
            costRecaptureErrorList = new ArrayList<CostRecaptureDTO>();

            File file = new File(SIT_INPUT_FILENAME);

            if (!file.exists()) {
                file = new File(PRD_INPUT_FILENAME);
                ENV = "PRD";
                if (!file.exists()) {
                    System.out.println("No input file to process");
                    System.exit(0);
                }
            }

            final FileInputStream fileStream = new FileInputStream(file);

            // Get the workbook instance for XLSX file
            final XSSFWorkbook workbook = new XSSFWorkbook(fileStream);

            // Get first sheet from the workbook
            final XSSFSheet sheet = workbook.getSheetAt(0);

            // Iterate through each rows from first sheet
            final Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                final CostRecaptureDTO costRecapture = new CostRecaptureDTO();
                final Row row = rowIterator.next();

                if (0 == row.getRowNum()) {
                    continue;
                }

                // For each row, iterate through each columns
                try {
                    costRecapture.setOfferingCode(row.getCell(0) != null ? String.valueOf((int) row.getCell(0)
                                                                                                   .getNumericCellValue())
                            : null);
                } catch (final Exception e) {
                    costRecapture.setOfferingCode(row.getCell(0).toString());
                }

                costRecapture.setOrderCenter(row.getCell(1) != null ? Arrays.asList(row.getCell(1)
                                                                                       .toString()
                                                                                       .split(","))
                        : new ArrayList<String>());
                try {
                    costRecapture.setScheduleGroup(row.getCell(2) != null ? String.valueOf((int) row.getCell(2)
                                                                                                    .getNumericCellValue())
                            : null);
                } catch (final Exception e) {
                    costRecapture.setScheduleGroup(row.getCell(2).toString());
                }

                costRecapture.setEffectiveDate(row.getCell(3) != null ? row.getCell(3).toString() : null);

                costRecapture.setProfileIdList(row.getCell(4) != null ? Arrays.asList(row.getCell(4)
                                                                                         .toString()
                                                                                         .split(","))
                        : new ArrayList<String>());

                costRecapture.setCostZero(row.getCell(5) != null ? getBooleanValue(row.getCell(5).toString()) : false);

                costRecaptureList.add(costRecapture);
            }
            fileStream.close();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static void revokeFlag(final WebDriver driver, final CostRecaptureDTO costRecaptureDTO) {
        // Find the text input element by its name
        final WebElement allCheckBox = driver.findElement(By.name("checkAllOrderCentersCB"));
        if (!allCheckBox.isSelected()) {
            allCheckBox.click();
        }
        // // // Find the text input element by its name
        // final List<WebElement> orderCenterOptionNew = driver.findElements(By.name("orderCenter"));
        //
        // for (final WebElement optionNew : orderCenterOptionNew) {
        // if (optionNew.getAttribute("value").equals(costRecaptureDTO.getOrderCenter())) {
        // optionNew.click();
        // break;
        // }
        // }
    }

    private static boolean validateData(final CostRecaptureDTO costRecapture) {

        boolean isValid = true;

        final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

        if (StringUtils.isEmpty(costRecapture.getOfferingCode())) {
            costRecapture.setMessage("Offering Code cannot be empty");
            isValid = false;
        }

        if (StringUtils.isEmpty(costRecapture.getScheduleGroup())) {
            costRecapture.setMessage("Schedule Group cannot be empty");
            isValid = false;
        }

        if (StringUtils.isNotEmpty(costRecapture.getEffectiveDate())) {
            try {
                dateFormat.parse(costRecapture.getEffectiveDate());
            } catch (final ParseException e) {
                costRecapture.setMessage("Invalid date. It must be in MM/dd/yyyy format");
                isValid = false;
            }
        }

        if (!isValid) {
            costRecaptureErrorList.add(costRecapture);
        }
        return isValid;
    }

    /**
     * @param costRecaptureList
     */
    public static void writeCostRecaptureErrorList(final List<CostRecaptureDTO> costRecaptureList,
                                                   final String errorFile) {
        final XSSFWorkbook xlsxBook = new XSSFWorkbook();
        final XSSFSheet sheet = xlsxBook.createSheet("Result");
        FileOutputStream fos = null;
        try {
            int rowCount = 0;
            final XSSFRow headerRow = sheet.createRow(rowCount);
            createCells(headerRow,
                        "Offering Code",
                        "Order Center",
                        "Schedule Group",
                        "Effective Date",
                        "Profile Id",
                        "Cost Zero Flag",
                        "Message");
            final XSSFFont font = xlsxBook.createFont();
            final XSSFCellStyle cellStyle = xlsxBook.createCellStyle();
            font.setBold(true);
            cellStyle.setFont(font);
            headerRow.setRowStyle(cellStyle);
            for (final CostRecaptureDTO costRecaptureDTO2 : costRecaptureList) {
                final CostRecaptureDTO costRecaptureDTO = costRecaptureDTO2;
                String orderCenter = "";
                int orderCenterCount = 0;
                for (final String str : costRecaptureDTO2.getOrderCenter()) {
                    if (orderCenterCount == 0) {
                        orderCenter = orderCenter + str;
                    } else {
                        orderCenter = orderCenter + "," + str;
                    }
                    orderCenterCount++;
                }

                String profileId = "";
                int profileIdCount = 0;
                for (final String str : costRecaptureDTO2.getProfileIdList()) {
                    if (profileIdCount == 0) {
                        profileId = profileId + str;
                    } else {
                        profileId = profileId + "," + str;
                    }
                    profileIdCount++;
                }

                final XSSFRow sheetRow = sheet.createRow(++rowCount);
                createCells(sheetRow,

                            costRecaptureDTO.getOfferingCode(),
                            orderCenter,
                            costRecaptureDTO.getScheduleGroup(),
                            costRecaptureDTO.getEffectiveDate(),
                            profileId,
                            costRecaptureDTO.isCostZero() ? "Y" : "N",
                            costRecaptureDTO.getMessage());

            }
            fos = new FileOutputStream(errorFile);
            xlsxBook.write(fos);
        } catch (final Exception e) {
            System.out.println(e);
        } finally {

            if (fos != null) {
                try {
                    fos.close();
                } catch (final IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
