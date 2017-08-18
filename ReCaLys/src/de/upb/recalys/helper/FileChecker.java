package de.upb.recalys.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * this class is used to check imported XML-Files if they represent a certain
 * type of data.
 * 
 * @author Roman Kober
 * @version 1.0
 */
public class FileChecker {

	private static Scanner scan;

	/**
	 * Check if the root of the xml file is the parameter {@code root}.
	 *
	 * @param xmlFile
	 *            the XML-File to check
	 * @param root
	 *            the name of the expected root node of the XML-File
	 * @return true, if the root node is equal to the root parameter. false,
	 *         otherwise.
	 */
	public static boolean checkRoot(File xmlFile, String root) {
		boolean foundHeader = false;
		FileInputStream fis;
		try {
			fis = new FileInputStream(xmlFile);
			scan = new Scanner(fis);
			while (scan.hasNext()) {
				String line = scan.nextLine().trim();
				if (!foundHeader && line.startsWith("<?xml version='1.0'?>")) {
					foundHeader = true;
					// root could be in the rest of the line
					line = line.substring("<?xml version='1.0'?>".length()).trim();
				}
				if (line.startsWith("<") && !line.startsWith("<" + root + ">")) {
					return false;
				} else if (line.startsWith("<" + root + ">")) {
					return true;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return false;

	}
}
