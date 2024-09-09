package com.qiniu.processing;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;

/**
 * 对七牛空间中的文件进行持久化处理，适用于官方的fop指令和客户开发的ufop指令
 * 例如图片处理指令，视频转码指令等
 *
 * <a href="http://developer.qiniu.com/dora"> 相关链接 </a>
 */
public final class OperationManager {
    /**
     * HTTP Client 对象
     * 该类需要通过该对象来发送HTTP请求
     */
    private final Client client;

    /**
     * Auth 对象
     * 该类需要使用QBox鉴权，所以需要指定Auth对象
     */
    private final Auth auth;

    /**
     * Configuration 对象
     * 该类相关的域名配置，解析配置，HTTP请求超时时间设置等
     */

    private Configuration configuration;

    /**
     * 构建一个新的 OperationManager 对象
     *
     * @param auth Auth对象
     * @param cfg  Configuration对象
     */
    public OperationManager(Auth auth, Configuration cfg) {
        this.auth = auth;
        this.configuration = cfg.clone();
        this.client = new Client(configuration);
    }

    public OperationManager(Auth auth, Client client) {
        this.auth = auth;
        this.client = client;
        this.configuration = new Configuration();
    }

    public OperationManager(Auth auth, Configuration cfg, Client client) {
        this.auth = auth;
        this.client = client;
        this.configuration = cfg;
    }

    /**
     * 发送请求对空间中的文件进行持久化处理
     *
     * @param bucket 空间名
     * @param key    文件名
     * @param fops   fops指令，如果有多个指令，需要使用分号(;)进行拼接，例如 avthumb/mp4/xxx|saveas/xxx;vframe/jpg/xxx|saveas/xxx
     * @return persistentId 请求返回的任务ID，可以根据该ID查询任务状态
     * @throws QiniuException 触发失败异常，包含错误响应等信息
     *
     *                        <a href="http://developer.qiniu.com/dora/api/persistent-data-processing-pfop"> 链接 </a>
     * @deprecated 数据持久化处理时，请指定 pipeline 参数以保障处理效率
     */
    public String pfop(String bucket, String key, String fops) throws QiniuException {
        return pfop(bucket, key, fops, null);
    }

    /**
     * 发送请求对空间中的文件进行持久化处理
     *
     * @param bucket 空间名
     * @param key    文件名
     * @param fops   fops指令，如果有多个指令，需要使用分号(;)进行拼接，例如 avthumb/mp4/xxx|saveas/xxx;vframe/jpg/xxx|saveas/xxx
     * @param params notifyURL、force、pipeline、type等参数
     * @return persistentId 请求返回的任务ID，可以根据该ID查询任务状态
     * @throws QiniuException 触发失败异常，包含错误响应等信息
     *                        <a href="http://developer.qiniu.com/dora/api/persistent-data-processing-pfop"> 相关链接 </a>
     */
    public String pfop(String bucket, String key, String fops, StringMap params) throws QiniuException {
        params = params == null ? new StringMap() : params;
        params.put("bucket", bucket).put("key", key).put("fops", fops);
        byte[] data = StringUtils.utf8Bytes(params.formString());
        String url = configuration.apiHost(auth.accessKey, bucket) + "/pfop/";
        StringMap headers = auth.authorizationV2(url, "POST", data, Client.FormMime);
        Response response = client.post(url, data, headers, Client.FormMime);
        if (!response.isOK()) {
            throw new QiniuException(response);
        }
        PfopResult status = response.jsonToObject(PfopResult.class);
        response.close();
        if (status != null) {
            return status.persistentId;
        }
        return null;
    }

    /**
     * 发送请求对空间中的文件进行持久化处理
     *
     * @param bucket    空间名
     * @param key       文件名
     * @param fops      fop指令
     * @param pipeline  持久化数据处理队列名称
     * @param notifyURL 处理结果通知地址，任务完成后自动以POST方式将处理结果提交到指定的地址
     * @return persistentId 请求返回的任务ID，可以根据该ID查询任务状态
     * @throws QiniuException 触发失败异常，包含错误响应等信息
     *                        <a href="http://developer.qiniu.com/dora/api/persistent-data-processing-pfop"> 相关链接 </a>
     */
    public String pfop(String bucket, String key, String fops, String pipeline, String notifyURL)
            throws QiniuException {
        StringMap params = new StringMap().putNotEmpty("pipeline", pipeline).putNotEmpty("notifyURL", notifyURL);
        return pfop(bucket, key, fops, params);
    }

    /**
     * 发送请求对空间中的文件进行持久化处理
     *
     * @param bucket   空间名
     * @param key      文件名
     * @param fops     fop指令
     * @param pipeline 持久化数据处理队列名称
     * @param force    用于对同一个指令进行强制处理时指定，一般用于覆盖空间已有文件或者重试失败的指令
     * @return persistentId 请求返回的任务ID，可以根据该ID查询任务状态
     * @throws QiniuException 触发失败异常，包含错误响应等信息
     *                        <a href="http://developer.qiniu.com/dora/api/persistent-data-processing-pfop"> 相关链接 </a>
     */
    public String pfop(String bucket, String key, String fops, String pipeline, boolean force)
            throws QiniuException {
        StringMap params = new StringMap().putNotEmpty("pipeline", pipeline).putWhen("force", 1, force);
        return pfop(bucket, key, fops, params);
    }

    /**
     * 发送请求对空间中的文件进行持久化处理
     *
     * @param bucket    空间名
     * @param key       文件名
     * @param fops      fop指令
     * @param pipeline  持久化数据处理队列名称
     * @param notifyURL 处理结果通知地址，任务完成后自动以POST方式将处理结果提交到指定的地址
     * @param force     用于对同一个指令进行强制处理时指定，一般用于覆盖空间已有文件或者重试失败的指令
     * @return persistentId 请求返回的任务ID，可以根据该ID查询任务状态
     * @throws QiniuException 触发失败异常，包含错误响应等信息
     *                        <a href="http://developer.qiniu.com/dora/api/persistent-data-processing-pfop"> 相关链接 </a>
     */
    public String pfop(String bucket, String key, String fops, String pipeline, String notifyURL, boolean force)
            throws QiniuException {
        StringMap params = new StringMap()
                .putNotEmpty("type", "1")
                .putNotEmpty("pipeline", pipeline)
                .putNotEmpty("notifyURL", notifyURL)
                .putWhen("force", 1, force);
        return pfop(bucket, key, fops, params);
    }

    /**
     * 发送请求对空间中的文件进行持久化处理
     *
     * @param bucket    空间名
     * @param key       文件名
     * @param fops      fop指令
     * @param pipeline  持久化数据处理队列名称
     * @param notifyURL 处理结果通知地址，任务完成后自动以POST方式将处理结果提交到指定的地址
     * @param type      任务类型，0：非闲时任务，1：闲时任务
     * @param force     用于对同一个指令进行强制处理时指定，一般用于覆盖空间已有文件或者重试失败的指令
     * @return persistentId 请求返回的任务ID，可以根据该ID查询任务状态
     * @throws QiniuException 触发失败异常，包含错误响应等信息
     *                        <a href="http://developer.qiniu.com/dora/api/persistent-data-processing-pfop"> 相关链接 </a>
     */
    public String pfop(String bucket, String key, String fops, String pipeline, String notifyURL, String type, boolean force)
            throws QiniuException {
        StringMap params = new StringMap()
                .putNotEmpty("type", type)
                .putNotEmpty("pipeline", pipeline)
                .putNotEmpty("notifyURL", notifyURL)
                .putWhen("force", 1, force);
        return pfop(bucket, key, fops, params);
    }

    /**
     * 根据persistentId查询任务状态
     * Use {@link OperationManager#prefop(String bucket, String persistentId)} instead
     *
     * @param persistentId 操作 ID
     * @return OperationStatus
     * @throws QiniuException 异常
     */
    @Deprecated
    public OperationStatus prefop(String persistentId) throws QiniuException {
        return prefop(persistentId, OperationStatus.class);
    }

    /**
     * 根据persistentId查询任务状态
     * 返回结果的 class
     * Use {@link OperationManager#prefop(String, String, Class) } instead
     *
     * @param <T>          泛型
     * @param persistentId 操作 ID
     * @param retClass     返回类型
     * @return T  retClass 声明类的对象
     * @throws QiniuException 异常
     */
    @Deprecated
    public <T> T prefop(String persistentId, Class<T> retClass) throws QiniuException {
        String url = String.format("%s/status/get/prefop?id=%s", configuration.apiHost(), persistentId);
        Response response = this.client.get(url);
        if (!response.isOK()) {
            throw new QiniuException(response);
        }
        T object = response.jsonToObject(retClass);
        response.close();
        return object;
    }

    /**
     * 根据persistentId查询任务状态，如果您配置的是 AutoRegion 请使用这个方法进行 prefop
     *
     * @param bucket       空间名
     * @param persistentId 操作 ID
     * @return OperationStatus
     * @throws QiniuException 异常
     */
    public OperationStatus prefop(String bucket, String persistentId) throws QiniuException {
        return prefop(bucket, persistentId, OperationStatus.class);
    }

    /**
     * 根据 persistentId 查询任务状态，如果您配置的是 AutoRegion 请使用这个方法进行 prefop
     * 返回结果的 class
     *
     * @param <T>          泛型
     * @param bucket       空间名
     * @param persistentId 操作 ID
     * @param retClass     返回对象类型
     * @return T
     * @throws QiniuException 异常
     */
    public <T> T prefop(String bucket, String persistentId, Class<T> retClass) throws QiniuException {
        String url = String.format("%s/status/get/prefop?id=%s", configuration.apiHost(auth.accessKey, bucket), persistentId);
        Response response = this.client.get(url);
        if (!response.isOK()) {
            throw new QiniuException(response);
        }
        T object = response.jsonToObject(retClass);
        response.close();
        return object;
    }

    private class PfopResult {
        public String persistentId;
    }
}
