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

public class ResultUtil<T>{

    public static<T> Result<T> success(T data){
        return new Result<T>(ResponseCode.SUCCESS, "success", data);
    }

    public static<T> Result<T> success(){
        return new Result<T>(ResponseCode.SUCCESS, "success");
    }

    public static<T> Result<T> error(String message){
        return new Result<T>(ResponseCode.ERROR,message);
    }

    public static<T> Result<T> timeout(String message){
        return new Result<T>(ResponseCode.TIMEOUT,message);
    }

}
