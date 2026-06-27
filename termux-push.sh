#!/bin/bash
# Запускай каждый раз когда хочешь собрать новый APK

REPO_URL=$1

if [ -z "$REPO_URL" ]; then
    read -p "URL репозитория (например https://github.com/ИМЯ/familia-financial-widget): " REPO_URL
fi

cd "$(dirname "$0")"

if [ ! -d ".git" ]; then
    echo "=== Инициализация git ==="
    git init
    git remote add origin "$REPO_URL"
fi

echo "=== Отправка кода на GitHub ==="
git add .
git commit -m "build: update widget"
git branch -M main
git push -u origin main

echo ""
echo "=== Готово! ==="
echo "Открой GitHub → репозиторий → Actions"
echo "Через ~3 минуты появится APK в Releases"
echo "Скачай APK и установи на телефон"
