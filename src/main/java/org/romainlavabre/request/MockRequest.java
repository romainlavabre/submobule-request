package org.romainlavabre.request;

import jakarta.servlet.http.Cookie;
import org.romainlavabre.request.exception.Http400Exception;
import org.romainlavabre.request.exception.Http422Exception;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Romain Lavabre <romainlavabre98@gmail.com>
 */
public class MockRequest implements Request {


    protected final Map< String, Object >       parameters;
    protected final Map< String, UploadedFile > files;
    protected final Map< String, String >       headers;
    protected final Map< String, String >       queryStrings;


    public MockRequest() {
        this.headers      = new HashMap<>();
        this.parameters   = new HashMap<>();
        this.files        = new HashMap<>();
        this.queryStrings = new HashMap<>();
    }


    @Override
    public boolean containsParameter( final String name ) {
        return this.parameters.containsKey( name );
    }


    @Override
    public Object getParameter( final String name ) {
        return this.parameters.get( name );
    }


    @Override
    public < T > T getParameter( String name, Class< T > type ) {
        return getParameter( name, type, false );
    }


    @Override
    public < T > T getParameter( String name, Class< T > type, boolean keepRawData ) {
        if ( parameters.get( name ) == null ) {
            return null;
        }

        if ( !keepRawData && parameters.get( name ) == "" ) {
            return null;
        }

        try {
            if ( String.class == type ) {
                return ( T ) parameters.get( name ).toString();
            }

            if ( Byte.class == type ) {
                return ( T ) Byte.valueOf( parameters.get( name ).toString() );
            }

            if ( Short.class == type ) {
                return ( T ) Short.valueOf( parameters.get( name ).toString() );
            }

            if ( Integer.class == type ) {
                return ( T ) Integer.valueOf( parameters.get( name ).toString() );
            }

            if ( Long.class == type ) {
                return ( T ) Long.valueOf( parameters.get( name ).toString() );
            }

            if ( Double.class == type ) {
                return ( T ) Double.valueOf( parameters.get( name ).toString() );
            }

            if ( Float.class == type ) {
                return ( T ) Float.valueOf( parameters.get( name ).toString() );
            }

            if ( Boolean.class == type ) {
                return ( T ) Boolean.valueOf( parameters.get( name ).toString() );
            }
        } catch ( ClassCastException classCastException ) {
            throw new Http422Exception( "BAD_PARAMETER_TYPE" );
        }

        throw new Http400Exception( "UNSUPPORTED_PARAMETER_TYPE" );
    }


    @Override
    public void setParameter( final String name, final Object value ) {
        this.parameters.put( name, value );
    }


    @Override
    public List< Object > getParameters( final String name ) {
        return ( List< Object > ) this.parameters.get( name );
    }


    @Override
    public Map< String, Object > getAllParameters() {
        return this.parameters;
    }


    @Override
    public Map< String, Object > getAllParameters( final String prefix ) {
        final Map< String, Object > parameters = new HashMap<>();

        for ( final Map.Entry< String, Object > entry : parameters.entrySet() ) {
            if ( entry.getKey().startsWith( prefix ) ) {
                parameters.put( entry.getKey().replace( prefix, "" ), entry.getValue() );
            }
        }

        return parameters;
    }


    @Override
    public String getFormUrlEncodedParameter( String parameter ) {
        return null;
    }


    @Override
    public String getRawQueryString() {
        StringJoiner stringJoiner = new StringJoiner( "&" );

        for ( Map.Entry< String, String > entry : this.queryStrings.entrySet() ) {
            stringJoiner.add( URLEncoder.encode( entry.getKey() + "=" + entry.getValue(), StandardCharsets.UTF_8 ) );
        }

        return stringJoiner.toString();
    }


    @Override
    public String getQueryString( final String name ) {
        return queryStrings.get( name );
    }


    @Override
    public void setQueryString( final String name, final String value ) {
        queryStrings.put( name, value );
    }


    @Override
    public UploadedFile getFile( final String name ) {
        return this.files.get( name );
    }


    @Override
    public List< UploadedFile > getFiles( final String name ) {
        final List< UploadedFile > list = new ArrayList<>();

        this.files.forEach( ( key, value ) -> {
            list.add( value );
        } );

        return list;
    }


    @Override
    public void setUploadedFile( final String name, final UploadedFile uploadedFile ) {
        this.files.put( name, uploadedFile );
    }


    @Override
    public void addUploadedFile( String name, UploadedFile uploadedFile ) {
        this.files.put( name, uploadedFile );
    }


    @Override
    public String getClientIp() {
        return null;
    }


    @Override
    public String getHeader( final String name ) {
        return headers.get( name );
    }


    public void setHeader( String name, String value ) {
        this.headers.put( name, value );
    }


    @Override
    public String getContentType() {
        return "application/json";
    }


    @Override
    public Integer getPort() {
        return 8085;
    }


    @Override
    public String getHost() {
        return "localhost";
    }


    @Override
    public String getScheme() {
        return "http";
    }


    @Override
    public String getUri() {
        return "/api";
    }


    @Override
    public String getBaseUrl() {
        return "http://localhost:8085";
    }


    @Override
    public String getMethod() {
        return null;
    }


    @Override
    public String getBody() {
        return "";
    }


    @Override
    public Cookie[] getCookies() {
        return new Cookie[ 0 ];
    }


    public static Request build( final Map< String, Object > parameters, final Map< String, UploadedFile > files, Map< String, String > headers, Map< String, String > queryStrings ) {

        final MockRequest request = new MockRequest();

        parameters.forEach( request::setParameter );
        files.forEach( request::setUploadedFile );
        headers.forEach( request::setHeader );
        queryStrings.forEach( request::setQueryString );

        return request;
    }


    public static Request build( final Map< String, Object > parameters, final Map< String, UploadedFile > files, Map< String, String > headers ) {

        final MockRequest request = new MockRequest();

        parameters.forEach( request::setParameter );
        files.forEach( request::setUploadedFile );
        headers.forEach( request::setHeader );


        return request;
    }


    public static Request build( final Map< String, Object > parameters, final Map< String, UploadedFile > files ) {

        final MockRequest request = new MockRequest();

        parameters.forEach( request::setParameter );
        files.forEach( request::setUploadedFile );


        return request;
    }


    public static Request build( final Map< String, Object > parameters ) {

        final Request request = new MockRequest();

        parameters.forEach( request::setParameter );

        return request;
    }


    public static Request build() {
        return new MockRequest();
    }
}
