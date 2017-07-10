
package galerie;


/**
 * (Constants) COPYRIGHT E.Caspari 2009
 *
 * The source code for this program is not published
 *
 * Created on 09.02.2009 by Ewald Caspari
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Verschiedene Routinen zum Lesen und schreiben von Files, auch Zipkomprimierte
 *
 * @author ewald
 *         created on 03.10.2012 14:59:11
 */
public class FileUtil {

    public static String defaultEncoding = "WINDOWS-1251";
    public static final String UTF8 = "UTF-8";

    /**
     * komprimiert den Stringbuffer mit Zip und schreibt ihn als Bytearray in das angegebene File.<br>
     * Wenn das Files schon existiert, wird es überschrieben.<br>
     *
     * @param filename
     *            String
     * @param sb
     *            StringBuffer
     * @throws IOException
     */
    public static int writeToCompressedFile_old(final String filename, final StringBuffer sb) throws IOException {

        OutputStream out = new DeflaterOutputStream(new FileOutputStream(filename, false));// immer neu schreiben,
                                                                                           // niemals append

        byte[] buffer = sb.toString().getBytes(defaultEncoding);
        out.write(buffer, 0, buffer.length);
        out.close();
        return buffer.length;
    }

    public static void writeToCompressedFile(final String filename, final StringBuffer sb) throws IOException {
        Writer out = new BufferedWriter(new OutputStreamWriter(new DeflaterOutputStream(new FileOutputStream(filename,
                false)), defaultEncoding));
        try {
            out.write(sb.toString());
        }
        finally {
            out.close();
        }

    }

    /**
     *
     * TODO (ec) 28.01.2016 22:39:17 Alle Methoden in FileUtil prüfen
     * seit Java 7 diese Methoden benutzen.
     *
     * @param filename
     * @param sb
     * @throws IOException
     */
    public static void write2file_UTF8(final String filename, final StringBuffer sb) throws IOException {
        List<String> lines = Arrays.asList(sb.toString());

        Path textFile = Paths.get(filename);
        Files.write(textFile, lines, StandardCharsets.UTF_8);

    }

    /**
     * Write to compressed or uncompressed or uncompressed file, depending on flag.
     *
     * @param filename the filename
     * @param sb the sb
     * @param compressed the compressed
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void writeToCompressedOrUncompressed(final String filename, final StringBuffer sb,
            final boolean compressed, final String encoding) throws IOException {
        if (compressed) {
            writeToCompressedFile(filename, sb);
        }
        else {
            writeToFile(filename, sb.toString(), encoding);
        }
    }

    /**
     * liest ein File ein und dekomprimiert die Daten.
     * wenn es ein unkomprimiertes File ist --> Exception
     *
     * @param filename
     *            String
     * @return
     * @throws Exception
     */
    public static StringBuffer readFromCompressedFile(final String filename) throws Exception {
        File file = new File(filename);
        InputStream in = new InflaterInputStream(new FileInputStream(file));

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type. It needs to be an int type.
        // Before converting to an int type, check to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            in.close();
            // File is too large.
            throw new Exception("File " + filename + " zu groß für Buffer");
        }

        StringBuffer sb = new StringBuffer();
        // Create the byte array to hold the data, read the bytes
        byte[] buffer = new byte[(int) length];
        int len;
        while ((len = in.read(buffer)) > 0) {
            sb.append(new String(buffer, 0, len, defaultEncoding));
        }

        // Close the input stream and return bytes
        in.close();

        return sb;
    }

    /**
     * Beispiel aus http://www.exampledepot.com/egs/java.util.zip/CompressFile.html
     */
    public static void makeGZIPFile() {
        try {
            // Create the GZIP output stream
            String outFilename = "outfile.gzip";
            GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(outFilename));

            // Open the input file
            String inFilename = "infilename";
            FileInputStream in = new FileInputStream(inFilename);

            // Transfer bytes from the input file to the GZIP output stream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();

            // Complete the GZIP file
            out.finish();
            out.close();
        }
        catch (IOException e) {}
    }

    /**
     * Beispiel aus http://www.exampledepot.com/egs/java.util.zip/UncompressFile.html
     */
    public static void unmakeGZIPFile() {
        try {
            // Open the compressed file
            String inFilename = "infile.gzip";
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(inFilename));

            // Open the output file
            String outFilename = "outfile";
            OutputStream out = new FileOutputStream(outFilename);

            // Transfer bytes from the compressed file to the output file
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            // Close the file and stream
            in.close();
            out.close();
        }
        catch (IOException e) {}
    }

    // ##################################################################################
    /**
     * Compresses a file with zlib compression.
     */
    public static void compressFile(final File raw, final File compressed) throws IOException {
        InputStream in = new FileInputStream(raw);
        OutputStream out = new DeflaterOutputStream(new FileOutputStream(compressed));
        shovelInToOut(in, out);
        in.close();
        out.close();
    }

    /**
     * Decompresses a zlib compressed file.
     */
    public static void decompressFile(final File compressed, final File raw) throws IOException {
        InputStream in = new InflaterInputStream(new FileInputStream(compressed));
        OutputStream out = new FileOutputStream(raw);
        shovelInToOut(in, out);
        in.close();
        out.close();
    }

    /**
     * Shovels all data from an input stream to an output stream.
     */
    private static void shovelInToOut(final InputStream in, final OutputStream out) throws IOException {
        byte[] buffer = new byte[1000];
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }

    /**
     * sucht einen Filename im Classpath. Wenn nicht gefunden return null,
     *
     * @param name
     * @return
     * @throws IOException
     */
    public static String findFileOnClassPath(final String name) throws IOException {
        String rc = null;

        URL url = FileUtil.class.getClassLoader().getResource(name);
        if (url == null) {
            System.err.println(name + " nicht im classpath gefunden.");
        }
        else {
            rc = url.getFile();
        }

        return rc;
    }

    /**
     *
     * Searching the file name
     *
     * @param fileName
     *
     * @return
     */

    /**
     * liest das übergebene File in einen Stringbuffer ein.
     *
     * @param filename
     * @return StringBuffer
     */
    public static StringBuffer loadFile(final String filename) {
        File f = new File(filename);
        StringBuffer strbuffer = new StringBuffer();
        String newLine = System.getProperty("line.separator");
        if (f.exists()) {
            try {

                BufferedReader breader = new BufferedReader(new FileReader(f));

                while (breader.ready()) {
                    strbuffer.append(breader.readLine());
                    strbuffer.append(newLine);
                }
                breader.close();
                breader = null;
            }
            catch (IOException io) {
                System.err.println("IOException: " + io.getMessage());
            }
        }
        else {
            System.err.println("No such file: " + f);
        }
        return strbuffer;
    }

    /**
     * schreibt den StringBuffer sb in das File filename.
     *
     * @param filename
     *            String
     * @param sb
     *            String
     * @throws IOException
     */
    public static void writeToFile(final String filename, final StringBuffer sb, final String encoding)
            throws IOException {
        writeToFile(filename, sb.toString(), encoding == null ? defaultEncoding : encoding); // Standard ist Ansi
    }

    /**
     * writes String to the defines Outputfile.
     *
     * @param outfile
     * @param aString
     * @param encoding
     * @throws IOException
     */
    public static void writeToFile(final String outfile, final String aString, final String encoding)
            throws IOException {
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile, false),
                encoding == null ? defaultEncoding : encoding));
        try {
            out.write(aString);
        }
        finally {
            out.close();
        }
    }

    /**
     * @param filename1
     *            String Quelle
     * @param filename2
     *            String Ziel
     * @param keepTimestamp
     *            boolean true, dann wird auf Ziel
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void copyFile(final String filename1, final String filename2, final boolean keepTimestamp)
            throws FileNotFoundException, IOException {
        if ((filename1 == null) || (filename2 == null)) {
            System.out.println("copy nicht möglich weil kein Name angeben");
            return;
        }
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename1));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filename2));
        copyFile(bis, bos, true);
        if (keepTimestamp) {
            File f = new File(filename1);
            long time = f.lastModified();
            f = new File(filename2);
            f.setLastModified(time);
        }
        bos.close();
        bis.close();
    }

    /**
     * @param is
     *            InputStream
     * @param os
     *            OutputStream
     * @param close
     *            boolean
     * @throws IOException
     */
    public static void copyFile(final InputStream is, final OutputStream os, final boolean close) throws IOException {
        int z = 0;
        while ((z = is.read()) != -1) {
            os.write(z);
        }
        is.close();
        if (close) {
            os.close();
            // File f = null;
        }
    }

    private static String getDateAsString(Date date, String dformat) {
        if (dformat == null) {
            dformat = "yyyyMMddHHmmss";
        }
        String datum = null;
        if (date == null) {
            date = new Date();
        }
        SimpleDateFormat df = new SimpleDateFormat(dformat);
        datum = df.format(date);
        return datum;
    }

    /**
     * renameFileWithDateTime(String filename1, String filename2)
     * geht auch über Festplattengrenzen.
     * add datetimestamp to filename1
     * copy filename1 to Newfilename
     * delete filename1
     *
     * @param filename1
     *            String Filename-from
     * @throws FileNotFoundException
     * @throws IOException
     * @return String filename-to
     */
    public static String renameFileWithDateTime(final String filename1) throws FileNotFoundException, IOException {

        String filename2 = filename1 + "." + getDateAsString(null, "yyyyMMddHHmmss");

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename1));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filename2));
        copyFile(bis, bos, true);
        deleteFile(filename1);
        bis.close();
        bos.close();
        return (filename2);
    }

    /**
     * hängt an den übergebenen Namen einen Datumsstring mit aktueller Datum/Zeit an.
     *
     * @param filename
     *            String
     * @return String
     */
    public static String makeFilenameWithDateTime(final String filename) {
        return makeFilenameWithDateTime(filename, new Date());
    }

    public static String makeTempFilename(String prefix, final String suffix) {
        String rc = null;
        File tmp = null;
        prefix = (prefix == null) || (prefix.length() < 3) ? "tmp" : prefix;
        try {
            tmp = File.createTempFile(prefix, suffix);
            tmp.deleteOnExit();
            rc = tmp.getName();
        }
        catch (IOException e) {
            System.err.println("Fehler bei erzeugen eines temporären Filenamens: " + e.getLocalizedMessage());
        }
        return rc;
    }

    public static Date getFileDate(final String filename) {
        File f = new File(filename);
        long l = f.lastModified();

        return new Date(l);
    }

    /**
     * hängt an den übergebenen Namen einen Datumsstring an.
     *
     * @param filename
     *            String
     * @return String
     */
    public static String makeFilenameWithDateTime(String filename, final Date date) {
        String datum = getDateAsString(date, null);
        int p = filename.lastIndexOf(".");
        if (p >= 0) {
            filename = filename.substring(0, p) + "_" + datum + filename.substring(p); // Datum vor die
            // Extension!
        }
        else {
            filename = filename + "." + datum; // keine endung gefunden, dann hängen wir's einfach an
        }

        return (filename);
    }

    /**
     * bennent ein File um
     *
     * @param filename
     *            String alter Name
     * @param filename2
     *            String neuer Name
     * @throws IOException
     * @return boolean true, wenn ok
     */
    public static boolean renameFile(final String filename, final String filename2) throws IOException {
        File f1 = new File(filename);
        File f2 = new File(filename2);
        // TODO testen, ob das auch für Verzeichnisse gilt und ob das auch quer über Verzeichnisgrenzen gilt
        boolean ok = f1.renameTo(f2);
        return ok;
    }

    /**
     * erzeugt ein neues Verzeichnis
     *
     * @param name
     *            String
     * @throws IOException
     */
    public static boolean createFolder(final String name) throws IOException {
        File f = new File(name);
        return f.mkdir();
    }

    /**
     * l�scht ein File
     *
     * @param filename
     *            String
     * @throws IOException
     */
    public static boolean deleteFile(final String filename) throws IOException {
        File f = new File(filename);
        return f.delete();
    }

    /**
     * @param filename
     * @throws IOException
     */
    public static String getFilePath(final String filename) {
        File f = new File(filename);
        String path = f.getParent(); // testen, ob die Angabe relativ oder absolut ist
        return path;
    }

    /**
     * @param path
     * @throws IOException
     */
    public static String getDirName(final String path) {
        File f = new File(path);
        String name = f.getName();
        return name;
    }

    /**
     * verkürzt den Aufruf (keine new File() nötig)
     *
     * @param filename
     * @throws IOException
     */
    public static String getFileName(final String filename) {
        File f = new File(filename);
        String name = f.getName();
        return name;
    }

    /**
     * setzt Fileattribute per Runtime.exec()
     *
     * @param filename
     *            String
     * @param attribString
     *            String e.g. +H = hidden
     * @throws IOException
     */
    public void setFileAttribute(final String filename, final String attribString) throws IOException {
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec("attrib " + attribString + " " + filename);
        int exitVal = proc.exitValue();
        System.out.println("Process exitValue: " + exitVal);
    }

    /**
     * liest alle Einträge im Verzeichnis und gibt sie im Stringarray zur�ck <br/>
     * Modifications:<br/>
     * <br/>
     *
     * @param folder
     *            String Directory-Name
     * @return String[] die Liste der Einträge im Directory
     */
    public static String[] getFileList(final String folder) {
        File dir = new File(folder);
        String[] files = dir.list();
        return files;
    }

    public static void copyFolder(final String from, final String to, final boolean recursive,
            final boolean keepTimestamp) throws FileNotFoundException, IOException {

        File dir = new File(from);
        File[] files = dir.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                if (!new File(to).exists()) {
                    createFolder(to);
                }
                copyFile(file.getPath(), to + File.separator + file.getName(), keepTimestamp);
            }
            else if (recursive) {
                if (!new File(to).exists()) {
                    createFolder(to);
                }
                copyFolder(file.getPath(), to + File.separator + file.getName(), true, keepTimestamp);
            }
            else {
                System.out.println("skipping folder " + file.getName());
            }
        }

    }

    /**
     * Ein Pfadname im externen Format (Trenner '\') wird umgeformt mit Trenner '/' <br/>
     * Modifications:<br/>
     * <br/>
     *
     * @param name
     * @return String
     */
    public static String convertFilename(final String name) {
        String internalName = null;
        char extSeparator = '\\';
        char intSeparator = File.separatorChar;
        if (intSeparator == '\\') {
            extSeparator = '/';
        }
        internalName = name.replace(extSeparator, intSeparator);

        return internalName;
    }

    /**
     * sucht die Klassen-Datei im Filesystem und gibt sie zurück<br/>
     * ist sehr aufwendig für JAR-Files. <br/>
     * <br/>
     * Modifications:<br/>
     * <br/>
     *
     * @return Date last modified
     */
    public File getMyself() {
        String thisPathName = null;
        File testfile = null;
        String userdir = null;
        String javaclasspath = null;
        Properties props = null;

        String thisClassName = this.getClass().getName();
        int p = thisClassName.indexOf('.');
        thisClassName = thisClassName.substring(p + 1);

        // wenn das ein echtes .class-File ist, bekomme ich hier die richtige Anwort
        URL u = this.getClass().getResource(".");
        if (u != null) // dann wird das eine class-Datei gewesen sein!
        {
            thisPathName = u.getPath();
            String fname = thisPathName + thisClassName + ".class";
            testfile = new File(fname);
        }
        if ((u != null) && (testfile != null) && testfile.exists() && !testfile.isDirectory()) {
            return testfile;
        }
        else
        // jar Dateien versuchen wir so, zumindest so wie ich die starte klappt das, natürlich nur, wenn das jar nicht
        // quer über die Platte aufgerufen wird:
        {
            props = System.getProperties();
            userdir = props.getProperty("user.dir"); // ist das aktuelle Homeverzeichnis.
            javaclasspath = props.getProperty("java.class.path"); // für Java sind jar-files auch nur Pfade!
            // jetzt den classpath zerlegen und jeweils probieren
            p = 0;
            while (p >= 0) {
                p = javaclasspath.lastIndexOf(';');
                String path = javaclasspath.substring(p + 1);
                if (path.startsWith(".\\")) {
                    path = path.substring(2); // wenn ein absoluter Pfad oder ..\ angegeben wurde klappt das nicht.
                }
                if (p > 0) {
                    javaclasspath = javaclasspath.substring(0, p);
                }
                else {
                    javaclasspath = "";
                }
                // ich nehme an: userdir+path = jar-Fle
                path = userdir + "\\" + path;
                if (path.indexOf(thisClassName) > -1) // damit wir nicht evtl. andere Jarfiles benutzen
                {
                    testfile = new File(path);
                    if (testfile.exists() && !testfile.isDirectory()) {
                        return testfile; // der war's!!
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks if filename is of a compressed file. currently checks only if the last letter is an 'c'.
     * TODO check must be more elaborate
     *
     * @param name the filename
     * @return true, if is compressed
     */
    public static boolean isCompressed(final String name) {

        if (name.toLowerCase().endsWith("c")) {
            return true;
        }
        else {
            return false;
        }
    }

    public static StringBuffer readFromCompressedOrUncompressedFile(final String name) throws Exception {
        StringBuffer sb = null;
        if (isCompressed(name)) {
            sb = readFromCompressedFile(name);
        }
        else {
            sb = readFromFile(name);
        }

        return sb;
    }

    public static StringBuffer readFromFile(final String name) {
        File fileObj = new File(name);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileObj));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException("Can't find the file", e);
        }
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");
        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            reader.close();
            return new StringBuffer(stringBuilder.toString());
        }
        catch (IOException e) {
            return null;
        }
        finally {
            reader = null;
        }
    }

    public static void rmDir(final String name, final boolean recursiv) throws Exception {
        if (recursiv) {
            String[] sFiles = FileUtil.getFileList(name);
            for (int i = 0; i < sFiles.length; i++) {
                File f = new File(name + File.separator + sFiles[i]);
                if (f.isDirectory()) {
                    rmDir(name + File.separator + sFiles[i], recursiv);
                }
                else {
                    f.delete();
                }
            }
        }
        // zum Schluss immer das aktuelle Directory löschen. Die Files sind entweder schon weg (recursiv) oder müssen
        // vorher gelöscht sein
        File f = new File(name);
        f.delete();
    }

    public static boolean exists(final String filename) {
        File f = new File(filename);
        return f.exists();
    }

}
