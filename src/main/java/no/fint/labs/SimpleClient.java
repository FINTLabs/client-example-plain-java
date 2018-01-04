package no.fint.labs;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.PasswordTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class SimpleClient {

    public static class Links extends GenericJson {

    }

    public static class Entry extends GenericJson {

    }

    public static class Embedded {
        @Key("_entries")
        public List<Entry> entries;

        @Override
        public String toString() {
            return "Embedded{" +
                    "entries=" + entries +
                    '}';
        }
    }

    public static class Resource {
        @Key("total_items")
        public int totalItems;
        @Key("_embedded")
        public Embedded embedded;
        @Key("_links")
        public Links links;

        @Override
        public String toString() {
            return "Resource{" +
                    "totalItems=" + totalItems +
                    ", embedded=" + embedded +
                    ", links=" + links +
                    '}';
        }
    }


    public static void main(String[] args) throws IOException {
        Properties properties = System.getProperties();
        Path propertiesFile = Paths.get("oauth.properties");
        if (Files.exists(propertiesFile)) {
            System.out.println("Loading OAuth configuration from " + propertiesFile.toAbsolutePath());
            properties.load(Files.newBufferedReader(propertiesFile));
        }
        String clientId = properties.getProperty("no.fint.labs.client-id");
        String clientSecret = properties.getProperty("no.fint.labs.client-secret");
        String username = properties.getProperty("no.fint.labs.username");
        String password = properties.getProperty("no.fint.labs.password");
        String tokenURI = properties.getProperty("no.fint.labs.token-uri", "https://namidp01.rogfk.no/nidp/oauth/nam/token");
        String scope = properties.getProperty("no.fint.labs.scope", "fint-client");

        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        TokenResponse tokenResponse = new PasswordTokenRequest(httpTransport, jsonFactory, new GenericUrl(tokenURI), username, password)
                        .setScopes(Collections.singleton(scope))
                        .setClientAuthentication(new BasicAuthentication(clientId, clientSecret)).execute();
        System.out.println("Access token: " + tokenResponse.getAccessToken());

        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod()).setFromTokenResponse(tokenResponse);
        HttpRequestFactory httpRequestFactory = httpTransport.createRequestFactory(credential);

        HttpRequest httpRequest = httpRequestFactory.buildGetRequest(new GenericUrl("https://beta.felleskomponent.no/administrasjon/personal/person"));
        httpRequest.getHeaders().set("x-org-id", "pwf.no");
        httpRequest.getHeaders().set("x-client", "fint-labs");
        httpRequest.setParser(new JsonObjectParser(jsonFactory));
        HttpResponse response = httpRequest.execute();
        System.out.println("response = " + response.getStatusCode());

        //response.download(System.out);

//        GenericJson jsonObject = response.parseAs(GenericJson.class);

//        System.out.println("totalItems = " + jsonObject.get("totalItems"));
//        System.out.println("links = " + jsonObject.get("links"));
//        System.out.println("content = " + jsonObject.get("embedded"));

        Resource resource = response.parseAs(Resource.class);

        System.out.println("resource = " + resource);
        System.out.println("resource.totalItems = " + resource.totalItems);
        System.out.println("resource.links = " + resource.links);
        System.out.println("resource.embedded = " + resource.embedded);
        System.out.println("resource.embedded.entries = " + resource.embedded.entries);
        System.out.println("resource.embedded.entries.get(0) = " + resource.embedded.entries.get(0));
        System.out.println("resource.embedded.entries.get(0).get(\"navn\") = " + resource.embedded.entries.get(0).get("navn"));
    }
}
