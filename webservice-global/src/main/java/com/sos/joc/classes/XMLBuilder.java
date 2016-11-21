package com.sos.joc.classes;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class XMLBuilder {
    
    private Element root;
    
    public XMLBuilder(String name) {
        this.root = DocumentHelper.createElement(name);
    }
    
    public Element getRoot() {
        return this.root;
    }

    public static Element create(String name) {
        return new XMLBuilder(name).getRoot();
    }
    
    public static Element parse(String xmlString) throws DocumentException {
        return DocumentHelper.parseText(xmlString).getRootElement();
    }
    
    public Element addElement(String name) {
        return this.root.addElement(name);
    }
    
    public Element addAttribute(String key, String value) {
        return this.root.addAttribute(key, value);
    }
    
    public void add(Element elem) {
        this.root.add(elem);
    }
    
    public String asXML() {
        return this.root.asXML();
    }

}
