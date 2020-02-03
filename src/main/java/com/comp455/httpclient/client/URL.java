package com.comp455.httpclient.client;

import lombok.Getter;
import lombok.Setter;

import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class URL {

    private static final List<String> VALID_PROTOCOLS = List.of("http");

    private String protocol;
    private String domainName;
    private List<String> path;
    private String query;

    public URL(String rawUrlString) throws ParseException {
        String remainingUrlPortion = rawUrlString;

        // PROTOCOL
        int protocolLength = remainingUrlPortion.indexOf("://");
        if(protocolLength < 2) {
            throw new ParseException("Missing protocol", 0);
        }
        this.protocol = remainingUrlPortion.substring(0, protocolLength);
        if(VALID_PROTOCOLS.indexOf(this.protocol) == -1) {
            throw new ParseException(String.format("Invalid protocol: '%s'", this.protocol), protocolLength);
        }
        remainingUrlPortion = remainingUrlPortion.substring(protocolLength + 3);

        // QUERY
        int queryStartIndex = remainingUrlPortion.indexOf("?") + 1;
        if(queryStartIndex != 0 && queryStartIndex != remainingUrlPortion.length()) {
            this.query = remainingUrlPortion.substring(queryStartIndex);
            if(this.query.length() == 0) {
                this.query = null;
            } else {
                remainingUrlPortion = remainingUrlPortion.substring(0, queryStartIndex - 1);
            }
        }

        // PATH
        int pathStartIndex = remainingUrlPortion.indexOf("/");
        if(pathStartIndex != -1) {
            this.path = List.of(remainingUrlPortion.substring(pathStartIndex + 1).split("/"));
            remainingUrlPortion = remainingUrlPortion.substring(0, pathStartIndex);
        }

        if(remainingUrlPortion.length() == 0) {
            throw new ParseException("Missing domain", protocolLength);
        }
        this.domainName = remainingUrlPortion;
        if(!this.domainName.contains(".")) {
            throw new ParseException(String.format("Invalid domain name: '%s'", this.domainName), protocolLength);
        }
    }

    public String toString() {

        String fullQueryString = this.query == null ?
                "" : ("?" + this.query);

        String fullPathString = Stream
                .concat(List.of("").stream(), path.stream())
                .collect(Collectors.joining("/"));

        return String.format("%s://%s%s%s", this.protocol, this.domainName, this.path, this.query);
    }
}
