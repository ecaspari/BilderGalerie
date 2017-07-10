/*
 * createGallery.java
 *
 * Copyright (c) 2016 Ewald Caspari, Neuwied, All rights reserved.
 */

package galerie;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import xnode.XNode;

/**
 * Erzeugt eine HTML-Bildergalerie (CSS) aus den In einem Verzeichnis vorliegenden Bildern.
 *
 * @author E.Caspari
 * @since 17.07.2016 12:44:52
 */
public class createGallery2 implements C {

    private static boolean debug = false;
    private static boolean info = false;
    private static String foldername = null;
    private static String initFile = null;
    private final XNode htmlpage = new XNode("html");
    private XNode defaults = null;

    /**
    *
    */
    public createGallery2() {}

    public void debugOut(final String s) {
        if (debug) {
            System.out.println(s);
        }
    }

    public void infoOut(final String s) {
        if (info) {
            System.out.println(s);
        }
    }

    public void errorOut(final String s) {
        System.out.println(s);
    }

    /**
     * @throws Exception
     *
     */
    public void create(final String folder) throws Exception {
        debugOut("create gallery for folder " + folder);
        this.defaults = initFile == null ? XNode.fromXml(defaultvalues.getBytes()) : XNode.fromXmlFile(initFile);
        this.defaults.toXmlFile("C:\\usr\\eclipse\\workspace\\BilderGalerie\\test.xml");
        ArrayList bilder = getPics(folder);
        addHeadElem(this.htmlpage);
        addBodyElem(this.htmlpage, bilder);
        // addFootElem(this.htmlpage);
    }

    /**
     * @param folder
     * @return
     */
    private ArrayList getPics(final String folder) {
        debugOut("getPics started");
        ArrayList pics = new ArrayList();

        String[] dircontent = FileUtil.getFileList(folder);
        for (String s : dircontent) {
            if (s.toLowerCase().endsWith(".jpg") || s.toLowerCase().endsWith(".png")
                    || s.toLowerCase().endsWith(".gif") || s.toLowerCase().endsWith(".bmp")) {
                pics.add(s);
            }
        }
        return pics;
    }

    public XNode addHeadElem(final XNode page) throws IOException, SAXException, ParserConfigurationException {
        debugOut("addHeadElem started");
        XNode comment = new XNode(xnode.C.ELEM_COMMENT);
        StringBuffer cmnt = new StringBuffer();
        for (String c : copyright) {
            cmnt.append(c);
            cmnt.append("\n");
        }
        comment.setValue(cmnt.toString());
        page.add(comment);
        XNode e = new XNode("head");

        // hier den eigentlichen Inhalt einfügen;

        for (String m : meta) {
            e.add(XNode.fromXml(m.getBytes()));
        }

        XNode style = new XNode("style");
        StringBuffer sb = new StringBuffer();
        for (String s : css) {
            sb.append(s);
            sb.append("\n");
        }
        style.setValue(sb.toString());
        e.add(style);

        page.add(e);
        return page;
    }

    public XNode addBodyElem(final XNode page, final ArrayList bilder) throws Exception {
        debugOut("addBodyElem started");
        XNode e = new XNode("body");

        e.add(addheader());
        e.addAll(createPicsList(bilder));
        e.add(addfooter());
        page.add(e);
        return page;
    }

    /**
     * @param bilder
     * @return
     */
    private ArrayList createPicsList(final ArrayList bildnamen) {
        debugOut("createPicsList started");
        String template = this.defaults.getChild("picdef").getValue();
        ArrayList bilder = new ArrayList();
        for (Iterator iterator = bildnamen.iterator(); iterator.hasNext();) {
            String s = (String) iterator.next();
            bilder.add(createPicNode(s, template));
        }

        return bilder;
    }

    private XNode createPicNode(final String bildname, final String template) {
        debugOut("createPicNode started");
        XNode pic = new XNode("div");
        pic.setAttribute("class", "responsive");
        XNode e = new XNode("div");
        e.setAttribute("class", "img");
        pic.add(e);
        XNode a = new XNode("a");
        a.setAttribute("target", "_blank");
        a.setAttribute("href", bildname);
        XNode img = new XNode("img");
        // img.setAttribute("alt", "imagedesc");
        img.setAttribute("src", bildname);
        XNode picdefs = this.defaults.getChild("pictures");
        Map<String, String> atts = picdefs.getAttributes();
        for (Map.Entry<String, String> entry : atts.entrySet()) {
            debugOut(entry.getKey() + "/" + entry.getValue());
            img.setAttribute(entry.getKey(), entry.getValue());
        }
        a.add(img);
        e.add(a);
        XNode desc = new XNode("div");
        desc.setAttribute("class", "desc");
        desc.setValue("Beschreibung einfügen");
        e.add(desc);

        return pic;
    }

    /**
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private XNode addheader() throws IOException, SAXException, ParserConfigurationException {
        debugOut("addHeader started");
        String s = this.defaults.getChild("header").getValue();

        return XNode.fromXml(s.getBytes());
    }

    public XNode addfooter() throws IOException, SAXException, ParserConfigurationException {
        debugOut("addFooter started");
        String s = this.defaults.getChild("footer").getValue();

        return XNode.fromXml(s.getBytes());
    }

    /**
     * @throws IOException
     * @throws UnsupportedEncodingException
     *
     */
    private void writeHtml() throws UnsupportedEncodingException, IOException {
        debugOut("writeHtml started");
        String fileName = foldername + "gallery.html";
        String page = this.htmlpage.toHtmlString();
        FileUtil.writeToFile(fileName, new StringBuffer(page), FileUtil.UTF8);

    }

    /**
     * @param args
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public static void main(final String[] args) throws Exception {
        for (String arg : args) {
            if (arg.toLowerCase().startsWith("i=")) {
                initFile = arg.substring(2);
            }
            else if (arg.toLowerCase().startsWith("debug")) {
                debug = true;
            }
            else if (arg.toLowerCase().startsWith("info")) {
                info = true;
            }
            else { // wenn kein PArameter angegeben ist, muss es der Verzeichnisname sein
                foldername = arg;
            }
        }

        createGallery2 gallery = new createGallery2();
        gallery.create(foldername);
        gallery.writeHtml();

    }

}
