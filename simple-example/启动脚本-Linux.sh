#!/bin/bash

echo "========================================"
echo "oneThread 简单示例 - 启动脚本 (Linux/Mac)"
echo "========================================"
echo ""

echo "[1/3] 正在构建项目..."
mvn clean package -DskipTests -Dspotless.check.skip=true

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ 构建失败！请检查错误信息。"
    exit 1
fi

echo ""
echo "[2/3] 构建成功！"
echo ""
echo "[3/3] 正在启动应用..."
echo ""
echo "⚠️  注意：请确保 Nacos 服务器已启动！"
echo "   Nacos 控制台：http://localhost:8848/nacos"
echo ""
echo "⏳ 应用启动中，请稍候..."
echo ""

java --add-opens=java.base/java.util.concurrent=ALL-UNNAMED \
     -jar target/onethread-simple-example-1.0.0.jar

