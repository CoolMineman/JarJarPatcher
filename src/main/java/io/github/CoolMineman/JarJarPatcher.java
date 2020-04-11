package io.github.CoolMineman;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.ZipEntry;

import org.apache.commons.compress.compressors.CompressorException;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarHeader;
import org.kamranzafar.jtar.TarInputStream;
import org.kamranzafar.jtar.TarOutputStream;
import org.zeroturnaround.zip.ByteSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipInfoCallback;
import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.IOUtils;
import io.sigpipe.jbsdiff.InvalidHeaderException;
import io.sigpipe.jbsdiff.Patch;

public class JarJarPatcher {
    private static ArrayList<String> zipentries = new ArrayList<String>();

    private static class ZipProcessor implements ZipInfoCallback {
        public void process(ZipEntry zipEntry) throws IOException {
            // System.out.println("Found " + zipEntry.getName());
            zipentries.add(zipEntry.getName());
        }
    }

    public static void jarfiletotarfile(String inJar, String outTar) throws IOException { // Being reproducible is mission critical
        File jarfile = new File(inJar);
        ZipUtil.iterate(jarfile, new ZipProcessor());
        Collections.sort(zipentries);

        FileOutputStream dest = new FileOutputStream(outTar);

        TarOutputStream out = new TarOutputStream(new BufferedOutputStream(dest));

        for (String filename : zipentries) {
            byte[] bytes = ZipUtil.unpackEntry(jarfile, filename);
            TarHeader header = new TarHeader();
            header.userName = new StringBuffer("JarJarTar");
            header.name = new StringBuffer(filename);
            header.size = bytes.length;
            TarEntry entry = new TarEntry(header);
            out.putNextEntry(entry);
            out.write(bytes, 0, bytes.length);
            out.flush();
            // System.out.println("added: " + filename);
        }
        out.close();
    }

    public static void tarfiletojarfile(String inTar, String outJar) throws FileNotFoundException, IOException { // Is Not reproducible
        ZipUtil.createEmpty(new File(outJar));
        TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(inTar)));
        TarEntry entry;

        ArrayList<ZipEntrySource> zipsources = new ArrayList<ZipEntrySource>();

        while ((entry = tis.getNextEntry()) != null) {
            String name = entry.getName();
            byte[] bytes = new byte[(int) entry.getSize()]; // Wont Work With Files >~2GB
            tis.read(bytes);
            zipsources.add(new ByteSource(name, bytes));
        }
        ZipUtil.addEntries(new File(outJar), zipsources.toArray(new ZipEntrySource[zipsources.size()]));
        tis.close();
    }

    public static void patchjar(String inJar, String outJar, String patchFile) throws IOException, CompressorException, InvalidHeaderException {
        //*Copied from jarfiletotarfile
        File jarfile = new File(inJar);
        ZipUtil.iterate(jarfile, new ZipProcessor());
        Collections.sort(zipentries);

        ByteArrayOutputStream dest = new ByteArrayOutputStream();

        TarOutputStream out = new TarOutputStream(new BufferedOutputStream(dest));

        for (String filename : zipentries) {
            byte[] bytes = ZipUtil.unpackEntry(jarfile, filename);
            TarHeader header = new TarHeader();
            header.userName = new StringBuffer("JarJarTar");
            header.name = new StringBuffer(filename);
            header.size = bytes.length;
            TarEntry entry = new TarEntry(header);
            out.putNextEntry(entry);
            out.write(bytes, 0, bytes.length);
            out.flush();
        }
        out.close();
        //*
        byte[] patchbytes = IOUtils.toByteArray(new FileInputStream(patchFile));

        byte[] tarbytes = dest.toByteArray();
        ByteArrayOutputStream patchedtar = new ByteArrayOutputStream();
        Patch.patch(tarbytes, patchbytes, patchedtar);

        //*Copied from tarfiletojarfile
        ZipUtil.createEmpty(new File(outJar));
        TarInputStream tis = new TarInputStream(new ByteArrayInputStream(patchedtar.toByteArray()));
        TarEntry entry;

        ArrayList<ZipEntrySource> zipsources = new ArrayList<ZipEntrySource>();

        while ((entry = tis.getNextEntry()) != null) {
            String name = entry.getName();
            if (entry.getSize() >= 0) {
                byte[] bytes = new byte[(int) entry.getSize()]; // Wont Work With Files >~2GB
                tis.read(bytes);
                zipsources.add(new ByteSource(name, bytes));
            } else {
                System.out.println("Smaller than zero?");
                System.out.println(entry.getSize());
                System.out.println(entry.getName());
                System.out.println("Patch Corruption Likely. Make sure you convert your jars to tars with jartotar before creating the patchfile");
            }
        }
        ZipUtil.addEntries(new File(outJar), zipsources.toArray(new ZipEntrySource[zipsources.size()]));
        tis.close();
        //*

    }

    public static void genpatch(String oldTar, String newTar, String patch) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("bsdiff " + oldTar + " " + newTar + " " + patch);
        System.out.println("bsdiff exited with code: " + Integer.toString(pr.waitFor()));
    }

    public static void verifymd5(String inFile, String hash) throws Exception, Error {
        if (hash.equals(MD5Checksum.getMD5Checksum(inFile))) {
            return;
        } else {
            throw new java.lang.Error("MD5 has Doesn't Match");
        }
    }

    public static void doHelp(int exit) {
        System.out.println("Usage:");
        System.out.println("  Convert Jar/Zip into a tar file (reproducible):");
        System.out.println("    JarJarPatcher jartotar [Input Jar/Zip] [Output Tar]");
        System.out.println("  Convert Tar into a Jar/Zip file (reproducible):");
        System.out.println("    JarJarPatcher tartojar [Input Tar] [Output Jar/Zip]");
        System.out.println("  Patch A Jar/Zip into a New Jar/Zip:");
        System.out.println("    JarJarPatcher patchjar [Input Jar/Zip] [Output Jar/Zip] [Patch File]");
        System.out.println("  Generate Patch File From 2 tars to be used with pachjar (requires dsdiff in path):");
        System.out.println("    JarJarPatcher genpatch [Input Tar] [Modified Tar] [Patch File]");
        System.out.println("  Verify MD5 Hash (Not Cryptographicly Secure):");
        System.out.println("    JarJarPatcher verifymd5 [File] [HASH]");
        System.exit(1);
    }
    public static void main(String[] args) {
        try {
            if (args[0].equals("jartotar")) {
                jarfiletotarfile(args[1], args[2]);
            } else if (args[0].equals("tartojar")) {
                tarfiletojarfile(args[1], args[2]);
            } else if (args[0].equals("patchjar")) {
                patchjar(args[1], args[2], args[3]);
            } else if (args[0].equals("genpatch")) {
                genpatch(args[1], args[2], args[3]);
            } else if (args[0].equals("verifymd5")) {
                verifymd5(args[1], args[2]);
            } else {
                doHelp(0);
            }
        } catch(Exception e) {
            System.out.println("---ERROR---");
            e.printStackTrace();
            System.out.println("---ERROR---");
            doHelp(1);
        }
    }
}