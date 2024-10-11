package com.qiniu.iam.apis;

import com.google.gson.annotations.SerializedName;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
import com.qiniu.storage.Api;
import com.qiniu.util.Json;


/**
 * 列举 IAM 子账号
 */
public class ApiGetUsers extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public ApiGetUsers(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public ApiGetUsers(Client client, Config config) {
        super(client, config);
    }

    /**
     * 发起请求
     *
     * @param request 请求对象【必须】
     * @return 响应对象
     * @throws QiniuException 请求异常
     */
    public Response request(Request request) throws QiniuException {
        return new Response(requestWithInterceptor(request));
    }

    /**
     * 请求信息
     */
    public static class Request extends Api.Request {

        /**
         * 子账号别名
         */
        private String alias = null;

        /**
         * 分页页号，从 1 开始，默认 1
         */
        private Integer page = null;

        /**
         * 分页大小，默认 20，最大 2000
         */
        private Integer pageSize = null;

        /**
         * 请求构造函数
         *
         * @param urlPrefix 请求 scheme + host 【可选】
         *                  若为空则会直接从 HostProvider 中获取
         */
        public Request(String urlPrefix) {
            super(urlPrefix);
            this.setMethod(MethodType.GET);
            this.setAuthType(AuthTypeQiniu);
        }

        /**
         * 设置参数【可选】
         *
         * @param alias 子账号别名
         * @return Request
         */
        public Request setAlias(String alias) {
            this.alias = alias;
            return this;
        }

        /**
         * 设置参数【可选】
         *
         * @param page 分页页号，从 1 开始，默认 1
         * @return Request
         */
        public Request setPage(Integer page) {
            this.page = page;
            return this;
        }

        /**
         * 设置参数【可选】
         *
         * @param pageSize 分页大小，默认 20，最大 2000
         * @return Request
         */
        public Request setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        @Override
        protected void prepareToRequest() throws QiniuException {

            super.prepareToRequest();
        }

        @Override
        protected void buildPath() throws QiniuException {
            addPathSegment("iam/v1/users");
            super.buildPath();
        }

        @Override
        protected void buildQuery() throws QiniuException {

            if (this.alias != null) {
                addQueryPair("alias", this.alias);
            }
            if (this.page != null) {
                addQueryPair("page", this.page);
            }
            if (this.pageSize != null) {
                addQueryPair("page_size", this.pageSize);
            }

            super.buildQuery();
        }

        @Override
        protected void buildHeader() throws QiniuException {

            super.buildHeader();
        }

        @Override
        protected void buildBodyInfo() throws QiniuException {

            super.buildBodyInfo();
        }

    }

    /**
     * 响应信息
     */
    public static class Response extends Api.Response {

        /**
         * 返回的 IAM 子账号列表响应
         */
        private GetIamUsersResp data;

        protected Response(com.qiniu.http.Response response) throws QiniuException {
            super(response);

            this.data = Json.decode(response.bodyString(), GetIamUsersResp.class);
        }

        /**
         * 响应信息
         *
         * @return GetIamUsersResp
         */
        public GetIamUsersResp getData() {
            return this.data;
        }

        /**
         * 返回的 IAM 子账号
         */
        public static final class GetIamUser {

            /**
             * 记录 ID
             */
            @SerializedName("id")
            private String id;

            /**
             * 根用户 uid
             */
            @SerializedName("root_uid")
            private Integer rootUid;

            /**
             * 子账号 uid
             */
            @SerializedName("iuid")
            private Integer iuid;

            /**
             * 子账号别名
             */
            @SerializedName("alias")
            private String alias;

            /**
             * 子账号创建时间
             */
            @SerializedName("created_at")
            private String createdAt;

            /**
             * 子账号上次更新时间
             */
            @SerializedName("updated_at")
            private String updatedAt;

            /**
             * 子账号上次更新时间
             */
            @SerializedName("last_login_time")
            private String lastLoginTime;

            /**
             * 子账号是否启用
             */
            @SerializedName("enabled")
            private Boolean enabled;

            /**
             * 获取变量值
             * 记录 ID
             *
             * @return id
             */
            public String getId() {
                return this.id;
            }

            /**
             * 获取变量值
             * 根用户 uid
             *
             * @return rootUid
             */
            public Integer getRootUid() {
                return this.rootUid;
            }

            /**
             * 获取变量值
             * 子账号 uid
             *
             * @return iuid
             */
            public Integer getIuid() {
                return this.iuid;
            }

            /**
             * 获取变量值
             * 子账号别名
             *
             * @return alias
             */
            public String getAlias() {
                return this.alias;
            }

            /**
             * 获取变量值
             * 子账号创建时间
             *
             * @return createdAt
             */
            public String getCreatedAt() {
                return this.createdAt;
            }

            /**
             * 获取变量值
             * 子账号上次更新时间
             *
             * @return updatedAt
             */
            public String getUpdatedAt() {
                return this.updatedAt;
            }

            /**
             * 获取变量值
             * 子账号上次更新时间
             *
             * @return lastLoginTime
             */
            public String getLastLoginTime() {
                return this.lastLoginTime;
            }

            /**
             * 获取变量值
             * 子账号是否启用
             *
             * @return enabled
             */
            public Boolean getEnabled() {
                return this.enabled;
            }
        }

        /**
         * 返回的 IAM 子账号列表信息
         */
        public static final class GetIamUsersData {

            /**
             * IAM 子账号数量
             */
            @SerializedName("count")
            private Integer count;

            /**
             * IAM 子账号列表
             */
            @SerializedName("list")
            private GetIamUser[] list;

            /**
             * 获取变量值
             * IAM 子账号数量
             *
             * @return count
             */
            public Integer getCount() {
                return this.count;
            }

            /**
             * 获取变量值
             * IAM 子账号列表
             *
             * @return list
             */
            public GetIamUser[] getList() {
                return this.list;
            }
        }

        /**
         * 返回的 IAM 子账号列表响应
         */
        public static final class GetIamUsersResp {

            /**
             * IAM 子账号信息
             */
            @SerializedName("data")
            private GetIamUsersData data;

            /**
             * 获取变量值
             * IAM 子账号信息
             *
             * @return data
             */
            public GetIamUsersData getData() {
                return this.data;
            }
        }
    }
}