/*
 * C.java
 *
 * Copyright (c) 2016 Ewald Caspari, Neuwied, All rights reserved.
 */

package galerie;


/**
 * TODO: DOCUMENT ME!
 *
 * @author E.Caspari
 * @since 17.07.2016 14:55:43
 */
public interface C {

    public static final String[] copyright = { "Design Ewald Caspari, Neuwied",
            "unter Benutzung von W3C StyleSheets (http://www.w3schools.com) " };

    public static String[] meta = { "<title>Bildergalerie</title>", "<meta charset=\"UTF-8\"/>",
            "<meta name=\"description\" content=\"Bildergallerie\"/>",
            "<meta name=\"author\" content=\"Ewald Caspari, Neuwied\"/>",
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>",
            "<link rel=\"stylesheet\" href=\"gallery.css\" />" };

    public static String css[] = { "div.img { border: 1px solid #ccc;}", "div.img:hover { border: 1px solid #777;}",
            "div.img img { width: 100%; height: auto;}", "div.desc { padding: 15px; text-align: center;}",
            "* { box-sizing: border-box;}", ".responsive { padding: 0 6px; float: left; width: 24.99999%;}",
            "@media only screen and (max-width: 700px){ .responsive { width: 49.99999%; margin: 6px 0; }}",
            "@media only screen and (max-width: 500px){ .responsive { width: 100%; }}",
            ".clearfix:after { content: \"\"; display: table; clear: both;}" };

    public static String defaultvalues = "<galerydefinitions>"
            + "   <header>"
            + "      <![CDATA["
            + "         <div style=\"header padding:6px;\">"
            + "            <h2>Responsive Image Gallery</h2>"
            + "            <h4>Resize the browser window to see the effect.</h4>"
            + "         </div>"
            + "     ]]>"
            + "   </header>"
            + "   <pictures width=\"600\" height=\"800\" />"
            + "   <footer>"
            + "      <![CDATA[<div>"
            + "         <div class=\"clearfix\" >.</div>"
            + "      <div style=\"footer padding:6px;\">"
            + "            <p>This example use media queries to re-arrange the images on different screen sizes: for screens larger than 700px wide, it"
            + "               will show four images side by side, for screens smaller than 700px, it will show two images side by side. For screens smaller"
            + "               than 500px, the images will stack vertically (100%).</p>" + "         </div></div>]]>"
            + "   </footer>" + "</galerydefinitions>";

    /**
     * Elementnames for definition file
     */
    public static final String COPYFOLDER = "copyfolder";
    public static final String RESOURCES = "resources";
    public static final String CONTENT = "contentfile";
    public static final String VARIABLES = "variables";
    public static final String VARIABLE = "variable";
    public static final String COLUMNDEF = "columndef";
    public static final String ROWDEF = "rowdef";
    public static final String GALLERYDEF = "galleryelements";
    public static final String PICDEF = "picdef";
    public static final String VIDDEF = "viddef";
    public static final String INDEXFILE = "indexfile";
    public static final String PREFIX_THUMBNAILS = "prefix_thumbnails";
    public static final String ATT_TO = "to";
    public static final String ATT_FROM = "from";
    public static final String ATT_WITHSUBFOLDER = "withsubfolder";
    public static final String ATT_VARNAME = "varname";
    public static final String ATT_NAME = "name";
    public static final String ATT_PATH = "path";
    public static final String ATT_SEPARATOR = "separator";
    public static final String ATT_VALUE = "value";

    public static final String ATT_NUMBER = "number";
    public static final String ATT_COLNUM = "colnum";
}
