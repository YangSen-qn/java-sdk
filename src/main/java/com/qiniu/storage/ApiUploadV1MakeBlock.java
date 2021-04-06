package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;

/**
 * 分片上传 v1 版 api: 创建块
 * 本接口用于为后续分片上传创建一个新的块，同时上传第一片数据。
 * <p>
 * <p>
 * 一个文件被分成多个 block ，一个块可以被分成多个 chunk
 * |----------------------------- file -----------------------------|
 * |------ block ------|------ block ------|------ block ------|...
 * |- chunk -|- chunk -|- chunk -|- chunk -|- chunk -|- chunk -|...
 * |- ctx01 -|- ctx02 -|- ctx10 -|- ctx12 -|- ctx20 -|- ctx22 -|...
 * allBlockCtx = [ctx02, ctx12, ctx22, ...]
 * <p>
 * 注意事项：
 * 1. 除了最后一个 block 外， 其他 block 的大小必须为 4M
 * 2. block 中所有的 chunk size 总和必须和 block size 相同
 * 3. 同一个 block 中的块上传需要依赖该块中上一次上传的返回的 ctx, 所以同一个块的上传无法实现并发，
 * 如果想实现并发，可以使一个 block 中仅包含一个 chunk, 也即 chunk size = 4M, make block 接口
 * 不依赖 ctx，可以实现并发；需要注意的一点是 ctx 的顺序必须与 block 在文件中的顺序一致。
 * <p>
 * <p>
 * https://developer.qiniu.com/kodo/1286/mkblk
 */

public class ApiUploadV1MakeBlock extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求client【必须】
     */
    public ApiUploadV1MakeBlock(Client client) {
        super(client);
    }

    /**
     * 发起请求
     *
     * @param request 请求对象【必须】
     * @return 响应对象
     * @throws QiniuException 请求异常
     */
    public Response request(Request request) throws QiniuException {
        return new Response(requestByClient(request));
    }

    /**
     * 请求信息
     */
    public static class Request extends Api.Request {
        private Integer blockSize;

        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【必须】
         * @param token     请求凭证【必须】
         * @param blockSize 块大小【必须】
         *                  每块均为 4MB，最后一块大小不超过 4MB。
         */
        public Request(String urlPrefix, String token, Integer blockSize) {
            super(urlPrefix);
            setToken(token);
            setMethod(Api.Request.HTTP_METHOD_POST);
            this.blockSize = blockSize;
        }

        /**
         * 配置块第一个上传片数据【必须】
         * 块数据：在 data 中，从 offset 开始的 size 大小的数据
         *
         * @param data        块数据源
         * @param offset      块数据在 data 中的偏移量
         * @param size        块数据大小
         * @param contentType 块数据类型
         * @return Request
         */
        public Request setFirstChunkData(byte[] data, int offset, int size, String contentType) {
            super.setBody(data, offset, size, contentType);
            return this;
        }

        @Override
        public void buildPath() throws QiniuException {
            if (blockSize == null) {
                ApiUtils.throwInvalidRequestParamException("block size");
            }

            addPathSegment("mkblk");
            addPathSegment(blockSize + "");
            super.buildPath();
        }

        @Override
        void buildBodyInfo() throws QiniuException {
            if (!hasBody()) {
                ApiUtils.throwInvalidRequestParamException("block data");
            }
        }
    }

    /**
     * 响应信息
     */
    public static class Response extends Api.Response {

        Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);
        }

        /**
         * 本次上传成功后的块级上传控制信息，用于后续上传片(bput)及创建文件(mkfile)。本字段是只能被七牛服务器解读使用的不透明字段，
         * 上传端不应修改其内容。每次返回的 ctx 都只对应紧随其后的下一个上传数据片，上传非对应数据片会返回 701 状态码。
         * 例如"ctx":"U1nAe4qJVwz4dYNslBCNNg...E5SEJJQQ=="
         *
         * @return ctx
         */
        public String getCtx() {
            return getStringValueFromDataMap("ctx");
        }

        /**
         * 上传块 sha1，使用URL安全的Base64编码，客户可通过此字段对上传块的完整性进行校验。
         * 例如"checksum":"wQ-csvpBHkZrhihcytio7HXizco="
         *
         * @return checksum
         */
        public String getChecksum() {
            return getStringValueFromDataMap("checksum");
        }

        /**
         * 下一个上传块在切割块中的偏移。
         * 例如"offset":4194304
         *
         * @return offset
         */
        public Long getOffset() {
            return getLongValueFromDataMap("offset");
        }

        /**
         * 后续上传接收地址。
         * 例如"host":"http://upload.qiniup.com"
         *
         * @return host
         */
        public String getHost() {
            return getStringValueFromDataMap("host");
        }

        /**
         * 上传块 crc32，客户可通过此字段对上传块的完整性进行校验。例如"crc32":659036110
         *
         * @return crc32
         */
        public Long getCrc32() {
            return getLongValueFromDataMap("crc32");
        }

        /**
         * ctx 过期时间。
         * 例如"expired_at":1514446175。
         *
         * @return expired_at
         */
        public Long getExpiredAt() {
            return getLongValueFromDataMap("expired_at");
        }
    }
}