package com.comp455.httpclient;

import com.comp455.httpclient.argparser.ArgParser;
import com.comp455.httpclient.argparser.Command;

import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        // parse cli args
        Optional<Command> command = new ArgParser(args).parse();

        System.out.println("yope");

        // depending on mode of operation, call http client library

        //

    }
}
