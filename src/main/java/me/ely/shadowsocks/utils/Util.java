/*
 * Copyright (c) 2015, Blake
 * All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. The name of the author may not be used to endorse or promote
 * products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package me.ely.shadowsocks.utils;

import me.ely.shadowsocks.protocol.Socks5Protocol;

import java.security.SecureRandom;

/**
 * Helper class
 */
public class Util {
    public static String dumpBytes(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%x", b & 0xff));
        return sb.toString();
    }

    public static byte[] randomBytes(int size) {
        byte[] bytes = new byte[size];
        new SecureRandom().nextBytes(bytes);
//        for (int i = 0; i < size; i++) {
//            bytes[i] = (byte) i;
//        }
        return bytes;
    }

    public static String getRequestedHostInfo(byte[] data) {
        String ret = "";
        int port;
        int destLength;
        switch (data[0]) {
            case Socks5Protocol.ATYP_IP_V4:
                // IP v4 Address
                // 4 bytes of IP, 2 bytes of port
                destLength = 6;
                if (data.length > destLength) {
                    port = getPort(data[5], data[6]);
                    ret = String.format("%d.%d.%d.%d:%d", byteToUnsignedByte(data[1]), byteToUnsignedByte(data[2]), byteToUnsignedByte(data[3]), byteToUnsignedByte(data[4]), port);
                }
                break;
            case Socks5Protocol.ATYP_DOMAIN_NAME:
                // domain
                destLength = data[1];
                port = getPort(data[data.length - 2], data[data.length - 1]);
                String domain = new String(data, 2, destLength);
                ret = String.format("%s:%d", domain, port);
                break;
            case Socks5Protocol.ATYP_IP_V6:
                // IP v6 Address
                // 16 bytes of IP, 2 bytes of port
                destLength = 18;
                if (data.length > destLength) {
                    port = getPort(data[17], data[18]);
                    ret = String.format("%x%x:%x%x:%x%x:%x%x:%x%x:%x%x:%x%x:%x%x:%d",
                            data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8],
                            data[9], data[10], data[11], data[12], data[13], data[14], data[15], data[16],
                            port);
                }
                break;
        }

        return ret;
    }


    public static byte[] composeSSHeader(String host, int port) {
        byte[] hostBytes = host.getBytes();
        // TYPE (1 byte) + LENGTH (1 byte) + HOST (var bytes) + PORT (2 bytes)
        byte[] respData = new byte[1 + 1 + hostBytes.length + 2];

        respData[0] = Socks5Protocol.ATYP_DOMAIN_NAME;
        respData[1] = (byte)host.length();
        System.arraycopy(hostBytes, 0, respData, 2, hostBytes.length);
        respData[respData.length - 2] = (byte)(port >> 8);
        respData[respData.length - 1] = (byte)(port & 0xFF);

        return  respData;
    }

    private static short byteToUnsignedByte(byte b) {
        return (short)(b & 0xff);
    }

    private static int getPort(byte b, byte b1) {
        return byteToUnsignedByte(b) << 8 | byteToUnsignedByte(b1);
    }


}
