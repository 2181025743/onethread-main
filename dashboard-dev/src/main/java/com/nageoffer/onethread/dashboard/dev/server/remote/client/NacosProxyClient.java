package com.nageoffer.onethread.dashboard.dev.server.remote.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosConfigDetailRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosConfigListRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosConfigRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosServiceListRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosServiceRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Nacos 代理客户端 - 混合使用 Nacos v3 Admin API 和 Client API
 * <p>
 * API 使用策略：
 * - 配置管理（列表、发布）：使用 Admin API (/nacos/v3/admin/cs/...)
 * - 服务发现（实例查询）：使用 Client API (/nacos/v3/client/ns/...)
 * <p>
 * 原因：
 * - Client API 不支持配置列表查询和发布操作
 * - Client API 的服务实例查询返回格式更简洁（data 直接是数组）
 * <p>
 * 参考文档：
 * - Admin API: https://nacos.io/docs/latest/manual/admin/admin-api/
 * - Client API: https://nacos.io/docs/latest/guide/user/open-api/
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-18
 * 重构时间：2025-10-31 (升级到 Nacos v3 混合 API)
 */
@Slf4j
@Component
public class NacosProxyClient {

    @Value("${onethread.nacos.server-addr}")
    private String serverAddr;

    @Value("${onethread.nacos.username:}")
    private String username;

    @Value("${onethread.nacos.password:}")
    private String password;

    private volatile String accessToken;
    private volatile long tokenExpireTime = 0;
    private volatile boolean noAuth = false;
    private final ReentrantLock tokenLock = new ReentrantLock();

    /**
     * 确保 token 有效
     */
    private void ensureToken() {
        // 检查是否配置了用户名密码
        if ((username == null || username.isEmpty() || password == null || password.isEmpty()) && !noAuth) {
            log.warn("Nacos username or password not configured, authentication will be skipped");
            noAuth = true;
        }

        if (noAuth) {
            return;
        }

        long now = System.currentTimeMillis() / 1000;
        if (accessToken != null && now < tokenExpireTime) {
            return; // token 仍然有效
        }

        refreshToken();
    }

    /**
     * 刷新 Nacos accessToken
     */
    private void refreshToken() {
        tokenLock.lock();
        try {
            // 双重检查
            long now = System.currentTimeMillis() / 1000;
            if (accessToken != null && now < tokenExpireTime) {
                return;
            }

            log.info("Refreshing Nacos access token...");

            // 尝试登录获取 token
            String loginUrl = serverAddr + "/nacos/v1/auth/login";
            HttpResponse response = HttpRequest.post(loginUrl)
                    .form("username", username)
                    .form("password", password)
                    .execute();

            if (!response.isOk()) {
                if (response.getStatus() == 404 || response.getStatus() == 405) {
                    log.warn("Nacos authentication not enabled (status: {}), disabling auth", response.getStatus());
                    noAuth = true;
                    return;
                }
                throw new RuntimeException("Failed to login Nacos: " + response.getStatus() + ", " + response.body());
            }

            JSONObject result = JSON.parseObject(response.body());
            String token = result.getString("accessToken");
            if (token == null || token.isEmpty()) {
                // 尝试从 data 字段读取
                JSONObject data = result.getJSONObject("data");
                if (data != null) {
                    token = data.getString("accessToken");
                }
            }

            if (token == null || token.isEmpty()) {
                throw new RuntimeException("Failed to get accessToken from Nacos response: " + response.body());
            }

            // 获取 token 过期时间
            Integer ttl = result.getInteger("tokenTtl");
            if (ttl == null) {
                JSONObject data = result.getJSONObject("data");
                if (data != null) {
                    ttl = data.getInteger("tokenTtl");
                }
            }
            if (ttl == null) {
                ttl = 18000; // 默认 5 小时
            }

            this.accessToken = token;
            this.tokenExpireTime = now + ttl - 60; // 提前 60 秒刷新

            String maskedToken = token.length() > 6 ? "******" + token.substring(token.length() - 6) : "******";
            log.info("Nacos access token refreshed successfully, token: {}, expires in {} seconds", maskedToken, ttl);

        } catch (Exception e) {
            log.error("Failed to refresh Nacos access token", e);
            throw new RuntimeException("Failed to refresh Nacos access token", e);
        } finally {
            tokenLock.unlock();
        }
    }

    /**
     * 为请求添加认证信息
     */
    private HttpRequest withAuth(HttpRequest request) {
        if (!noAuth && accessToken != null) {
            request.form("accessToken", accessToken);
        }
        return request;
    }

    /**
     * 解析 Nacos v3 API 响应并提取 data 字段
     *
     * @param result   HTTP 响应体
     * @param apiName  API 名称（用于日志）
     * @return data 字段的 JSONObject
     */
    private JSONObject parseV3Response(String result, String apiName) {
        JSONObject responseJson = JSON.parseObject(result);
        Integer code = responseJson.getInteger("code");

        if (code == null || code != 0) {
            String message = responseJson.getString("message");
            log.error("{} failed. Code: {}, Message: {}", apiName, code, message);
            throw new RuntimeException(String.format("%s failed: %s (code: %d)", apiName, message, code));
        }

        JSONObject data = responseJson.getJSONObject("data");
        if (data == null) {
            log.error("{} response missing 'data' field: {}", apiName, result);
            throw new RuntimeException(String.format("%s response format invalid", apiName));
        }

        return data;
    }

    /**
     * 查询命名空间下配置文件集合
     * <p>
     * Nacos v3 Admin API: GET /nacos/v3/admin/cs/config/list
     *
     * @param namespace 命名空间
     * @return 配置文件集合
     */
    public List<NacosConfigRespDTO> listConfig(String namespace) {
        ensureToken();

        // Nacos v3 Admin API
        String url = serverAddr + "/nacos/v3/admin/cs/config/list";

        HttpRequest request = HttpRequest.get(url)
                .form("pageNo", "1")
                .form("pageSize", "100") // 默认单个 namespace 最大 100 条数据,如果超过写个 while 循环读取即可
                .form("namespaceId", Objects.equals(namespace, "public") ? "" : namespace)
                .form("search", "blur");

        HttpResponse response = withAuth(request).execute();

        String result = response.body();
        if (!response.isOk()) {
            log.error("Failed to list configs from Nacos. Status: {}, Response: {}", response.getStatus(), result);
            throw new RuntimeException("Nacos server returned error: " + response.getStatus());
        }

        JSONObject data = parseV3Response(result, "listConfig");
        NacosConfigListRespDTO nacosRemoteResult = data.toJavaObject(NacosConfigListRespDTO.class);
        return nacosRemoteResult.getPageItems();
    }

    /**
     * 查询配置明细信息
     * <p>
     * Nacos v3 Admin API: GET /nacos/v3/admin/cs/config
     *
     * @param namespace 命名空间
     * @param dataId    数据 ID
     * @param group     分组标识
     * @return 配置明细
     */
    public NacosConfigDetailRespDTO getConfig(String namespace, String dataId, String group) {
        ensureToken();

        // Nacos v3 Admin API
        String url = serverAddr + "/nacos/v3/admin/cs/config";

        HttpRequest request = HttpRequest.get(url)
                .form("dataId", dataId)
                .form("groupName", group)
                .form("namespaceId", Objects.equals(namespace, "public") ? "" : namespace)
                .form("show", "all");

        HttpResponse response = withAuth(request).execute();

        String result = response.body();
        if (!response.isOk()) {
            log.error("Failed to get config from Nacos. Status: {}, dataId: {}, group: {}, namespace: {}",
                    response.getStatus(), dataId, group, namespace);
            throw new RuntimeException("Nacos server returned error: " + response.getStatus());
        }

        JSONObject data = parseV3Response(result, "getConfig");
        return data.toJavaObject(NacosConfigDetailRespDTO.class);
    }

    /**
     * 发布/更新配置
     * <p>
     * Nacos v3 Admin API: POST /nacos/v3/admin/cs/config
     *
     * @param namespace   命名空间
     * @param dataId      数据 ID
     * @param group       分组标识
     * @param appName     应用名称
     * @param id          配置 ID
     * @param md5         配置内容的 MD5 值
     * @param content     配置文件内容
     * @param contentType 配置文件格式 (yaml/properties/json/xml/text/html)
     */
    public void publishConfig(String namespace, String dataId, String group, String appName, String id, String md5, String content, String contentType) {
        ensureToken();

        // Nacos v3 Admin API
        String url = serverAddr + "/nacos/v3/admin/cs/config";

        Map<String, Object> form = new HashMap<>();
        form.put("namespaceId", Objects.equals(namespace, "public") ? "" : namespace);
        form.put("dataId", dataId);
        form.put("groupName", group);
        form.put("appName", appName);
        form.put("id", id);
        form.put("md5", md5);
        form.put("content", content);
        form.put("type", contentType);
        if (!noAuth && accessToken != null) {
            form.put("accessToken", accessToken);
        }

        HttpResponse response = HttpRequest.post(url)
                .form(form)
                .execute();

        if (!response.isOk()) {
            log.error("Failed to publish config to Nacos. Status: {}, dataId: {}, group: {}, namespace: {}",
                    response.getStatus(), dataId, group, namespace);
            throw new RuntimeException("Nacos server returned error: " + response.getStatus());
        }

        // 检查返回结果
        String result = response.body();
        try {
            parseV3Response(result, "publishConfig");
            log.info("Successfully published config: dataId={}, group={}, namespace={}", dataId, group, namespace);
        } catch (Exception e) {
            log.warn("Config published but response validation failed: {}", e.getMessage());
        }
    }

    /**
     * 查询命名空间下服务实例列表
     * <p>
     * Nacos v3 Client API: GET /nacos/v3/client/ns/instance/list
     * 文档: https://nacos.io/docs/latest/manual/admin/admin-api/#4%EF%B8%8F%E2%83%A3-%E6%9F%A5%E8%AF%A2%E6%9F%90%E4%B8%AA%E6%9C%8D%E5%8A%A1%E7%9A%84%E5%AE%9E%E4%BE%8B%E5%88%97%E8%A1%A8
     *
     * @param namespace   命名空间
     * @param serviceName 服务名
     * @return 服务明细响应
     */
    public NacosServiceListRespDTO getService(String namespace, String serviceName) {
        ensureToken();

        // Nacos v3 Client API
        String url = serverAddr + "/nacos/v3/client/ns/instance/list";

        HttpRequest request = HttpRequest.get(url)
                .form("serviceName", serviceName)
                .form("namespaceId", Objects.equals(namespace, "public") ? "" : namespace)
                .form("groupName", "DEFAULT_GROUP")
                .form("clusterName", "DEFAULT");

        HttpResponse response = withAuth(request).execute();

        String result = response.body();
        if (!response.isOk()) {
            log.warn("Failed to get service from Nacos. Status: {}, serviceName: {}, namespace: {}",
                    response.getStatus(), serviceName, namespace);
            return NacosServiceListRespDTO.builder()
                    .count(0)
                    .build();
        }

        try {
            JSONObject responseJson = JSON.parseObject(result);
            Integer code = responseJson.getInteger("code");

            if (code == null || code != 0) {
                log.warn("Nacos service query failed. serviceName: {}, namespace: {}, code: {}, response: {}",
                        serviceName, namespace, code, result);
                return NacosServiceListRespDTO.builder()
                        .count(0)
                        .build();
            }

            // Nacos v3 Client API 返回的 data 是数组，而不是对象
            List<NacosServiceRespDTO> serviceList = responseJson.getList("data", NacosServiceRespDTO.class);
            if (serviceList == null) {
                serviceList = new ArrayList<>();
            }

            return NacosServiceListRespDTO.builder()
                    .count(serviceList.size())
                    .serviceList(serviceList)
                    .build();

        } catch (Exception e) {
            log.warn("Failed to parse service response. serviceName: {}, namespace: {}, error: {}",
                    serviceName, namespace, e.getMessage());
            return NacosServiceListRespDTO.builder()
                    .count(0)
                    .build();
        }
    }
}
