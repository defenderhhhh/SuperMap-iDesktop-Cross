package com.supermap.desktop.ui.icloud;


import com.supermap.desktop.Application;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.ui.UICommonToolkit;
import com.supermap.desktop.ui.icloud.api.LicenseService;
import com.supermap.desktop.ui.icloud.commontypes.*;
import com.supermap.desktop.utilities.ComputerUtilities;
import com.supermap.desktop.utilities.SystemPropertyUtilities;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by xie on 2016/12/23.
 * 许可管理类
 */
public class LicenseManager {
    private static final String licDirctory = "C:\\Program Files\\Common Files\\SuperMap\\License";
    private static boolean hasOffLineLicense = false;

    /**
     * 获取离线许可文件
     *
     * @return
     */
    public static File getOffLineLicense() {
        File result = null;
        File licDir = null;
        if (SystemPropertyUtilities.isWindows()) {
            licDir = new File(licDirctory);
        } else {
            //linux系统暂不处理
            licDir = new File("");
        }
        if (licDir.exists() && licDir.isDirectory()) {
            File[] files = licDir.listFiles();
            int size = files.length;
            if (size > 0) {
                result = files[0];
                hasOffLineLicense = true;
            }
        }

        return result;
    }

    /**
     * 判断离线许可是否过期
     *
     * @return
     */
    public static boolean isOffLineLicenseOverdue() {
        boolean result = false;
        if (hasOffLineLicense) {
            File licFile = getOffLineLicense();
            FileInputStream stream = null;
            BufferedReader br;
            try {
                stream = new FileInputStream(licFile);
                br = new BufferedReader(new InputStreamReader(stream));
                String tempstr = "";
                while ((tempstr = br.readLine()) != null) {
                    if (tempstr.contains("<end>")) {
                        tempstr = tempstr.substring(tempstr.indexOf(">") + 1, tempstr.lastIndexOf("<"));
                        break;
                    }
                }
                if (compareDate(tempstr) == 1) {
                    result = true;
                }
            } catch (IOException e) {
                Application.getActiveApplication().getOutput().output(e);
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    Application.getActiveApplication().getOutput().output(e);
                }
            }
        }
        return result;
    }

    /**
     * 判断日期大小
     *
     * @param endStr
     * @return
     */
    private static int compareDate(String endStr) {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = df.format(date);
        try {
            Calendar calendar = Calendar.getInstance();
            int year = Integer.parseInt(endStr.substring(0, 4));
            int moth = Integer.parseInt(endStr.substring(4, 6));
            int day = Integer.parseInt(endStr.substring(6, 8));
            calendar.set(year, moth, day);
            Date endDate = calendar.getTime();
            Date dfDate = df.parse(dateStr);
            if (dfDate.getTime() > endDate.getTime()) {
                return 1;
            } else if (dfDate.getTime() < endDate.getTime()) {
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    /**
     * 是否存在离线许可
     *
     * @return
     */
    public static boolean hasOffLineLicense() {
        return getOffLineLicense() != null;
    }


    /**
     * 申请试用许可,许可可用将生成本地文件来存储试用许可以便启动桌面
     *
     * @param licenseService
     * @return
     */
    public static ApplyTrialLicenseResponse applyTrialLicense(LicenseService licenseService) {
        ServiceResponse<ApplyTrialLicenseResponse> response = null;
        try {
            ServiceResponse<QueryTrialLicenseResponse> queryTrialResult = licenseService.query(new QueryTrialLicenseRequest()
                    .productType(ProductType.ICLOUDMANAGER).version(Version.VERSION_8C));
            if (queryTrialResult.code == ServiceResponse.Code.SUCCESS) {
                //查询成功,获取许可状态
                //试用许可权限
                if (queryTrialResult.data.trial == true) {
                    //申请试用许可
                    ApplyTrialLicenseRequest request = new ApplyTrialLicenseRequest();
                    request.days = 90;
                    request.software.productType = ProductType.ICLOUDMANAGER;
                    request.software.version = Version.VERSION_8C;
                    request.machine.name = ComputerUtilities.getComputerName();
                    request.machine.macAddr = ComputerUtilities.getMac();
                    response = licenseService.apply(request);
                    //用于归还的试用许可信息
                    System.out.println(response.data.returnId);
                    System.out.println(response.data.license);
                }

            } else if (queryTrialResult.code == ServiceResponse.Code.LOGININ_OTHERPLAINT) {
                UICommonToolkit.showMessageDialog(CommonProperties.getString("String_LoginOnOtherPath"));
            }
        } catch (IOException e) {
            UICommonToolkit.showMessageDialog(CommonProperties.getString("String_ApplyTrialLicenseFalure"));
        }

        return response.data;
    }

    /**
     * 申请正式许可
     *
     * @param licenseService 自定义许可服务类
     * @param licenseId      查询得到的许可id
     * @return
     */
    public static ApplyFormalLicenseResponse applyFormalLicense(LicenseService licenseService, LicenseId licenseId) {
        ServiceResponse<ApplyFormalLicenseResponse> response = null;
        try {
            ApplyFormalLicenseRequest request = new ApplyFormalLicenseRequest();
            request.days = 2;
            request.licenseId = licenseId;
            request.software.productType = ProductType.ICLOUDMANAGER;
            request.software.version = Version.VERSION_8C;
            request.machine.name = ComputerUtilities.getComputerName();
            request.machine.macAddr = ComputerUtilities.getMac();
            response = licenseService.apply(request);
            System.out.println(response.data.returnId);
            System.out.println(response.data.license);
        } catch (IOException e) {
            UICommonToolkit.showConfirmDialog(CommonProperties.getString("String_ApplyFormalLicenseFalure"));
        }
        return response.data;
    }

    /**
     * 查询许可id
     *
     * @param licenseService
     * @return
     */
    public static LicenseId getFormalLicenseId(LicenseService licenseService) {
        boolean hasFormalLicense = false;
        LicenseId licenseId = null;
        ServiceResponse<QueryFormalLicenseResponse> queryFormalResult = null;
        try {
            queryFormalResult = licenseService.query(new QueryFormalLicenseRequest()
                    .productType(ProductType.ICLOUDMANAGER).version(Version.VERSION_8C).pageCount(10));
            if (queryFormalResult.code == ServiceResponse.Code.SUCCESS) {
                //查询成功，获取许可状态
                //正式许可个数
                int licenseCount = queryFormalResult.data.licenseCount;
                if (licenseCount > 0) {
                    System.out.println(queryFormalResult.data.licenseCount);
                    int falseDays = 0;
                    for (int i = 0; i < licenseCount; i++) {
                        LicenseInfo licenseInfo = queryFormalResult.data.licenses[i];
                        System.out.println(licenseInfo.id);
                        System.out.println(licenseInfo.days);
                        int remainDays = licenseInfo.remainDays;
                        if (remainDays == 0) {
                            falseDays++;
                        }
                        System.out.println(licenseInfo.remainDays);
                        System.out.println(licenseInfo.snId);
                    }
                    if (falseDays == licenseCount) {
                        UICommonToolkit.showMessageDialog(CommonProperties.getString("String_FormLicenseOverdue"));
                        hasFormalLicense = false;
                    } else {
                        hasFormalLicense = true;
                    }
                }
            }
        } catch (IOException e) {
            UICommonToolkit.showMessageDialog(CommonProperties.getString("String_PermissionCheckFailed"));
        }
        if (hasFormalLicense == true) {
            licenseId = queryFormalResult.data.licenses[0].id;
        }
        return licenseId;
    }

//    /**
//     * 创建本地许可
//     *
//     * @param license
//     */
//    public static void buildLicense(String license) {
//        if (null != license) {
//            FileOutputStream stream = null;
//            try {
//                byte[] trialLicenseData = license.getBytes();
//                File licFile = new File(licDirctory + ComputerUtilities.getComputerName() + "_8C.lic");
//                if (licFile.exists()){
//                    licFile.delete();
//                }
//                stream = new FileOutputStream(new File(licDirctory + ComputerUtilities.getComputerName() + "_8C.lic"));
//                stream.write(trialLicenseData);
//                stream.flush();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    stream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }
//    }
}
