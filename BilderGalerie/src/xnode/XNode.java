
package xnode;


import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*
 * XNode.java
 *
 * Copyright (c) 2013 Ascom Deutschland GmbH, All rights reserved.
 *
 * $Id: XNode.java 2935 2014-04-15 14:32:04Z mvdberg $
 */

/**
 * Simple class for xml style data.
 *
 * @author mvdberg
 * @since Sep 27, 2013 12:01:32 PM
 */
public class XNode implements List<XNode> {

    XNode parent = null;
    String name = "";
    String value = "";
    boolean detach = false;
    TreeMap<String, String> atts = new TreeMap<String, String>();
    ArrayList<XNode> subs = new ArrayList<XNode>();
    private final static String crlf = "\r\n";

    private final static String mPrefix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + crlf;

    /**
     * @param string
     * @return
     */
    ArrayList<XNode> getNodesByName(final String nameIn) {
        ArrayList<XNode> result = new ArrayList<XNode>();
        getNodesByNameRecursive(nameIn, result);
        return result;
    }

    /**
     * @param name2
     * @param result
     */
    private void getNodesByNameRecursive(final String nameIn, final ArrayList<XNode> result) {
        if (getName().equals(nameIn)) {
            result.add(this);
        }
        for (XNode child : this.children()) {
            child.getNodesByNameRecursive(nameIn, result);
        }
    }

    /**
     * @param string
     * @return
     */
    public XNode getRecursive(final String name) {
        XNode found = getChild(name);
        if (found != null) {
            return found;
        }
        for (XNode child : this.children()) {
            found = child.getRecursive(name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Constructor.
     *
     * @param nameIn
     */
    public XNode(final String nameIn) {
        this.name = nameIn;
    }

    /**
     * Clones just this node without children.
     *
     * @return the cloned node.
     */
    public XNode cloneNodeOnly() {
        XNode clone = new XNode(getName(), this.getValue());
        for (Entry<String, String> e : this.atts.entrySet()) {
            clone.setAttribute(e.getKey(), e.getValue());
        }
        return clone;
    }

    /**
     * Finds the first node by node name and attribute.
     *
     * @param name
     * @param attName
     * @param attValue
     * @return
     */
    public XNode getByNameAndAttribute(final String name, final String attName, final String attValue) {
        return getByNameAndAttribute(name, attName, attValue, Integer.MAX_VALUE);
    }

    /**
     * Finds the first node by node name and attribute.
     *
     * @param name
     * @param attName
     * @param attValue
     * @return
     */
    public XNode getByNameAndAttribute(final String name, final String attName, final String attValue,
            final int diveInto) {
        String xname = getName();
        if (xname.equals(name)) {
            String locValue = this.getAttribute(attName, "---");
            if (locValue.equals(attValue)) {
                return this;
            }
        }
        if (diveInto > 0) {
            for (XNode child : children()) {
                XNode found = child.getByNameAndAttribute(name, attName, attValue, diveInto - 1);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * Finds the first node by attribute.
     *
     * @param attName
     * @param attValue
     * @return
     */
    public XNode getByAttribute(final String attName, final String attValue) {
        String locValue = this.getAttribute(attName, "---");
        if (locValue.equals(attValue)) {
            return this;
        }
        for (XNode child : children()) {
            XNode found = child.getByAttribute(attName, attValue);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Gets the attribute with the given key.
     *
     * @param attName
     * @param defaultValue
     * @return
     */
    public String getAttribute(final String attName, final String defaultValue) {
        String value = this.getAttribute(attName);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Sets the name of the node.
     *
     * @param value2
     */
    public void setName(final String value2) {
        this.name = value2;
    }

    /**
     * Deletes all nodes recursively that have a delete flag.
     */
    public void deleteMarkedDetachs() {
        ArrayList<XNode> found = new ArrayList<XNode>();
        for (XNode child : children()) {
            if (child.detach) {
                found.add(child);
            }
        }
        for (XNode f : found) {
            this.remove(f);
        }
        for (XNode child : children()) {
            child.deleteMarkedDetachs();
        }
    }

    /**
     * Mark a node with the delete flag.
     */
    public void markDetach() {
        this.detach = true;
    }

    /**
     * Constructor.
     *
     * @param name2
     * @param value2
     */
    public XNode(final String name2, final String value2) {
        this.name = name2;
        this.value = value2;
    }

    /**
     * Gets a child with the given name, if it does not exist it will be created.
     *
     * @param string
     * @return
     */
    public XNode getOrCreate(final String string) {
        XNode found = this.get(string);
        if (found != null) {
            return found;
        }
        found = this.addChild(string);
        return found;
    }

    /**
     * Adds a child with the given name.
     *
     * @param name
     * @return
     */
    public XNode addChild(final String name) {
        XNode child = new XNode(name);
        child.parent = this;
        this.subs.add(child);
        return child;
    }

    /**
     * Adds a child with the given name and value.
     *
     * @param name
     * @param value
     * @return
     */
    public XNode addChild(final String name, final String value) {
        XNode child = new XNode(name, value);
        child.parent = this;
        this.subs.add(child);
        return child;
    }

    /**
     * Gets the first child with the given name.
     *
     * @param string
     * @return
     */
    public XNode get(final String string) {
        return getChild(string);
    }

    /**
     * Gets the first child with the given name.
     *
     * @param string
     * @return
     */
    public XNode getChild(final String string) {
        for (XNode child : this.subs) {
            if (child.getName().equals(string)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Gets the name of the node.
     *
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets an attribute.
     *
     * @param key
     * @param value
     */
    public XNode setAttribute(final String key, final String value) {
        this.atts.put(key, value);
        return this;
    }

    /**
     * Returns the children count.
     *
     * @return
     */
    @Override
    public int size() {
        return this.subs.size();
    }

    /**
     * Gets the child with the given index.
     *
     * @param col
     * @return
     */
    public XNode getChild(final int col) {
        return this.subs.get(col);
    }

    /**
     * Gets the child with the given index.
     *
     * @param col
     * @return
     */
    @Override
    public XNode get(final int col) {
        return getChild(col);
    }

    /**
     * Gets the value.
     *
     * @return
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Gets the value, if the value is null, the default value is returned.
     *
     * @param defaultval
     * @return
     */
    public String getValue(final String defaultval) {
        if (this.value == null) {
            return defaultval;
        }

        return this.value;
    }

    /**
     * Adds the node as a child.
     *
     * @param rowNode
     * @return
     */
    public XNode addChild(final XNode rowNode) {
        rowNode.parent = this;
        this.subs.add(rowNode);
        return rowNode;
    }

    /**
     * Gets an attribute.
     *
     * @param key
     * @return
     */
    public String getAttribute(final String key) {
        return this.atts.get(key);
    }

    /**
     * Gets the attribute as boolean. If the value is not '1' or 'true' then return false.
     *
     * @param key the key
     * @return the attribute as boolean
     */
    public boolean getAttributeAsBoolean(final String key) {
        if (key == null) {
            return false;
        }
        String s = this.atts.get(key);
        boolean b = ((s != null) && (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("1")));
        return b;
    }

    /**
     * Gets the attribute as boolean. If the value is not '1' or 'true' then return false.
     *
     * @param key the key
     * @return the attribute as boolean
     */
    public int getAttributeAsInt(final String key) {
        String s = this.atts.get(key);
        int i = 0;
        try {
            i = Integer.parseInt(s);
        }
        catch (Exception e) {
            // System.err.println("Fehler bei getAttributeAsInt:" + e.getLocalizedMessage());
        }
        return i;
    }

    /**
     * Removes an attribute.
     *
     * @param key
     */
    public void removeAttribute(final String key) {
        this.atts.remove(key);
    }

    /**
     * Returns the clone of the node.
     *
     * @return
     */
    public XNode cloneNode() {
        XNode clone = new XNode(getName(), this.getValue());
        for (Entry<String, String> e : this.atts.entrySet()) {
            clone.setAttribute(e.getKey(), e.getValue());
        }

        for (XNode sub : this.subs) {
            clone.addChild(sub.cloneNode());
        }

        return clone;
    }

    /**
     * Sets the value.
     *
     * @param content
     */
    public void setValue(final String content) {
        this.value = content;
    }

    /**
     * Gets the attributes.
     *
     * @return
     */
    public Map<String, String> getAttributes() {
        return this.atts;
    }

    /**
     * Gets the children.
     *
     * @return
     */
    public List<XNode> children() {
        return this.subs;
    }

    /**
     * Gets the children.
     *
     * @return
     */
    public List<XNode> getChildren() {
        return children();
    }

    /**
     * Gets the index of a child.
     *
     * @return
     */
    public int getIndex() {
        if (this.parent != null) {
            return this.parent.subs.indexOf(this);
        }
        return -1;
    }

    /**
     * Removes a child.
     *
     * @param child
     */
    public void remove(final XNode child) {
        this.subs.remove(child);
        child.parent = null;
    }

    /**
     * Removes a child.
     *
     * @param idx
     */
    public void removeChild(final int idx) {
        XNode child = this.subs.get(idx);
        this.subs.remove(idx);
        child.parent = null;
    }

    @Override
    public String toString() {
        StringBuffer out = new StringBuffer();
        log(out, "");
        return out.toString();
    }

    /**
     * Creates the toString string.
     *
     * @param out
     * @param tab
     */
    public void log(final StringBuffer out, final String tab) {
        out.append(tab + this.name);
        if (this.value != null) {
            out.append("=" + this.value);
        }
        if (this.atts.size() > 0) {
            out.append("[");
            boolean first = true;
            for (Entry<String, String> e : this.atts.entrySet()) {
                if (!first) {
                    out.append(", ");
                }
                first = false;
                out.append(e.getKey() + "=" + e.getValue());
            }
            out.append("]");
        }
        out.append("" + crlf);
        for (XNode child : this.subs) {
            child.log(out, tab + "\t");
        }
    }

    public List<XNode> getNodesByNameAndValue(final String name, final String value) {
        ArrayList<XNode> result = new ArrayList<XNode>();
        getNodesByNameAndValue(name, value, result);
        return result;
    }

    /**
     * @param name2
     * @param value2
     * @param result
     */
    private void getNodesByNameAndValue(final String name2, final String value2, final ArrayList<XNode> result) {
        long t = System.currentTimeMillis();
        String val1Check = this.value;
        if (val1Check == null) {
            val1Check = "" + t;
        }
        String val2Check = value2;
        if (val2Check == null) {
            val2Check = "" + t;
        }
        if (this.name.equals(name2) && this.value.equals(value2)) {
            result.add(this);
        }
        for (XNode child : this.children()) {
            child.getNodesByNameAndValue(name2, value2, result);
        }
    }

    /**
     * Gets a node by name and value.
     *
     * @param name
     * @param val
     * @return
     */
    public XNode getNodeByNameAndValue(final String name, final String val) {

        if ((name == null) || (name.length() <= 0) || (val == null) || (val.length() <= 0)) {
            return null;
        }
        final List<XNode> children = children();
        if ((children != null) && (!children.isEmpty())) {
            for (int idx = 0; idx < children.size(); ++idx) {
                final XNode child = children.get(idx);
                if (name.equals(child.getName())) {
                    final String value = child.getValue("---");
                    if (val.equals(value)) {
                        return child;
                    }
                }
                final XNode found = child.getNodeByNameAndValue(name, val);
                if (found != null) {

                    return found;
                }
            }
        }
        return null;
    }

    /**
     * Returns the node as xml string.
     *
     * @return
     */
    public String toXmlString() {

        final StringBuffer buf = new StringBuffer();
        buf.append(XNode.mPrefix);
        final String tab = "";
        toXmlString(this, buf, tab);
        return buf.toString();
    }

    /**
     * Internal xml string method.
     *
     * @param node
     * @param buf
     * @param tab
     */
    private static void toXmlString(final XNode node, final StringBuffer buf, final String tab) {

        final String name = node.getName();
        if (name.equals(C.ELEM_COMMENT)) {
            buf.append("<!-- ");
        }
        else {
            buf.append(tab + "<" + name);
            Iterator<Map.Entry<String, String>> it = node.getAttributes().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> me = it.next();
                final String key = me.getKey();
                String keyval = escapeXml(me.getValue(), true);
                buf.append(" " + key + "=\"" + keyval + "\"");
            }
            if ((node.getValue().length() == 0) && (node.children().size() == 0)) {
                buf.append(" />" + crlf);
                return;
            }
            buf.append(">");
        }

        if (name.equals(C.script)) {
            buf.append(node.getValue());
        }
        else {
            buf.append(escapeXml(node.getValue(), false));
        }
        if (node.size() > 0) {
            buf.append("" + crlf);
        }
        for (int idx = 0; idx < node.size(); ++idx) {
            final XNode child = node.getChild(idx);
            toXmlString(child, buf, tab + "\t");
        }
        if (name.equals(C.ELEM_COMMENT)) {
            buf.append(" -->" + crlf);
        }
        else {
            if (node.size() > 0) {
                buf.append(tab);
            }
            buf.append("</" + node.getName() + ">" + crlf);
        }
    }

    /**
     * @param srcIn
     * @return
     */
    public static String escapeXml(final String srcIn, final boolean quot) {
        String src = srcIn;
        if (null != src) {
            src = src.replaceAll("&", "&amp;");
            // src = src.replaceAll("'", "&apos;");
            src = src.replaceAll(">", "&gt;");
            src = src.replaceAll("<", "&lt;");
            if (quot) {
                src = src.replaceAll("\"", "&quot;");
            }
            src = src.replaceAll("&amp;nbsp;", "&nbsp;");
        }
        return src;
    }

    /**
     * Loads the node from xml stream.
     *
     * @param ser
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static XNode fromXml(final byte[] ser) throws IOException, SAXException, ParserConfigurationException {

        final DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        // Configure it to coalesce CDATA nodes
        df.setCoalescing(true);
        final DocumentBuilder db = df.newDocumentBuilder();
        final ByteArrayInputStream in = new ByteArrayInputStream(ser);
        final Document d = db.parse(in);
        final Element eroot = d.getDocumentElement();
        final XNode root = copyNode(eroot);
        return root;
    }

    /**
     * Creates a node out of a dom node.
     *
     * @param node
     * @return
     */
    private static XNode copyNode(final org.w3c.dom.Node node) {

        final String nodeName = node.getNodeName();
        if (nodeName.equals(C.ELEM_TEXT)) {
            return null;
        }
        if (nodeName.equals(C.ELEM_COMMENT)) {
            return null;
        }
        final XNode wnode = new XNode(node.getNodeName());
        String nodeValue = "";
        final NamedNodeMap atts = node.getAttributes();
        if (atts != null) {
            for (int idx = 0; idx < atts.getLength(); ++idx) {
                final org.w3c.dom.Node att = atts.item(idx);
                wnode.setAttribute(att.getNodeName(), att.getNodeValue());
            }
        }
        final NodeList children = node.getChildNodes();
        for (int idx = 0; idx < children.getLength(); ++idx) {
            final org.w3c.dom.Node child = children.item(idx);
            final XNode wchild = copyNode(child);
            if (wchild != null) {
                wnode.addChild(wchild);
            }
            final String childName = child.getNodeName();
            if (childName.equals(C.ELEM_TEXT)) {
                final String tmpValue = child.getNodeValue().trim();
                nodeValue += tmpValue;
            }
        }
        wnode.setValue(nodeValue);
        return wnode;
    }

    /**
     * Gets the node from a file.
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    public static XNode fromXmlFile(final String fileName) throws Exception {
        try {
            FileInputStream in = new FileInputStream(fileName);
            byte[] data = new byte[in.available()];
            in.read(data);
            in.close();
            return fromXml(data);
        }
        catch (Throwable t) {
            throw new Exception("Could not parse file: " + fileName, t);
        }
    }

    /**
     * Removes a child by name.
     *
     * @param string
     */
    public void removeChild(final String string) {
        XNode child = get(string);
        if (child != null) {
            this.subs.remove(child);
        }
    }

    /**
     * Saves the node to an xml file.
     *
     * @param string
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public void toXmlFile(final String fileName) throws UnsupportedEncodingException, IOException {
        String xmlStr = toXmlString();
        FileOutputStream out = new FileOutputStream(fileName);
        out.write(xmlStr.getBytes("UTF-8"));
        out.close();
    }

    public boolean equals(final XNode node) {
        String s1 = toXmlString();
        String s2 = node.toXmlString();

        return s1.equals(s2);

    }

    /**
     * Saves the node to an xml file.
     *
     * @param string
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public void toHtmlFile(final String fileName, final String encoding) throws UnsupportedEncodingException,
            IOException {
        String htmlStr = toHtmlString();
        FileOutputStream out = new FileOutputStream(fileName);
        out.write(htmlStr.getBytes(encoding));
        out.close();
    }

    /**
     * Saves the node to an xml file.
     *
     * @param string
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public String toHtmlString() throws UnsupportedEncodingException, IOException {
        String xmlStr = toXmlString();
        int index = xmlStr.indexOf("?>");
        xmlStr = xmlStr.substring(index + 2).trim();
        xmlStr = "<!DOCTYPE html>" + crlf + xmlStr;
        return xmlStr;
    }

    /**
     * Returns all children with the given name.
     *
     * @param name
     * @return
     */
    public List<XNode> children(final String name) {
        ArrayList<XNode> result = new ArrayList<XNode>();
        for (XNode child : this.subs) {
            if (child.getName().equals(name)) {
                result.add(child);
            }
        }

        return result;
    }

    /**
     * Returns all children with the given name.
     *
     * @param name
     * @return
     */
    public List<XNode> getChildren(final String name) {

        return children(name);
    }

    /**
     * Returns all children with the given name and a given attributevalue.
     *
     * @param name
     * @return
     */
    public List<XNode> childrenByAttribute(final String name, final String att, final String value) {
        ArrayList<XNode> result = new ArrayList<XNode>();
        for (XNode child : this.subs) {
            String childname = child.getName();
            String childatt = child.getAttribute(att);
            if ((childname != null) && (childatt != null)) {
                if (childname.equals(name) && childatt.equalsIgnoreCase(value)) {
                    result.add(child);
                }
            }
        }

        return result;
    }

    /**
     * @param string
     * @param string2
     * @param table
     * @return
     */
    public XNode getNodeByNameAndAttribute(final String name, final String attName, final String attValue) {
        for (XNode child : this.children()) {
            if (name.equals(child.getName()) && child.getAttribute(attName, "---").equals(attValue)) {
                return child;
            }
            final XNode found = child.getNodeByNameAndAttribute(name, attName, attValue);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Returns the parent.
     *
     * @return
     */
    public XNode getParent() {
        return this.parent;
    }

    /**
     * @param string
     * @param string2
     * @return
     */
    public String getChildValue(final String key, final String defaultValue) {
        XNode child = this.getChild(key);
        if (child == null) {
            return defaultValue;
        }
        String value = child.getValue();
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public String getChildValue(final String key) {
        XNode child = this.getChild(key);
        if (child == null) {
            return null;
        }
        String value = child.getValue();
        if (value == null) {
            return null;
        }
        return value;
    }

    /**
     * @param string
     * @return
     */
    public List<XNode> queryNodesByName(final String string) {
        ArrayList<XNode> result = new ArrayList<XNode>();
        queryNodesByNameRec(this, string, result);
        return result;
    }

    /**
     * @param string
     * @param result
     */
    private static void queryNodesByNameRec(final XNode parent, final String string, final ArrayList<XNode> result) {
        if (parent.getName().equals(string)) {
            result.add(parent);
        }
        for (XNode child : parent.children()) {
            queryNodesByNameRec(child, string, result);
        }
    }

    /**
     * @param filter
     * @return
     */
    public XNode getNodeByFilter(final XNode filter) {
        ArrayList<XNode> result = new ArrayList<XNode>();
        queryNodesByFilter(this, filter, result);
        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    /**
     * @param xNode
     * @param filter
     * @param result
     */
    private static void queryNodesByFilter(final XNode parent, final XNode filter, final ArrayList<XNode> result) {
        boolean found = true;
        for (XNode check : filter.children()) {
            if (!found) {
                break;
            }
            if (check.getName().equals("name.value")) {
                String name = check.getAttribute("name");
                String value = check.getAttribute("value");
                String value2 = parent.getChildValue(name, "---");
                if (!value.equals(value2)) {
                    found = false;
                }
            }
        }
        if (found) {
            result.add(parent);
        }
        for (XNode child : parent.children()) {
            queryNodesByFilter(child, filter, result);
        }
    }

    /**
     * @param xNode
     * @param index
     * @return
     */
    public XNode insertChild(final int index, final XNode xNode) {
        this.children().add(index, xNode);
        xNode.setParent(this);
        return xNode;
    }

    /**
     * @param xNode
     */
    private void setParent(final XNode xNode) {
        this.parent = xNode;
    }

    /**
     *
     */
    public void removeChildren() {
        for (XNode child : this.children()) {
            child.setParent(null);
        }
        this.subs.clear();
    }

    /**
     *
     */
    public void removeAttributes() {
        this.atts.clear();
    }

    /**
     * @param string
     */
    public void removeChildren(final String name) {
        List<XNode> res = this.children(name);
        for (XNode toDel : res) {
            toDel.detach();
        }
    }

    /**
     *
     */
    public void detach() {
        if (this.parent != null) {
            this.parent.remove(this);
        }
        this.parent = null;
    }

    /**
     * @param string
     */
    public void removeAttributeRecursive(final String key) {
        removeAttribute(key);
        for (XNode child : this.children()) {
            child.removeAttributeRecursive(key);
        }
    }

    /**
     * @param key
     * @param value
     */
    public void setAttributeRecursive(final String key, final String value) {
        setAttribute(key, value);
        for (XNode child : this.children()) {
            child.setAttributeRecursive(key, value);
        }
    }

    /**
     * @param string
     */
    public void removeChildrenRecursive(final String string) {
        List<XNode> tbr = this.children(string);
        for (XNode child : tbr) {
            child.detach();
        }
        for (XNode child : this.children()) {
            child.removeChildrenRecursive(string);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public boolean isEmpty() {
        return this.subs.isEmpty();
    }

    /**
     * {@inheritDoc}
     *
     * @param o
     * @return
     */
    @Override
    public boolean contains(final Object o) {
        return this.subs.contains(o);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Iterator<XNode> iterator() {
        return this.subs.iterator();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Object[] toArray() {
        return this.subs.toArray();
    }

    /**
     * {@inheritDoc}
     *
     * @param a
     * @return
     */
    @Override
    public <T> T[] toArray(final T[] a) {
        return this.subs.toArray(a);
    }

    /**
     * {@inheritDoc}
     *
     * @param e
     * @return
     */
    @Override
    public boolean add(final XNode e) {
        e.setParent(this);
        return this.subs.add(e);
    }

    /**
     * {@inheritDoc}
     *
     * @param o
     * @return
     */
    @Override
    public boolean remove(final Object o) {
        XNode sub = (XNode) o;
        sub.parent = null;
        return this.subs.remove(o);
    }

    /**
     * {@inheritDoc}
     *
     * @param c
     * @return
     */
    @Override
    public boolean containsAll(final Collection<?> c) {
        return this.subs.containsAll(c);
    }

    /**
     * {@inheritDoc}
     *
     * @param c
     * @return
     */
    @Override
    public boolean addAll(final Collection<? extends XNode> c) {
        boolean success = this.subs.addAll(c);
        for (XNode child : this.subs) {
            child.setParent(this);
        }
        return success;
    }

    /**
     * {@inheritDoc}
     *
     * @param index
     * @param c
     * @return
     */
    @Override
    public boolean addAll(final int index, final Collection<? extends XNode> c) {
        boolean success = this.subs.addAll(index, c);
        for (XNode child : this.subs) {
            child.setParent(this);
        }
        return success;
    }

    /**
     * {@inheritDoc}
     *
     * @param c
     * @return
     */
    @Override
    public boolean removeAll(final Collection<?> c) {
        this.removeChildren();
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @param c
     * @return
     */
    @Override
    public boolean retainAll(final Collection<?> c) {
        return this.subs.retainAll(c);
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public void clear() {
        this.removeChildren();
    }

    /**
     * {@inheritDoc}
     *
     * @param index
     * @param element
     * @return
     */
    @Override
    public XNode set(final int index, final XNode element) {
        element.parent = this;
        return this.subs.set(index, element);
    }

    /**
     * {@inheritDoc}
     *
     * @param index
     * @param element
     */
    @Override
    public void add(final int index, final XNode element) {
        this.subs.add(index, element);
        element.parent = this;
    }

    /**
     * {@inheritDoc}
     *
     * @param o
     * @return
     */
    @Override
    public int indexOf(final Object o) {
        return this.subs.indexOf(this);
    }

    /**
     * {@inheritDoc}
     *
     * @param o
     * @return
     */
    @Override
    public int lastIndexOf(final Object o) {
        return this.subs.lastIndexOf(o);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public ListIterator<XNode> listIterator() {
        return this.subs.listIterator();
    }

    /**
     * {@inheritDoc}
     *
     * @param index
     * @return
     */
    @Override
    public ListIterator<XNode> listIterator(final int index) {
        return this.subs.listIterator(index);
    }

    /**
     * {@inheritDoc}
     *
     * @param fromIndex
     * @param toIndex
     * @return
     */
    @Override
    public List<XNode> subList(final int fromIndex, final int toIndex) {
        return this.subs.subList(fromIndex, toIndex);
    }

    /**
     * {@inheritDoc}
     *
     * @param index
     * @return
     */
    @Override
    public XNode remove(final int index) {
        XNode node = this.subs.get(index);
        node.detach();
        return node;
    }

    /**
     * Sorts the children of the node by key.
     *
     * @param node
     * @param key
     */
    public void sortByAttribute(final String key) {

        for (int i = 0; i < size(); ++i) {
            for (int j = 1; j < (size() - i); ++j) {
                XNode node1 = this.getChild(j - 1);
                XNode node2 = this.getChild(j);
                String s1 = node1.getAttribute(key, C._undefined);
                String s2 = node2.getAttribute(key, C._undefined);
                if (s1.compareTo(s2) > 0) {
                    set(j - 1, node2);
                    set(j, node1);
                }
            }
        }
    }

    /**
     * @param dataNode
     */
    public XNode replaceChild(final XNode dataNode) {
        this.removeChildren(dataNode.getName());
        this.addChild(dataNode);
        return dataNode;
    }

    public List<XNode> queryNodesByAttribute(final String attributeName) {
        ArrayList<XNode> result = new ArrayList<XNode>();
        queryNodesByAttributeRec(attributeName, result);
        return result;
    }

    /**
     * @param xNode
     * @param result
     */
    private void queryNodesByAttributeRec(final String attributeName, final ArrayList<XNode> result) {
        if (this.getAttribute(attributeName) != null) {
            result.add(this);
        }
        for (XNode child : this.children()) {
            child.queryNodesByAttributeRec(attributeName, result);
        }

    }

}
