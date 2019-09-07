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
package org.apache.nifi.web.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.authorization.RequestAction;
import org.apache.nifi.authorization.resource.Authorizable;
import org.apache.nifi.authorization.user.NiFiUserUtils;
import org.apache.nifi.processor.DataUnit;
import org.apache.nifi.stream.io.StreamUtils;
import org.apache.nifi.web.api.entity.FileContentEntity;
import org.apache.nifi.web.api.entity.FileInfoEntity;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import com.google.common.collect.ImmutableSet;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

/**
 * 文件上传处理器，支持集群间同步
 */
@Component
@Path("file-uploader")
@Api(value = "/file-uploader", description = "Supports file upload with form[enctype=\"multipart/form-data\"]")
public class FileUploaderResource extends AbsOrchsymResource {

    private static final Logger logger = LoggerFactory.getLogger(FileUploaderResource.class);

    /**
     * 上传文件目录配置Key
     */
    private static final String UPLOAD_PATH = "orchsym.upload.repository.directory";
    /**
     * 上传文件最大限制配置Key
     */
    private static final String UPLOAD_MAX_SIZE = "orchsym.upload.size.max";
    /**
     * 上传文件默认目录
     */
    private static final String DEFAULT_UPLOAD_PATH = "./upload_repository";
    /**
     * 上传文件默认最大限制
     */
    private static final String DEFAULT_UPLOAD_MAX_SIZE = "30 MB";

    /**
     * 上传文件默认权限
     * 自己可读写，同组或者其他用户只读
     */
    private final Set<PosixFilePermission> perms = ImmutableSet.of(//
            PosixFilePermission.OWNER_READ, //
            PosixFilePermission.OWNER_WRITE, //
            PosixFilePermission.GROUP_READ, //
            PosixFilePermission.OTHERS_READ);
    /**
     * 集群间文件同步请求头信息
     */
    private final Map<String, String> replicateHeaders = new HashMap<String, String>(1, 1) {
        {
            put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        }
    };

    protected String getUploadPath() {
        // 初始化上传文件目录
        return properties.getProperty(UPLOAD_PATH, DEFAULT_UPLOAD_PATH);

    }

    protected long getFileMaxSize() {
        // 初始化上传文件最大限制，默认30M
        String maxSize = properties.getProperty(UPLOAD_MAX_SIZE, DEFAULT_UPLOAD_MAX_SIZE);
        return DataUnit.parseDataSize(maxSize.trim(), DataUnit.B).longValue();

    }

    /**
     * 上传文件
     *
     * @param subDir
     *            子目录路径，可为空，为空时文件放到上传根目录中
     * @param fileDetail
     *            文件元信息（文件名）
     * @param inputStream
     *            文件
     * @return 文件大小（单位byte）
     */
    @POST
    @Path("/upload")
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.TEXT_PLAIN })
    @ApiOperation(value = "上传文件，form表单上传方式，参数名为file，enctype=\"multipart/form-data\"", //
            response = Response.class, //
            authorizations = @Authorization(value = "Write - /file-uploader/upload"))
    @ApiResponses(value = { @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 500, message = "上传失败，服务器内部错误") })
    public Response upload(@ApiParam("文件子目录，可为空") @FormDataParam("subDir") String subDir, //
            @ApiParam("Form - Input[type=\"File\"]") @FormDataParam("file") FormDataContentDisposition fileDetail, //
            @ApiParam("Form - Input[type=\"File\"]") @FormDataParam("file") InputStream inputStream, //
            @ApiParam("如果存在，是否覆盖") @FormDataParam("overwrite") boolean overwrite) {

        // authorize access
        serviceFacade.authorizeAccess(lookup -> {
            final Authorizable authorizable = lookup.getController();
            authorizable.authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());
        });

        // 输入流转字节数组，后续集群间同步时，直接传输字节数组
        byte[] fileBytes;
        try {
            fileBytes = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            logger.error("Upload failed", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Upload failed\n" + e.getMessage()).build();
        }
        // 判断文件是否超过最大限制，以字节为单位
        long fileSize = fileBytes.length;
        long maxSize = getFileMaxSize();
        if (maxSize > -1 && fileSize > maxSize) {
            return Response.status(Response.Status.BAD_REQUEST).entity(String.format("File size exceeds maximum limit, maximum limit: %d, actual: %d", maxSize, fileSize)).build();
        }

        // 文件名编码格式转换，防止中文乱码
        String fileName = new String(fileDetail.getFileName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        // 存储文件
        return uploadReplicate(new FileContentEntity(subDir, fileName, fileBytes, overwrite));
    }

    /**
     * 处理集群文件同步请求
     *
     * @param fileContentEntity
     *            集群文件参数对象
     * @return Response
     */
    @PUT
    @Path("/upload")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.TEXT_PLAIN })
    @ApiOperation(value = "上传文件", //
            response = Response.class, //
            authorizations = @Authorization(value = "Write - /file-uploader/upload"))
    @ApiResponses(value = @ApiResponse(code = 500, message = "上传失败，服务器内部错误"))
    public Response uploadReplicate(@RequestBody FileContentEntity fileContentEntity) {
        final String fileName = fileContentEntity.getFileName();
        if (StringUtils.isBlank(fileName)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Must provide the file name argument").build();
        }
        // 支持自定义子目录
        File fullPath = new File(getUploadPath());
        String subDir = fileContentEntity.getSubDir();
        if (!StringUtils.isBlank(subDir)) {
            fullPath = new File(fullPath, subDir);
        }

        // 如果目录不存在，创建子目录
        java.nio.file.Path folder = fullPath.toPath();
        if (!Files.exists(folder)) {
            try {
                Files.createDirectories(folder);
            } catch (IOException e) {
                logger.error("Subdirectory creation failed", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Subdirectory creation failed\n" + e.getMessage()).build();
            }
        }

        File uploadFile = new File(fullPath, fileName);
        if (Files.exists(uploadFile.toPath()) && !fileContentEntity.isOverwrite()) {
            return Response.status(Response.Status.CONFLICT).entity("The file " + fileName + " has been existed, maybe should do overwrite").build();
        }

        /*
         * 集群间同步
         * 如果上传节点为普通节点，则此方法会转发到协调节点。协调节点执行时，会同步到集群所有节点。
         * 因此需要在此方法中判断是否需要做集群间同步，否则无法同步到所有节点（只有上传节点和协调节点）。
         */
        if (isReplicateRequest()) {
            super.replicate(HttpMethod.PUT, fileContentEntity, replicateHeaders);
        }

        // 开始存储文件
        try {
            FileUtils.writeByteArrayToFile(uploadFile, fileContentEntity.getFileBytes());
            // 设置权限
            Files.setPosixFilePermissions(uploadFile.toPath(), perms);

            return Response.ok("upload successfully").build();
        } catch (IOException e) {
            logger.error("replicateNodeResponse error :" + uploadFile, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Upload failed\n" + e.getMessage()).build();
        }
    }

    /**
     * 文件下载
     *
     * @param filePath
     *            文件名（含子路径）
     */
    @GET
    @Path("/download")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    @ApiOperation(value = "文件下载", //
            response = Response.class, //
            authorizations = @Authorization(value = "Read - /file-uploader/download"))
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404), //
    })
    public Response download(@RequestBody FileInfoEntity fileInfoEntity) {
        final String filePattern = fileInfoEntity.getFilePattern();
        final String fileName = fileInfoEntity.getFileName();

        if (StringUtils.isBlank(fileName) && StringUtils.isBlank(filePattern)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Must provide the file name or pattern argument").build();
        }
        File dir = new File(getUploadPath());
        if (StringUtils.isNotBlank(fileInfoEntity.getSubDir())) {
            dir = new File(dir, fileInfoEntity.getSubDir());
        }
        if (StringUtils.isNotBlank(fileName)) {
            final File file = new File(dir, fileName);
            return downloadFile(file, file.getName());
        }

        return Response.status(Response.Status.BAD_REQUEST).entity("Must provide the file name").build();
    }

    private Response downloadFile(final File file, final String downloadFilename) {
        // authorize access
        serviceFacade.authorizeAccess(lookup -> {
            final Authorizable authorizable = lookup.getController();
            authorizable.authorize(authorizer, RequestAction.READ, NiFiUserUtils.getNiFiUser());
        });

        if (!Files.exists(file.toPath())) {
            return Response.status(Response.Status.NOT_FOUND).entity("File does not exist").build();
        }

        // generate a streaming response
        final StreamingOutput response = output -> {
            StreamUtils.copy(new ByteArrayInputStream(FileUtils.readFileToByteArray(file)), output);
            output.flush();
        };
        String encodeFilename = downloadFilename;
        try {
            encodeFilename = URLEncoder.encode(encodeFilename, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            logger.warn("File name encoding failed\n", e);
        }

        return generateOkResponse(response).type(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename* = " + StandardCharsets.UTF_8.name() + "''" + encodeFilename).build();
    }

    /**
     * 文件下载
     *
     * @param filePath
     *            文件名（含子路径）
     */
    @PUT
    @Path("/clean")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.TEXT_PLAIN })
    @ApiOperation(value = "文件下载", //
            response = Response.class, //
            authorizations = @Authorization(value = "Read - /file-uploader/download"))
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404), //
    })
    public Response clean(@RequestBody FileInfoEntity fileInfoEntity) {
        final String filePattern = fileInfoEntity.getFilePattern();
        final String fileName = fileInfoEntity.getFileName();

        if (StringUtils.isBlank(fileName) && StringUtils.isBlank(filePattern)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Must provide the file name or pattern argument").build();
        }
        File uploadRepoFolder = new File(getUploadPath());
        File dir = uploadRepoFolder;
        if (StringUtils.isNotBlank(fileInfoEntity.getSubDir())) {
            dir = new File(uploadRepoFolder, fileInfoEntity.getSubDir());
        }

        if (!Files.exists(dir.toPath())) {
            return Response.status(Response.Status.NOT_FOUND).entity("No files to clean").build();
        }

        final Pattern pattern = StringUtils.isNotBlank(filePattern) ? Pattern.compile(filePattern) : null;

        Set<File> results = new HashSet<>();
        final File[] listFiles = dir.listFiles();
        if (null != listFiles) {
            for (File child : listFiles) { // 第一级直接处理
                collectFiles(results, child, fileName, pattern, fileInfoEntity.isRecursive());
            }
        }
        if (results.size() == 0) {
            return Response.status(Response.Status.NOT_FOUND).entity("No files to clean").build();
        }
        List<String> deletedFiles = new ArrayList<>();
        for (File f : results) {
            try {
                Files.delete(f.toPath());
                deletedFiles.add(f.getAbsolutePath().replace(uploadRepoFolder.getAbsolutePath(), ""));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Can't delete the file " + f + "  with error: " + e.getMessage()).build();
            }
        }
        deletedFiles = deletedFiles.stream().sorted().collect(Collectors.toList());
        return Response.ok("Successfully deleted the files: " + String.join(",", deletedFiles)).build();
    }

    private void collectFiles(Set<File> results, File file, String fileName, Pattern filePattern, boolean recursive) {
        if (file.isDirectory() && recursive) {
            final File[] listFiles = file.listFiles();
            if (null != listFiles) {
                for (File child : listFiles) {
                    collectFiles(results, child, fileName, filePattern, recursive);
                }
            }
        } else if (file.isFile()) {
            if (StringUtils.isNotBlank(fileName) && fileName.equals(file.getName())) {
                results.add(file);
            }
            if (null != filePattern && filePattern.matcher(file.getName()).find()) {
                results.add(file);
            }
        }
    }

}
