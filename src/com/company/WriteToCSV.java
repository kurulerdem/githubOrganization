package com.company;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class WriteToCSV {


    FileWriter fw;
    BufferedWriter bw;



    public void init(String filePath)
    {
        try {

            fw = new FileWriter(filePath);
            bw = new BufferedWriter(fw);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    public void write(String toBeWritten)
    {
        try {

            bw.write(toBeWritten);
            bw.newLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    public void close()
    {
        try {
            if(bw != null)
            {
                bw.flush();
            }
            if(fw != null)
            {
                fw.close();
            }


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}