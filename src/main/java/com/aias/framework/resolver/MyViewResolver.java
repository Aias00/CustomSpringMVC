package com.aias.framework.resolver;


import com.aias.framework.servlet.MyModelAndView;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyViewResolver {
    private String viewName;
    private File file;
    Pattern pattern = Pattern.compile("@\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);

    public String getViewName() {
        return viewName;
    }

    public MyViewResolver(String viewName, File file) {
        this.viewName = viewName;
        this.file = file;
    }

    public String parse(MyModelAndView mv) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        RandomAccessFile ra = null;
        // 模板框架的语法是非常复杂的
        // 但是原理是一样的
        // 都是用正则表达式来处理字符串
        try {
            ra = new RandomAccessFile(this.file, "r");
            String line = null;
            while (null != (line = ra.readLine())) {
                Matcher m = getMatcher(line);
                while (m.find()) {
                    for (int i = 1; i <= m.groupCount(); i++) {
                        String paramName = m.group(i);
                        Object paramValue = mv.getModel().get(paramName);
                        if (null == paramValue) {
                            continue;
                        }
                        line = line.replaceAll("@\\{" + paramName + "\\}", paramValue.toString());
                    }
                }
                stringBuffer.append(line);
            }
        } finally {
            if (null != ra) {
                ra.close();
            }
        }
        return stringBuffer.toString();
    }

    private Matcher getMatcher(String str) {
        Matcher matcher = pattern.matcher(str);
        return matcher;
    }

}
