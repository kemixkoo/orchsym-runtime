/*
 * Licensed to the Orchsym Runtime under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * this file to You under the Orchsym License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://github.com/orchsym/runtime/blob/master/orchsym/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.util;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestUtil.class);

    static abstract class HttpRequestEntity extends HttpEntityEnclosingRequestBase {

        public HttpRequestEntity(String url) {
            super();
            setURI(URI.create(url));
        }

    }

    public static class HttpHeader extends BasicHeader {
        private static final long serialVersionUID = -3259965740793159024L;

        public HttpHeader(String name, String value) {
            super(name, value);
        }

        @Override
        public int hashCode() {
            return 13 + this.getName().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Header)) {
                return false;
            }
            Header other = (Header) obj;
            return other.getName().equals(this.getName());
        }

    }

    public static HttpResponse putResponse(final String url, String payload, String contentType) throws IOException {
        return request(HttpPut.METHOD_NAME, url, payload, contentType);
    }

    public static HttpResponse deleteResponse(final String url, String payload, String contentType) throws IOException {
        return request(HttpDelete.METHOD_NAME, url, payload, contentType);
    }

    public static HttpResponse postResponse(String url, String payload, String contentType) throws IOException {
        return request(HttpPost.METHOD_NAME, url, payload, contentType);
    }

    public static HttpResponse getResponse(final String url, String payload, String contentType) throws IOException {
        return request(HttpGet.METHOD_NAME, url, payload, contentType);
    }

    public static String postString(final String url, String payload, String contentType) throws IOException {
        return response(postResponse(url, payload, contentType));
    }

    public static String getString(String url, String payload) throws IOException {
        return response(getResponse(url, payload, null));
    }

    public static String getString(String url) throws IOException {
        return getString(url, null);
    }

    public static String response(HttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

    public static HttpResponse request(final String method, String url, String payload, String contentType) throws IOException {
        Set<HttpHeader> headers = new HashSet<>();
        if (!StringUtils.isBlank(contentType)) {
            headers.add(new HttpHeader("Content-Type", contentType));
        }
        StringEntity entity = null;
        if (!StringUtils.isBlank(payload)) {
            entity = new StringEntity(payload);
        }
        return request(method, url, entity, headers);
    }

    public static HttpResponse request(final String method, String url, HttpEntity entity, Set<HttpHeader> headers) throws IOException {
        HttpRequestEntity request = new HttpRequestEntity(url) {

            @Override
            public String getMethod() {
                return method;
            }

        };
        if (null != headers) {
            headers.forEach(h -> request.addHeader(h));
        }

        if (null != entity) {
            request.setEntity(entity);
        }
        return getHttpClient(url).execute(request);
    }

    private static HttpClient getHttpClient(String testUrl) {
        HttpClient client = null;
        if (testUrl.contains("https://")) {
            try {
                client = getAllSSLClient();
            } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
                logger.warn("Cannot get all SSL client!", e);
            }
        } else {
            client = HttpClients.createDefault();
        }
        if (client == null)
            throw new AssertionError();
        return client;
    }

    @SuppressWarnings("deprecation")
    private static HttpClient getAllSSLClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }
        } };
        SSLContext context = SSLContext.getInstance("SSL");
        context.init(null, trustAllCerts, null);

        HttpClientBuilder builder = HttpClientBuilder.create();
        SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(context, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        builder.setSSLSocketFactory(sslConnectionFactory);

        PlainConnectionSocketFactory plainConnectionSocketFactory = new PlainConnectionSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create().register("https", sslConnectionFactory).register("http", plainConnectionSocketFactory).build();

        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);

        builder.setConnectionManager(ccm);

        return builder.build();

    }

    public static void trustAll() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } };
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            logger.error("Throw exception when ignore certification", e);
        }
    }

}