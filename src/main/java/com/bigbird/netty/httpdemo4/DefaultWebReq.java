package com.bigbird.netty.httpdemo4;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.util.*;

public class DefaultWebReq {
    HttpVersion version;
    HttpMethod method;
    boolean keepAlive;
    String path;
    String queryString;
    HttpHeaders headers;
    Map<String, String> cookies;
    String content;

    Map<String, Object> parameters;

    public boolean isHeadMethod() {
        return method == HttpMethod.HEAD;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void freeMemory() {
        queryString = null;
        content = null;
    }

    public DefaultWebReq setHeaders(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    public DefaultWebReq setHeader(String name, String value) {
        if (headers == null) headers = new DefaultHttpHeaders();
        headers.set(name, value);
        return this;
    }

    public DefaultWebReq setHost(String host) {
        if (headers == null) headers = new DefaultHttpHeaders();
        headers.set(HttpHeaderNames.HOST, host);
        return this;
    }

    public DefaultWebReq setContentType(String contentType) {
        if (headers == null) headers = new DefaultHttpHeaders();
        headers.set(HttpHeaderNames.CONTENT_TYPE, contentType);
        return this;
    }

    public String getHeader(String name) {
        if (headers == null) return null;
        return headers.get(name);
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

    public String getCharSet(String defaultCharSet) {
        if (headers == null) return null;
        String contentTypeStr = headers.get(HttpHeaderNames.CONTENT_TYPE);
        if (contentTypeStr == null) return defaultCharSet;
        int p = contentTypeStr.indexOf(";");
        return p >= 0 ? WebUtils.parseCharSet(contentTypeStr.substring(p + 1)) : defaultCharSet;
    }


    public String getHost() {
        if (headers == null) return null;
        return headers.get(HttpHeaderNames.HOST);
    }

    public String getHostNoPort() {
        if (headers == null) return null;
        String host = headers.get(HttpHeaderNames.HOST);
        if (host == null) return null;
        int p = host.indexOf(":");
        if (p >= 0) return host.substring(0, p);
        else return host;
    }

    public boolean isHttps() {
        if (headers == null) return false;
        String ht = headers.get("x-forwarded-proto");
        if (ht == null) return false;

        switch (ht) {
            case "http":
                return false;
            case "https":
                return true;
            default:
                return false;
        }
    }

    public Map<String, Object> getParameters() {
        if (parameters == null) parameters = new HashMap<>();
        return parameters;
    }

    public String getCookie(String name) {
        if (cookies == null) cookies = decodeCookie();
        return cookies.get(name);
    }

    HashMap<String, String> decodeCookie() {
        String cookie = getHeader("cookie");
        if (cookie != null && !cookie.isEmpty()) {
            Set<Cookie> decoded = ServerCookieDecoder.STRICT.decode(cookie);
            if (decoded != null && decoded.size() > 0) {
                HashMap<String, String> m = new HashMap<String, String>();
                for (Cookie c : decoded) {
                    m.put(c.name(), c.value());
                }
                return m;
            }
        }
        return new HashMap<>();
    }

    public HttpVersion getVersion() {
        return version;
    }

    public DefaultWebReq setVersion(HttpVersion version) {
        this.version = version;
        return this;
    }

    public String getMethodString() {
        return method.toString();
    }

    public HttpMethod getMethod() {
        return method;
    }

    public DefaultWebReq setMethod(HttpMethod method) {
        this.method = method;
        return this;
    }

    public String getQueryString() {
        return queryString;
    }

    public DefaultWebReq setQueryString(String queryString) {
        this.queryString = queryString;
        return this;
    }

    public String getContent() {
        return content;
    }

    public DefaultWebReq setContent(String content) {
        this.content = content;
        return this;
    }

    public String getPath() {
        return path;
    }

    public DefaultWebReq setPath(String path) {
        this.path = path;
        return this;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public DefaultWebReq setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

}
