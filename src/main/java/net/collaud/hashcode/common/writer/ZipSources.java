package net.collaud.hashcode.common.writer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Gaetan Collaud
 */
public class ZipSources {

	private final List<String> files;
	private final List<String> folders;
	private final String output;

	public ZipSources(String output) {
		files = new ArrayList<>();
		folders = new ArrayList<>();
		this.output = output;
	}

	public ZipSources addFile(String file) {
		files.add(file);
		return this;
	}
	
	public ZipSources addFolder(String folder) {
		folders.add(folder);
		return this;
	}
	
	public Runnable getRunnable(){
		return this::zip;
	}

	public void zip() {
		long start = System.currentTimeMillis();
		byte[] buffer = new byte[1024];
		
		for (String foler : folders) {
			List<String> folderFiles = new ArrayList<>();
			recursiveFiles(foler, folderFiles);
			this.files.addAll(folderFiles);
		}

		try {

			FileOutputStream fos = new FileOutputStream(output);
			ZipOutputStream zos = new ZipOutputStream(fos);

			for (String file : files) {
				ZipEntry ze = new ZipEntry(file);
				zos.putNextEntry(ze);
				FileInputStream in = new FileInputStream(file);

				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}

				in.close();
				zos.closeEntry();
			}

			//remember close it
			zos.close();

			System.out.println("Done");

		} catch (IOException ex) {
			throw new RuntimeException("Cannot create zip "+output, ex);
		}
		
		long end = System.currentTimeMillis();
		
		System.out.println("Ziping " + output + " took " + ((end - start) / 1000) + "s");
	}
	
	private void recursiveFiles(String folder, List<String> result){
		File f = new File(folder);
		if(f.isDirectory()){
			for(String sub : f.list()){
				recursiveFiles(folder+"/"+sub, result);
			}
		}else{
			result.add(folder);
		}
	}

}
