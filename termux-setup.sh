#!/bin/bash
# Запускай этот скрипт в Termux один раз для настройки

echo "=== Установка зависимостей ==="
pkg update -y
pkg install -y git

echo "=== Настройка git ==="
read -p "Твой email для GitHub: " email
read -p "Твоё имя: " name
git config --global user.email "$email"
git config --global user.name "$name"

echo "=== Готово! ==="
echo ""
echo "Теперь:"
echo "1. Создай репозиторий на github.com (назови: familia-financial-widget)"
echo "2. Запусти termux-push.sh"
