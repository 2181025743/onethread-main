package com.nageoffer.onethread.dashboard.dev.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.nageoffer.onethread.dashboard.dev.server.config.DashBoardConfigProperties;
import com.nageoffer.onethread.dashboard.dev.server.config.OneThreadProperties;
import com.nageoffer.onethread.dashboard.dev.server.dto.ThreadPoolDetailRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.dto.ThreadPoolListReqDTO;
import com.nageoffer.onethread.dashboard.dev.server.dto.ThreadPoolUpdateReqDTO;
import com.nageoffer.onethread.dashboard.dev.server.remote.client.NacosProxyClient;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosConfigDetailRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosConfigRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.remote.dto.NacosServiceListRespDTO;
import com.nageoffer.onethread.dashboard.dev.server.service.ThreadPoolManagerService;
import com.nageoffer.onethread.dashboard.dev.server.service.handler.YamlConfigParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 线程池管理接口实现层
 * <p>
 * 作者：杨潇
 * 开发时间：2025-05-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThreadPoolManagerServiceImpl implements ThreadPoolManagerService {

    private final OneThreadProperties oneThreadProperties;
    private final NacosProxyClient nacosProxyClient;
    private final YamlConfigParser yamlConfigParser;

    /**
     * Nacos 配置缓存，减少重复的 HTTP 请求
     * 缓存键格式：namespace:dataId:group
     * 缓存过期时间：30 秒
     * 最大缓存数量：动态计算（基于 namespace 数量）
     * 
     * 注意：使用 @PostConstruct 延迟初始化，避免在字段初始化时访问未注入的依赖
     */
    private Cache<String, NacosConfigDetailRespDTO> configCache;
    
    /**
     * 缓存最大容量（由 calculateOptimalCacheSize() 计算）
     */
    private int cacheMaxSize;

    /**
     * 初始化缓存（在构造函数和依赖注入完成后执行）
     */
    @PostConstruct
    public void initCache() {
        this.cacheMaxSize = calculateOptimalCacheSize();
        this.configCache = CacheBuilder.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .maximumSize(cacheMaxSize)
                .recordStats()  // 启用缓存统计
                .build();
    }

    /**
     * 动态计算最优缓存大小
     * <p>
     * 计算策略：namespace 数量 × 每个 namespace 平均配置数(20) × 冗余系数(1.5)
     * 最小值：100（保证小型项目的缓存效率）
     * 最大值：500（避免内存占用过大）
     *
     * @return 最优缓存大小
     */
    // 问题分析：
    //
    // | 因素 | 是否过度设计？ | 说明 |
    // |------|---------------|------|
    // | 动态计算 | ⚠️ 可能过度 | 99% 的项目配置数量稳定，不需要动态计算 |
    // | 复杂公式 | ⚠️ 可能过度 | namespace × 20 × 1.5 是基于假设，不一定准确 |
    private int calculateOptimalCacheSize() {
        List<String> namespaces = oneThreadProperties.getNamespaces();
        int namespaceCount = (namespaces != null && !namespaces.isEmpty()) ? namespaces.size() : 1;
        
        // 假设每个 namespace 平均有 20 个配置文件
        int estimatedConfigCount = namespaceCount * 20;
        
        // 增加 1.5 倍冗余，避免频繁淘汰
        int calculatedSize = (int) (estimatedConfigCount * 1.5);
        
        // 限制在合理范围内：最小 100，最大 500
        int cacheSize = Math.max(100, Math.min(500, calculatedSize));
        
        log.info("初始化 Nacos 配置缓存 - Namespace 数量: {}, 预估配置总数: {}, 缓存容量: {}",
                namespaceCount, estimatedConfigCount, cacheSize);
        
        return cacheSize;
    }

    @Override
    public List<ThreadPoolDetailRespDTO> listThreadPool(ThreadPoolListReqDTO requestParam) {
        // 处理 namespace 过滤：智能选择查询范围
        List<String> namespaces = determineNamespaces(requestParam);
        String requestedServiceName = requestParam.getServiceName();

        // 并行拉取各 namespace 的配置，并生成 (namespace, config) 任务对
        List<Map.Entry<String, NacosConfigRespDTO>> tasks = namespaces
                .parallelStream()
                .flatMap(ns -> {
                    List<NacosConfigRespDTO> cfgs = nacosProxyClient.listConfig(ns);
                    if (CollUtil.isEmpty(cfgs)) {
                        return Stream.<Map.Entry<String, NacosConfigRespDTO>>empty();
                    }
                    return cfgs.stream()
                            // 如果传入了 serviceName，则按服务名过滤；优先使用 appName，其次从 dataId 推断
                            .filter(each -> {
                                if (StrUtil.isBlank(requestedServiceName)) {
                                    return true;
                                }
                                String svc = StrUtil.isNotBlank(each.getAppName()) ? each.getAppName() : deriveServiceNameFromDataId(each.getDataId());
                                return Objects.equals(requestedServiceName, svc);
                            })
                            .map(cfg -> new AbstractMap.SimpleEntry<>(ns, cfg));
                })
                .toList();

        // 并行处理任务：补充明细 -> 解析 YAML -> 绑定配置 -> 查询服务实例数 -> 拼装返回
        List<ThreadPoolDetailRespDTO> result = tasks.parallelStream()
                .map(entry -> {
                    String namespace = entry.getKey();
                    NacosConfigRespDTO meta = entry.getValue();

                    // 使用缓存拉取配置明细，减少 HTTP 请求
                    String cacheKey = namespace + ":" + meta.getDataId() + ":" + meta.getGroup();
                    NacosConfigDetailRespDTO detail;
                    try {
                        // 使用 Guava Cache 的原子操作，避免并发情况下的重复加载
                        detail = configCache.get(cacheKey, () -> {
                            log.info("缓存未命中，从 Nacos 拉取配置: {}", cacheKey);
                            return nacosProxyClient.getConfig(namespace, meta.getDataId(), meta.getGroup());
                        });
                        // 如果是从缓存获取的（不会触发上面的 lambda），这里不会有日志
                        // 可以通过缓存统计来验证命中率
                    } catch (Exception e) {
                        log.error("获取配置失败: {}", cacheKey, e);
                        return Collections.<ThreadPoolDetailRespDTO>emptyList();
                    }

                    // 解析 YAML
                    Map<Object, Object> configInfoMap = yamlConfigParser.doParse(detail.getContent());
                    ConfigurationPropertySource sources = new MapConfigurationPropertySource(configInfoMap);
                    Binder binder = new Binder(sources);

                    // 绑定 onethread.*
                    BindResult<DashBoardConfigProperties> bound = binder.bind("onethread", Bindable.of(DashBoardConfigProperties.class));
                    if (!bound.isBound()) {
                        return Collections.<ThreadPoolDetailRespDTO>emptyList();
                    }

                    DashBoardConfigProperties refresherProperties = bound.get();

                    // 计算服务名：优先 appName，其次从 dataId 推断
                    String serviceName = StrUtil.isNotBlank(detail.getAppName()) ? detail.getAppName() : deriveServiceNameFromDataId(meta.getDataId());

                    // 查询当前服务在 Nacos 的实例数（serviceName 可能为空，空则记 0）getExecutors
                    NacosServiceListRespDTO service = StrUtil.isNotBlank(serviceName) ?
                            nacosProxyClient.getService(namespace, serviceName) :
                            NacosServiceListRespDTO.builder().count(0).build();

                    // 补齐返回字段
                    refresherProperties.getExecutors().forEach(each -> {
                        each.setNamespace(namespace);
                        each.setServiceName(serviceName);
                        each.setDataId(meta.getDataId());
                        each.setGroup(meta.getGroup());
                        each.setInstanceCount(service.getCount());
                    });

                    return refresherProperties.getExecutors();
                })
                .flatMap(List::stream)
                .collect(Collectors.toList());
        
        // 输出缓存统计信息
        long evictionCount = configCache.stats().evictionCount();
        double hitRate = configCache.stats().hitRate();
        long currentSize = configCache.size();
        
        log.info("缓存统计 - 命中率: {}, 总请求: {}, 命中: {}, 未命中: {}, 淘汰: {}, 缓存大小: {}/{}",
                String.format("%.2f%%", hitRate * 100),
                configCache.stats().requestCount(),
                configCache.stats().hitCount(),
                configCache.stats().missCount(),
                evictionCount,
                currentSize,
                cacheMaxSize);
        
        // 如果淘汰次数过多，输出警告
        if (evictionCount > 50) {
            log.warn("⚠️ 缓存淘汰次数过多: {}, 可能存在缓存抖动，建议检查系统配置文件数量", evictionCount);
        }
        
        // 如果命中率低于 80%，输出警告
        if (hitRate < 0.8 && configCache.stats().requestCount() > 100) {
            log.warn("⚠️ 缓存命中率偏低: {}%, 当前缓存容量可能不足，当前容量: {}",
                    String.format("%.2f", hitRate * 100), configCache.size());
        }
        return result;

    }

    /**
     * 智能选择要查询的 namespace 列表
     *
     * @param requestParam 请求参数
     * @return 需要查询的 namespace 列表
     */
    private List<String> determineNamespaces(ThreadPoolListReqDTO requestParam) {
        String requestedNamespace = requestParam.getNamespace();

        // 优先级 1：前端明确指定了 namespace
        if (StrUtil.isNotBlank(requestedNamespace)) {
            List<String> allNamespaces = oneThreadProperties.getNamespaces();
            if (allNamespaces != null && allNamespaces.contains(requestedNamespace)) {
                return Collections.singletonList(requestedNamespace);
            }
        }

        // 优先级 2：快速模式，只查询默认 namespace
        Boolean quickMode = oneThreadProperties.getQuickMode();
        if (quickMode != null && quickMode) {
            String defaultNamespace = oneThreadProperties.getDefaultNamespace();
            if (StrUtil.isNotBlank(defaultNamespace)) {
                return Collections.singletonList(defaultNamespace);
            }
        }

        // 优先级 3：全量模式，查询所有配置的 namespace
        List<String> namespaces = oneThreadProperties.getNamespaces();
        return namespaces != null ? new ArrayList<>(namespaces) : Collections.emptyList();
    }

    private String deriveServiceNameFromDataId(String dataId) {
        if (StrUtil.isBlank(dataId)) {
            return null;
        }
        String name = dataId;
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        // 常见前缀 onethread-
        if (name.startsWith("onethread-")) {
            name = name.substring("onethread-".length());
        }
        return name;
    }

    @SneakyThrows
    @Override
    public void updateGlobalThreadPool(ThreadPoolUpdateReqDTO requestParam) {
        NacosConfigDetailRespDTO configDetail = nacosProxyClient.getConfig(requestParam.getNamespace(), requestParam.getDataId(), requestParam.getGroup());
        String originalContent = configDetail.getContent();

        Map<Object, Object> configInfoMap = yamlConfigParser.doParse(originalContent);
        ConfigurationPropertySource source = new MapConfigurationPropertySource(configInfoMap);

        Binder binder = new Binder(source);
        DashBoardConfigProperties onethread = binder.bind("onethread", Bindable.of(DashBoardConfigProperties.class))
                .orElseThrow(() -> new RuntimeException("binding failed"));

        onethread.getExecutors().stream()
                .filter(e -> e.getThreadPoolId().equals(requestParam.getThreadPoolId()))
                .findFirst()
                .ifPresent(e -> {
                    e.setCorePoolSize(requestParam.getCorePoolSize());
                    e.setMaximumPoolSize(requestParam.getMaximumPoolSize());
                    e.setKeepAliveTime(requestParam.getKeepAliveTime());
                    e.setQueueCapacity(requestParam.getQueueCapacity());
                    e.setWorkQueue(requestParam.getWorkQueue());
                    e.setRejectedHandler(requestParam.getRejectedHandler());
                    e.setAllowCoreThreadTimeOut(requestParam.getAllowCoreThreadTimeOut());
                    e.setNotify(BeanUtil.toBean(requestParam.getNotify(), ThreadPoolDetailRespDTO.NotifyConfig.class));
                    e.setAlarm(BeanUtil.toBean(requestParam.getAlarm(), ThreadPoolDetailRespDTO.AlarmConfig.class));
                });

        Map<String, Object> updatedMap = new LinkedHashMap<>();
        updatedMap.put("onethread", onethread);

        YAMLFactory factory = new YAMLFactory();
        factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER); // 去除 Yaml 字符串开头 ---
        factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);

        ObjectMapper objectMapper = new ObjectMapper(factory);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 去除 null 字段
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE); // 驼峰命名转 -，比如 corePooSize 转 core-pool-size

        String yamlStr = objectMapper.writeValueAsString(Collections.singletonMap("onethread", onethread));
        nacosProxyClient.publishConfig(requestParam.getNamespace(), requestParam.getDataId(), requestParam.getGroup(), configDetail.getAppName(), configDetail.getId(), configDetail.getMd5(), yamlStr, "yaml");

        // 更新配置后，清除该配置的缓存，确保下次查询时获取最新数据
        String cacheKey = requestParam.getNamespace() + ":" + requestParam.getDataId() + ":" + requestParam.getGroup();
        configCache.invalidate(cacheKey);
    }
}
