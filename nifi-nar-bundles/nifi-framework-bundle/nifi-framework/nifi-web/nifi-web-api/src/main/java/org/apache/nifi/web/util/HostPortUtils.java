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
package org.apache.nifi.web.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * 抽取host port相关操作
 * @author liuxun
 */
public class HostPortUtils {
    /**
     * 测试本机端口是否被使用
     *
     * @param port
     * @return
     */
    public static boolean isLocalPortUsing(int port) {
        boolean flag = true;
        try {
            //如果该端口还在使用则返回true,否则返回false,127.0.0.1代表本机
            flag = isPortUsing("127.0.0.1", port);
        } catch (Exception e) {
        }
        return flag;
    }

    /***
     * 测试主机Host的port端口是否被使用
     * @param host
     * @param port
     * @throws UnknownHostException
     */
    public static boolean isPortUsing(String host, int port) throws UnknownHostException {
        boolean flag = false;
        InetAddress Address = InetAddress.getByName(host);
        try {
            Socket socket = new Socket(Address, port);  //建立一个Socket连接
            socket.close();
            flag = true;
        } catch (IOException e) {

        }
        return flag;
    }

    /**
     * 获取端口号
     *
     * @param href 网址, ftp, http, nntp, ... 等等
     * @return
     * @throws IOException
     */
    public static int parsePort(String href) throws IOException {
        //java.net中存在的类
        URL url = new URL(href);
        // 端口号; 如果 href 中没有明确指定则为 -1
        int port = url.getPort();
        if (port < 0) {
            // 获取对应协议的默认端口号
            port = url.getDefaultPort();
        }
        return port;
    }

    /**
     * 获取Host部分
     *
     * @param href 网址, ftp, http, nntp, ... 等等
     * @return
     * @throws IOException
     */
    public static String parseHost(String href) throws IOException {
        //
        URL url = new URL(href);
        // 获取 host 部分
        String host = url.getHost();
        return host;
    }

    /**
     * 根据域名(host)解析IP地址
     *
     * @param host 域名
     * @return
     * @throws IOException
     */
    public static String parseIp(String host) throws IOException {
        // 根据域名查找IP地址
        InetAddress inetAddress = InetAddress.getByName(host);
        // IP 地址
        String address = inetAddress.getHostAddress();
        return address;
    }


    public static void main(String[] args) throws IOException {
        //
//		   String href = "http://www.cncounter.com:8080/tools/shorturl.php";
		   String href = "http://localhost:8090/";
//        String href = "http://www.baidu.com";
        // 端口号
        int port = parsePort(href);
        // 域名
        String host = parseHost(href);
        // IP 地址
        String address = parseIp(host);
        //
        System.out.println("host=" + host);
        System.out.println("port=" + port);
        System.out.println("address=" + address);
    }

}
