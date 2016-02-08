package com.sample;

/**
 * Created by LuckyMan on 2/5/2016.
 */

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import javax.faces.application.FacesMessage;
import javax.faces.bean.*;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static javax.faces.context.FacesContext.getCurrentInstance;

@ManagedBean (name = "uploadBean")
@ViewScoped
public class UploadBean {

    private Part file1;
    private Part file2;

    private String text;

    private boolean ready = false;

    public boolean getReady() {
        return ready;
    }
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Part getFile1() {
        return file1;
    }

    public void setFile1(Part file1) {
        this.file1 = file1;
    }

    public Part getFile2() {
        return file2;
    }

    public void setFile2(Part file2) {
        this.file2 = file2;
    }

    public String Dif(String file1Content, String file2Content, int minLength, int maxLength, short fileNumber) {
        String Content = "";
        for (int i = 0; i < minLength; i++) {
            if (file1Content.charAt(i) == file2Content.charAt(i)) {
                char buf = file1Content.charAt(i);
                Content += Character.toString(buf);
            } else {
                Content += Character.toString(file1Content.charAt(i)) + Character.toString(file2Content.charAt(i));
            }

        }
        if (fileNumber == 0) {
            for (int i = minLength; i < maxLength; i++) {
                Content += file2Content.charAt(i);
            }
        } else {
            for (int i = minLength; i < maxLength; i++) {
                Content += file1Content.charAt(i);
            }
        }
        return Content;
    }


    public void upload() throws IOException {

        String file1Content = "", file2Content = "";

        String ext1 = FilenameUtils.getExtension(file1.getSubmittedFileName());
        String ext2 = FilenameUtils.getExtension(file2.getSubmittedFileName());

        if (!ext1.equals("txt")) {
            FacesContext.getCurrentInstance().addMessage("myform:uploader1", new FacesMessage("File has problems, insert .txt"));
        }
        if (!ext2.equals("txt")) {
            FacesContext.getCurrentInstance().addMessage("myform:uploader2", new FacesMessage("File has problems, insert .txt"));
        } else {
            setReady(true);
            Scanner s = new Scanner(file1.getInputStream());

            file1Content = s.useDelimiter("\\A").next();
            s.close();

            Scanner s2 = new Scanner(file2.getInputStream());
            file2Content = s2.useDelimiter("\\A").next();
            s2.close();

//            System.out.println(file1Content);
//            System.out.println(file2Content);

            short fileNumber;
            int shortest, longest;

            if (file1Content.length() <= file2Content.length()) {
                fileNumber = 0;
                shortest = file1Content.length();
                longest = file2Content.length();
            } else {
                fileNumber = 1;
                shortest = file2Content.length();
                longest = file1Content.length();
            }

            String Content = Dif(file1Content, file2Content, shortest, longest, fileNumber);

            setText(Content);

//            System.out.println(Content);

            Files.write(Paths.get("D:\\Content.txt"), Content.getBytes(), StandardOpenOption.CREATE);

        }

    }
    public void downloadFile() throws IOException {

        // writing string with result to file
        //
        if (getReady() == true) {
        String text = getText();
//        System.out.println("Text in downloadFile()  "+text);

            File file = new File("D://TestTask.txt");

            try (FileOutputStream fop = new FileOutputStream(file)) {
                // if file doesn't exists, then create it
                if (!file.exists()) {
                    file.createNewFile();
                }
                // get the content in bytes
                byte[] contentInBytes = text.getBytes();

                fop.write(contentInBytes);
                fop.flush();
                fop.close();

//            System.out.println("Done");

            } catch (IOException e) {
                e.printStackTrace();
            }

            // zipping file
            //
            byte[] buffer = new byte[1024];
            File zipFile = new File("D:\\MyFile.zip");

            try {
                FileOutputStream fos = new FileOutputStream(zipFile);
                ZipOutputStream zos = new ZipOutputStream(fos);
                ZipEntry ze = new ZipEntry("TestTask.txt");
                zos.putNextEntry(ze);
                FileInputStream in = new FileInputStream(file);

                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                in.close();
                zos.closeEntry();

                //remember close it
                zos.close();

//            System.out.println("zipping Done");

            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // send data to browser

            HttpServletResponse response = (HttpServletResponse) getCurrentInstance().getExternalContext().getResponse();

            response.setHeader("Content-Disposition", "attachment;filename=MyFile.zip");
            response.setContentLength((int) zipFile.length());
            ServletOutputStream out = null;
            try {
                FileInputStream input = new FileInputStream(zipFile);
                buffer = new byte[1024];
                out = response.getOutputStream();
                int i = 0;
                while ((i = input.read(buffer)) != -1) {
                    out.write(buffer);
                    out.flush();
                }
                getCurrentInstance().getResponseComplete();
            } catch (IOException err) {
                err.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException err) {
                    err.printStackTrace();
                }
            }

        }
        else{
            FacesContext.getCurrentInstance().addMessage("myform:downloadZip", new FacesMessage("At first, upload files"));
        }
    }

}