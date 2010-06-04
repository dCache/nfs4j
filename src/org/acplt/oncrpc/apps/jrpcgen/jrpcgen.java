/*
 * $Header: /cvsroot/remotetea/remotetea/src/org/acplt/oncrpc/apps/jrpcgen/jrpcgen.java,v 1.4 2005/11/11 21:28:48 haraldalbrecht Exp $
 *
 * Copyright (c) 1999, 2000
 * Lehrstuhl fuer Prozessleittechnik (PLT), RWTH Aachen
 * D-52064 Aachen, Germany.
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
//
// Personal note: this class probably suffers from a flashback on
// procedural programming ... but where do we need to be today?
//
package org.acplt.oncrpc.apps.jrpcgen;

import org.acplt.oncrpc.apps.jrpcgen.cup_runtime.Symbol;

import java.io.*;
import java.util.Enumeration;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class <code>jrpcgen</code> implements a Java-based rpcgen RPC protocol
 * compiler. jrpcgen is a Java-based tool that generates source code of Java
 * classes to implement an RPC protocol. The input to jrpcgen is a language
 * similar to C (but more probably much more similar to FORTRAN) known as
 * the RPC language (Remote Procedure Call Language).
 *
 * @version $Revision: 1.4 $ $Date: 2005/11/11 21:28:48 $ $State: Exp $ $Locker:  $
 * @author Harald Albrecht
 */
public class jrpcgen {

    /**
     * Print the help message describing the available command line options.
     */
    public static void printHelp() {
        System.out.println("Usage: jrpcgen [-options] x-file");
        System.out.println();
        System.out.println("where options include:");
        System.out.println("  -c <classname>  specify class name of client proxy stub");
        System.out.println("  -d <dir>        specify directory where to place generated source code files");
        System.out.println("  -p <package>    specify package name for generated source code files");
        System.out.println("  -s <classname>  specify class name of server proxy stub");
        System.out.println("  -ser            tag generated XDR classes as serializable");
        System.out.println("  -bean           generate accessors for usage as bean, implies -ser");
        System.out.println("  -noclamp        do not clamp version number in client method stubs");
        System.out.println("  -initstrings    initialize all strings to be empty instead of null");
        System.out.println("  -nobackup       do not make backups of old source code files");
        System.out.println("  -noclient       do not create client proxy stub");
        System.out.println("  -noserver       do not create server proxy stub");
        System.out.println("  -parseonly      parse x-file only but do not create source code files");
        System.out.println("  -verbose        enable verbose output about what jrpcgen is doing");
        System.out.println("  -version        print jrpcgen version and exit");
        System.out.println("  -debug          enables printing of diagnostic messages");
        System.out.println("  -? -help        print this help message and exit");
        System.out.println("  --              end options");
        System.out.println();
    }
    /**
     * Current version of jrpcgen.
     */
    public static final String VERSION = "1.0.7+";
    /**
     * A remote procedure has no parameters and thus needs to use the
     * XDR void wrapper class as a dummy.
     */
    public static final int PARAMS_VOID = 0;
    /**
     * A remote procedure expects only a single parameter, which is a
     * complex type (class).
     */
    public static final int PARAMS_SINGLE = 1;
    /**
     * A remote procedure expects only a single parameter, which is of
     * a base type, like integer, boolean, string, et cetera.
     */
    public static final int PARAMS_SINGLE_BASETYPE = 2;
    /**
     * A remote procedure expects more than one parameter and thus needs
     * an XDR wrapping class.
     */
    public static final int PARAMS_MORE = 3;
    /**
     * String containing date/time when a jrpcgen run was started. This string
     * is used in the headers of the generated source code files.
     */
    public static final String startDate =
            (new SimpleDateFormat()).format(new Date());
    /**
     * Contains all global identifiers for type, structure and union specifiers
     * as well as for constants and enumeration members. This static attribute
     * is directly manipulated by the parser.
     */
    public static Map globalIdentifiers = new HashMap();
    /**
     * Disable automatic backup of old source code files, if <code>true</code>.
     */
    public static boolean noBackups = false;
    /**
     * Holds information about the remote program defined in the jrpcgen
     * x-file.
     */
    public static List<JrpcgenProgramInfo> programInfos = null;
    /**
     * Clamp version and program number in client method stubs to the
     * version and program number specified in the x-file.
     */
    public static boolean clampProgAndVers = true;
    /**
     * Enable diagnostic messages when parsing the x-file.
     */
    public static boolean debug = false;
    /**
     * Verbosity flag. If <code>true</code>, then jrpcgen will report about
     * the steps it is taking when generating all the source code files.
     */
    public static boolean verbose = false;
    /**
     * Parse x-file only but do not create source code files if set to
     * <code>true</code>.
     */
    public static boolean parseOnly = false;
    /**
     * The x-file to parse (not: the X Files, the latter ones are something
     * completely different).
     */
    public static File xFile = null;
    /**
     * Destination directory where to place the generated files.
     */
    public static File destinationDir = new File(".");
    /**
     * Current FileWriter object receiving generated source code.
     */
    public static Writer currentFileWriter = null;
    /**
     * Current PrintWriter object sitting on top of the
     * {@link #currentFileWriter} object receiving generated source code.
     */
    public static PrintWriter currentPrintWriter = null;
    /**
     * Full name of the current source code file.
     */
    public static String currentFilename = null;
    /**
     * Specifies package name for generated source code, if not
     * <code>null</code>. If <code>null</code>, then no package statement
     * is emitted.
     */
    public static String packageName = null;
    /**
     * Name of class containing global constants. It is derived from the
     * filename with the extension (".x") and path removed.
     */
    public static String baseClassname = null;
    /**
     * Do not generate source code for the client proxy stub if
     * <code>true</code>.
     */
    public static boolean noClient = false;
    /**
     * Do not generate source code for the server proxy stub if
     * <code>true</code>.
     */
    public static boolean noServer = false;
    /**
     * Name of class containing the ONC/RPC server stubs.
     */
    public static String serverClass = null;
    /**
     * Name of class containing the ONC/RPC client stubs.
     */
    public static String clientClass = null;
    /**
     * Enable tagging of XDR classes as being Serializable
     */
    public static boolean makeSerializable = false;
    /**
     * Enable generation of accessors in order to use XDR classes as beans.
     */
    public static boolean makeBean = false;
    /**
     * Enable automatic initialization of String with empty Strings
     * instead of null reference.
     */
    public static boolean initStrings = false;

    /**
     * Creates a new source code file for a Java class based on its class
     * name. Same as {@link #createJavaSourceFile(String, boolean)} with
     * the <code>emitImport</code> parameter set to <code>true</code>.
     *
     * @param classname Name of Java class to generate. Must not contain
     *   a file extension -- especially ".java" is invalid. When the source
     *   code file is created, ".java" is appended automatically.
     *
     * @return PrintWriter to send source code to.
     */
    public static PrintWriter createJavaSourceFile(String classname) {
        return createJavaSourceFile(classname, true);
    }

    /**
     * Creates a new source code file for a Java class based on its class
     * name. If an old version of the source file exists, it is renamed first.
     * The backup will have the same name as the original file with "~"
     * appended.
     *
     * @param classname Name of Java class to generate. Must not contain
     *   a file extension -- especially ".java" is invalid. When the source
     *   code file is created, ".java" is appended automatically.
     * @param emitImports if <code>true</code>, then import statements for
     *   the remotetea ONC/RPC package and IOExceptions.
     *
     * @return PrintWriter to send source code to.
     */
    public static PrintWriter createJavaSourceFile(String classname, boolean emitImports) {
        String filename = classname + ".java";
        if (debug) {
            System.out.println("Generating source code for \""
                    + filename + "\" in \"" + destinationDir + "\"");
        }
        File file = new File(destinationDir, filename);
        //
        // If an old file of the same name already exists, then rename it
        // before creating the new file.
        //
        if (file.exists() && !noBackups) {
            if (!file.isFile()) {
                //
                // If the file to be created already exists and is not a
                // regular file, then bail out with an error.
                //
                System.err.println("error: source file \"" + filename
                        + "\"already exists and is not a regular file");
                System.exit(1);
            }
            File oldBackup = new File(destinationDir, filename + "~");
            if (oldBackup.isFile()) {
                oldBackup.delete();
            } else if (oldBackup.exists()) {
                System.err.println("error: backup source file \""
                        + filename + "~\" is not a regular file");
                System.exit(1);
            }
            if (!file.renameTo(new File(destinationDir, filename + "~"))) {
                System.err.println("error: can not rename old source code file \""
                        + filename + "\"");
                System.exit(1);
            }
            if (verbose) {
                System.out.println("Saved old source code file as \""
                        + filename + "~\"");
            }
        }
        //
        // Now create a new source code file...
        //
        try {
            currentFileWriter = new FileWriter(file);
        } catch (IOException e) {
            System.err.println("error: can not create \"" + filename
                    + "\": " + e.getLocalizedMessage());
            System.exit(1);
        }
        if (verbose) {
            System.out.print("Creating source code file \""
                    + filename + "\"...");
        }
        currentFilename = filename;
        PrintWriter out = new PrintWriter(currentFileWriter, true);
        currentPrintWriter = out;
        //
        // Create automatic header(s)...
        // Note that we always emit the import statements, regardless of
        // whether we're emitting a class file or an interface file consisting
        // of an enumeration.
        //
        out.println("/*");
        out.println(" * Automatically generated by jrpcgen " + VERSION
                + " on " + startDate);
        out.println(" * jrpcgen is part of the \"Remote Tea\" ONC/RPC package for Java");
        out.println(" * See http://remotetea.sourceforge.net for details");
        out.println(" *");
        out.println(" * This version of jrpcgen adopted by dCache project");
        out.println(" * See http://www.dCache.ORG for details");
        out.println(" */");

        //
        // Only generated package statement if a package name has been specified.
        //
        if ((packageName != null) && (packageName.length() > 0)) {
            out.println("package " + packageName + ";");
        }

        if (emitImports) {
            out.println("import org.dcache.xdr.*;");
            out.println("import java.io.IOException;");
            out.println();
        }

        return out;
    }

    /**
     * Create a new hash function object and initialize it using a class
     * and package name.
     *
     * @param classname Name of class.
     *
     * @return hash function object.
     */
    public static JrpcgenSHA createSHA(String classname) {
        JrpcgenSHA hash = new JrpcgenSHA();
        if ((packageName != null) && (packageName.length() > 0)) {
            hash.update(packageName + "." + classname);
        } else {
            hash.update(classname);
        }
        return hash;
    }

    /**
     * Closes the source code file previously opened with
     * <code>createJavaSourceFile</code>. This method writes a trailer
     * before closing the file.
     */
    public static void closeJavaSourceFile() {
        //
        // Create automatic footer before closing the file.
        //
        currentPrintWriter.println("// End of " + currentFilename);
        if (verbose) {
            System.out.println();
        }
        try {
            currentPrintWriter.close();
            currentFileWriter.close();
        } catch (IOException e) {
            System.err.println("Can not close source code file: "
                    + e.getLocalizedMessage());
        }
    }

    /**
     * Dump the value of a constant and optionally first dump all constants
     * it depends on.
     */
    public static void dumpConstantAndDependency(PrintWriter out, JrpcgenConst c) {
        //
        // This simple test avoids endless recursions: we already dumped this
        // particular constant in some place, so we should not proceed.
        //
        if (c.dontTraverseAnyMore) {
            return;
        }
        //
        // Since we will dump the constant below, we already set the flag,
        // to avoid endless recursions.
        //
        c.dontTraverseAnyMore = true;
        String dependencyIdentifier = c.getDependencyIdentifier();
        if (dependencyIdentifier != null) {
            //
            // There is a dependency, so try to resolve that first. In case
            // we depend on another identifier belonging to the same enclosure,
            // we dump this other identifier first. However, if the identifier
            // we depend on belongs to a different enclosure, then we must not
            // dump it: this will be the job of a later call when the proper
            // enclosure is in the works.
            //
            JrpcgenConst dc = (JrpcgenConst) globalIdentifiers.get(dependencyIdentifier);
            if (dc != null) {
                if (!c.enclosure.equalsIgnoreCase(dc.enclosure)) {
                    //
                    // In case we depend on a constant which belongs to a
                    // different enclosure then also dump the enclosure (that
                    // is, "enclosure.valueidentifier").
                    //
                    // Note that this code depends on the "value" starts
                    // with the identifier we depend on (which is currently
                    // the case), so we just need to prepend the enclosure.
                    //
                    out.println("    public static final int "
                            + c.identifier
                            + " = " + dc.enclosure + "." + c.value + ";");
                    return;
                }
                //
                // Only dump the identifier we're dependent on, if it's in
                // the same enclosure.
                //
                dumpConstantAndDependency(out, dc);
            }
        }
        //
        // Just dump the plain value (without enclosure).
        //
        out.println("    public static final int "
                + c.identifier
                + " = " + c.value + ";");
    }

    /**
     * Generate source code file containing all constants defined in the
     * x-file as well as all implicitely defined constants, like program,
     * version and procedure numbers, etc. This method creates a public
     * interface with the constants as public static final integers.
     */
    public static void dumpConstants() {
        //
        // Create new source code file containing a Java interface representing
        // all XDR constants.
        //
        PrintWriter out = createJavaSourceFile(baseClassname, false);
        //
        // Spit out some description for javadoc & friends...
        //
        out.println("/**");
        out.println(" * A collection of constants used by the \"" + baseClassname
                + "\" ONC/RPC program.");
        out.println(" */");
        out.println("public interface " + baseClassname + " {");

        for (Object o: globalIdentifiers.values()) {
            if (o instanceof JrpcgenConst) {
                JrpcgenConst c = (JrpcgenConst) o;
                //
                // Dump only such constants which belong to the global
                // constants enclosure. Ignore all other constants, as those
                // belong to other Java class enclosures.
                //
                if (baseClassname.equals(c.enclosure)) {
                    dumpConstantAndDependency(out, c);
                }
            }
        }

        out.println("}");
        closeJavaSourceFile();
    }

    /**
     * Generate a source code file containing all elements of an enumeration
     * defined in a x-file.
     *
     * @param e {@link JrpcgenEnum Description} of XDR enumeration.
     */
    public static void dumpEnum(JrpcgenEnum e) {
        //
        // Create new source code file containing a Java interface representing
        // the XDR enumeration.
        //
        PrintWriter out = createJavaSourceFile(e.identifier, false);
        //
        // Spit out some description for javadoc & friends...
        //
        out.println("/**");
        out.println(" * Enumeration (collection of constants).");
        out.println(" */");

        out.println("public interface " + e.identifier + " {");
        out.println();

        Enumeration enums = e.enums.elements();
        while (enums.hasMoreElements()) {
            JrpcgenConst c = (JrpcgenConst) enums.nextElement();
            //
            // In case an element depends on a global constant, then
            // this constant will automatically be duplicated as part
            // of this enumeration.
            //
            dumpConstantAndDependency(out, c);
        }
        //
        // Close class...
        //
        out.println();
        out.println("}");
        closeJavaSourceFile();
    }
    /**
     * Java base data types for which are XDR encoding and decoding helper
     * methods available.
     */
    private static String[] baseTypes = {
        "void",
        "boolean",
        "byte",
        "short", "int", "long",
        "float", "double",
        "String"
    };

    /**
     * Given a name of a data type return the name of the equivalent Java
     * data type (if it exists), otherwise return <code>null</code>.
     *
     * NOTE: "opaque" is considered like "byte" to be a base type...
     * FIXME: char/byte?
     *
     * @return Name of Java base data type or <code>null</code> if the
     *   given data type is not equivalent to one of Java's base data
     *   types.
     */
    public static String xdrBaseType(String type) {
        int size = baseTypes.length;
        if ("opaque".compareTo(type) == 0) {
            type = "byte";
        }
        for (int idx = 0; idx < size; ++idx) {
            if (baseTypes[idx].compareTo(type) == 0) {
                //
                // For base data types simply convert the first letter to
                // an upper case letter.
                //
                return "Xdr" + type.substring(0, 1).toUpperCase()
                        + type.substring(1);
            }
        }
        return null;
    }

    /**
     * Return the en-/decoding syllable XXX appropriate for a base data
     * type including arrays of base data types.
     *
     * @param decl declaration of a member of RPC struct or union.
     *
     * @return <code>null</code>, if the declaration does not specify a base data
     *   type. Otherwise a three-element String array, with [0] containing
     *   the type syllable for base type (including arrays), [1] containing
     *   parameter options when encoding (like maximum sizes, etc), and [2]
     *   containing options for decoding.
     */
    public static JrpcgenEnDecodingInfo baseEnDecodingSyllable(JrpcgenDeclaration decl) {
        String syllable = decl.type;
        boolean isBase = false;
        //
        // Check for Java base data types... if a match is found, then convert
        // the data type name, so that it becomes a valid syllable for use
        // with XDR en-/decoding functions xdrEncodingXXX() etc.
        // Example: "int" --> "Int" (because of xdrEncodingInt())
        // NOTE: we consider "opaque" to be a base type here...
        //
        int size = baseTypes.length;
        String type = decl.type;
        if ("opaque".compareTo(type) == 0) {
            type = "byte";
        }
        for (int idx = 0; idx < size; ++idx) {
            if (baseTypes[idx].compareTo(type) == 0) {
                //
                // For base data types simply convert the first letter to
                // an upper case letter.
                //
                isBase = true;
                syllable = syllable.substring(0, 1).toUpperCase()
                        + syllable.substring(1);
                break;
            }
        }
        //
        // Handle special case of enumerations, which have to be represented
        // using ints in the Java language.
        //
        if (!isBase) {
            Object o = globalIdentifiers.get(decl.type);
            if (o instanceof JrpcgenEnum) {
                isBase = true;
                syllable = "Int";
            }
        }
        //
        // In case we're dealing with an array, then add "Vector" to
        // the syllable to use the appropriate vector en-/decoding method
        // for base data types.
        // NOTE: unfortunately, strings do not adhere to this scheme, as
        // they are considered to be arrays of characters... what a silly
        // idea, as this makes a typedef necessary in case someone needs
        // an array of strings.
        // NOTE: also opaques break the scheme somehow, but the char=byte
        // versus opaque schisma anyhow drives me crazy...
        //
        if (isBase) {
            String encodingOpts = null;
            String decodingOpts = null;

            if ((decl.kind == JrpcgenDeclaration.FIXEDVECTOR)
                    || (decl.kind == JrpcgenDeclaration.DYNAMICVECTOR)) {
                if ("opaque".equals(decl.type)) {
                    if (decl.kind == JrpcgenDeclaration.FIXEDVECTOR) {
                        syllable = "Opaque";
                        encodingOpts = checkForEnumValue(decl.size);
                        decodingOpts = checkForEnumValue(decl.size);
                    } else {
                        syllable = "DynamicOpaque";
                        encodingOpts = null;
                        decodingOpts = null;
                    }
                } else if (!"String".equals(decl.type)) {
                    if (decl.kind == JrpcgenDeclaration.FIXEDVECTOR) {
                        syllable = syllable + "Fixed";
                        encodingOpts = checkForEnumValue(decl.size);
                        decodingOpts = checkForEnumValue(decl.size);
                    }
                    syllable = syllable + "Vector";
                }
            }

            JrpcgenEnDecodingInfo result = new JrpcgenEnDecodingInfo(syllable, encodingOpts, decodingOpts);
            return result;
        }
        return null;
    }

    /**
     * Return en- or decoding method appropriate for a struct or union member.
     */
    public static String codingMethod(JrpcgenDeclaration decl, boolean encode) {
        return codingMethod(decl, encode, null);
    }

    /**
     * Return en- or decoding method appropriate for a struct or union member.
     *
     * @param decl declaration for which the en-/decoding Java source code be
     *   returned.
     * @param encode <code>true</code> if encoding method should be returned,
     *   <code>false</code> if decoding method is to be returned.
     * @param oref name of object reference or <code>null</code> if
     *   "this" should be used instead.
     */
    public static String codingMethod(JrpcgenDeclaration decl, boolean encode,
            String oref) {
        //
        // Skip entries for void arms etc...
        //
        if (decl.identifier == null) {
            return "";
        }

        StringBuilder code = new StringBuilder();
        JrpcgenEnDecodingInfo data = baseEnDecodingSyllable(decl);

        //
        // In case no type was specified for the outer element, assume no
        // name, otherwise convert into a suitable prefix for code generation
        // by appending a dot.
        //
        if (oref == null) {
            oref = "";
        } else {
            oref = oref + ".";
        }

        if (data != null) {
            //
            // It's a base data type (including vectors). So we can use the
            // predefined en-/decoding methods:
            //   - xdr.xdrEncodeXXX(value);
            //   - value = xdr.xdrDecodeXXX(value);
            //
            if (encode) {
                code.append("        xdr.xdrEncode");
                code.append(data.syllable);
                code.append("(");
                code.append(oref).append(decl.identifier);
                if (data.encodingOptions != null) {
                    code.append(", ");
                    code.append(data.encodingOptions);
                }
                code.append(");\n");
            } else {
                code.append("        ");
                code.append(oref).append(decl.identifier);
                code.append(" = xdr.xdrDecode");
                code.append(data.syllable);
                code.append("(");
                if (data.decodingOptions != null) {
                    code.append(data.decodingOptions);
                }
                code.append(");\n");
            }
            return code.toString();
        } else {
            //
            // It's not a built-in base data type but instead something that
            // is represented by a class.
            //   - foo.xdrEncode(xdr);
            //   - foo = new FOO();
            //     foo.xdrDecode(xdr);
            // In case of arrays, this is going to be hairy...
            //
            if (decl.kind == JrpcgenDeclaration.SCALAR) {
                code.append("        ");
                if (encode) {
                    code.append(oref).append(decl.identifier);
                    code.append(".xdrEncode(xdr);\n");
                } else {
                    code.append(oref).append(decl.identifier);
                    code.append(" = new ");
                    code.append(decl.type);
                    code.append("(xdr);\n");
                }
                return code.toString();
                //
                // It's not a built-in base data type but instead an indirection
                // (reference) to some instance (optional data).
                //
            } else if (decl.kind == JrpcgenDeclaration.INDIRECTION) {
                code.append("        ");
                if (encode) {
                    code.append("if ( ");
                    code.append(oref).append(decl.identifier);
                    code.append(" != null ) { ");
                    code.append("xdr.xdrEncodeBoolean(true); ");
                    code.append(oref).append(decl.identifier);
                    code.append(".xdrEncode(xdr);");
                    code.append(" } else { ");
                    code.append("xdr.xdrEncodeBoolean(false);");
                    code.append(" };\n");
                } else {
                    code.append(oref).append(decl.identifier);
                    code.append(" = xdr.xdrDecodeBoolean() ? new ");
                    code.append(decl.type);
                    code.append("(xdr) : null;\n");
                }
                return code.toString();
            }
            //
            // Array case... Urgh!
            //
            if (encode) {
                code.append("        { ");
                code.append("int $size = ");
                if (decl.kind == JrpcgenDeclaration.DYNAMICVECTOR) {
                    //
                    // Dynamic array size. So we need to use the current size
                    // of the Java array.
                    //
                    code.append(oref).append(decl.identifier);
                    code.append(".length");
                } else {
                    code.append(checkForEnumValue(decl.size));
                }
                code.append("; ");
                if (decl.kind == JrpcgenDeclaration.DYNAMICVECTOR) {
                    //
                    // Dynamic array size. So we need to encode size information.
                    //
                    code.append("xdr.xdrEncodeInt($size); ");
                }
                //
                // Now encode all elements.
                //
                code.append("for ( int $idx = 0; $idx < $size; ++$idx ) { ");
                code.append(oref).append(decl.identifier);
                code.append("[$idx].xdrEncode(xdr); ");
                code.append("} }\n");
            } else {
                code.append("        { ");
                code.append("int $size = ");
                if (decl.kind == JrpcgenDeclaration.DYNAMICVECTOR) {
                    //
                    // Dynamic array size. So we need to decode size information.
                    //
                    code.append("xdr.xdrDecodeInt()");
                } else {
                    code.append(checkForEnumValue(decl.size));
                }
                code.append("; ");
                //
                // Now encode all elements.
                //
                code.append(oref).append(decl.identifier);
                code.append(" = new ");
                code.append(decl.type);
                code.append("[$size]; ");
                code.append("for ( int $idx = 0; $idx < $size; ++$idx ) { ");
                code.append(oref).append(decl.identifier);
                code.append("[$idx] = new ");
                code.append(decl.type);
                code.append("(xdr); ");
                code.append("} }\n");
            }
            return code.toString();
        }
    }

    /**
     * Checks whether a given data type identifier refers to an enumeration
     * type and then returns Java's int data type instead. In case of the
     * pseudo-type "opaque" return Java's byte data type. For all other
     * data types, the data type identifier is returned unaltered.
     *
     * @param dataType data type identifier to check.
     *
     * @return data type identifier.
     */
    public static String checkForSpecials(String dataType) {
        if (globalIdentifiers.get(dataType) instanceof JrpcgenEnum) {
            return "int";
        } else if ("opaque".equals(dataType)) {
            return "byte";
        }
        return dataType;
    }

    /**
     * Checks whether a given value references an identifier and then
     * returns the qualified identifier (interface where the value is
     * defined in) or simply the value in case of an integer literal.
     *
     * @param value Either an identifier to resolve or an integer literal.
     *
     * @return Integer literal or qualified identifier.
     */
    public static String checkForEnumValue(String value) {
        if (value.length() > 0) {
            //
            // If the value is an integer literal, then we just have to
            // return it. That's it.
            //
            if (Character.isDigit(value.charAt(0))
                    || (value.charAt(0) == '-')) {
                return value;
            }
            //
            // It's an identifier: we now need to find out in which
            // enclosure it lives, so we can return a qualified identifier.
            //
            Object id = jrpcgen.globalIdentifiers.get(value);
            if ((id != null)
                    && (id instanceof JrpcgenConst)) {
                JrpcgenConst c = (JrpcgenConst) id;
                if (c.enclosure == null) {
                    return c.value;
                }
                return c.enclosure + "." + c.identifier;
            }
        }
        return value;
    }

    /**
     * Generate a source code file containing all elements of a struct
     * defined in a x-file.
     *
     * @param s {@link JrpcgenStruct Description} of XDR struct.
     */
    public static void dumpStruct(JrpcgenStruct s) {
        //
        // Create new source code file containing a Java class representing
        // the XDR struct.
        //
        String access = "    public ";  // modify encapsulation with beans
        PrintWriter out = createJavaSourceFile(s.identifier);

        out.print("public class " + s.identifier + " implements XdrAble");
        if (makeSerializable) {
            out.print(", java.io.Serializable");
        }

        if (makeBean) {
            access = "    protected ";
        }
        out.println(" {");

        //
        // Generate declarations of all members of this XDR struct. This the
        // perfect place to also update the hash function using the elements
        // together with their type.
        //
        boolean useIteration = false;
        JrpcgenSHA hash = createSHA(s.identifier);
        Enumeration decls = s.elements.elements();
        while (decls.hasMoreElements()) {
            JrpcgenDeclaration d = (JrpcgenDeclaration) decls.nextElement();
            hash.update(d.type);
            hash.update(d.kind);
            hash.update(d.identifier);
            out.print(access + checkForSpecials(d.type) + " ");
            if (((d.kind == JrpcgenDeclaration.FIXEDVECTOR)
                    || (d.kind == JrpcgenDeclaration.DYNAMICVECTOR))
                    && !d.type.equals("String")) {
                out.print("[] ");
            }
            if (initStrings
                    && d.type.equals("String")) {
                out.println(d.identifier + " = \"\"; ");
            } else {
                out.println(d.identifier + ";");
            }
            //
            // If the last element in the XDR struct is a reference to
            // the type of the XDR struct (that is, a linked list), then
            // we can convert this tail recursion into an iteration,
            // avoiding deep recursions for large lists.
            //
            if (!decls.hasMoreElements()
                    && d.kind == JrpcgenDeclaration.INDIRECTION
                    && d.type.equals(s.identifier)) {
                useIteration = true;
            }
        }

        //
        // Generate serial version unique identifier
        //
        if (makeSerializable) {
            out.println();
            out.println("    private static final long serialVersionUID = "
                    + hash.getHash() + "L;");

            if (makeBean) {
                //
                // Also generate accessors (getters and setters) so that
                // class can be used as a bean.
                //
                decls = s.elements.elements();
                while (decls.hasMoreElements()) {
                    out.println();
                    JrpcgenDeclaration d = (JrpcgenDeclaration) decls.nextElement();
                    String jbName = d.identifier.substring(0, 1).toUpperCase() + d.identifier.substring(1);
                    boolean isArray = (((d.kind == JrpcgenDeclaration.FIXEDVECTOR)
                            || (d.kind == JrpcgenDeclaration.DYNAMICVECTOR))
                            && !d.type.equals("String"));
                    //
                    // Generate the setter(s)
                    //
                    if (isArray) {
                        out.println("    public void set" + jbName + "(" + checkForSpecials(d.type) + "[] x) { this." + d.identifier + " = x; }");
                        out.println("    public void set" + jbName + "(int index, " + checkForSpecials(d.type) + " x) { this." + d.identifier + "[index] = x; }");
                    } else {
                        out.println("    public void set" + jbName + "(" + checkForSpecials(d.type) + " x) { this." + d.identifier + " = x; }");
                    }
                    //
                    // Generate the getter(s)
                    //
                    if (isArray) {
                        out.println("    public " + checkForSpecials(d.type) + "[] get" + jbName + "() { return this." + d.identifier + "; }");
                        out.println("    public " + checkForSpecials(d.type) + " get" + jbName + "(int index) { return this." + d.identifier + "[index]; }");
                    } else {
                        out.println("    public " + checkForSpecials(d.type) + " get" + jbName + "() { return this." + d.identifier + "; }");
                    }
                }
            }
        }

        //
        // Now generate code for encoding and decoding this class (structure).
        //
        out.println();
        out.println("    public " + s.identifier + "() {");
        out.println("    }");
        out.println();
        out.println("    public " + s.identifier + "(XdrDecodingStream xdr)");
        out.println("           throws OncRpcException, IOException {");
        out.println("        xdrDecode(xdr);");
        out.println("    }");
        out.println();
        out.println("    public void xdrEncode(XdrEncodingStream xdr)");
        out.println("           throws OncRpcException, IOException {");
        decls = s.elements.elements();
        if (useIteration) {
            out.println("        " + s.identifier + " $this = this;");
            out.println("        do {");
            JrpcgenDeclaration decl = null;
            //
            // when using the iteration loop for serializing emit code for
            // all but the tail element, which is the reference to our type.
            //
            for (int size = s.elements.size(); size > 1; --size) {
                decl = (JrpcgenDeclaration) decls.nextElement();
                out.print("    if( $this." + decl.identifier + " != null)  " + codingMethod(decl, true, "$this"));
            }
            decl = (JrpcgenDeclaration) decls.nextElement();
            out.println("            $this = $this." + decl.identifier + ";");
            out.println("            xdr.xdrEncodeBoolean($this != null);");
            out.println("        } while ( $this != null );");
        } else {
            while (decls.hasMoreElements()) {
                out.print(codingMethod((JrpcgenDeclaration) decls.nextElement(), true));
            }
        }
        out.println("    }");

        out.println();
        out.println("    public void xdrDecode(XdrDecodingStream xdr)");
        out.println("           throws OncRpcException, IOException {");
        decls = s.elements.elements();
        if (useIteration) {
            out.println("        " + s.identifier + " $this = this;");
            out.println("        " + s.identifier + " $next;");
            out.println("        do {");
            JrpcgenDeclaration decl = null;
            //
            // when using the iteration loop for serializing emit code for
            // all but the tail element, which is the reference to our type.
            //
            for (int size = s.elements.size(); size > 1; --size) {
                decl = (JrpcgenDeclaration) decls.nextElement();
                out.print("    " + codingMethod(decl, false, "$this"));
            }
            decl = (JrpcgenDeclaration) decls.nextElement();
            out.println("            $next = xdr.xdrDecodeBoolean() ? new " + s.identifier + "() : null;");
            out.println("            $this." + decl.identifier + " = $next;");
            out.println("            $this = $next;");
            out.println("        } while ( $this != null );");
        } else {
            while (decls.hasMoreElements()) {
                out.print(codingMethod((JrpcgenDeclaration) decls.nextElement(), false));
            }
        }
        out.println("    }");
        //
        // Close class...
        //
        out.println();
        out.println("}");
        closeJavaSourceFile();
    }

    /**
     * Generate a source code file containing all elements of a union
     * defined in a x-file.
     *
     * @param u {@link JrpcgenUnion Description} of XDR union.
     */
    public static void dumpUnion(JrpcgenUnion u) {
        //
        // Create new source code file containing a Java class representing
        // the XDR union.
        //
        PrintWriter out = createJavaSourceFile(u.identifier);

        out.print("public class " + u.identifier + " implements XdrAble");
        if (makeSerializable) {
            out.print(", java.io.Serializable");
        }
        out.println(" {");
        //
        // Note that the descriminant can not be of an array type, string, etc.
        // so we don't have to handle all the special cases here.
        //
        out.println("    public " + checkForSpecials(u.descriminant.type) + " "
                + u.descriminant.identifier + ";");

        boolean boolDescriminant = u.descriminant.type.equals("boolean");

        JrpcgenSHA hash = createSHA(u.identifier);
        Enumeration arms = u.elements.elements();
        while (arms.hasMoreElements()) {
            JrpcgenUnionArm a = (JrpcgenUnionArm) arms.nextElement();
            //
            // Skip all arms which do not contain a variable but are
            // declared as "void" instead. Also skip all arms which are
            // mapped to another arm.
            //
            if ((a.element == null) || (a.element.identifier == null)) {
                continue;
            }
            //
            // In case we are working on the default arm and this arm
            // contains some variables, we hash the dummy descriminator
            // value "default".
            //
            if (a.value != null) {
                hash.update(a.value);
            } else {
                hash.update("default");
            }
            hash.update(a.element.type);
            hash.update(a.element.kind);
            hash.update(a.element.identifier);
            out.print("    public " + checkForSpecials(a.element.type) + " ");
            if (((a.element.kind == JrpcgenDeclaration.FIXEDVECTOR)
                    || (a.element.kind == JrpcgenDeclaration.DYNAMICVECTOR))
                    && !a.element.type.equals("String")) {
                out.print("[] ");
            }
            out.println(a.element.identifier + ";");
        }

        //
        // Generate serial version unique identifier
        //
        if (makeSerializable) {
            out.println();
            out.println("    private static final long serialVersionUID = "
                    + hash.getHash() + "L;");
        }

        //
        // Now generate code for encoding and decoding this class (structure).
        //
        out.println();
        out.println("    public " + u.identifier + "() {");
        out.println("    }");
        out.println();
        out.println("    public " + u.identifier + "(XdrDecodingStream xdr)");
        out.println("           throws OncRpcException, IOException {");
        out.println("        xdrDecode(xdr);");
        out.println("    }");
        out.println();
        out.println("    public void xdrEncode(XdrEncodingStream xdr)");
        out.println("           throws OncRpcException, IOException {");
        out.print(codingMethod(u.descriminant, true));
        if (!boolDescriminant) {
            //
            // Produce code using an ordinary switch statement...
            //
            out.println("        switch ( " + u.descriminant.identifier + " ) {");
            arms = u.elements.elements();
            while (arms.hasMoreElements()) {
                JrpcgenUnionArm a = (JrpcgenUnionArm) arms.nextElement();
                if (a.value != null) {
                    out.println("        case " + checkForEnumValue(a.value) + ":");
                } else {
                    //
                    // It's the default arm.
                    //
                    out.println("        default:");
                }
                //
                // Only emit code if arm does not map to another arm.
                //
                if (a.element != null) {
                    if (a.element.identifier != null) {
                        //
                        // Arm does not contain void, so we need to spit out
                        // encoding instructions.
                        //
                        out.print("    ");
                        out.print(codingMethod(a.element, true));
                    }
                    out.println("            break;");
                }
            }
            out.println("        }");
        } else {
            //
            // boolean descriminant: here we can have at most two arms, guess
            // why.
            //
            boolean firstArm = true;
            arms = u.elements.elements();
            while (arms.hasMoreElements()) {
                JrpcgenUnionArm a = (JrpcgenUnionArm) arms.nextElement();
                if (a.value == null) {
                    //
                    // Skip default branch this time...
                    //
                    continue;
                }
                if (a.element.identifier != null) {
                    //
                    // Arm contains data, so we need to create encoding
                    // instructions.
                    //
                    out.print("        ");
                    if (!firstArm) {
                        out.print("else ");
                    } else {
                        firstArm = false;
                    }
                    out.println("if ( " + u.descriminant.identifier
                            + " == " + checkForEnumValue(a.value) + " ) {");
                    out.print("    ");
                    out.print(codingMethod(a.element, true));
                    out.println("        }");
                }
            }
            arms = u.elements.elements();
            while (arms.hasMoreElements()) {
                JrpcgenUnionArm a = (JrpcgenUnionArm) arms.nextElement();
                if ((a.value == null) && (a.element.identifier != null)) {
                    out.print("        ");
                    if (!firstArm) {
                        out.print("else ");
                    }
                    out.println("{");
                    out.print("    ");
                    out.print(codingMethod(a.element, true));
                    out.println("        }");
                }
            }
        }
        out.println("    }");

        out.println();
        out.println("    public void xdrDecode(XdrDecodingStream xdr)");
        out.println("           throws OncRpcException, IOException {");
        out.print(codingMethod(u.descriminant, false));
        if (!boolDescriminant) {
            //
            // Produce code using an ordinary switch statement...
            //
            out.println("        switch ( " + u.descriminant.identifier + " ) {");
            arms = u.elements.elements();
            while (arms.hasMoreElements()) {
                JrpcgenUnionArm a = (JrpcgenUnionArm) arms.nextElement();

                if (a.value != null) {
                    out.println("        case " + checkForEnumValue(a.value) + ":");
                } else {
                    //
                    // It's the default arm.
                    //
                    out.println("        default:");
                }
                //
                // Only emit code if arm does not map to another arm.
                //
                if (a.element != null) {
                    if (a.element.identifier != null) {
                        //
                        // Arm does not contain void, so we need to spit out
                        // encoding instructions.
                        //
                        out.print("    ");
                        out.print(codingMethod(a.element, false));
                    }
                    out.println("            break;");
                }
            }
            out.println("        }");
        } else {
            //
            // boolean descriminant: here we can have at most two arms, guess
            // why.
            //
            boolean firstArm = true;
            arms = u.elements.elements();
            while (arms.hasMoreElements()) {
                JrpcgenUnionArm a = (JrpcgenUnionArm) arms.nextElement();
                if (a.value == null) {
                    //
                    // Skip default branch this time...
                    //
                    continue;
                }
                if (a.element.identifier != null) {
                    //
                    // Arm contains data, so we need to create encoding
                    // instructions.
                    //
                    out.print("        ");
                    if (!firstArm) {
                        out.print("else ");
                    } else {
                        firstArm = false;
                    }
                    out.println("if ( " + u.descriminant.identifier
                            + " == " + checkForEnumValue(a.value) + " ) {");
                    out.print("    ");
                    out.print(codingMethod(a.element, false));
                    out.println("        }");
                }
            }
            arms = u.elements.elements();
            while (arms.hasMoreElements()) {
                JrpcgenUnionArm a = (JrpcgenUnionArm) arms.nextElement();
                if ((a.value == null) && (a.element.identifier != null)) {
                    out.print("        ");
                    if (!firstArm) {
                        out.print("else ");
                    }
                    out.println("{");
                    out.print("    ");
                    out.print(codingMethod(a.element, false));
                    out.println("        }");
                }
            }
        }
        out.println("    }");
        //
        // Close class...
        //
        out.println();
        out.println("}");
        closeJavaSourceFile();
    }

    /**
     * Generate a source code file containing a wrapper class for a typedef
     * defined in a x-file.
     *
     * @param d {@link JrpcgenDeclaration Description} of XDR typedef.
     */
    public static void dumpTypedef(JrpcgenDeclaration d) {
        //
        // Create new source code file containing a Java class representing
        // the XDR struct.
        //
        PrintWriter out = createJavaSourceFile(d.identifier);

        out.print("public class " + d.identifier + " implements XdrAble");
        if (makeSerializable) {
            out.print(", java.io.Serializable");
        }
        out.println(" {");
        out.println();

        String paramType = checkForSpecials(d.type);
        if (((d.kind == JrpcgenDeclaration.FIXEDVECTOR)
                || (d.kind == JrpcgenDeclaration.DYNAMICVECTOR))
                && !d.type.equals("String")) {
            paramType += " []";
        }

        out.print("    public " + paramType + " value;");
        out.println();

        //
        // Generate serial version unique identifier
        //
        if (makeSerializable) {
            JrpcgenSHA hash = createSHA(d.identifier);
            hash.update(d.type);
            hash.update(d.kind);
            out.println();
            out.println("    private static final long serialVersionUID = "
                    + hash.getHash() + "L;");
        }

        //
        // Now generate code for encoding and decoding this class (typedef).
        //
        JrpcgenDeclaration dstar = null;
        try {
            dstar = (JrpcgenDeclaration) d.clone();
        } catch (CloneNotSupportedException e) {
            throw (new RuntimeException("fatal: can not clone JrpcgenDeclaration"));
        }
        dstar.identifier = "value";

        out.println();
        out.println("    public " + d.identifier + "() {");
        out.println("    }");
        out.println();
        out.println("    public " + d.identifier + "(" + paramType + " value) {");
        out.println("        this.value = value;");
        out.println("    }");
        out.println();
        out.println("    public " + d.identifier + "(XdrDecodingStream xdr)");
        out.println("           throws OncRpcException, IOException {");
        out.println("        xdrDecode(xdr);");
        out.println("    }");
        out.println();
        out.println("    public void xdrEncode(XdrEncodingStream xdr)");
        out.println("           throws OncRpcException, IOException {");
        out.print(codingMethod(dstar, true));
        out.println("    }");

        out.println();
        out.println("    public void xdrDecode(XdrDecodingStream xdr)");
        out.println("           throws OncRpcException, IOException {");
        out.print(codingMethod(dstar, false));
        out.println("    }");
        //
        // Close class...
        //
        out.println();
        out.println("}");
        closeJavaSourceFile();
    }

    /**
     * Generate source code files for all structures, unions and enumerations
     * as well as constants. All constants, which do not belong to enumerations,
     * are emitted to a single interface.
     */
    public static void dumpClasses() {
        for(Object o : globalIdentifiers.values()) {
            if (o instanceof JrpcgenEnum) {
                dumpEnum((JrpcgenEnum) o);
            } else if (o instanceof JrpcgenStruct) {
                dumpStruct((JrpcgenStruct) o);
            } else if (o instanceof JrpcgenUnion) {
                dumpUnion((JrpcgenUnion) o);
            } else if (o instanceof JrpcgenDeclaration) {
                dumpTypedef((JrpcgenDeclaration) o);
            }
        }
    }

    /**
     * Generate source code for client-side stub methods for a particular
     * remote program version. The client-side stub methods take the
     * parameter(s) from the caller, encode them and throw them over to the
     * server. After receiving a reply, they will unpack and return it as
     * the outcome of the method call.
     *
     * @param out Printer writer to send source code to.
     * @param versionInfo Information about the remote program version for
     *   which source code is to be generated.
     */
    public static void dumpClientStubMethods(PrintWriter out,
            JrpcgenVersionInfo versionInfo) {
        int size = versionInfo.procedures.size();
        for (int idx = 0; idx < size; ++idx) {
            JrpcgenProcedureInfo proc = (JrpcgenProcedureInfo) versionInfo.procedures.elementAt(idx);
            //
            // First spit out the stub method. While we don't need to
            // fiddle around with the data types of the method's
            // parameter(s) and result, we later have to care about
            // some primitive data types when serializing them.
            //
            String resultType = checkForSpecials(proc.resultType);

            out.println("    /**");
            out.println("     * Call remote procedure " + proc.procedureId + ".");
            //
            // If there are no parameters, skip the parameter documentation
            // section, otherwise dump javadoc @param entries for every
            // parameter encountered.
            //
            if (proc.parameters != null) {
                Enumeration params = proc.parameters.elements();
                while (params.hasMoreElements()) {
                    JrpcgenParamInfo param = (JrpcgenParamInfo) params.nextElement();
                    out.println("     * @param " + param.parameterName
                            + " parameter (of type " + param.parameterType
                            + ") to the remote procedure call.");
                }
            }
            //
            // Only generate javadoc for result, when it is non-void.
            //
            if (proc.resultType.compareTo("void") != 0) {
                out.println("     * @return Result from remote procedure call (of type "
                        + proc.resultType + ").");
            }
            out.println("     * @throws OncRpcException if an ONC/RPC error occurs.");
            out.println("     * @throws IOException if an I/O error occurs.");
            out.println("     */");
            out.print("    public "
                    + resultType + " "
                    + proc.procedureId + "(");
            //
            // If the remote procedure does not have any parameters, then
            // parameters will be null. Otherwise it contains a vector with
            // information about the individual parameters, which we use
            // in order to generate the parameter list. Note that all
            // parameters are named at this point (they will either have a
            // user supplied name, or an automatically generated one).
            //
            int paramsKind;

            if (proc.parameters != null) {
                int psize = proc.parameters.size();
                for (int pidx = 0; pidx < psize; ++pidx) {
                    JrpcgenParamInfo paramInfo = (JrpcgenParamInfo) proc.parameters.elementAt(pidx);
                    if (pidx > 0) {
                        out.print(", ");
                    }
                    out.print(checkForSpecials(paramInfo.parameterType));
                    out.print(" ");
                    out.print(paramInfo.parameterName);
                }
                //
                // Now find out what kind of parameter(s) we have. In case
                // the remote procedure only expects a single parameter, check
                // whether it is a base type. In this case we later need to
                // wrap the single parameter. If the remote procedure expects
                // more than a single parameter, then we always need a
                // XDR wrapper.
                //
                if (psize > 1) {
                    paramsKind = PARAMS_MORE;
                } else {
                    //
                    // psize must be equal to one, otherwise proc.parameters
                    // must have been null.
                    //
                    String firstParamType =
                            ((JrpcgenParamInfo) proc.parameters.elementAt(0)).parameterType;
                    if (xdrBaseType(checkForSpecials(firstParamType)) == null) {
                        //
                        // No, it is not a base type, so we don't need one
                        // of the special XDR wrapper classes.
                        //
                        paramsKind = PARAMS_SINGLE;
                    } else {
                        //
                        // The single parameter to the remote procedure is
                        // a base type, so we will later need a wrapper.
                        //
                        paramsKind = PARAMS_SINGLE_BASETYPE;
                    }
                }
            } else {
                //
                // Remote procedure does not expect parameters at all.
                //
                paramsKind = PARAMS_VOID;
            }
            out.println(")");
            out.println("           throws OncRpcException, IOException {");
            //
            // Do generate code for wrapping parameters here, if necessary.
            //
            String xdrParamsName = null; // Name of variable representing XDR-able arguments

            switch (paramsKind) {
                case PARAMS_VOID:
                    xdrParamsName = "args$";
                    out.println("        XdrVoid args$ = XdrVoid.XDR_VOID;");
                    break;

                case PARAMS_SINGLE: {
                    JrpcgenParamInfo paramInfo = (JrpcgenParamInfo) proc.parameters.elementAt(0);
                    xdrParamsName = paramInfo.parameterName;
                    //
                    // We do not need to emit an args$ declaration here, as we
                    // can immediately make use of the one and only argument
                    // the remote procedure expects.
                    //
                    break;
                }

                case PARAMS_SINGLE_BASETYPE: {
                    JrpcgenParamInfo paramInfo = (JrpcgenParamInfo) proc.parameters.elementAt(0);
                    xdrParamsName = "args$";
                    String xdrParamsType = xdrBaseType(checkForSpecials(paramInfo.parameterType));
                    out.println("        " + xdrParamsType + " args$ = new "
                            + xdrParamsType + "(" + paramInfo.parameterName + ");");
                    break;
                }
                case PARAMS_MORE:
                    xdrParamsName = "args$";
                    out.println("        class XdrAble$ implements XdrAble {");

                    int psize = proc.parameters.size();
                    for (int pidx = 0; pidx < psize; ++pidx) {
                        JrpcgenParamInfo pinfo = (JrpcgenParamInfo) proc.parameters.elementAt(pidx);
                        out.println("            public "
                                + checkForSpecials(pinfo.parameterType)
                                + " " + pinfo.parameterName + ";");
                    }

                    out.println("            public void xdrEncode(XdrEncodingStream xdr)");
                    out.println("                throws OncRpcException, IOException {");

                    //
                    // Emit serialization code for all parameters.
                    // Note that not we do not need to deal with all kinds of
                    // parameters here, as things like "int<5>" are invalid,
                    // a typedef declaration is then necessary.
                    //
                    JrpcgenDeclaration decl = new JrpcgenDeclaration(null, null);
                    for (int pidx = 0; pidx < psize; ++pidx) {
                        JrpcgenParamInfo pinfo = (JrpcgenParamInfo) proc.parameters.elementAt(pidx);
                        decl.kind = JrpcgenDeclaration.SCALAR;
                        decl.identifier = pinfo.parameterName;
                        decl.type = pinfo.parameterType;
                        out.print("        ");
                        out.print(codingMethod(decl, true));
                    }

                    out.println("            }");
                    out.println("            public void xdrDecode(XdrDecodingStream xdr)");
                    out.println("                throws OncRpcException, IOException {");
                    out.println("            }");

                    out.println("        };");

                    out.println("        XdrAble$ args$ = new XdrAble$();");
                    for (int pidx = 0; pidx < psize; ++pidx) {
                        JrpcgenParamInfo pinfo = (JrpcgenParamInfo) proc.parameters.elementAt(pidx);
                        out.println("        args$." + pinfo.parameterName + " = "
                                + pinfo.parameterName + ";");
                    }
                    break;
            }
            //
            // Check the return data type of the result to be of one of
            // the base data types, like int, boolean, etc. In this case we
            // have to unwrap the result from one of the special XDR wrapper
            // classes and return the base data type instead.
            //
            String xdrResultType = xdrBaseType(resultType);
            //
            // Handle the result of the method: similiar to what we did
            // above. However, in all other cases we always need to
            // create a result object, regardless of whether we have to
            // deal with a basic data type (except void) or with some
            // "complex" data type.
            //
            if (resultType.equals("void")) {
                out.println("        XdrVoid result$ = XdrVoid.XDR_VOID;");
            } else if (xdrResultType != null) {
                out.println("        " + xdrResultType + " result$ = new "
                        + xdrResultType + "();");
            } else {
                out.println("        " + resultType + " result$ = new "
                        + resultType + "();");
            }
            //
            // Now emit the real ONC/RPC call using the (optionally
            // wrapped) parameter and (optionally wrapped) result.
            //
            if (clampProgAndVers) {
                out.println("        client.call("
                        + baseClassname + "." + proc.procedureId
                        + ", " + baseClassname + "." + versionInfo.versionId
                        + ", " + xdrParamsName + ", result$);");
            } else {
                out.println("        client.call("
                        + baseClassname + "." + proc.procedureId
                        + ", client.getVersion(), "
                        + xdrParamsName + ", result$);");
            }
            //
            // In case of a wrapped result we need to return the value
            // of the wrapper, otherwise we can return the result
            // itself (which then is not a base data type). As a special
            // case, we can not return void values...anyone for a
            // language design with first class void objects?!
            //
            if (xdrResultType != null) {
                //
                // Data type of result is a Java base data type, so we need
                // to unwrap the XDR-able result -- if it's not a void, which
                // we do not need to return at all.
                //
                if (!resultType.equals("void")) {
                    out.println("        return result$."
                            + resultType.toLowerCase() + "Value();");
                }
            } else {
                //
                // Data type of result is a complex type (class), so we
                // do not unwrap it but can return it immediately.
                //
                out.println("        return result$;");
            }
            //
            // Close the stub method (just as a hint, as it is
            // getting rather difficult to see what code is produced
            // at this stage...)
            //
            out.println("    }");
            out.println();
        }
    }

    /**
     * Generate source code for the client stub proxy object. This client
     * stub proxy object is then used by client applications to make remote
     * procedure (aka method) calls to an ONC/RPC server.
     */
    public static void dumpClient(JrpcgenProgramInfo programInfo) {
        //
        // When several versions of a program are defined, we search for the
        // latest and greatest one. This highest version number ist then
        // used to create the necessary <code>OncRpcClient</code> for
        // communication when the client proxy stub is constructed.
        //
        int version = Integer.parseInt(
                ((JrpcgenVersionInfo) programInfo.versions.elementAt(0)).versionNumber);
        int versionSize = programInfo.versions.size();
        for (int idx = 1; idx < versionSize; ++idx) {
            int anotherVersion = Integer.parseInt(
                    ((JrpcgenVersionInfo) programInfo.versions.elementAt(idx)).versionNumber);
            if (anotherVersion > version) {
                version = anotherVersion;
            }
        }

        //
        // Create new source code file containing a Java class representing
        // the XDR struct.
        // In case we have several programs defines, build the source code
        // file name from the program's name (this case is identified by a
        // null clientClass name).
        //
        String clientClass = jrpcgen.clientClass;
        if (clientClass == null) {
            clientClass = baseClassname + "_" + programInfo.programId + "_Client";
            System.out.println("CLIENT: " + clientClass);
        }
        PrintWriter out = createJavaSourceFile(clientClass);

        out.println("import java.net.InetAddress;");
        out.println();

        out.println("/**");
        out.println(" * The class <code>" + clientClass + "</code> implements the client stub proxy");
        out.println(" * for the " + programInfo.programId + " remote program. It provides method stubs");
        out.println(" * which, when called, in turn call the appropriate remote method (procedure).");
        out.println(" */");
        out.println("public class " + clientClass
                + " extends OncRpcClientStub {");
        out.println();
        //
        // Generate constructors...
        //
        out.println("    /**");
        out.println("     * Constructs a <code>" + clientClass + "</code> client stub proxy object");
        out.println("     * from which the " + programInfo.programId + " remote program can be accessed.");
        out.println("     * @param host Internet address of host where to contact the remote program.");
        out.println("     * @param protocol {@link org.acplt.oncrpc.OncRpcProtocols Protocol} to be");
        out.println("     *   used for ONC/RPC calls.");
        out.println("     * @throws OncRpcException if an ONC/RPC error occurs.");
        out.println("     * @throws IOException if an I/O error occurs.");
        out.println("     */");
        out.println("    public " + clientClass + "(InetAddress host, int protocol)");
        out.println("           throws OncRpcException, IOException {");
        out.println("        super(host, "
                + baseClassname + "." + programInfo.programId + ", "
                + version + ", 0, protocol);");
        out.println("    }");
        out.println();

        out.println("    /**");
        out.println("     * Constructs a <code>" + clientClass + "</code> client stub proxy object");
        out.println("     * from which the " + programInfo.programId + " remote program can be accessed.");
        out.println("     * @param host Internet address of host where to contact the remote program.");
        out.println("     * @param port Port number at host where the remote program can be reached.");
        out.println("     * @param protocol {@link org.acplt.oncrpc.OncRpcProtocols Protocol} to be");
        out.println("     *   used for ONC/RPC calls.");
        out.println("     * @throws OncRpcException if an ONC/RPC error occurs.");
        out.println("     * @throws IOException if an I/O error occurs.");
        out.println("     */");
        out.println("    public " + clientClass + "(InetAddress host, int port, int protocol)");
        out.println("           throws OncRpcException, IOException {");
        out.println("        super(host, "
                + baseClassname + "." + programInfo.programId + ", "
                + version + ", port, protocol);");
        out.println("    }");
        out.println();

        out.println("    /**");
        out.println("     * Constructs a <code>" + clientClass + "</code> client stub proxy object");
        out.println("     * from which the " + programInfo.programId + " remote program can be accessed.");
        out.println("     * @param client ONC/RPC client connection object implementing a particular");
        out.println("     *   protocol.");
        out.println("     * @throws OncRpcException if an ONC/RPC error occurs.");
        out.println("     * @throws IOException if an I/O error occurs.");
        out.println("     */");
        out.println("    public " + clientClass + "(OncRpcClient client)");
        out.println("           throws OncRpcException, IOException {");
        out.println("        super(client);");
        out.println("    }");
        out.println();

        out.println("    /**");
        out.println("     * Constructs a <code>" + clientClass + "</code> client stub proxy object");
        out.println("     * from which the " + programInfo.programId + " remote program can be accessed.");
        out.println("     * @param host Internet address of host where to contact the remote program.");
        out.println("     * @param program Remote program number.");
        out.println("     * @param version Remote program version number.");
        out.println("     * @param protocol {@link org.acplt.oncrpc.OncRpcProtocols Protocol} to be");
        out.println("     *   used for ONC/RPC calls.");
        out.println("     * @throws OncRpcException if an ONC/RPC error occurs.");
        out.println("     * @throws IOException if an I/O error occurs.");
        out.println("     */");
        out.println("    public " + clientClass + "(InetAddress host, int program, int version, int protocol)");
        out.println("           throws OncRpcException, IOException {");
        out.println("        super(host, program, version, 0, protocol);");
        out.println("    }");
        out.println();

        out.println("    /**");
        out.println("     * Constructs a <code>" + clientClass + "</code> client stub proxy object");
        out.println("     * from which the " + programInfo.programId + " remote program can be accessed.");
        out.println("     * @param host Internet address of host where to contact the remote program.");
        out.println("     * @param program Remote program number.");
        out.println("     * @param version Remote program version number.");
        out.println("     * @param port Port number at host where the remote program can be reached.");
        out.println("     * @param protocol {@link org.acplt.oncrpc.OncRpcProtocols Protocol} to be");
        out.println("     *   used for ONC/RPC calls.");
        out.println("     * @throws OncRpcException if an ONC/RPC error occurs.");
        out.println("     * @throws IOException if an I/O error occurs.");
        out.println("     */");
        out.println("    public " + clientClass + "(InetAddress host, int program, int version, int port, int protocol)");
        out.println("           throws OncRpcException, IOException {");
        out.println("        super(host, program, version, port, protocol);");
        out.println("    }");
        out.println();

        //
        // Generate method stubs... This is getting hairy in case someone
        // uses basic data types as parameters or the procedure's result.
        // In these cases we need to encapsulate these basic data types in
        // XDR-able data types.
        //
        for (int versionIdx = 0; versionIdx < versionSize; ++versionIdx) {
            JrpcgenVersionInfo versionInfo = (JrpcgenVersionInfo) programInfo.versions.elementAt(versionIdx);
            dumpClientStubMethods(out, versionInfo);
        }
        //
        // Close class...done!
        //
        out.println("}");
        closeJavaSourceFile();
    }

    /**
     *
     */
    public static void dumpServerStubMethodCall(PrintWriter out,
            JrpcgenProcedureInfo proc) {
        //
        // Check for special return types, like enumerations, which we
        // map to their corresponding Java base data type.
        //
        String resultType = checkForSpecials(proc.resultType);
        //
        // If the remote procedure does not have any parameters, then
        // parameters will be null. Otherwise it contains a vector with
        // information about the individual parameters, which we use
        // in order to generate the parameter list. Note that all
        // parameters are named at this point (they will either have a
        // user supplied name, or an automatically generated one).
        //
        int paramsKind;

        if (proc.parameters != null) {
            int psize = proc.parameters.size();
            //
            // Now find out what kind of parameter(s) we have. In case
            // the remote procedure only expects a single parameter, check
            // whether it is a base type. In this case we later need to
            // wrap the single parameter. If the remote procedure expects
            // more than a single parameter, then we always need a
            // XDR wrapper.
            //
            if (psize > 1) {
                paramsKind = PARAMS_MORE;
            } else {
                //
                // psize must be equal to one, otherwise proc.parameters
                // must have been null.
                //
                String firstParamType =
                        ((JrpcgenParamInfo) proc.parameters.elementAt(0)).parameterType;
                if (xdrBaseType(checkForSpecials(firstParamType)) == null) {
                    //
                    // No, it is not a base type, so we don't need one
                    // of the special XDR wrapper classes.
                    //
                    paramsKind = PARAMS_SINGLE;
                } else {
                    //
                    // The single parameter to the remote procedure is
                    // a base type, so we will later need a wrapper.
                    //
                    paramsKind = PARAMS_SINGLE_BASETYPE;
                }
            }
        } else {
            //
            // Remote procedure does not expect parameters at all.
            //
            paramsKind = PARAMS_VOID;
        }

        //
        // Do generate code for unwrapping here, if necessary.
        //
        String params = "";

        switch (paramsKind) {
            case PARAMS_VOID:
                //
                // Almost nothing to do here -- well, we need to retrieve nothing
                // so the RPC layer can do its book keeping.
                //
                out.println("                call.retrieveCall(XdrVoid.XDR_VOID);");
                params = "call";
                break;

            case PARAMS_SINGLE: {
                //
                // Only a single parameter, which is in addition immediately
                // ready for serialization.
                //
                JrpcgenParamInfo paramInfo = (JrpcgenParamInfo) proc.parameters.elementAt(0);
                out.println("                " + paramInfo.parameterType
                        + " args$ = new "
                        + paramInfo.parameterType + "();");
                out.println("                call.retrieveCall(args$);");
                params = "call, args$";
                break;
            }

            case PARAMS_SINGLE_BASETYPE: {
                //
                // Only a single parameter, but we have to unwrap it first.
                //
                JrpcgenParamInfo paramInfo = (JrpcgenParamInfo) proc.parameters.elementAt(0);
                String paramsType = checkForSpecials(paramInfo.parameterType);
                String xdrParamsType = xdrBaseType(paramsType);
                out.println("                " + xdrParamsType + " args$ = new "
                        + xdrParamsType + "();");
                out.println("                call.retrieveCall(args$);");
                params = "call, args$." + paramsType.toLowerCase() + "Value()";
                break;
            }

            case PARAMS_MORE: {
                //
                //
                //
                StringBuffer paramsBuff = new StringBuffer();

                out.println("                class XdrAble$ implements XdrAble {");

                int psize = proc.parameters.size();
                for (int pidx = 0; pidx < psize; ++pidx) {
                    JrpcgenParamInfo pinfo = (JrpcgenParamInfo) proc.parameters.elementAt(pidx);
                    out.println("                    public "
                            + checkForSpecials(pinfo.parameterType)
                            + " " + pinfo.parameterName + ";");
                }

                out.println("                    public void xdrEncode(XdrEncodingStream xdr)");
                out.println("                        throws OncRpcException, IOException {");
                out.println("                    }");
                out.println("                    public void xdrDecode(XdrDecodingStream xdr)");
                out.println("                        throws OncRpcException, IOException {");

                //
                // Emit serialization code for all parameters.
                // Note that not we do not need to deal with all kinds of
                // parameters here, as things like "int<5>" are invalid,
                // a typedef declaration is then necessary.
                //
                JrpcgenDeclaration decl = new JrpcgenDeclaration(null, null);
                for (int pidx = 0; pidx < psize; ++pidx) {
                    JrpcgenParamInfo pinfo = (JrpcgenParamInfo) proc.parameters.elementAt(pidx);
                    decl.kind = JrpcgenDeclaration.SCALAR;
                    decl.identifier = pinfo.parameterName;
                    decl.type = pinfo.parameterType;
                    out.print("                ");
                    out.print(codingMethod(decl, false));
                }

                out.println("                    }");

                out.println("                };");
                out.println("                XdrAble$ args$ = new XdrAble$();");
                out.println("                call.retrieveCall(args$);");


                if (psize > 0) {
                    paramsBuff.append("call, ");
                } else {
                    paramsBuff.append("call");
                }

                for (int pidx = 0; pidx < psize; ++pidx) {
                    JrpcgenParamInfo pinfo = (JrpcgenParamInfo) proc.parameters.elementAt(pidx);
                    if (pidx > 0) {
                        paramsBuff.append(", ");
                    }
                    paramsBuff.append("args$.");
                    paramsBuff.append(pinfo.parameterName);
                }

                params = paramsBuff.toString();
                break;
            }
        }

        //
        // Check the return data type of the result to be of one of
        // the base data types, like int, boolean, etc. In this case we
        // have to unwrap the result from one of the special XDR wrapper
        // classes and return the base data type instead.
        //
        String xdrResultType = xdrBaseType(resultType);

        if (resultType.equals("void")) {
            //
            // It's a remote procedure, so it does return simply nothing.
            // We use the singleton XDR_VOID to return a "nothing".
            //
            out.println("                " + proc.procedureId + "("
                    + params + ");");
            out.println("                call.reply(XdrVoid.XDR_VOID);");
        } else if (xdrResultType != null) {
            //
            // The return type is some Java base data type, so we need to
            // wrap the return value before we can serialize it.
            //
            out.println("                " + xdrResultType + " result$ = new "
                    + xdrResultType + "("
                    + proc.procedureId + "(" + params + "));");
            out.println("                call.reply(result$);");
        } else {
            //
            // The return type is a complex type which supports XdrAble.
            //
            out.println("                " + resultType + " result$ = "
                    + proc.procedureId + "(" + params + ");");
            out.println("                call.reply(result$);");
        }
    }

    /**
     * Generate public abstract method signatures for all remote procedure
     * calls. This ensures that they have to be implemented before any
     * derived server class gets useful.
     */
    public static void dumpServerStubMethods(PrintWriter out,
            JrpcgenVersionInfo versionInfo) {
        int procSize = versionInfo.procedures.size();
        for (int idx = 0; idx < procSize; ++idx) {
            JrpcgenProcedureInfo proc = (JrpcgenProcedureInfo) versionInfo.procedures.elementAt(idx);
            //
            // Fold enumerations et cetera back to their Java base data types.
            //
            String resultType = checkForSpecials(proc.resultType);
            //
            // Now emit the method signature, checking each argument for
            // specials, like enumerations. Also take care of no parameters
            // at all... Fortunately, this is relatively easy as we do not
            // need to care about parameter wrapping/unwrapping here.
            //
            out.print("    public abstract " + resultType + " "
                    + proc.procedureId + "(");
            if (proc.parameters != null) {

                out.print("RpcCall call$, ");

                int psize = proc.parameters.size();
                for (int pidx = 0; pidx < psize; ++pidx) {
                    JrpcgenParamInfo paramInfo = (JrpcgenParamInfo) proc.parameters.elementAt(pidx);
                    if (pidx > 0) {
                        out.print(", ");
                    }
                    out.print(checkForSpecials(paramInfo.parameterType));
                    out.print(" ");
                    out.print(paramInfo.parameterName);
                }
            } else {
                out.print("RpcCall call$");
            }
            out.println(");");
            out.println();
        }
    }

    /**
     *
     */
    public static void dumpServer(JrpcgenProgramInfo programInfo) {
        //
        // Create new source code file containing a Java class representing
        // the XDR struct.
        // In case we have several programs defines, build the source code
        // file name from the program's name (this case is identified by a
        // null clientClass name).
        //
        String serverClass = jrpcgen.serverClass;
        if (serverClass == null) {
            serverClass = baseClassname + "_" + programInfo.programId + "_ServerStub";
        }
        PrintWriter out = createJavaSourceFile(serverClass);

        out.println("import org.dcache.xdr.*;");
        out.println();

        out.println("/**");
        out.println(" */");
        out.println("public abstract class " + serverClass + " implements RpcDispatchable {");
        out.println();

        int versionSize = programInfo.versions.size();

        //
        // Generate dispatcher code...
        //
        out.println("    public void dispatchOncRpcCall(RpcCall call)");
        out.println("           throws OncRpcException, IOException {");
        out.println();
        out.println("        int version = call.getProgramVersion();");
        out.println("        int procedure = call.getProcedure();");
        out.println();

        for (int versionIdx = 0; versionIdx < versionSize; ++versionIdx) {
            JrpcgenVersionInfo versionInfo = (JrpcgenVersionInfo) programInfo.versions.elementAt(versionIdx);
            out.print(versionIdx == 0 ? "        " : "        } else ");
            out.println("if ( version == " + versionInfo.versionNumber + " ) {");
            int procSize = versionInfo.procedures.size();
            out.println("            switch ( procedure ) {");
            for (int procIdx = 0; procIdx < procSize; ++procIdx) {
                //
                // Emit case arms for every procedure defined. We have to
                // take care that the procedure number might be a constant
                // comming from an enumeration: in this case we need also to
                // dump the enclosure.
                //
                JrpcgenProcedureInfo procInfo = (JrpcgenProcedureInfo) versionInfo.procedures.elementAt(procIdx);
                out.println("            case " + checkForEnumValue(procInfo.procedureNumber) + ": {");
                dumpServerStubMethodCall(out, procInfo);
                out.println("                break;");
                out.println("            }");
            }
            out.println("            default:");
            out.println("                call.failProcedureUnavailable();");
            out.println("            }");
        }

        out.println("        } else {");
        out.println("            call.failProgramUnavailable();");
        out.println("        }");
        out.println("    }");
        out.println();

        //
        // Generate the stub methods for all specified remote procedures.
        //
        for (int versionIdx = 0; versionIdx < versionSize; ++versionIdx) {
            JrpcgenVersionInfo versionInfo = (JrpcgenVersionInfo) programInfo.versions.elementAt(versionIdx);
            dumpServerStubMethods(out, versionInfo);
        }

        //
        // Close class...done!
        //
        out.println("}");
        closeJavaSourceFile();
    }

    /**
     * Create the source code files based on the parsed information from the
     * x-file.
     */
    public static void dumpFiles() {
        dumpConstants();
        dumpClasses();
        for (JrpcgenProgramInfo progInfo: programInfos) {
            if (!noClient) {
                dumpClient(progInfo);
            }
            if (!noServer) {
                dumpServer(progInfo);
            }
        }
    }

    /**
     * The main part of jrpcgen where all things start.
     */
    public static void main(String[] args) {
        //
        // First parse the command line (options)...
        //
        int argc = args.length;
        int argIdx = 0;
        for (; argIdx < argc; ++argIdx) {
            //
            // Check to see whether this is an option...
            //
            String arg = args[argIdx];
            if ((arg.length() > 0)
                    && (arg.charAt(0) != '-')) {
                break;
            }
            //
            // ...and which option is it?
            //
            if (arg.equals("-d")) {
                // -d <dir>
                if (++argIdx >= argc) {
                    System.out.println("jrpcgen: missing directory");
                    System.exit(1);
                }
                destinationDir = new File(args[argIdx]);
            } else if (arg.equals("-package")
                    || arg.equals("-p")) {
                // -p <package name>
                if (++argIdx >= argc) {
                    System.out.println("jrpcgen: missing package name");
                    System.exit(1);
                }
                packageName = args[argIdx];
            } else if (arg.equals("-c")) {
                // -c <class name>
                if (++argIdx >= argc) {
                    System.out.println("jrpcgen: missing client class name");
                    System.exit(1);
                }
                clientClass = args[argIdx];
            } else if (arg.equals("-s")) {
                // -s <class name>
                if (++argIdx >= argc) {
                    System.out.println("jrpcgen: missing server class name");
                    System.exit(1);
                }
                serverClass = args[argIdx];
            } else if (arg.equals("-ser")) {
                makeSerializable = true;
            } else if (arg.equals("-bean")) {
                makeSerializable = true;
                makeBean = true;
            } else if (arg.equals("-initstrings")) {
                initStrings = true;
            } else if (arg.equals("-noclamp")) {
                clampProgAndVers = false;
            } else if (arg.equals("-debug")) {
                debug = true;
            } else if (arg.equals("-nobackup")) {
                noBackups = true;
            } else if (arg.equals("-noclient")) {
                noClient = true;
            } else if (arg.equals("-noserver")) {
                noServer = true;
            } else if (arg.equals("-parseonly")) {
                parseOnly = true;
            } else if (arg.equals("-verbose")) {
                verbose = true;
            } else if (arg.equals("-version")) {
                System.out.println("jrpcgen version \"" + VERSION + "\"");
                System.exit(1);
            } else if (arg.equals("-help") || arg.equals("-?")) {
                printHelp();
                System.exit(1);
            } else if (arg.equals("--")) {
                //
                // End of options...
                //
                ++argIdx;
                break;
            } else {
                //
                // It's an unknown option!
                //
                System.out.println("Unrecognized option: " + arg);
                System.exit(1);
            }
        }
        //
        // Otherwise we regard the current command line argument to be the
        // name of the x-file to compile. Check, that there is exactly one
        // x-file specified.
        //
        if ((argIdx >= argc) || (argIdx < argc - 1)) {
            printHelp();
            System.exit(1);
        }
        String xfilename = args[argIdx];
        xFile = new File(".", xfilename);
        //
        // Try to parse the file and generate the different class source
        // code files...
        //
        try {
            doParse();
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            //
            // Exit application with non-zero outcome, so in case jrpcgen is
            // used as part of, for instance, a make process, such tools can
            // detect that there was a problem.
            //
            System.exit(1);
        }
    }

    /**
     * The real parsing and code generation part. This has been factored out
     * of main() in order to make it available as an Ant task.
     */
    public static void doParse()
            throws FileNotFoundException, Exception {
        //
        // Get the base name for the client and server classes, it is derived
        // from the filename.
        //
        if (baseClassname == null) {
            String name = xFile.getName();
            int dotIdx = name.lastIndexOf('.');
            if (dotIdx < 0) {
                baseClassname = name;
            } else {
                baseClassname = name.substring(0, dotIdx);
            }
        }
        //
        //
        //
        FileInputStream in = null;
        try {
            in = new FileInputStream(xFile.getCanonicalPath());
        } catch (FileNotFoundException e) {
            throw (new FileNotFoundException("jrpcgen: can not open source x-file \""
                    + xFile.getCanonicalPath() + "\""));
        }
        JrpcgenScanner scanner = new JrpcgenScanner(in);
        JrpcgenParser parser = new JrpcgenParser(scanner);

        jrpcgen.globalIdentifiers.put("TRUE", new JrpcgenConst("TRUE", "true"));
        jrpcgen.globalIdentifiers.put("FALSE", new JrpcgenConst("FALSE", "false"));

        try {
            Symbol sym = parser.parse();
            if (!parseOnly) {
                if (programInfos.size() <= 1) {
                    if (clientClass == null) {
                        clientClass = baseClassname + "Client";
                    }
                    if (serverClass == null) {
                        serverClass = baseClassname + "ServerStub";
                    }
                }
                dumpFiles();
            }
        } catch (JrpcgenParserException pe) {
            throw (new Exception("jrpcgen: compilation aborted (" + pe.getMessage() + ")"));
        }
    }
}

/**
 * The class <code>JrpcgenEnDecodingInfo</code> contains information which
 * is necessary to generate source code calling appropriate XDR encoding
 * and decoding methods.
 *
 * @version $Revision: 1.4 $ $Date: 2005/11/11 21:28:48 $ $State: Exp $ $Locker:  $
 * @author Harald Albrecht
 */
class JrpcgenEnDecodingInfo {

    /**
     * Construct a <code>JrpcgenEnDecodingInfo</code> object containing
     * information for generating source code for encoding and decoding
     * of XDR/Java base data types.
     *
     * @param syllable Syllable of encoding/decoding method.
     * @param encodingOptions Optional parameters necessary to encode
     *   base data type.
     * @param decodingOptions Optional parameters necessary to decode
     *   base data type.
     */
    public JrpcgenEnDecodingInfo(String syllable, String encodingOptions,
            String decodingOptions) {
        this.syllable = syllable;
        this.encodingOptions = encodingOptions;
        this.decodingOptions = decodingOptions;
    }
    /**
     * (Type) syllable of the encoding or decoding method. The full name
     * of the encoding or decoding method is always in the form of
     * "xdrEncodeXXX(...)" or "xdrDecodeXXX(...)", where "XXX" is the
     * syllable contained in this attribute.
     */
    public String syllable;
    /**
     * Optional parameters to use when encoding a base data type. This
     * typically includes the size parameter for encoding fixed-size
     * vectors/arrays. When this attribute is not <code>null</code>, then
     * these parameters need to be appended. The attribute never contains
     * a leading parameter separator (aka "comma").
     */
    public String encodingOptions;
    /**
     * Optional parameters to use when decoding a base data type. This
     * typically includes the size parameter for decoding fixed-size
     * vectors/arrays. When this attribute is not <code>null</code>, then
     * these parameters need to be appended. The attribute never contains
     * a leading parameter separator (aka "comma").
     */
    public String decodingOptions;
}


// End of file jrpcgen.java

