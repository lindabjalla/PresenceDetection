package se.mogumogu.presencedetector.rest;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public final class StringConverterFactory extends Converter.Factory {

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(final Type type,
                                                            final Annotation[] annotations,
                                                            final Retrofit retrofit) {

        if (String.class.equals(type)) {

            return new Converter<ResponseBody, Object>() {

                @Override
                public Object convert(final ResponseBody responseBody) throws IOException {

                    return responseBody.string();
                }
            };
        }

        return null;
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(final Type type,
                                                          final Annotation[] parameterAnnotations,
                                                          final Annotation[] methodAnnotations,
                                                          final Retrofit retrofit) {

        if (String.class.equals(type)) {

            return new Converter<String, RequestBody>() {

                @Override
                public RequestBody convert(final String value) throws IOException {

                    return RequestBody.create(MediaType.parse("text/plain"), value);
                }
            };
        }

        return null;
    }
}
