package org.romainlavabre.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.romainlavabre.request.exception.Http400Exception;
import org.romainlavabre.request.exception.Http422Exception;
import org.romainlavabre.request.exception.Http500Exception;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequestScope
public class RequestImpl implements Request {

    private final HttpServletRequest    request;
    private final Map< String, Object > parameters;
    private final Map< String, String > queryStrings;
    private       String                body;


    public RequestImpl() throws JsonProcessingException {
        this.parameters   = new HashMap<>();
        this.queryStrings = new HashMap<>();
        this.request      = ( ( ServletRequestAttributes ) RequestContextHolder.getRequestAttributes() ).getRequest();
        this.parseJson();
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
    public String getRawQueryString() {
        return this.request.getQueryString();
    }


    @Override
    public Map< String, Object > getAllParameters( final String prefix ) {
        final Map< String, Object > parameters = new HashMap<>();

        for ( final Map.Entry< String, Object > entry : this.parameters.entrySet() ) {
            if ( entry.getKey().startsWith( prefix ) ) {
                parameters.put( entry.getKey().replace( prefix, "" ), entry.getValue() );
            }
        }

        return parameters;
    }


    @Override
    public String getFormUrlEncodedParameter( String parameter ) {
        return request.getParameter( parameter );
    }


    @Override
    public String getQueryString( final String name ) {
        if ( this.queryStrings.containsKey( name ) ) {
            return this.queryStrings.get( name );
        }

        return this.request.getParameter( name );
    }


    @Override
    public void setQueryString( final String name, final String value ) {
        this.queryStrings.put( name, value.toString() );
    }


    @Override
    public UploadedFile getFile( final String name ) {
        return ( UploadedFile ) this.parameters.get( name );
    }


    @Override
    public List< UploadedFile > getFiles( final String name ) {
        return ( List< UploadedFile > ) this.parameters.get( name );
    }


    @Override
    public void setUploadedFile( final String name, final UploadedFile uploadedFile ) {
        this.parameters.put( name, uploadedFile );
    }


    @Override
    public void addUploadedFile( final String name, final UploadedFile uploadedFile ) {
        if ( this.parameters.get( name ) instanceof List ) {
            final List< UploadedFile > uploadedFiles = ( List< UploadedFile > ) this.parameters.get( name );

            uploadedFiles.add( uploadedFile );

            return;
        }

        this.parameters.put( name, uploadedFile );
    }


    @Override
    public String getClientIp() {
        return this.request.getRemoteAddr();
    }


    @Override
    public String getHeader( final String name ) {
        return this.request.getHeader( name );
    }


    @Override
    public String getContentType() {
        return this.request.getContentType();
    }


    @Override
    public Integer getPort() {
        return this.request.getServerPort();
    }


    @Override
    public String getHost() {
        return this.request.getRemoteHost();
    }


    @Override
    public String getScheme() {
        return this.request.getScheme();
    }


    @Override
    public String getUri() {
        return this.request.getRequestURI();
    }


    @Override
    public String getBaseUrl() {
        return this.request
                .getRequestURL()
                .toString()
                .replaceAll( this.getUri(), "" );
    }


    @Override
    public String getMethod() {
        return this.request.getMethod();
    }


    @Override
    public String getBody() {
        return this.body;
    }


    @Override
    public Cookie[] getCookies() {
        return request.getCookies();
    }


    private void parseJson() throws JsonProcessingException {

        try {
            body = StreamUtils.copyToString( request.getInputStream(), StandardCharsets.UTF_8 );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        if ( this.request.getContentType() == null
                || !this.request.getContentType().contains( "application/json" ) ) {
            return;
        }


        if ( body == null ) {

            final StringBuffer json = new StringBuffer();


            String         line   = null;
            BufferedReader reader = null;

            try {

                reader = this.request.getReader();

                while ( ( line = reader.readLine() ) != null ) {
                    json.append( line );
                }
            } catch ( final IOException e ) {
                e.printStackTrace();
            } catch ( IllegalStateException e ) {
                e.printStackTrace();
                throw new Http500Exception( "INTERNAL_SERVER_ERROR" );
            }

            final String jsonStr = json.toString();

            this.body = jsonStr;

            if ( jsonStr.isBlank() ) {
                return;
            }
        }

        final ObjectMapper objectMapper = new ObjectMapper();

        final Map< String, Object > map = objectMapper.readValue( body, HashMap.class );

        for ( final Map.Entry< String, Object > input : map.entrySet() ) {

            if ( input.getKey().equals( "uploaded_file" ) ) {

                for ( final Map.Entry< String, Map< String, Object > > entry : ( ( Map< String, Map< String, Object > > ) input.getValue() ).entrySet() ) {

                    if ( entry.getValue() instanceof Map ) {
                        this.setUploadedFile( entry.getKey(), this.getUploadedFile( entry.getValue() ) );
                    }

                    if ( entry.getValue() instanceof List ) {
                        for ( final Map< String, Object > uploadedFile : ( List< Map< String, Object > > ) entry.getValue() ) {

                            if ( this.parameters.containsKey( entry.getKey() ) ) {
                                final List< UploadedFile > list = ( List< UploadedFile > ) this.parameters.get( entry.getKey() );
                                list.add( this.getUploadedFile( uploadedFile ) );

                                continue;
                            }

                            final List< UploadedFile > list = new ArrayList<>();

                            list.add( this.getUploadedFile( uploadedFile ) );

                            this.parameters.put( entry.getKey(), list );
                        }
                    }
                }


                continue;
            }

            if ( input.getValue() instanceof Map ) {
                final Map< String, Object > secondLevel = ( Map< String, Object > ) input.getValue();

                for ( final Map.Entry< String, Object > content : secondLevel.entrySet() ) {
                    this.parameters.put( input.getKey() + "_" + content.getKey(), content.getValue() );
                }

                continue;
            }

            if ( input.getValue() instanceof List ) {

                for ( final Map< String, Object > thirdLevel : ( List< Map< String, Object > > ) input.getValue() ) {
                    for ( final Map.Entry< String, Object > content : thirdLevel.entrySet() ) {
                        final String key = input.getKey() + "_" + content.getKey();

                        if ( this.parameters.containsKey( key ) ) {
                            final List< Object > list = ( List< Object > ) this.parameters.get( key );
                            list.add( content.getValue() );
                        } else {
                            final List< Object > list = new ArrayList<>();
                            list.add( content.getValue() );

                            this.parameters.put( key, list );
                        }
                    }
                }

                continue;
            }

            parameters.put( input.getKey(), input.getValue() );
        }
    }


    protected UploadedFile getUploadedFile( final Map< String, Object > map ) {
        final UploadedFile uploadedFile = new UploadedFileImpl();
        uploadedFile.setName( ( String ) map.get( "name" ) );
        uploadedFile.setContent( Base64.getDecoder().decode( ( String ) map.get( "content" ) ) );
        uploadedFile.setContentType( map.get( "content-type" ) == null ? ( String ) map.get( "content_type" ) : ( String ) map.get( "content-type" ) );
        uploadedFile.setSize( uploadedFile.getContent().length );
        uploadedFile.setInfos( ( Map< String, Object > ) map.get( "infos" ) );

        return uploadedFile;
    }
}
