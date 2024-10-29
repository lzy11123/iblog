package com.shanzhu.blog.common.utils;

/**
 * 处理并记录日志文件
 *
 * @author: ShanZhu
 * @date: 2023-12-09
 */
public class LogUtils
{
    public static String getBlock(Object msg)
    {
        if (msg == null)
        {
            msg = "";
        }
        return "[" + msg.toString() + "]";
    }
}
