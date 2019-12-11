package com.bms.project.spring;

import com.mysql.jdbc.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;

/**
 * @author 网上抄的
 * @date 2019/11/7 22:42
 * 拷贝来源:https://blog.csdn.net/qq_30401609/article/details/82760578
 */
public class BodyReaderHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BodyReaderHttpServletRequestWrapper.class);

    private byte[] body;

    BodyReaderHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        String charset = request.getCharacterEncoding();
        if (StringUtils.isNullOrEmpty(charset)) {
            charset = "UTF-8";
        }
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = request.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, charset));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            body = sb.toString().getBytes(Charset.forName(charset));
        } catch (IOException e) {
            LOGGER.error("读取请求参数异常", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error("关闭InputStream异常", e);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error("关闭BufferedReader异常", e);
                }
            }
        }
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // Do nothing
            }

        };
    }

}
