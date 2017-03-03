#!/bin/bash
# 清除SocksFirewallProxy的设置
networksetup -setsocksfirewallproxystate off
networksetup -setsocksfirewallproxy "Wi-Fi" "" "" "" "" ""
networksetup -setautoproxyurl "Wi-Fi" "http://127.0.0.1:8090/proxy.pac"