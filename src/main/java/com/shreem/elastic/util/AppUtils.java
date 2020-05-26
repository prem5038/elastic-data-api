package com.shreem.elastic.util;

import org.springframework.util.FileCopyUtils;

import java.io.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AppUtils {

    public static String asString(InputStream inputStream){
        try(Reader reader = new InputStreamReader(inputStream, UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException ioException){
            throw new UncheckedIOException(ioException);
        }
    }

}
