/*
 * createGallery.java
 * 
 * Copyright (c) 2016 Ewald Caspari, Neuwied, All rights reserved.
 */

package galerie;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import xnode.XNode;

import com.sun.xml.internal.fastinfoset.stax.events.Util;

/**
 * Erzeugt eine HTML-Bildergalerie (CSS) aus den In einem Verzeichnis vorliegenden Bildern.
 *
 * @author E.Caspari
 * @since 17.07.2016 12:44:52
 */
public class createGallery implements C {

    /**
     *
     */
    private static final String VAR_DEFINITION = "$$=";
    private static final String INDEXPATH = "INDEXPATH";
    private static final String RELDESTPATH = "RELDESTPATH";
    private static final String IMGPATH = "IMGPATH";
    private static final String SEPARATOR_WIN = "\\";
    private static final String SEPARATOR_UNIX = "/";
    private static final String SEPWIN = java.util.regex.Matcher.quoteReplacement(SEPARATOR_WIN);
    private static final String SEPUNX = java.util.regex.Matcher.quoteReplacement(SEPARATOR_UNIX);
    private static final String PARENTPATH = "..";
    private static final String CURRENTPATH = ".";

    public static final String DEFAULT_INDEX = "gallery.html";
    private static final String HTMLnewLine = "<br />";
    private static boolean debug = false;
    private static boolean info = false;

    private String dest_foldername = null;
    private final StringBuffer htmlpage = new StringBuffer();
    private XNode defaults = null;
    private String prefix_thumb = null;
    private XNode indexFile = null;

    // jetzt kommen die Variablen
    private final static String VWG_GALLERYELEM = "$$galleryelements$$";
    private final static String VAR_THUMB = "$$thumb$$";
    private final static String VAR_PIC = "$$pic$$";
    private final static String VAR_PICNAME = "$$picNameOnly$$";
    private final static String VAR_DIR = "$$dirname$$";
    private final static String VAR_DIR_CAPITALIZED = "$$dirLesbar$$";
    private final static String VAR_VID = "$$video$$";
    private final static String VAR_VIDNAME = "$$videoNameOnly$$";
    private final static String VAR_DESCRIPTION = "$$desc$$";

    private static String var_dir_value = "";
    private Map<String, String> vars = null;
    private Map<String, String> pathnames = null;

    /**
    *
    */
    public createGallery() {}

    public void debugOut(final String s) {
        if (debug) {
            System.out.println(s);
        }
    }

    public static void infoOut(final String s) {
        if (info) {
            System.out.println(s);
        }
    }

    public static void errorOut(final String s) {
        System.out.println(s);
    }

    /**
     * @throws Exception
     *
     */
    public void create(final String folder, final String initfile) throws Exception {
        debugOut("create gallery for folder " + folder);
        this.dest_foldername = folder;
        this.defaults = XNode.fromXmlFile(initfile);

        this.indexFile = this.defaults.getChild(INDEXFILE);
        this.pathnames = getPathnames(this.dest_foldername, this.indexFile);
        // String indexfilename = getIndexFilename(this.indexFile);
        this.vars = getVars(this.defaults.getChild(VARIABLES));
        Map<String, String> descriptions = getDescriptions(this.defaults.getChild(CONTENT).getAttribute(ATT_NAME),
                this.defaults.getChild(CONTENT).getAttribute(ATT_SEPARATOR));

        XNode prefixThumbnails = this.defaults.getChild(PREFIX_THUMBNAILS);
        this.prefix_thumb = prefixThumbnails != null ? prefixThumbnails.getAttribute(ATT_NAME) : "thumb_";

        ArrayList<String> bilder = getPics(folder);
        ArrayList<String> videos = getVideos(folder);

        addHeadElem(this.htmlpage);
        this.htmlpage.append(System.lineSeparator());

        addContent(this.htmlpage, bilder, videos, descriptions);
        this.htmlpage.append(System.lineSeparator());

        addFootElem(this.htmlpage);
        this.htmlpage.append(System.lineSeparator());
    }

    /**
     * @param separator
     * @param string
     * @return
     * @throws UnsupportedEncodingException
     */
    private Map<String, String> getDescriptions(final String contentfile, String separator)
            throws UnsupportedEncodingException {
        if (Util.isEmptyString(separator)) {
            separator = " ";
        }
        Map<String, String> desc = new HashMap<String, String>();

        String s = "";
        try {
            String path = createPathnameFromRelName(this.pathnames.get(IMGPATH), contentfile);
            s = FileUtil.readFromFile(path).toString();
        }
        catch (Exception e1) {
            // nicht lesbar -> raus
            return desc;
        }
        s = new String(s.getBytes(), "UTF-8");
        String[] lines = s.split(System.lineSeparator());
        for (String line : lines) {
            String n = null;
            String d = "";
            if (line.startsWith("$$")) {
                String key = line.split("=")[0];
                String value = line.substring(key.length() + 1);
                debugOut("setze Variable " + key + SEPARATOR_UNIX + value);
                this.vars.put(key, value);
            }
            else {
                try {
                    String[] teile = line.split(separator);
                    n = teile[0];
                    if (separator.equals(" ")) {
                        d = line.substring(n.length());
                        if (d.charAt(0) == ' ') {
                            d = d.substring(1);
                        }
                    }
                    else {
                        if (teile.length > 1) {
                            for (int i = 1; i < teile.length; i++) {
                                d = d + teile[i];
                                if (i < (teile.length - 1)) {
                                    d = d + HTMLnewLine;
                                }
                            }
                        }
                    }
                    desc.put(n.toLowerCase(), d);
                }
                catch (Exception e) {
                    // quick and dirty: ignore
                }
            }
        }

        return desc;
    }

    /**
     * @param htmlpage2
     * @param bilder
     * @param videos
     * @param descriptions
     * @throws Exception
     */
    private void addContent(final StringBuffer page, final ArrayList<String> bilder, final ArrayList<String> videos,
            final Map<String, String> descriptions) throws Exception {
        debugOut("addContent started");

        XNode def = this.defaults.getChild(COLUMNDEF);
        if (def != null) {
            createColumnStyle(page, bilder, videos, descriptions, def);
        }
        else {
            def = this.defaults.getChild(ROWDEF);
            createRowStyle(page, bilder, videos, descriptions, def);
        }
        page.append(System.lineSeparator());

        // addVideoElem(page, videos);
        // addPictureElem(page, bilder);
    }

    /**
     * @param page
     * @param bilder
     * @param videos
     * @param descriptions
     * @param coldef
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private void createColumnStyle(final StringBuffer page, final ArrayList<String> bilder,
            final ArrayList<String> videos, final Map<String, String> descriptions, final XNode coldef)
            throws IOException, SAXException, ParserConfigurationException {
        int noCols = coldef.getAttributeAsInt(ATT_NUMBER);

        StringBuffer[] cols = new StringBuffer[noCols]; // einen eigenen StringBuffer für jede Spalte
        ArrayList<String> elements = createVidsList(null, videos, descriptions);
        elements = createPicsList(elements, bilder, descriptions);
        // Jetzt sind die HTML-Bilddefinitionen zusammengefasst.

        // die einzelnen Videos/Bilder auf die Spalten verteilen
        for (int i = 0; i < cols.length; i++) {
            cols[i] = new StringBuffer();
        }
        int i = 0;
        for (Iterator<String> iterator = elements.iterator(); iterator.hasNext();) {
            String s = iterator.next();
            cols[i].append(s);
            i++;
            i = i % noCols;
        }
        // jetzt sind die Bilder zeilenweise sortiert

        String template = coldef.getValue(); // codef ist ein CDATA
        for (int j = 0; j < cols.length; j++) {
            page.append(System.lineSeparator());
            String elem = template.replace(VWG_GALLERYELEM, cols[j]);
            page.append(elem);
        }
    }

    /**
     * @param page
     * @param bilder
     * @param videos
     * @param descriptions
     * @param rowdef
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private void createRowStyle(final StringBuffer page, final ArrayList<String> bilder,
            final ArrayList<String> videos, final Map<String, String> descriptions, final XNode rowdef)
            throws IOException, SAXException, ParserConfigurationException {
        String rowTemplate = rowdef.getValue(); // rowdef ist ein CDATA
        int noCols = rowdef.getAttributeAsInt(ATT_COLNUM);

        StringBuffer lines = new StringBuffer();
        ArrayList<String> elements = createVidsList(null, videos, descriptions);
        elements = createPicsList(elements, bilder, descriptions);
        // Jetzt sind die HTML-Bilddefinitionen zusammengefasst.

        // die einzelnen Videos/Bilder auf die Zeilen verteilen
        StringBuffer cols = new StringBuffer();
        int i = 0;
        for (Iterator<String> iterator = elements.iterator(); iterator.hasNext();) {
            String s = iterator.next();
            cols.append(s);
            i++;
            if ((i % noCols) == 0) {

                String oneLine = rowTemplate.replace(VWG_GALLERYELEM, cols);
                lines.append(oneLine);
                cols = new StringBuffer();
            }
        }
        if (cols.length() != 0) {
            String oneLine = rowTemplate.replace(VWG_GALLERYELEM, cols);
            lines.append(oneLine);
        }
        // jetzt sind die Bilder zeilenweise sortiert
        page.append(lines);

    }

    /**
     * @param child
     * @return
     */
    private Map<String, String> getVars(final XNode variables) {
        Map<String, String> varMap2 = new HashMap<String, String>();
        try {
            Map<String, String> varMap = new HashMap<String, String>();
            List<XNode> varNodes = variables.getChildren(VARIABLE);
            for (Iterator<XNode> var = varNodes.iterator(); var.hasNext();) {
                XNode node = var.next();
                varMap.put(node.getAttribute(ATT_VARNAME), node.getAttribute(ATT_VALUE));
            }
            // jetzt in den Variablen evtl. genutzte Variablen ersetzen. Nur einmal, damit nicht durch schlechte
            // Konfiguration ein Loop entsteht.
            // zuerst fest definierte Variablen einfügen, damit sie ggfs. durch eine Definition überschrieben werden
            // können

            String dir = var_dir_value;
            debugOut("setze Variable " + VAR_DIR + SEPARATOR_UNIX + dir);
            varMap2.put(VAR_DIR, dir);
            if (var_dir_value.length() > 1) {
                dir = Character.toString(var_dir_value.charAt(0)).toUpperCase()
                        + var_dir_value.substring(1).toLowerCase();
            }
            debugOut("setze Variable " + VAR_DIR_CAPITALIZED + SEPARATOR_UNIX + dir);
            varMap2.put(VAR_DIR_CAPITALIZED, dir);

            for (Map.Entry<String, String> entry : varMap.entrySet()) {
                String key = entry.getKey();
                String value = replaceVars(entry.getValue(), varMap, key);
                debugOut("setze Variable " + key + SEPARATOR_UNIX + value);
                varMap2.put(key, value);
            }
        }
        catch (Exception e) {
            errorOut("Fehler bei Lesen der Variablen: " + e.getLocalizedMessage());
        }

        return varMap2;
    }

    /**
     * @param htmlpage2
     */
    private void addFootElem(final StringBuffer page) {
        debugOut("addFootElem started");
        String end = this.defaults.getChildValue("footer", "</body></html>");
        page.append(end);
    }

    /**
     * @param folder
     * @return
     */
    private ArrayList<String> getPics(final String folder) {
        debugOut("getPics started");
        ArrayList<String> pics = new ArrayList<String>();

        String[] dircontent = FileUtil.getFileList(folder);
        for (String s : dircontent) {
            if (s.toLowerCase().endsWith(".jpg") || s.toLowerCase().endsWith(".png")
                    || s.toLowerCase().endsWith(".gif") || s.toLowerCase().endsWith(".bmp")) {
                pics.add(s);
            }
        }
        return pics;
    }

    /**
     * @param folder
     * @return
     */
    private ArrayList<String> getVideos(final String folder) {
        debugOut("getVideos started");
        ArrayList<String> vids = new ArrayList<String>();

        String[] dircontent = FileUtil.getFileList(folder);
        for (String s : dircontent) {
            if (s.toLowerCase().endsWith(".mp4") || s.toLowerCase().endsWith(".ogg")) {
                vids.add(s);
            }
        }
        return vids;
    }

    public void addHeadElem(final StringBuffer page) throws IOException, SAXException, ParserConfigurationException {
        debugOut("addHeadElem started");
        // hier den eigentlichen Inhalt einfügen;
        String start = this.defaults.getChildValue("header", "<html><body>keine korrekte Definition im Initfile");
        page.append(start);
        return;
    }

    @Deprecated
    public void addPictureElem(final StringBuffer page, final ArrayList<String> bilder) throws Exception {
        debugOut("addPictureElem started");
        page.append(System.lineSeparator());
        ArrayList<String> bildzeilen = createPicsList(null, bilder, null);
        for (Iterator<String> iterator = bildzeilen.iterator(); iterator.hasNext();) {
            String s = iterator.next();
            page.append(s);
        }
        return;
    }

    @Deprecated
    public void addVideoElem(final StringBuffer page, final ArrayList<String> videos) throws Exception {
        debugOut("addVideoElem started");
        page.append(System.lineSeparator());
        ArrayList<String> vidzeilen = createVidsList(null, videos, null);
        for (Iterator<String> iterator = vidzeilen.iterator(); iterator.hasNext();) {
            String s = iterator.next();
            page.append(s);
        }
        return;
    }

    /**
     * @param descriptions
     * @param bilder
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private ArrayList<String> createPicsList(final ArrayList<String> col, final ArrayList<String> bildnamen,
            final Map<String, String> descriptions) throws IOException, SAXException, ParserConfigurationException {
        debugOut("createPicsList started");
        String template = this.defaults.getChild(GALLERYDEF).getChild(PICDEF).getValue();

        ArrayList<String> column = col;
        if (column == null) {
            column = new ArrayList<String>();
        }
        for (Iterator<String> iterator = bildnamen.iterator(); iterator.hasNext();) {
            String s = iterator.next();
            if (s.startsWith(this.prefix_thumb)) {
                continue;
            }
            String desc = "";
            if (descriptions != null) {
                desc = descriptions.containsKey(s.toLowerCase()) ? descriptions.get(s.toLowerCase()) : "";
            }
            column.add(createPicNode(s, template, desc));
        }

        return column;
    }

    private String createPicNode(final String bildname, final String template, final String desc) throws IOException,
            SAXException, ParserConfigurationException {
        debugOut("createPicNode started");

        Map<String, String> picVars = setupLocalVarMap(desc);
        String indexpath = this.pathnames.get(INDEXPATH);
        String imagepath = this.pathnames.get(IMGPATH);
        String relpath = CURRENTPATH + SEPUNX + imagepath.substring(indexpath.length());
        String name = bildname.substring(0, bildname.lastIndexOf('.'));
        picVars.put(VAR_PIC, relpath + bildname);
        picVars.put(VAR_PICNAME, name);
        picVars.put(VAR_THUMB, relpath + this.prefix_thumb + bildname);

        String elem = replaceVars(template, picVars, null);
        return elem + System.lineSeparator();
    }

    /**
     * @param descriptions
     * @param bilder
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private ArrayList<String> createVidsList(final ArrayList<String> col, final ArrayList<String> videoNamen,
            final Map<String, String> descriptions) throws IOException, SAXException, ParserConfigurationException {
        debugOut("createVidsList started");
        ArrayList<String> column = col;
        if (column == null) {
            column = new ArrayList<String>();
        }
        String template = this.defaults.getChild(GALLERYDEF).getChild(VIDDEF).getValue();
        for (Iterator<String> iterator = videoNamen.iterator(); iterator.hasNext();) {
            String s = iterator.next();
            String desc = "";
            if (descriptions != null) {
                desc = descriptions.containsKey(s.toLowerCase()) ? descriptions.get(s.toLowerCase()) : "";
            }
            column.add(createVideoNode(s, template, desc));
        }

        return column;
    }

    // alt:
    // private String createVideoNode(final String videoName, final String template, final String desc)
    // throws IOException, SAXException, ParserConfigurationException {
    // debugOut("createVideoNode started");
    // String vid = java.util.regex.Matcher.quoteReplacement(VAR_VID);
    // String elem = template.replaceAll(vid, this.pathnames.get(RELDESTPATH) + videoName);
    // return elem + System.lineSeparator();
    // }

    private String createVideoNode(final String videoname, final String template, final String desc)
            throws IOException, SAXException, ParserConfigurationException {
        debugOut("createvideoNode started");

        Map<String, String> videoVars = setupLocalVarMap(desc);
        String name = videoname.substring(0, videoname.lastIndexOf('.'));
        videoVars.put(VAR_VIDNAME, name);

        String relpath = this.pathnames.get(RELDESTPATH);
        videoVars.put(VAR_VID, relpath + videoname);
        videoVars.put(VAR_THUMB, relpath + this.prefix_thumb + videoname);

        String elem = replaceVars(template, videoVars, null);
        return elem + System.lineSeparator();
    }

    /**
     * @param desc
     * @param videoVars
     */
    private Map<String, String> setupLocalVarMap(final String desc) {
        Map<String, String> vars = new HashMap<String, String>();
        String description = "";
        String[] desclines = desc.split(HTMLnewLine);
        for (int i = 0; i < desclines.length; i++) {
            if (desclines[i].contains(VAR_DEFINITION)) {
                String[] var = desclines[i].split("=");
                // TODO wenn noch weitere Gleichheitszeichen drin sind, dann wird der Text dahinter verschluckt
                vars.put(var[0], var[1]);
            }
            else {
                if (!Util.isEmptyString(description)) {
                    description = description + HTMLnewLine;
                }
                description = description + desclines[i];
            }
        }
        vars.put(VAR_DESCRIPTION, description);
        return vars;
    }

    /**
     * @throws IOException
     * @throws FileNotFoundException
     *
     */
    private void copyAdditionalFiles(final String initfolder) throws FileNotFoundException, IOException {
        XNode resources = this.defaults.getChild(RESOURCES);
        if (resources != null) {
            List<XNode> resourceList = resources.children();
            for (XNode child : resourceList) {
                String from = initfolder + child.getAttribute(ATT_FROM);
                String to = this.dest_foldername + child.getAttribute(ATT_TO);
                if ("copy".equals(child.getName())) {
                    FileUtil.copyFile(from, to, true);
                }
                else if (COPYFOLDER.equals(child.getName())) {
                    String withSub = child.getAttribute(ATT_WITHSUBFOLDER);
                    FileUtil.copyFolder(from, to, Boolean.parseBoolean(withSub), true);
                }
            }
        }

    }

    /**
     * @throws IOException
     * @throws UnsupportedEncodingException
     *
     */
    void writeHtml() throws UnsupportedEncodingException, IOException {
        debugOut("writeHtml started");
        String fileName = getIndexFilename(this.indexFile);
        debugOut("write to " + fileName);
        String page = this.htmlpage.toString();
        page = replaceVars(page, this.vars, null);
        FileUtil.writeToFile(fileName, new StringBuffer(page), FileUtil.UTF8);

    }

    /**
     * @param dest_foldername2
     * @param indexFile2
     * @return
     */
    private String getIndexFilename(final XNode indexfile) {
        String filename = indexfile != null ? indexfile.getAttribute(ATT_NAME) : DEFAULT_INDEX;
        filename = replaceVars(filename, this.vars, null);
        return this.pathnames.get(INDEXPATH) + filename;
    }

    /**
     * ermittelt die zu benutzenden Pfadnamen
     *
     * @param fullDestPath
     * @param indexfile
     * @return
     */
    private Map<String, String> getPathnames(final String fullDestPath, final XNode indexfile) {
        Map<String, String> pathnames = new HashMap();

        pathnames.put(IMGPATH, fullDestPath.replaceAll(SEPWIN, SEPUNX)); // imagepath im UNixstil
        String indexpath = indexfile.getAttribute(ATT_PATH);
        if (indexpath != null) {
            if (indexpath.startsWith(PARENTPATH)) {
                String splitstr = indexpath.contains(SEPARATOR_UNIX) ? SEPUNX : SEPWIN;
                String[] indexpathelements = indexpath.split(splitstr);
                pathnames.put(RELDESTPATH, getRelBasePath(fullDestPath, indexpathelements));
                ;
                pathnames.put(INDEXPATH, createPathnameFromRelName(fullDestPath, indexpath) + SEPUNX);

                if (pathnames.get(INDEXPATH).contains("..")) {
                    // dann enthält es einen relativen Pfadnamen zum übergeordneten Verzeichnis
                    errorOut("indexPathname ist ungültig:" + pathnames.get(INDEXPATH));
                }
            }
            else {
                // wenn eine absolute Pfadangabe war, dann muss ich den vollen Pfad mitgeben.
                String fullIndexPath = new File(indexpath).getPath();
                pathnames.put(INDEXPATH, fullIndexPath);
                pathnames.put(RELDESTPATH, fullDestPath);
            }
        }
        else {
            // wenn kein Pfad angegeben war
            pathnames.put(INDEXPATH, fullDestPath);
            pathnames.put(RELDESTPATH, "." + SEPARATOR_UNIX);
        }
        return pathnames;
    }

    private String createPathnameFromRelName(final String basepath, final String relpath) {
        String path = "";

        String splitstr = relpath.contains(SEPARATOR_UNIX) ? SEPUNX : SEPWIN;
        String[] relpathelements = relpath.split(splitstr);

        path = getRelBasePath(basepath, relpathelements);
        path = path.substring(0, path.length() - 1); // das letzte / entfernen
        // jetzt ist der BasePath für das relative Element ok.

        for (String pathelem : relpathelements) {
            if (!pathelem.startsWith(PARENTPATH) && !pathelem.startsWith(CURRENTPATH)) {
                path = path + SEPARATOR_UNIX + pathelem;
            }
        }

        return path;
    }

    /**
     * @param basepath
     * @param path
     * @param relpathelements
     * @return
     */
    private String getRelBasePath(final String basepath, final String[] relpathelements) {
        String path = "";
        String splitstr2 = basepath.contains(SEPARATOR_UNIX) ? SEPUNX : SEPWIN;
        String[] basepathelements = basepath.split(splitstr2);
        // jetzt muss ich den Pfad zusammensetzen.
        // für jedes ..\ im indexpath muss ich im destpath eins rauf gehen
        int depth = basepathelements.length;
        for (String pathelem : relpathelements) {
            if (pathelem.startsWith(PARENTPATH)) {
                depth--;
                continue;
            }
            break; // der erste nicht '..' bricht ab
        }
        for (int i = 0; i < depth; i++) {
            path = path + basepathelements[i] + SEPARATOR_UNIX;
        }
        return path;
    }

    /**
     * ersetzt alle Vorkommnisse der definierten Variablen mit den entsprechenden Werten.
     * nicht sonderlich schnell, aber für unseren Gebrauch ausreichend.
     *
     * @param text
     * @param doNotReplace
     * @return
     */
    private String replaceVars(final String text, final Map<String, String> vars, final String doNotReplace) {
        String rc = text;
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            String key = entry.getKey();
            if ((doNotReplace != null) && key.equals(doNotReplace)) {
                continue;
            }
            key = java.util.regex.Matcher.quoteReplacement(key);
            String value = entry.getValue();
            rc = rc.replaceAll(key, value);
        }
        return rc;
    }

    /**
     * @param args
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public static void main(final String[] args) throws Exception {
        String dest_foldername = "";
        String initFile = "";
        String initfolder = "";
        for (String arg : args) {
            if (arg.toLowerCase().startsWith("i=")) {
                initFile = arg.substring(2);
                initfolder = new File(initFile).getParent() + File.separator;
            }
            else if (arg.toLowerCase().startsWith("debug")) {
                debug = true;
            }
            else if (arg.toLowerCase().startsWith("info")) {
                info = true;
            }
            else { // wenn kein Parameter angegeben ist, muss es der Verzeichnisname sein
                dest_foldername = new File(arg).getPath() + File.separator;
                var_dir_value = FileUtil.getDirName(dest_foldername);
            }
        }

        createGallery gallery = new createGallery();
        gallery.create(dest_foldername, initFile);
        gallery.writeHtml();
        gallery.copyAdditionalFiles(initfolder);

    }
}
