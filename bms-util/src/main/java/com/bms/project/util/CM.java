package com.bms.project.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * @version 1.0.0
 * @author <a href=" ">liqiang</a>
 */
public class CM {

    public static int toInt(Object str) {
        if (str == null) {
            return 0;
        }
        if (str instanceof Integer) {
            return (Integer) str;
        } else if (str instanceof Long) {
            Long n = (Long) str;
            return n.intValue();
        } else if (str instanceof Double) {
            Double n = (Double) str;
            return n.intValue();
        } else if (str instanceof BigDecimal) {
            BigDecimal n = (BigDecimal) str;
            return n.intValue();
        }
        try {
            return Integer.parseInt(str.toString().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public static String toString(Object o) {
        return toString(o, "");
    }

    public static String toStringTrim(Object o) {
        return toString(o).replaceAll(" ", "");
    }

    public static String toString(Object o, String defaultStr) {
        if (o == null) {
            return defaultStr;
        }
        String str = o.toString();
        if ("".equals(str.trim())) {
            return defaultStr;
        }
        return str;
    }

    public static long toLong(Object str) {
        if (str == null) {
            return 0;
        }
        if (str instanceof Integer) {
            Integer n = (Integer) str;
            return n.longValue();
        } else if (str instanceof Long) {
            Long n = (Long) str;
            return n;
        } else if (str instanceof Double) {
            Double n = (Double) str;
            return n.longValue();
        } else if (str instanceof BigDecimal) {
            BigDecimal n = (BigDecimal) str;
            return n.longValue();
        }
        try {
            return Long.parseLong(str.toString().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 将Unicode转换为UTF8
     *
     * @param instr
     *            String
     * @return String 返回UTF-8字符串
     */
    public static String convertUnicode2UTF8Byte(String instr) {
        if (instr == null) {
            return "";
        }
        int len = instr.length();
        byte[] abyte = new byte[len << 2];
        int j = 0;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char c = instr.charAt(i);
            if (c < 0x80) {
                abyte[j++] = (byte) c;
                str.append(c);
            } else if (c < 0x0800) {
                abyte[j++] = (byte) (((c >> 6) & 0x1F) | 0xC0);
                str.append("%").append(Integer.toHexString(abyte[j - 1]).substring(6));
                abyte[j++] = (byte) ((c & 0x3F) | 0x80);
                str.append("%").append(Integer.toHexString(abyte[j - 1]).substring(6));
            } else if (c < 0x010000) {
                abyte[j++] = (byte) (((c >> 12) & 0x0F) | 0xE0);
                str.append("%").append(Integer.toHexString(abyte[j - 1]).substring(6));
                abyte[j++] = (byte) (((c >> 6) & 0x3F) | 0x80);
                str.append("%").append(Integer.toHexString(abyte[j - 1]).substring(6));
                abyte[j++] = (byte) ((c & 0x3F) | 0x80);
                str.append("%").append(Integer.toHexString(abyte[j - 1]).substring(6));
            } else if (c < 0x200000) {
                abyte[j++] = (byte) (((c >> 18) & 0x07) | 0xF8);
                str.append("%").append(Integer.toHexString(abyte[j - 1]).substring(6));
                abyte[j++] = (byte) (((c >> 12) & 0x3F) | 0x80);
                str.append("%").append(Integer.toHexString(abyte[j - 1]).substring(6));
                abyte[j++] = (byte) (((c >> 6) & 0x3F) | 0x80);
                str.append("%").append(Integer.toHexString(abyte[j - 1]).substring(6));
                abyte[j++] = (byte) ((c & 0x3F) | 0x80);
                str.append("%").append(Integer.toHexString(abyte[j - 1]).substring(6));
            }
        }
        return str.toString();
    }

    public static boolean isNew(Map map) {
        return "isNew".equals(map.get("_rowcheck"));
    }

    public static double toDouble(Object str) {
        if (str == null) {
            return 0;
        }
        if (str instanceof Integer) {
            Integer n = (Integer) str;
            return n.doubleValue();
        } else if (str instanceof Long) {
            Long n = (Long) str;
            return n.doubleValue();
        } else if (str instanceof Double) {
            Double n = (Double) str;
            return n;
        } else if (str instanceof BigDecimal) {
            BigDecimal n = (BigDecimal) str;
            return n.doubleValue();
        }

        try {
            return Double.parseDouble(str.toString().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public static Double toDoubleObj(Object str) {
        if (str == null) {
            return (double) 0;
        }
        if (str instanceof Double) {
            return (Double) str;
        }
        return CM.toDouble(str);
    }

    public static Integer toIntegerObj(Object str) {
        if (str == null) {
            return 0;
        }
        if (str instanceof Integer) {
            return (Integer) str;
        }
        return CM.toInt(str);
    }

    public static Long toLongObj(Object str) {
        if (str == null) {
            return 0L;
        }
        if (str instanceof Long) {
            return (Long) str;
        }
        return CM.toLong(str);
    }

    public static String java2sqlName(Object obj) {
        if (obj == null) {
            return null;
        }
        String s = obj.toString();
        StringBuilder sqlBuffer = new StringBuilder(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) >= 'A' && s.charAt(i) <= 'Z') {
                sqlBuffer.append('_');
            }
            sqlBuffer.append(s.charAt(i));
        }
        return sqlBuffer.toString().toLowerCase();
    }

    public static String sql2JavaName(Object obj) {
        if (obj == null) {
            return null;
        }
        String s = obj.toString();
        StringBuilder strBuffer = new StringBuilder(s.length());
        boolean bf = false;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '_') {
                bf = true;
                continue;
            }
            if (bf) {
                strBuffer.append(Character.toUpperCase(s.charAt(i)));
                bf = false;
            } else {
                strBuffer.append(Character.toLowerCase(s.charAt(i)));
            }
        }
        return strBuffer.toString();
    }

    public static String stringNoNull(Object obj) {
        if (obj == null) {
            return "";
        }
        return obj.toString();
    }

    public static String stringNoNull(Object obj, String defaultStr) {
        if (obj == null) {
            return defaultStr;
        }
        return obj.toString();
    }

    public static void main(String[] args) {

        System.out.println(CM.formatNumber(3, "000"));
    }

    public static String formatNumber(Object d) {
        if (d == null) {
            return "0.00";
        }
        String retStr = "0.00";
        try {
            DecimalFormat dFormat = new DecimalFormat("#0.00");
            if (d instanceof String) {
                retStr = dFormat.format(Double.valueOf(d.toString()));
            } else {
                retStr = dFormat.format(d);
            }
            return retStr;
        } catch (Exception e) {
            return retStr;
        }
    }

    public static String formatNumber(Object d, String format) {
        if (d == null) {
            d = (double) 0;
        }
        String retStr = "0";
        try {
            DecimalFormat dFormat = new DecimalFormat(format);
            if (d instanceof String) {
                retStr = dFormat.format(Double.valueOf(d.toString()));
            } else {
                retStr = dFormat.format(d);
            }
            return retStr;
        } catch (Exception e) {
            return retStr;
        }
    }

    public static String formatDate() {
        return CM.formatDate("yyyy-MM-dd");
    }

    public static String formatDate(Object o) {
        if (o instanceof String) {
            return CM.formatDate(new Date(), o.toString());
        } else {
            return CM.formatDate(o, "yyyy-MM-dd");
        }
    }

    public static String formatDate(Object o, String format) {
        String retStr = "";
        try {
            Date dt = null;
            if (o instanceof java.util.Calendar) {
                java.util.Calendar cal = (java.util.Calendar) o;
                dt = cal.getTime();
            } else if (o instanceof Date) {
                dt = (Date) o;
            } else {
                return "Error Object";
            }
            DateFormat dFormat = new SimpleDateFormat(format);
            retStr = dFormat.format(dt);
            return retStr;
        } catch (Exception e) {
            return retStr;
        }
    }

    public static String formatNumber(double d) {
        String retStr = "0.00";
        try {
            DecimalFormat dFormat = new DecimalFormat("#0.00");
            retStr = dFormat.format(d);
            return retStr;
        } catch (Exception e) {
            return retStr;
        }
    }

    public static String formatNumber(double d, String format) {
        String retStr = "0";
        try {
            DecimalFormat dFormat = new DecimalFormat(format);
            retStr = dFormat.format(d);
            return retStr;
        } catch (Exception e) {
            return retStr;
        }
    }

    public static Date toDate(Object strDate) {
        return CM.toDate(strDate, "yyyy-MM-dd");
    }

    public static Date toDate(Object strDate, String format) {
        if (strDate == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(strDate.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public static void setDefault(Map map, String key, String defaultValue) {
        Object obj = map.get(key);
        if (obj == null || "".equals(obj)) {
            map.put(key, defaultValue);
        }
    }

    /**
     * Pads a String <code>s</code> to take up <code>n</code> characters,
     * padding with char <code>c</code> on the left (<code>true</code>) or on
     * the right (<code>false</code>). Returns <code>null</code> if passed a
     * <code>null</code> String.
     **/
    public static String paddingString(String s, int n, char c, boolean paddingLeft) {
        if (s == null) {
            return null;
        }
        int add = n - s.length();
        if (add <= 0) {
            return s;
        }
        StringBuilder str = new StringBuilder(s);
        char[] ch = new char[add];
        Arrays.fill(ch, c);
        if (paddingLeft) {
            str.insert(0, ch);
        } else {
            str.append(ch);
        }
        return str.toString();
    }

}
