/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.ivy.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.ivy.url.URLHandlerRegistry;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;


public abstract class XMLHelper {
    private static SAXParserFactory _validatingFactory = SAXParserFactory.newInstance();

    private static SAXParserFactory _factory = SAXParserFactory.newInstance();

    static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

    static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    private static boolean _canUseSchemaValidation = true;

    static {
        _validatingFactory.setNamespaceAware(true);
        _validatingFactory.setValidating(true);
    }

    private static SAXParser newSAXParser(URL schema, InputStream schemaStream) throws ParserConfigurationException, SAXException {
        if (!_canUseSchemaValidation || schema == null) {
            return _factory.newSAXParser();
        }
        try {
            SAXParser parser = _validatingFactory.newSAXParser();
            parser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            parser.setProperty(JAXP_SCHEMA_SOURCE, schemaStream);
            return parser;
        } catch (SAXNotRecognizedException ex) {
            System.err.println("WARNING: problem while setting JAXP validating property on SAXParser... XML validation will not be done: " + ex.getMessage());
            _canUseSchemaValidation = false;
            return _factory.newSAXParser();
        }
    }

    // IMPORTANT: validation errors are only notified to the given handler, and
    // do not cause exception
    // implement warning error and fatalError methods in handler to be informed
    // of validation errors
    public static void parse(URL xmlURL, URL schema, DefaultHandler handler) throws SAXException, IOException, ParserConfigurationException {
    	parse(xmlURL, schema, handler, null);
    }
    
    public static void parse(URL xmlURL, URL schema, DefaultHandler handler, LexicalHandler lHandler) throws SAXException, IOException, ParserConfigurationException {
       InputStream xmlStream = URLHandlerRegistry.getDefault().openStream(xmlURL);
       try {
           parse(xmlStream, schema, handler, lHandler);
       } finally {
           try {
               xmlStream.close();
           } catch (IOException e) {}
       }
    } 
    
    public static void parse(InputStream xmlStream, URL schema, DefaultHandler handler, LexicalHandler lHandler) throws SAXException, IOException, ParserConfigurationException {
        InputStream schemaStream = null;
        try {
            if (schema != null) {
                schemaStream = URLHandlerRegistry.getDefault().openStream(schema);
            }
            SAXParser parser = XMLHelper.newSAXParser(schema, schemaStream);
            
            if (lHandler != null) {
            	try {
            		parser.setProperty("http://xml.org/sax/properties/lexical-handler", lHandler);
            	} catch (SAXException ex) {
                    System.err.println("WARNING: problem while setting the lexical handler property on SAXParser: " + ex.getMessage());
                    // continue without the lexical handler
            	}
            }
            
            parser.parse(xmlStream, handler);
        } finally {
            if (schemaStream != null) {
                try {
                    schemaStream.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    public static boolean canUseSchemaValidation() {
        return _canUseSchemaValidation;
    }

}