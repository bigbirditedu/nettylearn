package com.bigbird.netty.httpdemo4;


import javax.activation.MimetypesFileTypeMap;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class WebUtils {

    final private static String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    final private static String HTTP_DATE_GMT_TIMEZONE = "GMT";

    final private static String CHARSET_TAG = "charset=";

    final private static MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();

    final static ThreadLocal<SimpleDateFormat> df_tl = new ThreadLocal<SimpleDateFormat>() {
        public SimpleDateFormat initialValue() {
            SimpleDateFormat df = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            df.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));
            return df;
        }
    };

    static public Date parseDate(String ifModifiedSince) {
        try {
            return df_tl.get().parse(ifModifiedSince);
        } catch (Exception e) {
            return null;
        }
    }

    static public String formatDate(Date dt) {
        return df_tl.get().format(dt);
    }

    static public String getContentType(String filename) {
        return mimeTypes.getContentType(filename);
    }

    static public String toHeaderName(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            if (i > 0 && ch >= 'A' && ch <= 'Z')
                b.append("-").append(ch);
            else
                b.append(ch);
        }
        return b.toString();
    }

    static public String parseCharSet(String s) {
        s = s.toLowerCase();
        int p = s.indexOf(CHARSET_TAG);
        if (p >= 0) {
            p += CHARSET_TAG.length();
            int p2 = s.indexOf(";", p);
            if (p2 >= 0)
                return s.substring(p, p2);
            else
                return s.substring(p);
        } else {
            return WebConstants.DefaultCharSet;
        }
    }

    static public String decodeUrl(String s) {
        try {
            return URLDecoder.decode(s, WebConstants.DefaultCharSet);
        } catch (Exception e) {
            return s;
        }
    }

    static public String encodeUrl(String s) {
        try {
            return URLEncoder.encode(s, WebConstants.DefaultCharSet);
        } catch (Exception e) {
            return s;
        }
    }

}
