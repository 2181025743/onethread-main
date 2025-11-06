/*
 * 动态线程池（oneThread）基础组件项目
 *
 * 版权所有 (C) [2024-至今] [山东流年网络科技有限公司]
 *
 * 保留所有权利。
 *
 * 1. 定义和解释
 *    本文件（包括其任何修改、更新和衍生内容）是由[山东流年网络科技有限公司]及相关人员开发的。
 *    "软件"指的是与本文件相关的任何代码、脚本、文档和相关的资源。
 *
 * 2. 使用许可
 *    本软件的使用、分发和解释均受中华人民共和国法律的管辖。只有在遵守以下条件的前提下，才允许使用和分发本软件：
 *    a. 未经[山东流年网络科技有限公司]的明确书面许可，不得对本软件进行修改、复制、分发、出售或出租。
 *    b. 任何未授权的复制、分发或修改都将被视为侵犯[山东流年网络科技有限公司]的知识产权。
 *
 * 3. 免责声明
 *    本软件按"原样"提供，没有任何明示或暗示的保证，包括但不限于适销性、特定用途的适用性和非侵权性的保证。
 *    在任何情况下，[山东流年网络科技有限公司]均不对任何直接、间接、偶然、特殊、典型或间接的损害（包括但不限于采购替代商品或服务；使用、数据或利润损失）承担责任。
 *
 * 4. 侵权通知与处理
 *    a. 如果[山东流年网络科技有限公司]发现或收到第三方通知，表明存在可能侵犯其知识产权的行为，公司将采取必要的措施以保护其权利。
 *    b. 对于任何涉嫌侵犯知识产权的行为，[山东流年网络科技有限公司]可能要求侵权方立即停止侵权行为，并采取补救措施，包括但不限于删除侵权内容、停止侵权产品的分发等。
 *    c. 如果侵权行为持续存在或未能得到妥善解决，[山东流年网络科技有限公司]保留采取进一步法律行动的权利，包括但不限于发出警告信、提起民事诉讼或刑事诉讼。
 *
 * 5. 其他条款
 *    a. [山东流年网络科技有限公司]保留随时修改这些条款的权利。
 *    b. 如果您不同意这些条款，请勿使用本软件。
 *
 * 未经[山东流年网络科技有限公司]的明确书面许可，不得使用此文件的任何部分。
 *
 * 本软件受到[山东流年网络科技有限公司]及其许可人的版权保护。
 */

package com.nageoffer.onethread.dashboard.dev.server.remote.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosConfigDetailRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosConfigListRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosConfigRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosServiceListRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Nacos 代理客户端
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-18
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
     * 查询命名空间下配置文件集合
     *
     * @param namespace 命名空间
     * @return 配置文件集合
     */
    public List<NacosConfigRespDTO> listConfig(String namespace) {
        ensureToken(); // 确保 token 有效

        // Nacos 3.x 使用 v3 admin API
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

        // Nacos 3.x 返回格式: {"code":0,"message":"success","data":{...}}
        JSONObject responseJson = JSON.parseObject(result);
        JSONObject data = responseJson.getJSONObject("data");
        if (data == null) {
            log.error("Nacos response missing 'data' field: {}", result);
            throw new RuntimeException("Invalid Nacos response format");
        }
        
        NacosConfigListRespDTO nacosRemoteResult = data.toJavaObject(NacosConfigListRespDTO.class);
        return nacosRemoteResult.getPageItems();
    }

    /**
     * 查询配置明细信息
     *
     * @param namespace 命名空间
     * @param dataId    数据 ID
     * @param group     分组标识
     * @return 配置明细
     */
    public NacosConfigDetailRespDTO getConfig(String namespace, String dataId, String group) {
        ensureToken();

        // Nacos 3.x 使用 v3 admin API
        String url = serverAddr + "/nacos/v3/admin/cs/config";

        // 构建请求并发送
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

        // Nacos 3.x 返回格式: {"code":0,"message":"success","data":{...}}
        JSONObject responseJson = JSON.parseObject(result);
        JSONObject data = responseJson.getJSONObject("data");
        if (data == null) {
            log.error("Nacos response missing 'data' field: {}", result);
            throw new RuntimeException("Invalid Nacos response format");
        }
        
        return data.toJavaObject(NacosConfigDetailRespDTO.class);
    }

    /**
     * 发布配置
     *
     * @param namespace   命名空间
     * @param dataId      数据 ID
     * @param group       分组标识
     * @param content     配置文件内容
     * @param contentType 配置文件内容文件格式
     */
    public void publishConfig(String namespace, String dataId, String group, String appName, String id, String md5, String content, String contentType) {
        ensureToken();

        // Nacos 3.x 使用 v3 admin API
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

        // 发起 POST 请求
        HttpResponse response = HttpRequest.post(url)
                .form(form)
                .execute();

        if (!response.isOk()) {
            log.error("Failed to publish config to Nacos. Status: {}, dataId: {}, group: {}, namespace: {}", 
                    response.getStatus(), dataId, group, namespace);
            throw new RuntimeException("Nacos server returned error: " + response.getStatus());
        }
    }

    /**
     * 查询命名空间下服务明细
     *
     * @param namespace   命名空间
     * @param serviceName 服务名
     * @return 服务明细响应
     */
    public NacosServiceListRespDTO getService(String namespace, String serviceName) {
        ensureToken();

        // Nacos 3.x 可继续使用 v2 ns API
        String url = serverAddr + "/nacos/v2/ns/catalog/instances";

        HttpRequest request = HttpRequest.get(url)
                .form("pageNo", "1")
                .form("pageSize", "100") // 默认单个 service 最大 100 条数据,如果超过写个 while 循环读取即可
                .form("clusterName", "DEFAULT")
                .form("groupName", "DEFAULT_GROUP")
                .form("serviceName", serviceName)
                .form("namespaceId", Objects.equals(namespace, "public") ? "" : namespace);

        HttpResponse response = withAuth(request).execute();

        String result = response.body();
        if (!response.isOk()) {
            log.warn("Failed to get service from Nacos. Status: {}, serviceName: {}, namespace: {}", 
                    response.getStatus(), serviceName, namespace);
            return NacosServiceListRespDTO.builder()
                    .count(0)
                    .build();
        }

        return JSON.parseObject(result, NacosServiceListRespDTO.class);
    }
}
