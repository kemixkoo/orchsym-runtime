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
package org.apache.nifi.http;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("result")
public class Result<T>{

    private String code;
    private String message;
    private T data;
    public Result(String code, String message) {
        super();
        this.code = code;
        this.message = message;
    }
    public Result(String code, String message, T data) {
        super();
        this.code = code;
        this.message = message;
        this.data = data;
    }
    public Result() {
        super();
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }
    @Override
    public String toString(){
        if(data == null ){
            return "{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}";
        }
        return "{\"code\":\"" + code + "\",\"message\":\"" + message + "\",\"data\":\"" + data.toString() + "\"}";
    }

}
