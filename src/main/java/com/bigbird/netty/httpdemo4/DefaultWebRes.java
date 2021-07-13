package com.bigbird.netty.httpdemo4;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultWebRes {
    HttpVersion version;
    boolean keepAlive;
    boolean isHeadMethod;

    int httpCode = 200;
    HttpHeaders headers;
    List<Cookie> cookies;
    String content;

    Map<String, Object> results;

    int logRetCode = Integer.MIN_VALUE;

    public DefaultWebRes(DefaultWebReq req, int httpCode) {
        this.version = req.getVersion();
        this.keepAlive = req.isKeepAlive();
        this.isHeadMethod = req.isHeadMethod();
        this.httpCode = httpCode;
    }

    public String getStringResult(String key) {
        if (results == null) return null;
        Object o = results.get(key);
        if (o == null) return null;
        if ((o instanceof String)) return (String) o;
        return o.toString();
    }

    public String getCookie(String name) {
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (c.name().equals(name)) return c.value();
        }
        return null;
    }

    public DefaultWebRes setLogRetCode(int logRetCode) {
        if (logRetCode > 0) return this;
        this.logRetCode = logRetCode;
        return this;
    }

    public int getLogRetCode() {
        return logRetCode;
    }

    public String getHeader(String name) {
        if (headers == null) return null;
        return headers.get(name);
    }

    public DefaultWebRes setHeader(String name, String value) {
        if (headers == null) headers = new DefaultHttpHeaders();
        headers.set(name, value);
        return this;
    }

    public DefaultWebRes setContentType(String contentType) {
        if (headers == null) headers = new DefaultHttpHeaders();
        headers.set(HttpHeaderNames.CONTENT_TYPE, contentType);
        return this;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public String getContentType() {
        if (headers == null) return null;
        String contentTypeStr = headers.get(HttpHeaderNames.CONTENT_TYPE);
        if (contentTypeStr == null) return null;
        int p = contentTypeStr.indexOf(";");
        return p >= 0 ? contentTypeStr.substring(0, p) : contentTypeStr;
    }

    public String getCharSet() {
        if (headers == null) return null;
        String contentTypeStr = headers.get(HttpHeaderNames.CONTENT_TYPE);
        if (contentTypeStr == null) return WebConstants.DefaultCharSet;
        int p = contentTypeStr.indexOf(";");
        return p >= 0 ? WebUtils.parseCharSet(contentTypeStr.substring(p + 1)) : WebConstants.DefaultCharSet;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public DefaultWebRes setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
        return this;
    }

    public HttpVersion getVersion() {
        return version;
    }

    public DefaultWebRes setVersion(HttpVersion version) {
        this.version = version;
        return this;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public DefaultWebRes setHttpCode(int httpCode) {
        this.httpCode = httpCode;
        return this;
    }

    public String getContent() {
        return content;
    }

    public DefaultWebRes setContent(String content) {
        this.content = content;
        return this;
    }

    public Map<String, Object> getResults() {
        if (results == null) results = new LinkedHashMap<>();
        return results;
    }

    public DefaultWebRes setResults(Map<String, Object> results) {
        this.results = results;
        return this;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public DefaultWebRes setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    public boolean isHeadMethod() {
        return isHeadMethod;
    }

    public void setHeadMethod(boolean isHeadMethod) {
        this.isHeadMethod = isHeadMethod;
    }
}
