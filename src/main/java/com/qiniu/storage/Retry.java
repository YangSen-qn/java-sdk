package com.qiniu.storage;


import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;

final class Retry {
    private Retry() {
    }

    static boolean canSwitchRegionAndRetry(Response response, QiniuException exception) {
        Response checkResponse = response;
        if (checkResponse == null && exception != null) {
            checkResponse = exception.response;
        }

        if (checkResponse != null) {
            int statusCode = checkResponse.statusCode;
            return (statusCode > -2 && statusCode < 200) || (statusCode > 299
                    && statusCode != 401 && statusCode != 413 && statusCode != 419
                    && statusCode != 608 && statusCode != 614 && statusCode != 630);
        }

        return exception == null || !exception.isUnrecoverable();
    }

    static Boolean canRequestRetryAgain(Response response, QiniuException exception) {
        if (response != null) {
            return response.needRetry();
        }

        if (exception == null || exception.isUnrecoverable()) {
            return false;
        }

        if (exception.response != null) {
            return exception.response.needRetry();
        }

        return true;
    }

    static Boolean requestShouldSwitchHost(Response response, QiniuException exception) {
        if (response != null) {
            return response.needSwitchServer();
        }

        if (exception == null) {
            return true;
        }

        if (exception.isUnrecoverable()) {
            return false;
        }

        if (exception.response != null) {
            return exception.response.needSwitchServer();
        }

        return true;
    }

    static Response retryRequestAction(RequestRetryConfig config, RequestRetryAction action) throws QiniuException {
        if (config == null) {
            throw QiniuException.unrecoverable("RequestRetryConfig can't empty");
        }

        if (action == null) {
            throw QiniuException.unrecoverable("RequestRetryAction can't empty");
        }

        Response response = null;
        QiniuException exception = null;

        int retryCount = 0;

        do {
            boolean shouldSwitchHost = false;
            boolean shouldRetry = false;

            exception = null;
            String host = action.getRequestHost();
            try {
                response = action.doRequest(host);
                // 判断是否需要重试
                shouldRetry = canRequestRetryAgain(response, null);
                // 判断是否需要切换 host
                shouldSwitchHost = requestShouldSwitchHost(response, null);
            } catch (QiniuException e) {
                exception = e;
                // 判断是否需要重试
                shouldRetry = canRequestRetryAgain(null, e);
                // 判断是否需要切换 host
                shouldSwitchHost = requestShouldSwitchHost(null, e);
            }

            if (!shouldRetry) {
                break;
            }

            retryCount++;
            if (retryCount >= config.retryMax) {
                QiniuException innerException = null;
                if (response != null) {
                    innerException = new QiniuException(response);
                } else {
                    innerException = new QiniuException(exception);
                }
                throw new QiniuException(innerException, "failed after retry times");
            }

            if (shouldSwitchHost) {
                action.tryChangeRequestHost(host);
            }

        } while (true);

        if (exception != null) {
            throw exception;
        }

        return response;
    }

    static final class RequestRetryConfig {
        final int retryMax;

        RequestRetryConfig(int retryMax) {
            this.retryMax = retryMax;
        }

        static final class Builder {
            private int retryMax = 3;

            public Builder setRetryMax(int retryMax) {
                this.retryMax = retryMax;
                return this;
            }

            RequestRetryConfig build() {
                return new RequestRetryConfig(retryMax);
            }
        }
    }

    interface RequestRetryAction {
        String getRequestHost() throws QiniuException;

        void tryChangeRequestHost(String oldHost) throws QiniuException;

        Response doRequest(String host) throws QiniuException;
    }

    interface Interval {

        /**
         * 重试时间间隔，单位：毫秒
         **/
        int interval();
    }

    static Interval defaultInterval() {
        return staticInterval(200);
    }

    static Interval staticInterval(final int interval) {
        return new Retry.Interval() {
            @Override
            public int interval() {
                return interval;
            }
        };
    }

    interface RetryCondition {

        /**
         * 是否需要重试
         **/
        boolean shouldRetry(Api.Request request, Api.Response response, QiniuException exception);
    }

    static RetryCondition defaultCondition() {
        return new RetryCondition() {
            @Override
            public boolean shouldRetry(Api.Request request, Api.Response response, QiniuException exception) {
                return request.canRetry() && canRequestRetryAgain(response != null ? response.getResponse() : null, exception);
            }
        };
    }

    interface HostFreezeCondition {
        boolean shouldFreezeHost(Api.Request request, Api.Response response, QiniuException exception);
    }

    static HostFreezeCondition defaultHostFreezeCondition() {
        return new HostFreezeCondition() {
            @Override
            public boolean shouldFreezeHost(Api.Request request, Api.Response response, QiniuException exception) {
                return true;
            }
        };
    }
}
