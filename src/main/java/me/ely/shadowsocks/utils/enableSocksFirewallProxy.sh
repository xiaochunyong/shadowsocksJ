#!/bin/bash
# 清除AutoProxyConfiguration的设置
networksetup -setautoproxystate off
networksetup -setautoproxyurl Wi-Fi ""
networksetup -setsocksfirewallproxy Wi-Fi "127.0.0.1" "1080"